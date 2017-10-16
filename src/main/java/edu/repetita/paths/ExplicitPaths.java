package edu.repetita.paths;

import edu.repetita.core.Topology;

import java.util.HashMap;
import java.util.Map;

public class ExplicitPaths {
    // map: demandIndex -> array of edges
    Map<Integer,int[]> pathsPerDemand;

    public ExplicitPaths(Topology topology){
        this.pathsPerDemand = new HashMap<>();
    }

    public void setPath(int demandIndex, int[] edges) {
        this.pathsPerDemand.put(demandIndex,edges);
    }

    public int[] getPath(int demandIndex){
        return this.pathsPerDemand.get(demandIndex);
    }

    public Map<Integer,int[]> getAllPaths() {
        return this.pathsPerDemand;
    }
}
