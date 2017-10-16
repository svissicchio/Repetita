package edu.repetita.solvers.wo.tabuLS;

import java.util.HashMap;

public class TabuTableScore implements TabuTable<Double> {
  private HashMap<Double, Long> scoreTable;
  
  private long magicTimestamp = 0L;
  private long ttl;
  
  public TabuTableScore(long timeToLive) {
    scoreTable = new HashMap<>();
    ttl = timeToLive;
  }  
  
  public void forbid(Double score, boolean isChosenState) {
    if (isChosenState) {
      scoreTable.put(score, magicTimestamp);
      magicTimestamp++;
    }
  }
  
  public boolean isAllowed(Double score) {
    return !scoreTable.containsKey(score) || scoreTable.get(score) + ttl < magicTimestamp;
  }
  
  public void reset() {
    scoreTable.clear();
  }
}
