package edu.repetita.solvers.wo.tabuLS;

import java.util.Random;

import edu.repetita.core.Topology;
import edu.repetita.paths.ShortestPaths;

/*
 * Neighborhood from "Increasing Internet capacity using local search", Fortz & Thorup 2000,
 * Journal Computational Optimization and Applications, section 6.1 "Neighborhood structure".
 * We are trying to replicate exactly that neighborhood. 
 */


public class NeighborhoodEvenlyBalancingFlows implements Neighborhood {
  Topology topology;
  int nNodes;
  int nEdges;
  int maxWeight;
  Random random;
  
  private ShortestPaths sp;

  State state;  
  
  public NeighborhoodEvenlyBalancingFlows(State state, Topology topology, int maxWeight, ShortestPaths sp) {
    nNodes = topology.nNodes;
    nEdges = topology.nEdges;
    
    this.topology = topology;
    this.maxWeight = maxWeight;
    this.state = state;
    random = new Random();
    
    this.sp = sp;
  }
  
  public int size() {
    return nNodes * nNodes;
  }
  
  public void applyRandom() {
    int dest = random.nextInt(nNodes);
    int node = random.nextInt(nNodes);
    
    if (node == dest) return;
    
    // compute max distance of successors of node to dest
    int maxDistance = 0;
    for (int outEdge: topology.outEdges[node]) {
      int succ = topology.edgeDest[outEdge];
      maxDistance = Math.max(maxDistance, sp.distance[succ][dest]);
    }
    
    // put maxLinkLoad on every edge with some probability
    for (int edge: topology.outEdges[node]) {
      boolean putLoad = random.nextBoolean();
      int succ = topology.edgeDest[edge];
      
      int newWeight = maxDistance + 1 - sp.distance[succ][dest] + (putLoad ? 0 : 1);
      
      if (newWeight > maxWeight) {
        state.restore();
        return;
      }
      
      state.set(edge, newWeight);
    }
  }
}
