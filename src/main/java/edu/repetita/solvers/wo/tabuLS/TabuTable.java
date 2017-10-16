package edu.repetita.solvers.wo.tabuLS;

/*
 * What needs to be implemented to be a used as a tabu table
 */

public interface TabuTable<T> {
  public void forbid(T partialState, boolean isChosenState);  // isChosenState is true if the tabu search moved to that state
  public boolean isAllowed(T partialState);
  public void reset();

}
