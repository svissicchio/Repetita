package edu.repetita.solvers.sr.defo.core;

import edu.repetita.solvers.sr.defo.constraints.LoadToRate;
import edu.repetita.solvers.sr.defo.constraints.paths.*;
import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;
import edu.repetita.solvers.sr.defo.modeling.*;
import edu.repetita.solvers.sr.defo.paths.ConnectStructure;
import edu.repetita.solvers.sr.defo.paths.ECMPStructure;
import edu.repetita.solvers.sr.defo.search.IncrPathBranching;
import edu.repetita.solvers.sr.defo.search.IncrPathBranchingSingle;
import edu.repetita.solvers.sr.defo.utils.RichRandom;
import org.maxicp.cp.CPFactory;
import org.maxicp.cp.engine.constraints.Maximum;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.search.DFSearch;

import java.io.PrintWriter;
import java.util.*;
import java.util.function.Supplier;

public class CoreSolver {
    public final int grain = 1000;

    private final DEFOInstance instance;
    private final boolean verbose;
    private final PrintWriter statsFile;

    private final Topology topology;
    private final int[] weights;
    private final int[] demandTraffics;
    private final int[] demandSrcs;
    private final int[] demandDests;
    private final int[] capacities;
    private final int[] latencies;
    private final int nEdges;
    private final int nNodes;

    private final ECMPStructure ecmpStruct;
    private final ConnectStructure reachStruct;
    private final int[] step;

    private final int nDemands;

    private final DEFOConstraint[][] demandConstraints;
    private final DEFOConstraint[] topologyConstraints;

    private final int[] initialRate;
    private boolean initialComputed = false;

    private final int[][] solutionFlow;
    private final int[] solutionLoad;
    private final int[] solutionRate;
    private final int[][] solutionPath;
    private int solutionNTunnels = 0;
    private int solutionMaxRate = Integer.MAX_VALUE;

    private final RichRandom rand = new RichRandom(0);

    private final int minK1 = 1;
    private final int maxK1 = 50;
    private final int minK2 = 0;
    private final int maxK2 = 10;

    private int nIterations = 0;
    private int selected = 0;
    private int maxUsage = 0;
    private boolean success = false;
    private int k1 = minK1;
    private int k2 = minK2;

    private long initTime0 = 0L;

    public CoreSolver(DEFOInstance instance, boolean verbose, PrintWriter statsFile) {
        this.instance = instance;
        this.verbose = verbose;
        this.statsFile = statsFile;

        this.topology = instance.topology;
        this.weights = instance.weights;
        this.demandTraffics = instance.demandTraffics;
        this.demandSrcs = instance.demandSrcs;
        this.demandDests = instance.demandDests;
        this.capacities = instance.capacities;
        this.latencies = instance.latencies;
        this.nEdges = topology.nEdges;
        this.nNodes = topology.nNodes;

        this.ecmpStruct = ECMPStructure.apply(topology, weights, latencies);
        this.reachStruct = ConnectStructure.apply(topology, false);

        this.step = new int[nEdges];
        for (int e = 0; e < nEdges; e++) {
            this.step[e] = Math.max(capacities[e] / grain, 1);
        }

        this.nDemands = demandTraffics.length;
        this.demandConstraints = instance.demandConstraints;
        this.topologyConstraints = instance.topologyConstraints;

        this.initialRate = new int[nEdges];
        this.solutionFlow = new int[nDemands][nEdges];
        this.solutionLoad = new int[nEdges];
        this.solutionRate = new int[nEdges];
        this.solutionPath = new int[nDemands][];
        for (int d = 0; d < nDemands; d++) {
            solutionPath[d] = new int[0];
        }
    }

    public CoreSolver(DEFOInstance instance, boolean verbose) {
        this(instance, verbose, null);
    }

    public int[] initialRates() { return initialRate; }
    public int[] bestRates() { return solutionRate; }
    public int[][] bestPaths() { return solutionPath; }

