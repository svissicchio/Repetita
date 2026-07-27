package edu.repetita.solvers.sr.rls.neighborhood;

import edu.repetita.solvers.sr.rls.core.Neighborhood;
import edu.repetita.solvers.sr.rls.state.PathState;

public class Remove extends Neighborhood<Integer> {
    public final String name = "Remove";

    private final PathState pathState;
    private final boolean debug;

    private int demand = -1;
    private int source = -1;
    private int destination = -1;
    private int size = 0;
    private int position = 0;

    private int storedPosition = 0;

    public Remove(PathState pathState, boolean debug) {
        this.pathState = pathState;
        this.debug = debug;
    }

    public Remove(PathState pathState) {
        this(pathState, false);
    }

    @Override public String name() { return name; }

    @Override
    public void setNeighborhood(Integer demand) {
        this.demand = demand;
        source = pathState.source(demand);
        destination = pathState.destination(demand);
        position = 0;
        size = pathState.size(demand);
    }

    @Override
    public boolean hasNext() {
        return size > 2 && position < size - 2;
    }

    @Override
    public void next() {
        position++;
    }

    @Override
    public void apply() {
        pathState.remove(demand, position);
    }

    @Override
    public void saveBest() {
        storedPosition = position;
    }

    @Override
    public void applyBest() {
        position = storedPosition;
        if (debug) {
            System.out.println("Removing detour at position " + position + " for demand " + demand);
        }
        apply();
    }
}
