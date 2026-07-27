package edu.repetita.solvers.sr.rls.core;

import edu.repetita.solvers.sr.rls.ShortestPaths;
import edu.repetita.solvers.sr.rls.io.TopologyData;

public interface DelayData {
    int delay(int source, int destination);
}
