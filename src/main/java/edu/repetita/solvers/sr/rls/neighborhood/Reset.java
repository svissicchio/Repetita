package edu.repetita.solvers.sr.rls.neighborhood;

import edu.repetita.solvers.sr.rls.core.Neighborhood;
import edu.repetita.solvers.sr.rls.state.PathState;

public class Reset extends Neighborhood<Integer> {
    public final String name = "Reset";

    private final PathState pathState;
    private final boolean debug;

    private int demand = -1;
    private int source = -1;
    private int destination = -1;
    private boolean neverTried = false;

    public Reset(PathState pathState, boolean debug) {
        this.pathState = pathState;
        this.debug = debug;
    }

    public Reset(PathState pathState) {
        this(pathState, false);
    }

    @Override public String name() { return name; }

    @Override
    public void setNeighborhood(Integer demand) {
        this.demand = demand;
        source = pathState.source(demand);
        destination = pathState.destination(demand);
        neverTried = true;
    }

    @Override
    public boolean hasNext() {
        return pathState.size(demand) > 2 && neverTried;
    }

    @Override
    public void next() {
        neverTried = false;
    }

    @Override
    public void apply() {
        pathState.reset(demand);
    }

    @Override public void saveBest() {}

    @Override
    public void applyBest() {
        if (debug) {
            System.out.println("Resetting demand " + demand);
        }
        apply();
    }
}
