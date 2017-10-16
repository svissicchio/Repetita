package edu.repetita.solvers.sr;

import edu.repetita.core.Demands;
import edu.repetita.core.Setting;
import edu.repetita.core.Topology;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.utils.datastructures.CubicForwardingGraphs;
import edu.repetita.paths.SRPaths;
import edu.repetita.paths.ShortestPaths;
import edu.repetita.solvers.SRSolver;
import gurobi.*;
import gurobi.GRB.*;

/*
 * Implementation of Bhatia et al's segment routing solution at INFOCOM2015 section IV,
 * improved in size by factoring traffic by detour, modified by disallowing splitting.
 */

public class MIPTwoSRNoSplit extends SRSolver {

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
            // Initialize and parametrize gurobi model
            GRBEnv env = new GRBEnv();
            GRBModel model = new GRBModel(env);

            model.getEnv().set(StringParam.LogFile, "");
            if (this.verbose == 0) model.getEnv().set(IntParam.LogToConsole, 0);
            model.getEnv().set(IntParam.Method, 1);  // dual simplex works best
            model.getEnv().set(IntParam.Presolve, 0);  // model is tight enough
            // model.getEnv().set(GRB.IntParam.MIPFocus, 1);  // focus on finding good solutions

            /*
             *  MIP variables
             *  fraction(source)(dest)(detour) is the fraction of traffic from source to dest going through detour.
             *  toRouteSegmentX(source)(dest) is the amount of traffic to route from source to dest with (source, dest) as segment X, X = 1 or 2
             *  data: flowRatio(source)(dest)(edge)
             */

            GRBVar[][][] fraction = new GRBVar[nNodes][nNodes][nNodes];
            for (int source = 0; source < nNodes; source++) {
                for (int dest = 0; dest < nNodes; dest++) {
                    for (int detour = 0; detour < nNodes; detour++) {
                        fraction[source][dest][detour] = model.addVar(0.0, 1.0, 0.0, GRB.INTEGER, this.verbose > 1 ? String.format("fraction_%d_%d_%d", source, dest, detour) : "");
                    }
                }
            }

            double totalToRoute = 0.0;
            for (int demand = 0; demand < demands.nDemands; demand++) totalToRoute += demands.amount[demand];
            totalToRoute /= scaling;

            GRBVar[][][] toRouteSegment = new GRBVar[nSegments][nNodes][nNodes];
            for (int i = 0; i < nSegments; i++) {
                for (int source = 0; source < nNodes; source++) {
                    for (int dest = 0; dest < nNodes; dest++) {
                        toRouteSegment[i][source][dest] = model.addVar(0.0, totalToRoute, 0.0, GRB.CONTINUOUS, this.verbose > 1 ? String.format("toRoute_%d_%d_%d", i, source, dest) : "");
                    }
                }
            }

            // supposing ratios can never go above 2^16, would put infinity if that did not mess with LP float computations
            GRBVar maxLoadRatio = model.addVar(0.0, 1 << 16, 0.0, GRB.CONTINUOUS, "maxLoadRatio");

            model.update();

            // minimize that
            GRBLinExpr objExpr = new GRBLinExpr();
            objExpr.addTerm(1.0, maxLoadRatio);
            model.setObjective(objExpr, GRB.MINIMIZE);

      
            /*
             *  Constraints
             */

            // split traffic between SR paths
            for (int source = 0; source < nNodes; source++)
                for (int dest = 0; dest < nNodes; dest++)
                    if (source != dest) {
                        GRBLinExpr sumFractions = new GRBLinExpr();
                        for (int detour = 0; detour < nNodes; detour++)
                            sumFractions.addTerm(1.0, fraction[source][dest][detour]);
                        model.addConstr(sumFractions, GRB.EQUAL, 1.0, this.verbose > 1 ? String.format("equation1_%d_%d", source, dest) : "");
                    }

            // toRoute(0)(source)(detour) = sum_{dest} traffic(source)(dest) * fraction(source)(dest)(detour)
            for (int source = 0; source < nNodes; source++) {
                for (int detour = 0; detour < nNodes; detour++) {
                    GRBLinExpr equation = new GRBLinExpr();
                    for (int dest = 0; dest < nNodes; dest++)
                        equation.addTerm(traffic[source][dest] / scaling, fraction[source][dest][detour]);
                    equation.addTerm(-1.0, toRouteSegment[0][source][detour]);
                    model.addConstr(equation, GRB.EQUAL, 0.0, this.verbose > 1 ? String.format("toRoute_0_%d_%d", source, detour) : "");
                }
            }

