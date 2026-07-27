package edu.repetita.solvers.sr.defo.modeling;

import edu.repetita.solvers.sr.defo.modeling.variables.DEFOLoadVar;

public class DEFOEdge {
    public final int edgeId;
    public final String label;
    private DEFOLoadVar loadVar;

    public DEFOEdge(int edgeId, String label) {
        this.edgeId = edgeId;
        this.label = label;
    }

    public synchronized DEFOLoadVar load() {
        if (loadVar == null) {
            loadVar = new DEFOLoadVar(edgeId);
        }
        return loadVar;
    }

    @Override
    public String toString() {
        return label;
    }
}
