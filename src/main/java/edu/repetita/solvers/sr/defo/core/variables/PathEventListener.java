package edu.repetita.solvers.sr.defo.core.variables;

import edu.repetita.solvers.sr.defo.constraints.PathConstraint;

public class PathEventListener {
    public final PathEventListener next;
    public final PathConstraint constraint;
    public final int priority;

    public PathEventListener(PathEventListener next, PathConstraint constraint, int priority) {
        this.next = next;
        this.constraint = constraint;
        this.priority = priority;
    }

    public boolean hasNext() {
        return next != null;
    }

    @Override
    public String toString() {
        return "PathEvent(constraint: " + constraint + ")";
    }
}
