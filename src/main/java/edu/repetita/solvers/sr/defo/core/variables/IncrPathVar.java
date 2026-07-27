package edu.repetita.solvers.sr.defo.core.variables;

import edu.repetita.solvers.sr.defo.constraints.PathConstraint;
import edu.repetita.solvers.sr.defo.constraints.paths.PathLength;
import edu.repetita.solvers.sr.defo.core.NetworkStore;
import org.maxicp.cp.CPFactory;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.state.State;
import org.maxicp.state.StateInt;
import org.maxicp.util.exception.InconsistencyException;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IncrPathVar implements Iterable<Integer> {
    public final NetworkStore store;
    public final int origId;
    public final int destId;
    public final int nNodes;
    public final String name;

    private final State<PathEventListener> visitedListener;
    private final State<PathEventListener> removedListener;

    private final int[] nodes;
    private final int[] positions;

    private final StateInt removedPtr;
    private final StateInt visitedPtr;

    public final CPIntVar length;

    public IncrPathVar(NetworkStore store, int origId, int destId, int nNodes, String name) {
        this.store = store;
        this.origId = origId;
        this.destId = destId;
        this.nNodes = nNodes;
        this.name = name;

        this.visitedListener = store.getStateManager().makeStateRef(null);
        this.removedListener = store.getStateManager().makeStateRef(null);

        this.nodes = new int[nNodes];
        this.positions = new int[nNodes];
        for (int i = 0; i < nNodes; i++) {
            nodes[i] = i;
            positions[i] = i;
        }

        this.removedPtr = store.getStateManager().makeStateInt(nNodes);
        this.visitedPtr = store.getStateManager().makeStateInt(0);

        nodes[origId] = 0;
        nodes[0] = origId;
        positions[0] = origId;
        positions[origId] = 0;

        this.length = CPFactory.makeIntVar(store.getCPSolver(), 2, nNodes);
        store.post(new PathLength(this));
    }

    public IncrPathVar(NetworkStore store, int origId, int destId, int nNodes) {
        this(store, origId, destId, nNodes, "PATH_VAR");
    }

    public int[] preferences() {
        int[] prefered = new int[nNodes];
        PathEventListener node = visitedListener.value();
        while (node != null) {
            PathConstraint constraint = node.constraint;
            if (constraint.hasPreference()) {
                int[] prefs = constraint.preferences();
                for (int p : prefs) prefered[p] = 1;
            }
            node = node.next;
        }
        node = removedListener.value();
        while (node != null) {
            PathConstraint constraint = node.constraint;
            if (constraint.hasPreference()) {
                int[] prefs = constraint.preferences();
                for (int p : prefs) prefered[p] = 1;
            }
            node = node.next;
        }
        return prefered;
    }

    public int nVisited() {
        return visitedPtr.value() + 1;
    }

    public int nPossible() {
        return removedPtr.value() - visitedPtr.value() - 1;
    }

    public boolean isBound() {
        return nodes[visitedPtr.value()] == destId;
    }

    public void callVisitedWhenVisit(PathConstraint c) {
        visitedListener.setValue(new PathEventListener(visitedListener.value(), c, c.priorityBindL1));
    }

    public void remove(int nodeId) {
        int p1 = positions[nodeId];
        if (p1 <= visitedPtr.value()) return;
        if (p1 >= removedPtr.value()) return;
        if (nPossible() == 1) {
            removedPtr.decrement();
            throw InconsistencyException.INCONSISTENCY;
        } else {
            removedPtr.decrement();
            int p2 = removedPtr.value();
            int v2 = nodes[p2];
            nodes[p1] = v2;
            nodes[p2] = nodeId;
            positions[v2] = p1;
            positions[nodeId] = p2;
            store.notifyForbidden(visitedListener.value(), this, nodeId);
        }
    }

    public void removeAllBut(int nodeId) {
        int p1 = positions[nodeId];
        if (p1 <= visitedPtr.value() || p1 >= removedPtr.value()) {
            throw InconsistencyException.INCONSISTENCY;
        }
        if (nPossible() == 1) return;
        int p2 = visitedPtr.value() + 1;
        int v2 = nodes[p2];
        nodes[p1] = v2;
        nodes[p2] = nodeId;
        positions[v2] = p1;
        positions[nodeId] = p2;
        removedPtr.setValue(p2 + 1);
    }

    public void visit(int nodeId) {
        if (isBound()) {
            throw InconsistencyException.INCONSISTENCY;
        } else {
            int oldLastVisited = lastVisited();
            visitedPtr.increment();
            int p1 = positions[nodeId];
            int p2 = visitedPtr.value();
            int v2 = nodes[p2];
            nodes[p1] = v2;
            nodes[p2] = nodeId;
            positions[v2] = p1;
            positions[nodeId] = p2;

            if (nodeId != destId) {
                removedPtr.setValue(nNodes);
            } else {
                removedPtr.setValue(p2 + 1);
            }

            store.notifyVisited(visitedListener.value(), this, oldLastVisited, nodeId);
        }
    }

    public int lastVisited() {
        return nodes[visitedPtr.value()];
    }

    public boolean isPossible(int nodeId) {
        int p = positions[nodeId];
        return p > visitedPtr.value() && p < removedPtr.value();
    }

    public boolean hasVisited(int nodeId) {
        if (nodeId == origId) return true;
        return positions[nodeId] <= visitedPtr.value();
    }

    public boolean hasVisited(int from, int to) {
        int p1 = positions[from];
        if (p1 >= visitedPtr.value()) return false;
        return nodes[p1 + 1] == to;
    }

    public int position(int nodeId) {
        int p = positions[nodeId];
        if (p <= visitedPtr.value()) return p;
        throw new IllegalStateException("node not visited");
    }

    public int nodeAt(int position) {
        if (position <= visitedPtr.value()) return nodes[position];
        throw new IllegalStateException("position >= nVisited");
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int i = visitedPtr.value() + 1;

            @Override
            public boolean hasNext() {
                return i < removedPtr.value();
            }

            @Override
            public Integer next() {
                if (!hasNext()) throw new NoSuchElementException();
                int node = nodes[i];
                i++;
                return node;
            }
        };
    }

    public int[] visited() {
        int nv = nVisited();
        int[] vis = new int[nv];
        System.arraycopy(nodes, 0, vis, 0, nv);
        return vis;
    }

    public int[] possible() {
        int n = nPossible();
        int[] poss = new int[n];
        int offset = visitedPtr.value() + 1;
        System.arraycopy(nodes, offset, poss, 0, n);
        return poss;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name).append(": ");
        for (int i = 0; i <= visitedPtr.value(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(nodes[i]);
        }
        return sb.toString();
    }
}
