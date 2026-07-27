package edu.repetita.solvers.sr.rls;

import edu.repetita.solvers.sr.rls.constraint.MaxLoad;
import edu.repetita.solvers.sr.rls.core.CapacityData;
import edu.repetita.solvers.sr.rls.core.Neighborhood;
import edu.repetita.solvers.sr.rls.core.Topology;
import edu.repetita.solvers.sr.rls.io.DemandsData;
import edu.repetita.solvers.sr.rls.io.TopologyData;
import edu.repetita.solvers.sr.rls.neighborhood.InsertGuarded;
import edu.repetita.solvers.sr.rls.neighborhood.Remove;
import edu.repetita.solvers.sr.rls.neighborhood.ReplaceGuarded;
import edu.repetita.solvers.sr.rls.neighborhood.Reset;
import edu.repetita.solvers.sr.rls.preprocessing.DetoursFilter;
import edu.repetita.solvers.sr.rls.state.EdgeDemandStateTree;
import edu.repetita.solvers.sr.rls.state.FlowStatePrecomputeDAG;
import edu.repetita.solvers.sr.rls.state.FlowStateRecomputeDAGOnCommit;
import edu.repetita.solvers.sr.rls.state.PathState;
import edu.repetita.solvers.sr.rls.state.SavedPathState;
import java.util.Random;

public class LoadOptimizer {
    private final Topology topology;
    private final CapacityData capacityData;
    private final ShortestPaths shortestPaths;
    private final int[] allowedDetours;

    private final int nNodes;
    private final int nEdges;
    private final int nDemands;

    private final PathState pathState;
    private final FlowStatePrecomputeDAG flowState;
    private final EdgeDemandStateTree edgeDemandState;
    private final FlowStateRecomputeDAGOnCommit flowStateOnCommit;
    private final MaxLoad maxLoad;
    private final SavedPathState bestPaths;

    private final ECMP ecmp;
    @SuppressWarnings("unchecked")
    private final Neighborhood<Integer>[] neighborhoods;
    @SuppressWarnings("unchecked")
    private final Neighborhood<Integer>[] kickNeighborhoods;

    private final DemandsData decisionDemands;
    private final Random random = new Random();

    public LoadOptimizer(TopologyData topologyData, DemandsData decisionDemands, boolean debug) {
        this.topology = Topology.apply(topologyData);
        this.decisionDemands = decisionDemands;

        this.capacityData = new CapacityData() {
            private final double[] capa = topologyData.edgeCapacities;
            private final double[] invcapa = calculateInvCapa(topologyData.edgeCapacities);

            private double[] calculateInvCapa(double[] capacities) {
                double[] res = new double[capacities.length];
                for (int i = 0; i < capacities.length; i++) res[i] = 1.0 / capacities[i];
                return res;
            }

            @Override public double[] capacity() { return capa; }
            @Override public double[] invCapacity() { return invcapa; }
        };

        this.shortestPaths = new ShortestPaths(topology, topologyData.edgeWeights);
        this.allowedDetours = DetoursFilter.apply(shortestPaths, debug);

        this.nNodes = topology.nNodes;
        this.nEdges = topology.nEdges;
        this.nDemands = decisionDemands.nDemands;

        if (debug) System.out.println(nNodes + " nodes, " + nEdges + " edges, " + nDemands + " demands");

        this.pathState = new PathState(decisionDemands);
        this.flowState = new FlowStatePrecomputeDAG(nNodes, nEdges, shortestPaths, pathState, decisionDemands);
        this.edgeDemandState = new EdgeDemandStateTree(nDemands, nEdges, capacityData);
        this.flowStateOnCommit = new FlowStateRecomputeDAGOnCommit(nNodes, nEdges, shortestPaths, pathState, decisionDemands, edgeDemandState);

        pathState.addTrial(flowState);
        pathState.addTrial(flowStateOnCommit);

        this.maxLoad = new MaxLoad(topology, capacityData, flowState, shortestPaths, debug);
        flowState.addTrial(maxLoad);

        this.bestPaths = new SavedPathState(pathState);
        pathState.addTrial(bestPaths);

        this.ecmp = new ECMP(nNodes, nEdges, shortestPaths);

        this.neighborhoods = new Neighborhood[]{
                new Reset(pathState),
                new Remove(pathState),
                new ReplaceGuarded(nNodes, nEdges, ecmp, pathState, maxLoad),
                new InsertGuarded(nNodes, nEdges, ecmp, pathState, maxLoad)
        };

        this.kickNeighborhoods = new Neighborhood[]{
                new Reset(pathState),
                new Remove(pathState)
        };

        this.demandsModifier = new DemandsModifier(pathState, flowState, flowStateOnCommit, decisionDemands);
    }

