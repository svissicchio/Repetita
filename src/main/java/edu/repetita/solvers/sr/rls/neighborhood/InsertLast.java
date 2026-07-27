package edu.repetita.solvers.sr.rls.neighborhood;

import edu.repetita.solvers.sr.rls.core.Neighborhood;
import edu.repetita.solvers.sr.rls.state.PathState;
import edu.repetita.solvers.sr.rls.state.Trial;

public class InsertLast extends Neighborhood<Integer> implements Trial {
    public final String name = "InsertLast";
    private final PathState pathState;
    private final boolean debug;

    public int lastDetour = 0;

    private int demand = -1;
    private int position = 0;
    private int size = 0;
    private final int maxDetourSize;

    private int storedPosition = 0;
    private int storedNode = lastDetour;

    public InsertLast(PathState pathState, boolean debug) {
        this.pathState = pathState;
        this.debug = debug;
        this.maxDetourSize = pathState.maxDetourSize;
    }

    public InsertLast(PathState pathState) {
        this(pathState, false);
    }

    @Override public String name() { return name; }

    @Override public void update() {}
    @Override public void revert() {}
    @Override public boolean check() { return true; }

    @Override
    public void commit() {
        int nChanged = pathState.nChanged();
        if (nChanged > 0) {
            int d = pathState.changed[0];
            int[] path = pathState.path(d);
            int[] oldPath = pathState.oldPath(d);
            int limit = Math.min(pathState.size(d), pathState.oldSize(d));
            int p = 1;
            while (p < limit && path[p] == oldPath[p]) p++;
            if (p < limit) lastDetour = path[p];
        }
    }

    @Override
    public void setNeighborhood(Integer demand) {
        this.demand = demand;
        this.position = 1;
        this.size = pathState.size(demand);
    }

    @Override
    public boolean hasNext() {
        return position != size - 1 && size < maxDetourSize;
    }

    @Override
    public void next() {
        position++;
    }

    @Override
    public void apply() {
        pathState.insert(demand, lastDetour, position);
    }

    @Override
    public void saveBest() {
        storedPosition = position;
        storedNode = lastDetour;
    }

    @Override
    public void applyBest() {
        position = storedPosition;
        lastDetour = storedNode;
        if (debug) {
            System.out.println("Inserting " + lastDetour + " at position " + position + " for demand " + demand);
        }
        apply();
    }
}
