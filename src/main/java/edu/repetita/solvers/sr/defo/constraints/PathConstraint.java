package edu.repetita.solvers.sr.defo.constraints;

import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;
import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPSolver;

public abstract class PathConstraint extends AbstractCPConstraint {
    public int priorityBindL1 = 0;

    public PathConstraint(CPSolver cp) {
        super(cp);
    }

    public void visited(IncrPathVar pathVar, int srcId, int sinkId) {}
    public void forbidden(IncrPathVar pathVar, int nodeId) {}
    public boolean hasPreference() { return false; }
    public int[] preferences() { return new int[0]; }
}
