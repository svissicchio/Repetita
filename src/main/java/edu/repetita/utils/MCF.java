package edu.repetita.utils;

import edu.repetita.core.Demands;
import edu.repetita.core.Topology;
import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

/**
 * Computes the Multi-Commodity Flow lower bound for the maximum utilization.
 * This uses an LP model, made with Google OR-Tools.
 *  
 * @author Steven Gay
 */

public class MCF {
  static {
      Loader.loadNativeLibraries();
  }

  private Topology topology;
  private Demands demands;
  private double[][] traffic;
  private int nNodes;
  private int nEdges;
  
  private MPSolver model;
  
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
    catch (Throwable e) {
      e.printStackTrace();
    }
  }
  
  private MPVariable maxUtilization;
  private MPVariable[] load;
  private MPVariable[][] loadToDest;
  
  private MPConstraint[][] flowConservation;
  private MPConstraint[] loadLimits;
  
  private void initialize() throws Exception {
    model = MPSolver.createSolver("GLOP");
    if (model == null) {
      throw new Exception("GLOP solver not available");
    }
    
    loadToDest = new MPVariable[nNodes][nEdges];
    for (int dest = 0; dest < nNodes; dest++) {
      for (int edge = 0; edge < nEdges; edge++) {
        loadToDest[dest][edge] = model.makeNumVar(0.0, MPSolver.infinity(), "");
      }
    }

    load = new MPVariable[nEdges];
    for (int edge = 0; edge < nEdges; edge++) {
        load[edge] = model.makeNumVar(0.0, MPSolver.infinity(), "");
    }
    
    maxUtilization = model.makeNumVar(0.0, 1000.0, "");

    MPObjective objExpr = model.objective();
    objExpr.setCoefficient(maxUtilization, 1.0);
    objExpr.setMinimization();
    
    for (int edge = 0; edge < nEdges; edge++) {
      MPConstraint expr = model.makeConstraint(0, 0, "");
      for (int dest = 0; dest < nNodes; dest++) {
        expr.setCoefficient(loadToDest[dest][edge], 1.0);
      }
      expr.setCoefficient(load[edge], -1.0);
    }
    
    flowConservation = new MPConstraint[nNodes][nNodes];
    for (int dest = 0; dest < nNodes; dest++) {
      for (int node = 0; node < nNodes; node++) {
        if (node != dest) {
          flowConservation[dest][node] = model.makeConstraint(traffic[node][dest] / scalingFactor, traffic[node][dest] / scalingFactor, "");
          for (int edge: topology.inEdges[node])  flowConservation[dest][node].setCoefficient(loadToDest[dest][edge], -1);
          for (int edge: topology.outEdges[node]) flowConservation[dest][node].setCoefficient(loadToDest[dest][edge], 1);
        }
      }
    }
    
    for (int dest = 0; dest < nNodes; dest++) {
      for (int edge : topology.outEdges[dest]) {
        MPConstraint expr = model.makeConstraint(0, 0, "");
        expr.setCoefficient(loadToDest[dest][edge], 1.0);
      }
    }
    
    loadLimits = new MPConstraint[nEdges];
    for (int edge = 0; edge < nEdges; edge++) {
      loadLimits[edge] = model.makeConstraint(0.0, MPSolver.infinity(), "");
      loadLimits[edge].setCoefficient(maxUtilization, topology.edgeCapacity[edge] / scalingFactor);
      loadLimits[edge].setCoefficient(load[edge], -1.0);
    }
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
      
      for (int dest = 0; dest < nNodes; dest++) {
        for (int node = 0; node < nNodes; node++) {
          if (node != dest) {
            flowConservation[dest][node].setBounds(traffic[node][dest] / scalingFactor, traffic[node][dest] / scalingFactor);
          }
        }
      }

      for (int edge = 0; edge < nEdges; edge++) {
        if (topology.edgeWeight[edge] == Topology.INFINITE_DISTANCE || topology.edgeCapacity[edge] <= 0.0) {
          loadLimits[edge].setCoefficient(maxUtilization, 0.0);
        }
        else {
          loadLimits[edge].setCoefficient(maxUtilization, topology.edgeCapacity[edge] / scalingFactor);
        }
      }

      MPSolver.ResultStatus resultStatus = model.solve();
      
      if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
          return model.objective().value();
      }
    }
    catch (Throwable e) {
      e.printStackTrace();
    }
    return -1.0;
  }
}
