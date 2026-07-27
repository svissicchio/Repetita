package edu.repetita.solvers.sr.rls.io;

import java.util.HashMap;
import java.util.Map;

public class PathsData {
    public final double maxLoad;
    private final Map<Integer, int[]> pathMap;
    public final int[] demandsWithPaths;

    public PathsData(double maxLoad, Map<Integer, int[]> pathMap, int[] demandsWithPaths) {
        this.maxLoad = maxLoad;
        this.pathMap = pathMap;
        this.demandsWithPaths = demandsWithPaths;
    }

    public boolean hasPath(int demand) {
        return pathMap.containsKey(demand);
    }

    public int[] pathOf(int demand) {
        return pathMap.get(demand);
    }
}
