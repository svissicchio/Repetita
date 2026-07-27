package edu.repetita.solvers.sr.defo.modeling;

import edu.repetita.solvers.sr.defo.modeling.units.DEFOPassThroughBuilder;
import edu.repetita.solvers.sr.defo.modeling.variables.DEFOLatencyVar;
import edu.repetita.solvers.sr.defo.modeling.variables.DEFOLengthVar;

public class DEFODemand {
    public final int demandId;
    public final String label;
    private DEFOLatencyVar latencyVar;
    private DEFOLengthVar lengthVar;

    public DEFODemand(int demandId, String label) {
        this.demandId = demandId;
        this.label = label;
    }

    public synchronized DEFOLatencyVar latency() {
        if (latencyVar == null) latencyVar = new DEFOLatencyVar(demandId);
        return latencyVar;
    }

    public synchronized DEFOLengthVar length() {
        if (lengthVar == null) lengthVar = new DEFOLengthVar(demandId);
        return lengthVar;
    }

    public DEFOPassThroughBuilder passThrough(DEFONode... nodes) {
        int[] nodeIds = new int[nodes.length];
        for (int i = 0; i < nodes.length; i++) nodeIds[i] = nodes[i].nodeId;
        return new DEFOPassThroughBuilder(demandId, nodeIds);
    }

    public DEFOConstraint avoidNode(DEFONode node) {
        return new DEFOConstraint.DEFOAvoidNode(demandId, node.nodeId);
    }

    public DEFOConstraint avoidEdge(DEFOEdge edge) {
        return new DEFOConstraint.DEFOAvoidEdge(demandId, edge.edgeId);
    }

    @Override
    public String toString() {
        return label;
    }
}
