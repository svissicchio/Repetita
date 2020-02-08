
package edu.repetita.solvers.sr.cg4sr;

import edu.repetita.solvers.sr.cg4sr.config.RunConfig;
import edu.repetita.solvers.sr.cg4sr.data.Demand;
import edu.repetita.solvers.sr.cg4sr.data.DemandPathMap;
import edu.repetita.solvers.sr.cg4sr.data.NetworkEdge;
import edu.repetita.solvers.sr.cg4sr.data.Triple;
import edu.repetita.solvers.sr.cg4sr.segmentRouting.SrPath;
import gurobi.*;

import java.util.ArrayList;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class MinFactorColGenModel {

    private SRTEInstance instance;
    private double flowValue;
    private GRBModel model;
    private DemandPathMap demandPathMap;
    private ArrayList<GRBConstr> capaConstraints, demandConstraints;
    private GRBConstr flowConstraint;
    private GRBVar lambda;
    private ArrayList<GRBVar>[] x;
    private boolean integral;

    public MinFactorColGenModel(SRTEInstance instance, DemandPathMap demandPathMap, double flowValue, boolean integral) {
        this.instance = instance;
        this.demandPathMap = demandPathMap;
        this.flowValue = flowValue;
        this.integral = integral;
        createModel();
    }

    private String getVarName(int d, int p) {
        return String.format("x[%d][%d]", d, p);
    }

    private void createModel() {
        try {
            GRBEnv env = new GRBEnv();
            model = new GRBModel(env);
            model.getEnv().set(GRB.IntParam.OutputFlag, RunConfig.MIP_VERBOSE ? 1 : 0);
            // lambda = capacity factor for the edges
            lambda = model.addVar(0, GRB.INFINITY, 1, GRB.CONTINUOUS, "lambda");
            // x[p][d] = 1 iff sr-path p is used for demand d
            x = new ArrayList[instance.nbDemands()];
            for (int d = 0; d < instance.nbDemands(); d++) {
                x[d] = new ArrayList<>();
                Demand demand = instance.getDemand(d);
                for (int p = 0; p < demandPathMap.nbCompatiblePaths(demand); p++) {
                    x[d].add(model.addVar(0, 1, 0, integral ? GRB.BINARY : GRB.CONTINUOUS, getVarName(d, p)));
                }
            }
            model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
            model.update();
            capaConstraints = new ArrayList<>();
            // add capacity constraints
            for (int e = 0; e < instance.nbEdges(); e++) {
                NetworkEdge edge = instance.getEdges().get(e);
                GRBLinExpr expr = new GRBLinExpr();
                for (int d = 0; d < instance.nbDemands(); d++) {
                    Demand demand = instance.getDemand(d);
                    ArrayList<SrPath> compatiblePaths = demandPathMap.getCompatiblePaths(demand);
                    for (int p = 0; p < compatiblePaths.size(); p++) {
                        SrPath path = compatiblePaths.get(p);
                        expr.addTerm(path.ratio(instance.getSplit(), edge) * demand.volume, x[d].get(p));
                    }
                }
                expr.addTerm(-edge.cap(), lambda);
                capaConstraints.add(model.addConstr(expr, GRB.LESS_EQUAL, 0, String.format("e%d", e)));
            }
            model.update();
            // add at most one path per demand constraint
            demandConstraints = new ArrayList<>();
            for (int d = 0; d < instance.nbDemands(); d++) {
                Demand demand = instance.getDemand(d);
                GRBLinExpr expr = new GRBLinExpr();
                ArrayList<SrPath> compatiblePaths = demandPathMap.getCompatiblePaths(demand);
                for (int p = 0; p < compatiblePaths.size(); p++) {
                    expr.addTerm(1, x[d].get(p));
                }
                demandConstraints.add(model.addConstr(expr, GRB.LESS_EQUAL, 1, String.format("d%d", d)));
            }
            // add the flow constraint
            GRBLinExpr expr = new GRBLinExpr();
            for(int d = 0; d < instance.nbDemands(); d++) {
                Demand demand = instance.getDemand(d);
                ArrayList<SrPath> compatiblePaths = demandPathMap.getCompatiblePaths(demand);
                for(int p = 0; p < compatiblePaths.size(); p++) {
                    expr.addTerm(demand.volume, x[d].get(p));
                }
            }
            flowConstraint = model.addConstr(expr, GRB.GREATER_EQUAL, flowValue, "flow");
            model.update();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    public double optimize() {
        try {
            model.optimize();
            return model.get(GRB.DoubleAttr.ObjVal);
        } catch (GRBException e) {
            e.printStackTrace();
            try {
                model.computeIIS();
                model.write("model.ilp");
            } catch (GRBException e2) {
                e2.printStackTrace();
            }
        }
        assert false;
        return 0;
    }

    public int gurobiStatus() {
        try {
            model.optimize();
            return model.get(GRB.IntAttr.Status);
        } catch (GRBException e) {
            e.printStackTrace();
        }
        assert false;
        return 0;
    }

    public SRTESolution getSolution() {
        try {
            SRTESolution sol = new SRTESolution(model.get(GRB.DoubleAttr.ObjVal));
            for(int d = 0; d < instance.nbDemands(); d++) {
                Demand demand = instance.getDemand(d);
                ArrayList<SrPath> compatiblePaths = demandPathMap.getCompatiblePaths(demand);
                for(int p = 0; p < x[d].size(); p++) {
                    double value = x[d].get(p).get(GRB.DoubleAttr.X);
                    if(value > RunConfig.MIP_POSITIVE_EPS) {
                        sol.addPath(demand, compatiblePaths.get(p), value);
                    }
                }
            }
            return sol;
        } catch (GRBException e) {
            e.printStackTrace();
        }
        assert false;
        return null;
    }

    public Triple<double[], double[], Double> getDualValues() {
        try {
            double[] y = new double[instance.nbEdges()];
            double[] z = new double[instance.nbDemands()];
            for(int i = 0; i < capaConstraints.size(); i++) {
                y[i] = capaConstraints.get(i).get(GRB.DoubleAttr.Pi);
            }
            for(int i = 0; i < demandConstraints.size(); i++) {
                z[i] = demandConstraints.get(i).get(GRB.DoubleAttr.Pi);
            }
            double w = flowConstraint.get(GRB.DoubleAttr.Pi);
            return new Triple<>(y, z, w);
        } catch (GRBException e) {
            e.printStackTrace();
        }
        assert false;
        return null;
    }

}
