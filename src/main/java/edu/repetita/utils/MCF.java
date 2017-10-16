package edu.repetita.utils;

import edu.repetita.core.Demands;
import edu.repetita.core.Topology;
import gurobi.*;

/**
 * Computes the Multi-Commodity Flow lower bound for the maximum utilization.
 * This uses an LP model, made with the Gurobi LP solvers,
 * and thus needs gurobi installed and gurobi.jar in the classpath.
 *  
 * @author Steven Gay
 */

public class MCF {
  private Topology topology;
  private Demands demands;
  private double[][] traffic;
  private int nNodes;
  private int nEdges;
  
  private GRBEnv env;
  private GRBModel model;
  
  private boolean verbose = false;
  
  final private double scalingFactor = 1024;
  
  /**
   * Silent MCF, equivalent to MCF(topology, demands, false)
   */
  public MCF(Topology topology, Demands demands) {
    this(topology, demands, false);
  }
  
  /**
   * Creates a class that computes minimum max utilization on a topology under some demands.
   * <p>
   * This constructor uses the Topology and the Demands passed as arguments internally, thus it is not thread-safe.
   * The topology must be connected, or some flow will be dropped silently.
   * 
   *  @param topology the topology on which max utilization is computed.
   *  @param demands the demands on which max utilzation is computed.
   *  @param verbose whether the underlying algorithm is allowed to show its progress or not.
   */
  public MCF(Topology topology, Demands demands, boolean verbose) {
    this.topology = topology;
    this.demands = demands;
    this.verbose = verbose;
    
    nNodes = topology.nNodes;
    nEdges = topology.nEdges;
        
    traffic = new double[nNodes][nNodes];
    
    for (int demand = 0; demand < demands.nDemands; demand++) {
      int source = demands.source[demand];
      int dest   = demands.dest[demand];
      traffic[source][dest] += demands.amount[demand];
    }
    
    try {
      initialize();
    }
    catch (GRBException e) {
      e.printStackTrace();
    }
  }
  
  private GRBVar maxUtilization;
  private GRBVar[] load;
  private GRBVar[][] loadToDest;
  
  private GRBConstr[][] flowConservation;
  private GRBConstr[] loadLimits;
  
  private void initialize() throws GRBException {
    /*
     *  Build model, we will change it at each optimization request
     */
    env = new GRBEnv();
    model = new GRBModel(env);
    model.getEnv().set(GRB.StringParam.LogFile, "");
    if (!verbose) model.getEnv().set(GRB.IntParam.LogToConsole, 0);
    model.getEnv().set(GRB.IntParam.Threads, 4);    // do not use all available procs, to mimick generic computer
    
    // Partial maxLinkLoad variables, loadToDest[dest][edge] = the part of the maxLinkLoad on edge for destination dest
    loadToDest = new GRBVar[nNodes][];
    
    for (int dest = 0; dest < nNodes; dest++) {
      loadToDest[dest] = model.addVars(nEdges, GRB.CONTINUOUS);
    }

    // Load variables, maxLinkLoad[edge] = maxLinkLoad of edge
    load = model.addVars(nEdges, GRB.CONTINUOUS);
    
    maxUtilization = model.addVar(0.0, 1000.0, 0.0, GRB.CONTINUOUS, "");
    model.update();
    // end of variables

    // objective
    GRBLinExpr objExpr = new GRBLinExpr();
    objExpr.addTerm(1.0, maxUtilization);
    model.setObjective(objExpr, GRB.MINIMIZE);
    
    // Sum partial loads = total maxLinkLoad
    for (int edge = 0; edge < nEdges; edge++) {
      GRBLinExpr expr = new GRBLinExpr();
      for (int dest = 0; dest < nNodes; dest++) {
        expr.addTerm(1.0, loadToDest[dest][edge]);
      }
      model.addConstr(expr, GRB.EQUAL, load[edge], "");
    }
    
    // Flow conservation: for all destination, for all nodes, flowsIn + demand = flowsOut <=> - flowsIn + flowsOut = demand
    flowConservation = new GRBConstr[nNodes][nNodes];
    for (int dest = 0; dest < nNodes; dest++) {
      for (int node = 0; node < nNodes; node++) {
        GRBLinExpr expr = new GRBLinExpr();
        for (int edge: topology.inEdges[node])  expr.addTerm(-1, loadToDest[dest][edge]);
        for (int edge: topology.outEdges[node]) expr.addTerm( 1, loadToDest[dest][edge]);
        
        if (node != dest) {
          flowConservation[dest][node] = model.addConstr(expr, GRB.EQUAL, traffic[node][dest] / scalingFactor, "");
        }
        /*
        else {
          model.addConstr(expr, GRB.EQUAL, -sumDemandsToDest(dest), s"flowConservation($dest,$node)")
        }
        */
      }
    }
    
    // simplify the problem a little: out edges of destination should have no maxLinkLoad in destination's partial maxLinkLoad graph
    for (int dest = 0; dest < nNodes; dest++) {
      for (int edge : topology.outEdges[dest]) {
        model.addConstr(loadToDest[dest][edge], GRB.EQUAL, 0, "");
      }
    }
    
    // Links flow, capacity and maxUsage
    loadLimits = new GRBConstr[nEdges];
    for (int edge = 0; edge < nEdges; edge++) {
      GRBLinExpr expr = new GRBLinExpr();
      expr.addTerm(topology.edgeCapacity[edge] / scalingFactor, maxUtilization);
      loadLimits[edge] = model.addConstr(expr, GRB.GREATER_EQUAL, load[edge], "");
    }
    
    model.update();    
  }

  /**
   * Computes minimum max utilization of the network.
   * <p>
   * This method makes an LP model from its internal topology and demands,
   * and computes the minimum maximum utilization reachable by best dispatching flows.
   * 
   * @return the max utilization
   */
  public double computeMaxUtilization() {
    try {
      // set parameters according to topology and demands
      
      // recompute traffic from demands
      for (int i = 0; i < nNodes; i++) {
        for (int j = 0; j < nNodes; j++) {
          traffic[i][j] = 0.0;
        }
      }
      
      for (int demand = 0; demand < demands.nDemands; demand++) {
        int source = demands.source[demand];
        int dest   = demands.dest[demand];
        traffic[source][dest] += demands.amount[demand];
      }
      
      // set required traffic in partial flows

      // set capacities from edge capacities and weight (infinite weight = edge unusable)
      for (int edge = 0; edge < nEdges; edge++) {
        if (topology.edgeWeight[edge] == Topology.INFINITE_DISTANCE || topology.edgeCapacity[edge] <= 0.0) {
          model.chgCoeff(loadLimits[edge], maxUtilization, 0.0);
        }
        else {
          model.chgCoeff(loadLimits[edge], maxUtilization, topology.edgeCapacity[edge] / scalingFactor);
        }
      }

    
      model.update();
      
      // compute MCF
      model.optimize();
      
      // return MCF if available
      double obj = model.get(GRB.DoubleAttr.ObjVal);
      return obj;      
    }
    catch (GRBException e) {
      e.printStackTrace();
    }
    return -1.0;
  }

}
