package edu.repetita.solvers.sr.defo.constraints.paths;

import edu.repetita.solvers.sr.defo.constraints.PathConstraint;
import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;
import edu.repetita.solvers.sr.defo.paths.ConnectStructure;
import org.maxicp.util.exception.InconsistencyException;

public class CanReach extends PathConstraint {
    public final IncrPathVar path;
    public final ConnectStructure connectStruct;

    public CanReach(IncrPathVar path, ConnectStructure connectStruct) {
        super(path.store.getCPSolver());
        this.path = path;
        this.connectStruct = connectStruct;
    }

    @Override
    public void post() {
        if (connectStruct.isStronglyConnected()) {
            setActive(false);
        } else {
            init();
            path.callVisitedWhenVisit(this);
        }
    }

    @Override
    public void propagate() {}

    private void init() {
        int nVisited = path.nVisited();
        for (int i = 0; i < nVisited - 1; i++) {
            int src = path.nodeAt(i);
            int dest = path.nodeAt(i + 1);
            if (!connectStruct.reachable(src, dest)) {
                throw InconsistencyException.INCONSISTENCY;
            }
        }
        visited(path, path.lastVisited(), path.lastVisited());
    }

    private void checkReachable(int from, int to) {
        if (!connectStruct.reachable(from, to)) {
            path.remove(to);
        } else if (!connectStruct.reachable(to, path.destId)) {
            path.remove(to);
        }
    }

    @Override
    public void visited(IncrPathVar path, int from, int to) {
        int[] possible = path.possible();
        for (int n : possible) {
            checkReachable(to, n);
        }
    }
}