    public LoadOptimizer(TopologyData topologyData, DemandsData decisionDemands) {
        this(topologyData, decisionDemands, false);
    }

    public void startMoving(long timeLimit, double objectiveLimit) {
        long startTime = System.nanoTime();
        long stopTime = startTime + (timeLimit * 1000000L);
        double bestLoad = maxLoad.score();
        long nIterations = 0L;
        long bestIteration = 0L;

        while (System.nanoTime() < stopTime && bestLoad > objectiveLimit) {
            nIterations++;

            if (maxLoad.score() > bestLoad && nIterations > bestIteration + 1000) {
                bestPaths.restorePaths();
                pathState.update();
                pathState.commit();
                bestIteration = nIterations - 1;
            }

            int demand = selectDemand();

            if (maxLoad.score() == bestLoad && nIterations > bestIteration + 3) {
                bestIteration = nIterations;
                kick(kickNeighborhoods, maxLoad, demand);
            }

            boolean improvementFound = false;
            int pNeighborhood = 0;
            while (!improvementFound && pNeighborhood < neighborhoods.length) {
                Neighborhood<Integer> neighborhood = neighborhoods[pNeighborhood];
                improvementFound = visitNeighborhood(neighborhood, demand);

                if (improvementFound) {
                    neighborhood.applyBest();
                    pathState.update();
                    pathState.commit();

                    if (maxLoad.score() < bestLoad) {
                        bestPaths.savePaths();
                        bestLoad = maxLoad.score();
                        bestIteration = nIterations;
                    }
                }
                pNeighborhood++;
            }
        }
    }

    private void kick(Neighborhood<Integer>[] neighborhoods, MaxLoad maxLoad, int demand) {
        maxLoad.active = false;
        int choice = random.nextInt(neighborhoods.length);
        Neighborhood<Integer> neighborhood = neighborhoods[choice];
        if (visitNeighborhood(neighborhood, demand)) {
            neighborhood.applyBest();
            pathState.update();
            pathState.commit();
        }
        maxLoad.active = true;
    }

    private boolean visitNeighborhood(Neighborhood<Integer> neighborhood, int setter) {
        double bestNeighborhoodLoad = Double.MAX_VALUE;
        int nBestMoves = 0;
        boolean improvementFound = false;

        neighborhood.setNeighborhood(setter);
        while (neighborhood.hasNext()) {
            neighborhood.next();
            neighborhood.apply();

            if (pathState.nChanged() > 0 && pathState.check()) {
                double score = maxLoad.score();

                if (score == bestNeighborhoodLoad) {
                    nBestMoves++;
                    if (random.nextInt(nBestMoves) == 0) neighborhood.saveBest();
                } else if (score < bestNeighborhoodLoad) {
                    nBestMoves = 1;
                    improvementFound = true;
                    neighborhood.saveBest();
                    bestNeighborhoodLoad = maxLoad.score();
                }
                pathState.revert();
            }
        }
        return improvementFound;
    }

    private int selectDemand() {
        int edge = maxLoad.selectRandomMaxEdge();
        return edgeDemandState.selectRandomDemand(edge);
    }

    private final DemandsModifier demandsModifier;

    public void setDemandBandwidth(int demand, double newTraffic) {
        double diffTraffic = newTraffic - decisionDemands.demandTraffics[demand];
        demandsModifier.add(demand, diffTraffic);
    }

    public PathState solve(long timeLimit, double objectiveLimit) {
        startMoving(timeLimit, objectiveLimit);
        bestPaths.restorePaths();
        pathState.update();
        pathState.commit();
        return pathState;
    }
}
