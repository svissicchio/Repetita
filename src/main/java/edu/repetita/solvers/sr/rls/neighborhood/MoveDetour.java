package edu.repetita.solvers.sr.rls.neighborhood;

import edu.repetita.solvers.sr.rls.core.Neighborhood;
import edu.repetita.solvers.sr.rls.state.PathState;

public class MoveDetour extends Neighborhood<MoveDetour.DemandPair> {
    public static class DemandPair {
        public final int d1;
        public final int d2;
        public DemandPair(int d1, int d2) {
            this.d1 = d1;
            this.d2 = d2;
        }
    }

    public final String name = "MoveDetour";

    private final PathState pathState;
    private final boolean debug;

    private int demand1 = -1;
    private int demand2 = -1;
    private int position1 = 0;
    private int position2 = 0;
    private int size1 = 0;
    private int size2 = 0;
    private final int maxDetourSize;

    private int storedPosition1 = position1;
    private int storedPosition2 = position2;

    public MoveDetour(PathState pathState, boolean debug) {
        this.pathState = pathState;
        this.debug = debug;
        this.maxDetourSize = pathState.maxDetourSize;
    }

    public MoveDetour(PathState pathState) {
        this(pathState, false);
    }

    @Override public String name() { return name; }

    @Override
    public void setNeighborhood(DemandPair demands) {
        this.demand1 = demands.d1;
        this.demand2 = demands.d2;
        this.position1 = 0;
        this.position2 = 1;
        this.size1 = pathState.size(demand1);
        this.size2 = pathState.size(demand2);
    }

    @Override
    public boolean hasNext() {
        return size2 < maxDetourSize && size1 > 2 && demand1 != demand2 && (position1 != size1 - 2 || position2 != size2 - 1);
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
        int node = pathState.path(demand1)[position1];
        pathState.remove(demand1, position1);
        pathState.insert(demand2, node, position2);
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
