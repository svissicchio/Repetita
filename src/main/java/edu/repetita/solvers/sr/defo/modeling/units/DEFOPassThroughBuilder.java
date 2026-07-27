package edu.repetita.solvers.sr.defo.modeling.units;

import edu.repetita.solvers.sr.defo.modeling.DEFOConstraint;
import edu.repetita.solvers.sr.defo.modeling.DEFONode;

import java.util.ArrayList;
import java.util.List;

public class DEFOPassThroughBuilder {
    private final int demandId;
    private final List<int[]> seqNodes = new ArrayList<>();

    public DEFOPassThroughBuilder(int demandId, int[] nodes) {
        this.demandId = demandId;
        this.seqNodes.add(nodes);
    }

    public DEFOPassThroughBuilder then(DEFONode node) {
        return then(new DEFONode[]{node});
    }

    public DEFOPassThroughBuilder then(DEFONode... nodes) {
        int[] n = new int[nodes.length];
        for (int i = 0; i < nodes.length; i++) n[i] = nodes[i].nodeId;
        seqNodes.add(n);
        return this;
    }

    public DEFOConstraint.DEFODemandConstraint toConstraint() {
        if (seqNodes.size() == 1) {
            return new DEFOConstraint.DEFOPassThrough(demandId, seqNodes.get(0));
        } else {
            return new DEFOConstraint.DEFOPassThroughSeq(demandId, seqNodes.toArray(new int[0][]));
        }
    }
}
