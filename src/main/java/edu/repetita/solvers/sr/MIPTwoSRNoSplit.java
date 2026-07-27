package edu.repetita.solvers.sr;

import edu.repetita.core.Demands;
import edu.repetita.core.Setting;
import edu.repetita.core.Topology;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.utils.datastructures.CubicForwardingGraphs;
import edu.repetita.paths.SRPaths;
import edu.repetita.paths.ShortestPaths;
import edu.repetita.simulators.FlowSimulator;
import edu.repetita.solvers.SRSolver;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

/*
 * Implementation of Bhatia et al's segment routing solution at INFOCOM2015 section IV,
 * improved in size by factoring traffic by detour, modified by disallowing splitting.
 */

public class MIPTwoSRNoSplit extends SRSolver {
    static {
        Loader.loadNativeLibraries();
    }

    /* Variables */
    private final int nSegments = 2;        // only works with 2 segments
    private final double scaling = 1000.0;
    private long solveTimeValue = 0;

    /* Interface methods */
    @Override
    protected void setObjective() {
        this.objective = 0;
    }

    @Override
    public String name() {
        return "MIPTwoSRNoSplit";
    }

    @Override
    public String getDescription() {
        return "A Segment Routing path optimizer inspired by \"Bhatia et al., Optimized network traffic engineering " +
                "using segment routing. In INFOCOM, 2015.\" (it uses very similar Linear Programs but does not allow " +
                "arbitrary split ratios)";
    }

    @Override
    public void solve(Setting setting, long milliseconds) {
        long startTime = System.nanoTime();
        this.computeSegments(setting, milliseconds);
        this.solveTimeValue = System.nanoTime() - startTime;
    }

    @Override
    public long solveTime(Setting setting) {
        return solveTimeValue;
    }

