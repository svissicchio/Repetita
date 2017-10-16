package edu.repetita.solvers.wo.tabuLS;

import java.util.Random;

public class NeighborhoodSingleWeightChange implements Neighborhood {
  int nEdges;
  int maxWeight;
  Random random;
  State state;
  
  public NeighborhoodSingleWeightChange(State state, int nEdges, int maxWeight) {
    this.nEdges = nEdges;
    this.maxWeight = maxWeight;
    random = new Random();
    this.state = state;
  }
  
  public int size() {
    return nEdges * maxWeight;
  }
  
  public void applyRandom() {
    int selectedEdge = random.nextInt(nEdges - 1);
    int oldWeight = state.get(selectedEdge);
    int newWeight = 1 + random.nextInt(maxWeight - 2);      // newWeight in [1, maxWeight - 1]
    if (newWeight == oldWeight) newWeight = maxWeight;      // newWeight in [1, maxWeight]
    
    state.set(selectedEdge, newWeight);
  }
}
