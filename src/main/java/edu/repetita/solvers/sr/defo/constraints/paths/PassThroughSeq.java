package edu.repetita.solvers.sr.defo.constraints.paths;

import edu.repetita.solvers.sr.defo.constraints.PathConstraint;
import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;
import org.maxicp.state.StateInt;
import org.maxicp.util.exception.InconsistencyException;

import java.util.HashSet;
import java.util.Set;

public class PassThroughSeq extends PathConstraint {
    private final IncrPathVar path;
    private final Set<Integer>[] seqSet;
    private final StateInt step;

    @SuppressWarnings("unchecked")
    public PassThroughSeq(IncrPathVar path, int[][] seqNodes) {
        super(path.store.getCPSolver());
        this.path = path;
        this.seqSet = new Set[seqNodes.length];
        for (int i = 0; i < seqNodes.length; i++) {
            this.seqSet[i] = new HashSet<>();
            for (int n : seqNodes[i]) this.seqSet[i].add(n);
        }
        this.step = getSolver().getStateManager().makeStateInt(0);
    }

    @Override
    public void post() {
        path.callVisitedWhenVisit(this);
        path.length.propagateOnBoundChange(this);
    }

    @Override
    public void visited(IncrPathVar path, int from, int to) {
        if (seqSet[step.value()].contains(to)) {
            step.increment();
        }
        if (step.value() == seqSet.length) {
            setActive(false);
        } else {
            checkLength();
        }
    }

    @Override
    public void propagate() {
        int[] visited = path.visited();
        int i = 0;
        int s = step.value();
        while (i < visited.length && s < seqSet.length) {
            if (seqSet[s].contains(visited[i])) {
                s++;
            }
            i++;
        }
        step.setValue(s);
        if (s == seqSet.length) {
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
        int nRemaining = seqSet.length - step.value();
        if (delta == 0) {
            propagate();
        } else if (delta == 1 && isActive()) {
            throw InconsistencyException.INCONSISTENCY;
        } else if (delta > nRemaining) {
            return;
        } else {
            int[] possibles = path.possible();
            for (int candidate : possibles) {
                if (!seqSet[step.value()].contains(candidate)) {
                    path.remove(candidate);
                }
            }
        }
    }
}
