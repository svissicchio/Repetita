package edu.repetita.solvers.sr.defo.constraints.paths;

import edu.repetita.solvers.sr.defo.constraints.PathConstraint;
import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;
import org.maxicp.util.exception.InconsistencyException;

import java.util.Set;

public class PassThrough extends PathConstraint {
    private final IncrPathVar path;
    private final Set<Integer> nodes;

    public PassThrough(IncrPathVar path, Set<Integer> nodes) {
        super(path.store.getCPSolver());
        this.path = path;
        this.nodes = nodes;
    }

    @Override
    public boolean hasPreference() {
        return isActive();
    }

    @Override
    public int[] preferences() {
        return nodes.stream().mapToInt(x -> x).toArray();
    }

    @Override
    public void post() {
        propagate();
        if (isActive()) {
            path.callVisitedWhenVisit(this);
            path.length.propagateOnBoundChange(this);
        }
    }

    @Override
    public void visited(IncrPathVar path, int from, int to) {
        if (nodes.contains(to)) {
            setActive(false);
        } else {
            checkLength();
        }
    }

    @Override
    public void propagate() {
        int[] visited = path.visited();
        boolean ok = false;
        int i = 0;
        while (i < visited.length && !ok) {
            ok = nodes.contains(visited[i]);
            i++;
        }
        if (ok) {
            setActive(false);
        } else if (i >= path.length.max()) {
            throw InconsistencyException.INCONSISTENCY;
        } else {
            checkLength();
        }
    }

    private void checkLength() {
        int maxLength = path.length.max();
        int nVisited = path.nVisited();
        int delta = maxLength - nVisited;
        if (delta == 0) {
            propagate();
        } else if (delta == 1 && isActive()) {
            throw InconsistencyException.INCONSISTENCY;
        } else if (delta != 2) {
            return;
        } else {
            int[] possibles = path.possible();
            for (int candidate : possibles) {
                if (!nodes.contains(candidate)) {
                    path.remove(candidate);
                }
            }
        }
    }
}
