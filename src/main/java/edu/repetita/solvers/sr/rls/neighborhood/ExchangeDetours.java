package edu.repetita.solvers.sr.rls.neighborhood;

import edu.repetita.solvers.sr.rls.core.Neighborhood;
import edu.repetita.solvers.sr.rls.state.PathState;

public class ExchangeDetours extends Neighborhood<MoveDetour.DemandPair> {
    public final String name = "ExchangeDetours";

    private final PathState pathState;
    private final boolean debug;

    private int demand1 = -1;
    private int demand2 = -1;

    private int position1 = 0;
    private int position2 = 1;

    private int size1 = 0;
    private int size2 = 0;

    private int storedPosition1 = position1;
    private int storedPosition2 = position2;

    public ExchangeDetours(PathState pathState, boolean debug) {
        this.pathState = pathState;
        this.debug = debug;
    }

    public ExchangeDetours(PathState pathState) {
        this(pathState, false);
    }

    @Override public String name() { return name; }

    @Override
    public void setNeighborhood(MoveDetour.DemandPair demands) {
        this.demand1 = demands.d1;
        this.demand2 = demands.d2;

        position1 = 0;
        position2 = 1;

        size1 = pathState.size(demand1);
        size2 = pathState.size(demand2);
    }

    @Override
    public boolean hasNext() {
        return size2 > 2 && size1 > 2 && demand1 != demand2 && (position1 != size1 - 2 || position2 != size2 - 2);
    }

    @Override
    public void next() {
        position1++;
        if (position1 >= size1 - 1) {
            position1 = 1;
            position2++;
        }
    }

    @Override
    public void apply() {
        int node1 = pathState.path(demand1)[position1];
        int node2 = pathState.path(demand2)[position2];

        pathState.replace(demand1, node2, position1);
        pathState.replace(demand2, node1, position2);
    }

    @Override
    public void saveBest() {
        storedPosition1 = position1;
        storedPosition2 = position2;
    }

    @Override
    public void applyBest() {
        position1 = storedPosition1;
        position2 = storedPosition2;
        apply();
    }
}