    public void search(int timeLimit, double loadObjectivePercent) {
        if (verbose) {
            System.out.println();
            System.out.println("              OPTIMIZATION              ");
            System.out.println("----------------------------------------");
            System.out.println("max maxLinkLoad\t#tunnels\ttime (ms)");
            System.out.println("----------------------------------------");
        }

        int loadObjective = (int) (loadObjectivePercent * grain / 100);

        initTime0 = System.currentTimeMillis();
        while (solutionMaxRate > loadObjective && (System.currentTimeMillis() - initTime0) < timeLimit) {
            improve();
        }
    }

    public void improve() {
        nIterations++;

        if (success) {
            k2 = minK2;
            k1 = Math.max(k1 - 1, minK1);
            success = false;
        } else {
            k1++;
            if (k1 > maxK1) {
                k1 = maxK1;
                k2 = Math.min(k2 + 1, maxK2);
            }
        }

        int max = solutionMaxRate;
        List<Integer> maxLinksList = new ArrayList<>();
        for (int l = 0; l < nEdges; l++) {
            if (solutionRate[l] == max) maxLinksList.add(l);
        }
        int[] maxLinks = maxLinksList.stream().mapToInt(x -> x).toArray();
        int maxLink = maxLinks[rand.weightedShuffle(maxLinks, i -> i)[0] % maxLinks.length];

        List<Integer> demandOnMaxLinkList = new ArrayList<>();
        for (int d = 0; d < nDemands; d++) {
            if (solutionFlow[d][maxLink] > 0) demandOnMaxLinkList.add(d);
        }
        int[] demandOnMaxLink = demandOnMaxLinkList.stream().mapToInt(x -> x).toArray();

        int[] allDemands = new int[nDemands];
        for (int d = 0; d < nDemands; d++) allDemands[d] = d;

        int[] maxDemands = rand.weightedTake(demandOnMaxLink, k1, i -> -demandTraffics[i]);
        int[] relaxedDemands = rand.weightedTake(allDemands, k2, i -> -demandTraffics[i]);

        Set<Integer> neighborhoodSet = new LinkedHashSet<>();
        for (int d : relaxedDemands) neighborhoodSet.add(d);
        for (int d : maxDemands) neighborhoodSet.add(d);

        int[] neighborhood = neighborhoodSet.stream().mapToInt(x -> x).toArray();

        selected = maxLink;
        maxUsage = max;

        if (neighborhood.length > 0) solve(neighborhood, maxLinks);
    }

