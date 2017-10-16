package edu.repetita.solvers.wo.tabuLS;


/*
 *   An array of ints that can be modified, saved and restored.
 *   
 *   A typical use of this would be to:
 *   _ set some indices to some values
 *   _ get some values to compute something, or use fillDeltaIndices + getOld to do that incrementally
 *   _ either be happy about the result and call save(), or unhappy and take the state back to its previous value with restore()
 *   
 */

public class State {
  private int n;
  private int[] values;
  private int[] oldValues;
  
  private int nChanged;
  private boolean[] changed;
  private int[] delta;
  
  public State(int[] values) {
    this.values = values;
    oldValues = values.clone();
    n = values.length;
    
    nChanged = 0;
    changed = new boolean[n];
    delta = new int[n];
  }
  
  public int get(int index) {
    return values[index];
  }
  
  public int getOld(int index) {
    return oldValues[index];
  }
  
  public void set(int index, int newValue) {
    if (!changed[index]) {
      oldValues[index] = values[index];
      changed[index] = true;
      delta[nChanged] = index;
      nChanged++;
    }
    values[index] = newValue;
  }
  
  public void save() {
    while (nChanged > 0) {
      nChanged -= 1;
      int index = delta[nChanged];
      changed[index] = false;
    }
  }
  
  public void restore() {
    while (nChanged > 0) {
      nChanged -= 1;
      int index = delta[nChanged];
      changed[index] = false;
      values[index] = oldValues[index];
    }
  }
  
  public int fillDeltaIndex(int[] changedIndices) {
    System.arraycopy(delta, 0, changedIndices, 0, nChanged);
    return nChanged;
  }
  
  public int deltaSize() {
    return nChanged;
  }
  
  public void applyDeltaTo(State other) {
    for (int p = 0; p < nChanged; p++) {
      int index = delta[p]; 
      other.set(index, values[index]);      
    }        
  }
  
  public void copyTo(State other) {
    for (int index = 0; index < n; index++) other.set(index, values[index]);
  }
}
