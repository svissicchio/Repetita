package edu.repetita.solvers.sr.defo.constraints.paths;

import edu.repetita.solvers.sr.defo.constraints.PathConstraint;
import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;

public class Visit extends PathConstraint {
    private final IncrPathVar path;
    private final int nodeId;

    public Visit(IncrPathVar path, int nodeId) {
        super(path.store.getCPSolver());
        this.path = path;
        this.nodeId = nodeId;
    }

    @Override
    public void post() {
        path.visit(nodeId);
    }

    @Override
    public void propagate() {}
}