            // toRoute(1)(detour)(dest) = sum_{source} traffic(source)(dest) * fraction(source)(dest)(detour)
            for (int detour = 0; detour < nNodes; detour++) {
                for (int dest = 0; dest < nNodes; dest++) {
                    GRBLinExpr equation = new GRBLinExpr();
                    for (int source = 0; source < nNodes; source++)
                        equation.addTerm(traffic[source][dest] / scaling, fraction[source][dest][detour]);
                    equation.addTerm(-1.0, toRouteSegment[1][detour][dest]);
                    model.addConstr(equation, GRB.EQUAL, 0.0, this.verbose > 1 ? String.format("toRoute_1_%d_%d", detour, dest) : "");
                }
            }

            // maxLinkLoad on every edge should be smaller than maxLoadRatio
            for (int edge = 0; edge < nEdges; edge++) {
                GRBLinExpr sumUsage = new GRBLinExpr();

                // for every (source, destination, ratio) using edge, add it to the maxLinkLoad of the edge
                for (int nItems = fg.elementsOfEdge[edge] - 1; nItems >= 0; nItems--) {
                    int source = fg.sources[edge][nItems];
                    int dest = fg.dests[edge][nItems];
                    double ratio = fg.ratios[edge][nItems];

                    sumUsage.addTerm(ratio, toRouteSegment[0][source][dest]);
                    sumUsage.addTerm(ratio, toRouteSegment[1][source][dest]);
                }

                // add <= capacity * theta
                sumUsage.addTerm(-topology.edgeCapacity[edge] / scaling, maxLoadRatio);
                model.addConstr(sumUsage, GRB.LESS_EQUAL, 0, this.verbose > 1 ? ("equation2_" + edge) : "");
            }

            // model simplification: fraction(source)(dest)(detour) = 0 if source != dest && dest == detour
            // more explicitly: how does one represent the path source->dest with no detour?
            // there are two representations: (source, source, dest) and (source, dest, dest).
            // we put the second to 0 to reduce search space.
            // WARNING: writing paths assumes that this is used and detour is never dest!
            for (int source = 0; source < nNodes; source++)
                for (int dest = 0; dest < nNodes; dest++)
                    if (source != dest) {
                        GRBLinExpr equation = new GRBLinExpr();
                        equation.addTerm(1.0, fraction[source][dest][dest]);
                        model.addConstr(equation, GRB.EQUAL, 0, this.verbose > 1 ? String.format("fraction_%d_%d_%d_eq_0", source, dest, dest) : "");
                    }


            model.update();
            RepetitaWriter.appendToOutput("Modelling done in ${(System.nanoTime() - launchTime) / 1000000}ms.",1);

            /*
             *  Warm start using current paths
             */
            for (int demand = 0; demand < demands.nDemands; demand++) {
                int source = demands.source[demand];
                int dest = demands.dest[demand];

                if (paths.getPathLength(demand) == 2) {
                    fraction[source][dest][source].set(DoubleAttr.Start, 1.0);
                } else { // length > 2
                    int detour = paths.getPathElement(demand, 1);
                    fraction[source][dest][detour].set(DoubleAttr.Start, 1.0);
                }
            }


            /*
             *  Solving
             */
            model.getEnv().set(DoubleParam.TimeLimit, ((double) timeMillis) / 1000.0);
            model.getEnv().set(IntParam.Threads, 4);    // do not use all available procs, to mimic generic computer

            long timeBefore = System.nanoTime();
            model.optimize();
            long timeAfter = System.nanoTime();
            solveTimeValue = timeAfter - timeBefore;

            if (this.verbose > 1) model.write("model.lp");
      
            /*
             *  Fill SRAssignment output
             */
            if (model.get(IntAttr.SolCount) > 0) {
                int[] newPath = {0, 0, 0};

                for (int demand = 0; demand < demands.nDemands; demand++) {
                    int source = demands.source[demand];
                    int dest = demands.dest[demand];

                    if (source != dest) {
                        // find which detour current best solution is using
                        int detour = nNodes - 1;
                        while (detour >= 0 && fraction[source][dest][detour].get(DoubleAttr.X) == 0) detour--;
                        if (detour == -1) {
                            RepetitaWriter.appendToOutput(String.format("oh nodes %d %d %d", source, dest, detour));
                        }

                        // change behaviour whether we make a detour or not
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

            model.dispose();
            env.dispose();
        }

        // in the case of any problem with modeling or solving the MIP, we don't update the segment routing paths
        // and we just print the error
        catch (GRBException e) {
            e.printStackTrace();
        }
    }

}
