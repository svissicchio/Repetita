package edu.repetita.solvers.sr.defo.constraints.paths;

import edu.repetita.solvers.sr.defo.constraints.PathConstraint;
import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;
import org.maxicp.util.exception.InconsistencyException;

public class PathLength extends PathConstraint {
    public final IncrPathVar path;

    public PathLength(IncrPathVar path) {
        super(path.store.getCPSolver());
        this.path = path;
    }

    @Override
    public void post() {
        propagate();
        if (!path.isBound()) {
            path.callVisitedWhenVisit(this);
            path.length.propagateOnBoundChange(this);
        }
    }

    @Override
    public void propagate() {
        int maxLength = path.length.max();
        int minLength = path.length.min();
        int nVisited = path.nVisited();
        if (nVisited == maxLength - 1) {
            path.removeAllBut(path.destId);
        } else if (nVisited < minLength - 1) {
            path.remove(path.destId);
        } else if (nVisited > maxLength) {
            throw InconsistencyException.INCONSISTENCY;
        }
    }

    @Override
    public void visited(IncrPathVar path, int from, int to) {
        if (to == path.destId) {
            path.length.fix(path.nVisited());
        } else {
            path.length.removeBelow(path.nVisited() + 1);
        }
        propagate();
    }
}
