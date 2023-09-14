package edu.repetita.solvers.sr.cg4sr;

import edu.repetita.solvers.sr.cg4sr.config.RunConfig;
import edu.repetita.solvers.sr.cg4sr.data.Demand;
import edu.repetita.solvers.sr.cg4sr.data.DemandPathMap;
import edu.repetita.solvers.sr.cg4sr.data.NetworkEdge;
import edu.repetita.solvers.sr.cg4sr.data.Pair;
import edu.repetita.solvers.sr.cg4sr.segmentRouting.SrPath;
import gurobi.*;

import java.util.ArrayList;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class SRTEColGenModel {

    public GRBModel model;
    private SRTEInstance instance;
    private DemandPathMap demandPathMap;
    private ArrayList<GRBConstr> capaConstraints, demandConstraints;
    private ArrayList<GRBVar>[] x;
    private GRBVar[] edgeFlow;
    private boolean integral;
    private double capacityFactor;

    public SRTEColGenModel(SRTEInstance instance, InitialColumnGen initColGen, double capacityFactor, boolean integral) {
        this.instance = instance;
        this.integral = integral;
        this.capacityFactor = capacityFactor;
        ArrayList<SrPath> paths = initColGen.getInitialColumns(instance.getDemands());
        demandPathMap = new DemandPathMap(this, instance);
        demandPathMap.addPaths(paths);
        createModel(integral);
	    updateCapacityFactor(this.capacityFactor);
    }

    public SRTEColGenModel(SRTEInstance instance, DemandPathMap demandPathMap, double capacityFactor, boolean integral) {
        this.instance = instance;
        this.integral = integral;
        this.capacityFactor = capacityFactor;
        this.demandPathMap = demandPathMap;
        createModel(integral);
        updateCapacityFactor(this.capacityFactor);
    }

    public DemandPathMap getDemandPathMap() {
        return demandPathMap;
    }

    private String getVarName(int d, int p) {
        return String.format("x;%d;%d;", d, p);
    }

    public int getNbColumns() {
        return demandPathMap.size();
    }

    public void updateCapacityFactor(double capacityFactor) {
        try {
            this.capacityFactor = capacityFactor;
            for (int i = 0; i < capaConstraints.size(); i++) {
                capaConstraints.get(i).set(GRB.DoubleAttr.RHS, capacityFactor * instance.getEdges().get(i).cap());
            }
            model.update();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    public  double getCapacityFactor() {
        return capacityFactor;
    }

    private void createModel(boolean integral) {
        try {
            GRBEnv env = new GRBEnv();
            model = new GRBModel(env);
            model.getEnv().set(GRB.IntParam.OutputFlag, RunConfig.MIP_VERBOSE ? 1 : 0);
            // x[p][d] = 1 iff sr-path p is used for demand d
            x = new ArrayList[instance.nbDemands()];
            for (int d = 0; d < instance.nbDemands(); d++) {
                x[d] = new ArrayList<>();
                Demand demand = instance.getDemand(d);
                for (int p = 0; p < demandPathMap.nbCompatiblePaths(demand); p++) {
                    x[d].add(model.addVar(0, GRB.INFINITY, demand.volume, integral ? GRB.INTEGER : GRB.CONTINUOUS, getVarName(d, p)));
                }
            }
            // initialize edge flow variables (used just to retrive values)
            edgeFlow = new GRBVar[instance.nbEdges()];
            for(int e = 0; e < instance.nbEdges(); e++) {
                edgeFlow[e] = model.addVar(0, instance.getEdges().get(e).cap(), 0, GRB.CONTINUOUS, "eflow" + e);
            }
            model.set(GRB.IntAttr.ModelSense, GRB.MAXIMIZE);
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
                capaConstraints.add(model.addConstr(expr, GRB.LESS_EQUAL, capacityFactor * edge.cap(), String.format("e%d", e)));
                GRBLinExpr expr2 = new GRBLinExpr();
                expr2.add(expr);
                expr2.addTerm(-1, edgeFlow[e]);
                model.addConstr(expr2, GRB.EQUAL, 0, "flowbinding" + e);
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
            model.update();
            // objective function
            /*
            GRBLinExpr obj = new GRBLinExpr();
            for(int d = 0; d < instance.nbDemands(); d++) {
                for(int p = 0; p < x[d].size(); p++) {
                    obj.addTerm(instance.getDemand(d).volume, x[d].get(p));
                }
            }
            model.setObjective(obj, GRB.MAXIMIZE);
            model.update();
            */
        } catch (GRBException e) {
            e.printStackTrace();
        }

        for(int d = 0; d < instance.nbDemands(); d++) {
            assert x[d].size() == demandPathMap.getCompatiblePaths(instance.getDemand(d)).size();
        }

    }

    public double optimize() {
        try {
            model.optimize();
            return model.get(GRB.DoubleAttr.ObjVal);
        } catch (GRBException e) {
            e.printStackTrace();
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

    public double[] getEdgeFlows() {
        try {
            double[] edgeFlowValue = new double[instance.nbEdges()];
            for(int e = 0; e < instance.nbEdges(); e++) {
                edgeFlowValue[e] = edgeFlow[e].get(GRB.DoubleAttr.X);
            }
            return edgeFlowValue;
        } catch (GRBException e) {
            e.printStackTrace();
        }
        assert false;
        return null;
    }

    public SRTESolution getSolution() {
        try {
            double value = model.get(GRB.DoubleAttr.ObjVal);
            SRTESolution sol = new SRTESolution(value);
            double checkvalue = 0;
            for(int d = 0; d < instance.nbDemands(); d++) {
                Demand demand = instance.getDemand(d);
                ArrayList<SrPath> compatiblePaths = demandPathMap.getCompatiblePaths(demand);
                assert compatiblePaths.size() == x[d].size();
                for(int p = 0; p < x[d].size(); p++) {
                    double xdp = x[d].get(p).get(GRB.DoubleAttr.X);
                    checkvalue += xdp * demand.volume;
                    if(value > RunConfig.MIP_POSITIVE_EPS) {
                        sol.addPath(demand, compatiblePaths.get(p), xdp);
                    }
                }
            }
            assert Math.abs(checkvalue - value) < 1e-4;
            sol.setDemandPathMap(demandPathMap);
            return sol;
        } catch (GRBException e) {
            e.printStackTrace();
        }
        assert false;
        return null;
    }

    public Pair<double[], double[]> getDualValues() {
        try {
            double[] y = new double[instance.nbEdges()];
            double[] z = new double[instance.nbDemands()];
            for(int i = 0; i < capaConstraints.size(); i++) {
                y[i] = capaConstraints.get(i).get(GRB.DoubleAttr.Pi);
            }
            for(int i = 0; i < demandConstraints.size(); i++) {
                z[i] = demandConstraints.get(i).get(GRB.DoubleAttr.Pi);
            }
            return new Pair<>(y, z);
        } catch (GRBException e) {
            e.printStackTrace();
        }
        assert false;
        return null;
    }

    public void addPath(int demandIndex, SrPath path) {

        Demand demand = instance.getDemand(demandIndex);

        GRBColumn column = new GRBColumn();
        try {
            demandPathMap.addPath(path);
            // set the coefficients for the capacity constraints (one per edge)
            int e = 0;
            for (NetworkEdge edge : instance.getEdges()) {
                column.addTerm(path.ratio(instance.getSplit(), edge) * demand.volume, capaConstraints.get(e));
                e++;
            }
            // set the coefficients for the demand constraints
            column.addTerm(1, demandConstraints.get(demandIndex));
            // add the new column and push modifications
            int pathIndex = demandPathMap.nbCompatiblePaths(demand) - 1;
            String name = getVarName(demandIndex, pathIndex);

            GRBVar newVar = model.addVar(0, GRB.INFINITY, demand.volume, integral ? GRB.INTEGER : GRB.CONTINUOUS, column, name);
            x[demandIndex].add(newVar);

            model.update();
        } catch (GRBException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public double computeObjValue() {

        int nbx = 0;
        for(int d = 0; d < instance.nbDemands(); d++) {
            nbx += x[d].size();
        }

        assert nbx + instance.nbEdges() == model.getVars().length;

        double val = 0;
        double max = 0;
        for(int d = 0; d < instance.nbDemands(); d++) {
            for(int p = 0; p < x[d].size(); p++) {
                try {
                    max = Math.max(max, x[d].get(p).get(GRB.DoubleAttr.X));
                    val += instance.getDemand(d).volume * x[d].get(p).get(GRB.DoubleAttr.X);
                } catch (GRBException e) {
                    e.printStackTrace();
                }
            }
        }

        double val2 = 0;


        ArrayList<Integer>[] tmp = new ArrayList[instance.nbDemands()];
        for(int i = 0; i < instance.nbDemands(); i++) {
            tmp[i] = new ArrayList<>();
        }

        return val;
    }

    public double getCoeff(GRBVar var) throws GRBException {
        GRBLinExpr obj = (GRBLinExpr) model.getObjective();
        for (int i = 0; i < obj.size(); i++) {
            if (obj.getVar(i).get(GRB.StringAttr.VarName).equals(var.get(GRB.StringAttr.VarName))) {
                return obj.getCoeff(i);
            }
        }
        return 0; /* Cannot be found */
    }

}
