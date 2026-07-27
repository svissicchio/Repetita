package edu.repetita.solvers.sr.defo.core;

import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;
import edu.repetita.solvers.sr.defo.core.variables.PathEventListener;
import org.maxicp.cp.CPFactory;
import org.maxicp.cp.engine.core.CPConstraint;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.state.StateManager;

public class NetworkStore {
    private final CPSolver cp;

    public NetworkStore() {
        this.cp = CPFactory.makeSolver();
    }

    public CPSolver getCPSolver() {
        return cp;
    }

    public StateManager getStateManager() {
        return cp.getStateManager();
    }

    public void post(CPConstraint c) {
        cp.post(c);
    }

    public void notifyVisited(PathEventListener listener, IncrPathVar path, int srcId, int sinkId) {
        PathEventListener e = listener;
        while (e != null) {
            if (e.constraint.isActive()) {
                e.constraint.visited(path, srcId, sinkId);
            }
            e = e.next;
        }
    }

    public void notifyForbidden(PathEventListener listener, IncrPathVar path, int nodeId) {
        PathEventListener e = listener;
        while (e != null) {
            if (e.constraint.isActive()) {
                e.constraint.forbidden(path, nodeId);
            }
            e = e.next;
        }
    }
}