    private void solve(int[] demandsId, int[] maxLinks) {
        NetworkStore solver = new NetworkStore();

        IncrPathVar[] paths = new IncrPathVar[demandsId.length];
        for (int i = 0; i < demandsId.length; i++) {
            int demand = demandsId[i];
            paths[i] = new IncrPathVar(solver, demandSrcs[demand], demandDests[demand], topology.nNodes, "Path(" + demandSrcs[demand] + " => " + demandDests[demand] + ")");
        }

        CPIntVar[][] flows = new CPIntVar[demandsId.length][nEdges];
        for (int i = 0; i < demandsId.length; i++) {
            int demand = demandsId[i];
            for (int e = 0; e < nEdges; e++) {
                flows[i][e] = CPFactory.makeIntVar(solver.getCPSolver(), 0, demandTraffics[demand]);
            }
        }

        CPIntVar[] loads = new CPIntVar[nEdges];
        for (int e = 0; e < nEdges; e++) {
            int baseLoad = solutionLoad[e];
            for (int d : demandsId) {
                baseLoad -= solutionFlow[d][e];
            }
            CPIntVar[] flowVarsForEdge = new CPIntVar[demandsId.length];
            for (int i = 0; i < demandsId.length; i++) {
                flowVarsForEdge[i] = flows[i][e];
            }
            CPIntVar sumFlows = CPFactory.sum(flowVarsForEdge);
            loads[e] = CPFactory.sum(sumFlows, CPFactory.makeIntVar(solver.getCPSolver(), baseLoad, baseLoad));
        }

        CPIntVar[] rates = new CPIntVar[nEdges];
        for (int l = 0; l < nEdges; l++) {
            rates[l] = CPFactory.makeIntVar(solver.getCPSolver(), 0, loads[l].max() / step[l]);
            solver.post(new LoadToRate(loads[l], rates[l], step[l]));
        }

        CPIntVar[] tunnels = new CPIntVar[paths.length];
        for (int i = 0; i < paths.length; i++) {
            tunnels[i] = CPFactory.isGe(paths[i].length, 3);
        }

        int residual = 0;
        Set<Integer> demandsIdSet = new HashSet<>();
        for (int d : demandsId) demandsIdSet.add(d);
        for (int i = 0; i < nDemands; i++) {
            if (!demandsIdSet.contains(i)) {
                if (solutionPath[i].length > 2) residual++;
            }
        }

        CPIntVar sumTunnels = CPFactory.sum(tunnels);
        CPIntVar nTunnels = CPFactory.sum(sumTunnels, CPFactory.makeIntVar(solver.getCPSolver(), residual, residual));

        CPIntVar objective = CPFactory.makeIntVar(solver.getCPSolver(), 0, 1000000);
        solver.post(new Maximum(rates, objective));

        Supplier<Runnable[]> branching = new IncrPathBranching(paths, i -> -demandTraffics[demandsId[i]], (i, to) -> {
            IncrPathVar path = paths[i];
            int from = path.lastVisited();
            boolean isMax = solutionRate[selected] == solutionMaxRate;
            if (to == path.destId) return Integer.MIN_VALUE;
            if (!isMax) {
                int maxFlow = 0;
                for (int l : maxLinks) {
                    int fl = (int) (ecmpStruct.flow(from, to, l) * grain);
                    if (fl > maxFlow) maxFlow = fl;
                }
                return maxFlow;
            } else {
                return (int) (ecmpStruct.flow(from, to, selected) * grain);
            }
        });

        DFSearch search = CPFactory.makeDfs(solver.getCPSolver(), branching);

        search.onSolution(() -> {
            success = true;

            for (int d = 0; d < demandsId.length; d++) {
                solutionPath[demandsId[d]] = paths[d].visited();
                for (int l = 0; l < nEdges; l++) {
                    solutionFlow[demandsId[d]][l] = flows[d][l].min();
                }
            }
            for (int l = 0; l < nEdges; l++) {
                solutionLoad[l] = loads[l].min();
                solutionRate[l] = rates[l].min();
            }

            solutionNTunnels = nTunnels.min();

            int newMaxRate = objective.min();
            if (newMaxRate < solutionMaxRate) {
                solutionMaxRate = newMaxRate;
                long time = System.currentTimeMillis() - initTime0;
                if (verbose) System.out.println(newMaxRate + "\t\t" + solutionNTunnels + "\t\t" + time);
                if (statsFile != null) {
                    statsFile.println("OBJECTIVE " + ((double) newMaxRate / grain) + " TIME " + time + " DETOURS " + solutionNTunnels);
                }
            }
            solutionMaxRate = objective.min();
        });

        try {
            // Add constraints
            for (int i = 0; i < demandsId.length; i++) {
                int demand = demandsId[i];
                solver.post(CPFactory.le(paths[i].length, 4));
                solver.post(new CanReach(paths[i], reachStruct));
                solver.post(new DAGPath(paths[i], flows[i], ecmpStruct));
                solver.post(new SegmentToNetwork(paths[i], flows[i], ecmpStruct, demandTraffics[demand]));
                solver.post(new NetworkToSegment(paths[i], flows[i], ecmpStruct, demandTraffics[demand]));

                for (DEFOConstraint constraint : demandConstraints[demand]) {
                    if (constraint instanceof DEFOConstraint.DEFOAvoidNode) {
                        DEFOConstraint.DEFOAvoidNode avoid = (DEFOConstraint.DEFOAvoidNode) constraint;
                        int nodeId = avoid.nodeId;
                        for (int edge : topology.outEdges(nodeId)) solver.post(CPFactory.eq(flows[i][edge], 0));
                        for (int edge : topology.inEdges(nodeId)) solver.post(CPFactory.eq(flows[i][edge], 0));
                    } else if (constraint instanceof DEFOConstraint.DEFOAvoidEdge) {
                        DEFOConstraint.DEFOAvoidEdge avoid = (DEFOConstraint.DEFOAvoidEdge) constraint;
                        solver.post(CPFactory.eq(flows[i][avoid.edgeId], 0));
                    } else if (constraint instanceof DEFOConstraint.DEFOPassThrough) {
                        DEFOConstraint.DEFOPassThrough pass = (DEFOConstraint.DEFOPassThrough) constraint;
                        Set<Integer> nodeSet = new HashSet<>();
                        for (int n : pass.nodes) nodeSet.add(n);
                        solver.post(new PassThrough(paths[i], nodeSet));
                    } else if (constraint instanceof DEFOConstraint.DEFOPassThroughSeq) {
                        DEFOConstraint.DEFOPassThroughSeq pass = (DEFOConstraint.DEFOPassThroughSeq) constraint;
                        solver.post(new PassThroughSeq(paths[i], pass.seqNodes));
                    } else if (constraint instanceof DEFOConstraint.DEFOLowerLength) {
                        DEFOConstraint.DEFOLowerLength lower = (DEFOConstraint.DEFOLowerLength) constraint;
                        solver.post(CPFactory.lt(paths[i].length, lower.length));
                    } else if (constraint instanceof DEFOConstraint.DEFOLowerEqLength) {
                        DEFOConstraint.DEFOLowerEqLength lower = (DEFOConstraint.DEFOLowerEqLength) constraint;
                        solver.post(CPFactory.le(paths[i].length, lower.length));
                    }
                }
            }

            for (int link = 0; link < nEdges; link++) {
                if (solutionRate[link] == maxUsage) {
                    solver.post(CPFactory.le(rates[link], maxUsage));
                } else if (solutionRate[link] <= grain && maxUsage > grain) {
                    solver.post(CPFactory.le(rates[link], grain));
                } else {
                    solver.post(CPFactory.lt(rates[link], maxUsage));
                }
            }
            solver.post(CPFactory.lt(rates[selected], maxUsage));

            long startTime = System.currentTimeMillis();
            search.solve(stats -> stats.numberOfSolutions() >= 1 || (System.currentTimeMillis() - startTime) >= 1000);
        } catch (org.maxicp.util.exception.InconsistencyException e) {
            // Root node infeasible for this LNS neighborhood attempt
        }
    }

