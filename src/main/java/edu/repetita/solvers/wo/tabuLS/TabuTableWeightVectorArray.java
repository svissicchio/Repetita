package edu.repetita.solvers.wo.tabuLS;

import java.util.Random;

public class TabuTableWeightVectorArray implements TabuTable<State> {
  int primarySize;
  int primarySizeLg;     // bitsize of a hash value
  int hashMask;          // primarySize 1s
  boolean[] primaryTable;
  long[] primaryHVector;
  int nEdges;

  Random random;
  
  /* Hashing function from "Increasing Internet  Capacity Using Local Search", Fortz and Thorup, 2000. */
  public TabuTableWeightVectorArray(int primarySizeLg, int nEdges) {
    this.primarySizeLg = primarySizeLg;
    this.primarySize = 1 << primarySizeLg;
    primaryTable = new boolean[primarySize];
    hashMask = primarySize - 1;
    
    this.nEdges = nEdges;

    // generate primary h vector, used is Dietzfelbinger's hashing
    random = new Random();
    primaryHVector = new long[nEdges];

    for (int i = 0; i < nEdges; i++) {
      primaryHVector[i] = random.nextLong() | 1; // force number to be odd
    }
  }
  
  public void forbid(State state, boolean isChosenState) {
    long hash = primaryHashValue(state);
    primaryTable[(int) hash] = true;
  }
  
  public boolean isAllowed(State state) {
    long hash = primaryHashValue(state);
    return !primaryTable[(int) hash];
  }
  
  public void reset() {
    for (int i = 0; i < primarySize; i++) {
      primaryTable[i] = false;
    }
  }

  private long hashEdge(int edge, int weight) {
    long prod = weight * primaryHVector[edge];
    prod >>>= 16;
    return (int) (prod & hashMask);
  }
  
  // returns XOR_{edge} weight[edge] * primaryHashVector[edge]
  public long primaryHashValue(State state) {
    long acc = 0;  
    for (int edge = 0; edge < nEdges; edge++) {
      int weight = state.get(edge);
      acc ^= hashEdge(edge, weight);
    }
    return acc;
  }
}