    /* Core method */
    public void computeSegments(Setting setting, long timeMillis) {
        // extract information from Setting
        Topology topology = setting.getTopology();
        int nNodes = topology.nNodes;
        int nEdges = topology.nEdges;
        Demands demands = setting.getDemands();
        double[][] traffic = Demands.toTrafficMatrix(demands, nNodes);
        SRPaths paths = setting.getSRPaths();
        if (paths == null){
            paths = new SRPaths(demands,this.nSegments+1);
        }

        // Compute Forwarding Graph
        ShortestPaths sp = new ShortestPaths(topology);
        CubicForwardingGraphs fg = new CubicForwardingGraphs(topology, sp);

        // try to build and solve a MIP model
        try {
            MPSolver model = MPSolver.createSolver("SCIP");
            if (model == null) {
                throw new Exception("SCIP solver not available");
            }

            MPVariable[][][] fraction = new MPVariable[nNodes][nNodes][nNodes];
            for (int source = 0; source < nNodes; source++) {
                for (int dest = 0; dest < nNodes; dest++) {
                    for (int detour = 0; detour < nNodes; detour++) {
                        fraction[source][dest][detour] = model.makeIntVar(0.0, 1.0, "");
                    }
                }
            }

            double totalToRoute = 0.0;
            for (int demand = 0; demand < demands.nDemands; demand++) totalToRoute += demands.amount[demand];
            totalToRoute /= scaling;

            MPVariable[][][] toRouteSegment = new MPVariable[nSegments][nNodes][nNodes];
            for (int i = 0; i < nSegments; i++) {
                for (int source = 0; source < nNodes; source++) {
                    for (int dest = 0; dest < nNodes; dest++) {
                        toRouteSegment[i][source][dest] = model.makeNumVar(0.0, totalToRoute, "");
                    }
                }
            }

            MPVariable maxLoadRatio = model.makeNumVar(0.0, 1 << 16, "maxLoadRatio");

            MPObjective objExpr = model.objective();
            objExpr.setCoefficient(maxLoadRatio, 1.0);
            objExpr.setMinimization();

            // split traffic between SR paths
            for (int source = 0; source < nNodes; source++) {
                for (int dest = 0; dest < nNodes; dest++) {
                    if (source != dest) {
                        MPConstraint sumFractions = model.makeConstraint(1.0, 1.0, "");
                        for (int detour = 0; detour < nNodes; detour++) {
                            sumFractions.setCoefficient(fraction[source][dest][detour], 1.0);
                        }
                    }
                }
            }

            // toRoute(0)(source)(detour) = sum_{dest} traffic(source)(dest) * fraction(source)(dest)(detour)
            for (int source = 0; source < nNodes; source++) {
                for (int detour = 0; detour < nNodes; detour++) {
                    MPConstraint equation = model.makeConstraint(0.0, 0.0, "");
                    for (int dest = 0; dest < nNodes; dest++) {
                        equation.setCoefficient(fraction[source][dest][detour], traffic[source][dest] / scaling);
                    }
                    equation.setCoefficient(toRouteSegment[0][source][detour], -1.0);
                }
            }

            // toRoute(1)(detour)(dest) = sum_{source} traffic(source)(dest) * fraction(source)(dest)(detour)
            for (int detour = 0; detour < nNodes; detour++) {
                for (int dest = 0; dest < nNodes; dest++) {
                    MPConstraint equation = model.makeConstraint(0.0, 0.0, "");
                    for (int source = 0; source < nNodes; source++) {
                        equation.setCoefficient(fraction[source][dest][detour], traffic[source][dest] / scaling);
                    }
                    equation.setCoefficient(toRouteSegment[1][detour][dest], -1.0);
                }
            }

            // maxLinkLoad on every edge should be smaller than maxLoadRatio
            for (int edge = 0; edge < nEdges; edge++) {
                MPConstraint sumUsage = model.makeConstraint(-MPSolver.infinity(), 0.0, "");

                for (int nItems = fg.elementsOfEdge[edge] - 1; nItems >= 0; nItems--) {
                    int source = fg.sources[edge][nItems];
                    int dest = fg.dests[edge][nItems];
                    double ratio = fg.ratios[edge][nItems];

                    sumUsage.setCoefficient(toRouteSegment[0][source][dest], ratio);
                    sumUsage.setCoefficient(toRouteSegment[1][source][dest], ratio);
                }

                sumUsage.setCoefficient(maxLoadRatio, -topology.edgeCapacity[edge] / scaling);
            }

            for (int source = 0; source < nNodes; source++) {
                for (int dest = 0; dest < nNodes; dest++) {
                    if (source != dest) {
                        MPConstraint equation = model.makeConstraint(0.0, 0.0, "");
                        equation.setCoefficient(fraction[source][dest][dest], 1.0);
                    }
                }
            }

            long launchTime = System.nanoTime();

            for (int demand = 0; demand < demands.nDemands; demand++) {
                int source = demands.source[demand];
                int dest = demands.dest[demand];

                if (paths.getPathLength(demand) == 2) {
                    model.setHint(new MPVariable[]{fraction[source][dest][source]}, new double[]{1.0});
                } else { // length > 2
                    int detour = paths.getPathElement(demand, 1);
                    model.setHint(new MPVariable[]{fraction[source][dest][detour]}, new double[]{1.0});
                }
            }

            model.setTimeLimit(timeMillis);
            
            long timeBefore = System.nanoTime();
            MPSolver.ResultStatus resultStatus = model.solve();
            long timeAfter = System.nanoTime();
            solveTimeValue = timeAfter - timeBefore;

            if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
                int[] newPath = {0, 0, 0};

                for (int demand = 0; demand < demands.nDemands; demand++) {
                    int source = demands.source[demand];
                    int dest = demands.dest[demand];

                    if (source != dest) {
                        int detour = nNodes - 1;
                        while (detour >= 0 && fraction[source][dest][detour].solutionValue() < 0.5) detour--;
                        if (detour == -1) {
                            // RepetitaWriter.appendToOutput(String.format("oh nodes %d %d %d", source, dest, detour));
                        }

                        if (detour == source) {
                            if (paths.getPathLength(demand) > 2) {
                                newPath[0] = source;
                                newPath[1] = dest;
                                paths.setPath(demand, newPath);
                            }
                        } else {
                            int previousLength = paths.getPathLength(demand);
                            if (previousLength != 3 ||
                                    (previousLength == 3 && paths.getPathElement(demand, 1) != detour)) {
                                newPath[0] = source;
                                newPath[1] = detour;
                                newPath[2] = dest;
                                paths.setPath(demand, newPath);
                            }
                        }
                    }
                }
            }

            setting.setSRPaths(paths);
            model.delete();
        }
        catch (Throwable e) {
            RepetitaWriter.appendToOutput("OR-Tools solver unavailable (" + e.getMessage() + "). Running 2-segment heuristic...", 1);
            FlowSimulator simulator = FlowSimulator.getInstance();
            simulator.setup(setting);
            simulator.computeFlows();

            for (int demand = 0; demand < demands.nDemands; demand++) {
                int src = demands.source[demand];
                int dst = demands.dest[demand];
                if (src == dst) continue;

                int bestDetour = -1;
                double bestUtil = simulator.getMaxUtilization();

                for (int detour = 0; detour < nNodes; detour++) {
                    if (detour == src || detour == dst) continue;
                    paths.setPath(demand, new int[]{src, detour, dst});
                    simulator.setup(setting);
                    simulator.computeFlows();
                    double util = simulator.getMaxUtilization();
                    if (util < bestUtil) {
                        bestUtil = util;
                        bestDetour = detour;
                    }
                }

                if (bestDetour != -1) {
                    paths.setPath(demand, new int[]{src, bestDetour, dst});
                } else {
                    paths.setPath(demand, new int[]{src, dst});
                }
                simulator.setup(setting);
                simulator.computeFlows();
            }
            setting.setSRPaths(paths);
        }
    }
}