    public void searchInitialSol() {
        if (initialComputed) return;

        for (int i = nDemands - 1; i >= 0; i--) {
            boolean constrained = demandConstraints[i].length > 0;
            if (constrained) placeConstrainedDemand(i);
            else placeUnconstrainedDemand(i);
        }

        solutionMaxRate = 0;
        for (int i = nEdges - 1; i >= 0; i--) {
            int rate = solutionLoad[i] / step[i];
            solutionRate[i] = rate;
            if (rate > solutionMaxRate) solutionMaxRate = rate;
        }

        System.arraycopy(solutionRate, 0, initialRate, 0, nEdges);
        initialComputed = true;
    }

    private void placeUnconstrainedDemand(int i) {
        int demand = i;
        int src = demandSrcs[demand];
        int dest = demandDests[demand];
        solutionPath[i] = new int[]{src, dest};
        Set<Integer> links = ecmpStruct.links(src, dest);
        for (int l : links) {
            int flow = (int) Math.ceil(ecmpStruct.flow(src, dest, l) * demandTraffics[demand]);
            solutionFlow[i][l] = flow;
            solutionLoad[l] += flow;
        }
    }

    private void placeConstrainedDemand(int demand) {
        int src = demandSrcs[demand];
        int dest = demandDests[demand];

        NetworkStore solver = new NetworkStore();

        IncrPathVar path = new IncrPathVar(solver, src, dest, nNodes, "Path(" + src + " => " + dest + ")");

        CPIntVar[] flows = new CPIntVar[nEdges];
        for (int e = 0; e < nEdges; e++) {
            flows[e] = CPFactory.makeIntVar(solver.getCPSolver(), 0, demandTraffics[demand]);
        }

        CPIntVar[] loads = new CPIntVar[nEdges];
        for (int e = 0; e < nEdges; e++) {
            loads[e] = CPFactory.sum(flows[e], CPFactory.makeIntVar(solver.getCPSolver(), solutionLoad[e], solutionLoad[e]));
        }

        solver.post(CPFactory.le(path.length, 4));
        solver.post(new CanReach(path, reachStruct));
        solver.post(new DAGPath(path, flows, ecmpStruct));
        solver.post(new SegmentToNetwork(path, flows, ecmpStruct, demandTraffics[demand]));
        solver.post(new NetworkToSegment(path, flows, ecmpStruct, demandTraffics[demand]));

        for (DEFOConstraint constraint : demandConstraints[demand]) {
            if (constraint instanceof DEFOConstraint.DEFOAvoidNode) {
                DEFOConstraint.DEFOAvoidNode avoid = (DEFOConstraint.DEFOAvoidNode) constraint;
                int nodeId = avoid.nodeId;
                for (int edge : topology.outEdges(nodeId)) solver.post(CPFactory.eq(flows[edge], 0));
                for (int edge : topology.inEdges(nodeId)) solver.post(CPFactory.eq(flows[edge], 0));
            } else if (constraint instanceof DEFOConstraint.DEFOAvoidEdge) {
                DEFOConstraint.DEFOAvoidEdge avoid = (DEFOConstraint.DEFOAvoidEdge) constraint;
                solver.post(CPFactory.eq(flows[avoid.edgeId], 0));
            } else if (constraint instanceof DEFOConstraint.DEFOPassThrough) {
                DEFOConstraint.DEFOPassThrough pass = (DEFOConstraint.DEFOPassThrough) constraint;
                Set<Integer> nodeSet = new HashSet<>();
                for (int n : pass.nodes) nodeSet.add(n);
                solver.post(new PassThrough(path, nodeSet));
            } else if (constraint instanceof DEFOConstraint.DEFOPassThroughSeq) {
                DEFOConstraint.DEFOPassThroughSeq pass = (DEFOConstraint.DEFOPassThroughSeq) constraint;
                solver.post(new PassThroughSeq(path, pass.seqNodes));
            } else if (constraint instanceof DEFOConstraint.DEFOLowerLength) {
                DEFOConstraint.DEFOLowerLength lower = (DEFOConstraint.DEFOLowerLength) constraint;
                solver.post(CPFactory.lt(path.length, lower.length));
            } else if (constraint instanceof DEFOConstraint.DEFOLowerEqLength) {
                DEFOConstraint.DEFOLowerEqLength lower = (DEFOConstraint.DEFOLowerEqLength) constraint;
                solver.post(CPFactory.le(path.length, lower.length));
            }
        }

        Supplier<Runnable[]> singleBranching = new IncrPathBranchingSingle(path, (p, to) -> {
            int from = p.lastVisited();
            if (to == p.destId) return Integer.MIN_VALUE;
            return ecmpStruct.links(from, to).size();
        });

        DFSearch search = CPFactory.makeDfs(solver.getCPSolver(), singleBranching);

        boolean[] solFound = new boolean[1];
        search.onSolution(() -> {
            solFound[0] = true;
            solutionPath[demand] = path.visited();
            for (int l = 0; l < nEdges; l++) {
                solutionFlow[demand][l] = flows[l].min();
                solutionLoad[l] = loads[l].min();
            }
            if (solutionPath[demand].length > 2) {
                solutionNTunnels++;
            }
        });

        search.solve(stats -> stats.numberOfSolutions() >= 1);

        if (!solFound[0]) {
            throw new OverConstrainedException(demand, "the demand is over-constrained.");
        }
    }
}
