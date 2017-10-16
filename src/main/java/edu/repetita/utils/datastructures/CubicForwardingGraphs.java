package edu.repetita.utils.datastructures;

import edu.repetita.core.Topology;
import edu.repetita.paths.ShortestPaths;


/**
 * Computes the ECMP forwarding graphs and the associated flow ratios.
 * <p>
 * For every edge e, compute all tuples (source, destination, ratio) such that 
 * an ECMP (all shortest paths) demand of 1.0 from {@code source} to {@code destination}
 * will put {@code ratio} amount of flow on edge e.
 * <p>
 * This structure precomputes all tuples, and stores only the tuples with nonzero ratios.
 * 
 * @author Steven Gay
 */
public class CubicForwardingGraphs {
  private ShortestPaths sp;
  
  private int nNodes;
  private int nEdges;

  /** elementsOfEdge[edge] = number of (source, destination, ratio) with a nonzero ratio for the given edge */
  public int[] elementsOfEdge;
  /** sources[edge] contains the sources of the tuples of edge */
  public int[][] sources;
  /** dests[edge] contains the dests of the tuples of edge */
  public int[][] dests;
  /** ratios[edge] contains the ratios of the tuples of edge */
  public double[][] ratios;
  
  /**
   * Builds forwarding graphs on the given topology, by precomputing all tuples.
   * This takes O(V&sup2; E) space in theory, but if graphs are sparse and their diameter small, typically more of a O(V&sup2;). 
   * 
   * @param topology the topology on which forwarding graphs are to be computed
   * @param sp the shortest paths of the topology, used to compute forwarding graphs at construction time. Should be initialized!
   */
  public CubicForwardingGraphs(Topology topology, ShortestPaths sp) {
    this.sp = sp;
    
    this.nNodes = topology.nNodes;
    this.nEdges = topology.nEdges;
    
    elementsOfEdge = new int[nEdges];  // initialized to 0
    sources = new int[nEdges][8];
    dests   = new int[nEdges][8];
    ratios = new double[nEdges][8];
    
    initialize();
  }
  
  private void addToEdge(int edge, int src, int dest, double ratio) {
    int n = elementsOfEdge[edge];
    
    // grow arrays if needed. Better than using ArrayList<NonprimitiveType>?
    if (n == sources[edge].length) {
      int newSize = 2 * n;
      int[] newS = new int[newSize];
      int[] newD = new int[newSize];
      double[] newR = new double[newSize];
      
      System.arraycopy(sources[edge], 0, newS, 0, n);
      System.arraycopy(dests[edge], 0, newD, 0, n);
      System.arraycopy(ratios[edge], 0, newR, 0, n);
      
      sources[edge] = newS;
      dests[edge] = newD;
      ratios[edge] = newR;
    }
    
    sources[edge][n] = src;
    dests[edge][n] = dest;
    ratios[edge][n] = ratio;
    elementsOfEdge[edge]++;
  }
  
  
  // fill above structures by simulating the routing of 1.0 flow from source to destination
  // add source, destination, ratio to all visited edges 
  private void initialize() {
    double[] toRoute = new double[nNodes];
    
    for (int source = 0; source < nNodes; source++) {
      for (int destination = 0; destination < nNodes; destination++) {
        // we visit nodes in subDAG of source to destination in topological order
        int[] ordering = sp.topologicalOrdering;
        int nOrdering = sp.makeTopologicalOrdering(source, destination);
        
        // put 1.0 to route from source, push it to neighboring nodes in subDAG, adding info to relevant edges
        toRoute[source] = 1.0;
        while (nOrdering > 0) {
          int node = ordering[--nOrdering];
          
          int pSucc = sp.nSuccessors[destination][node];
          double amountToRoute = toRoute[node] / pSucc;
          while (pSucc > 0) {
            pSucc--;
            int succNode = sp.successorNodes[destination][node][pSucc];
            toRoute[succNode] += amountToRoute;
            
            int succEdge = sp.successorEdges[destination][node][pSucc];
            addToEdge(succEdge, source, destination, amountToRoute);
          }
          
          toRoute[node] = 0.0;
        }
      }
    }    
  }
}
