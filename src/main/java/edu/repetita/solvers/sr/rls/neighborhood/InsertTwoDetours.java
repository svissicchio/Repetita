package edu.repetita.solvers.sr.rls.neighborhood;

import edu.repetita.solvers.sr.rls.core.Neighborhood;
import edu.repetita.solvers.sr.rls.state.PathState;

public class InsertTwoDetours extends Neighborhood<Integer> {
    public final String name = "InsertTwoDetours";

    private final int[] detours;
    private final int nDetours;
    private final PathState pathState;
    private final boolean debug;

    private int demand = -1;
    private int source = -1;
    private int destination = -1;
    private int position = 0;
    private int pDetour1 = 0;
    private int pDetour2 = 0;
    private int size = 0;
    private final int maxDetourSize;

    private int storedPosition = 0;
    private int storedPDetour1 = pDetour1;
    private int storedPDetour2 = pDetour2;

    public InsertTwoDetours(int[] detours, PathState pathState, boolean debug) {
        this.detours = detours;
        this.nDetours = detours.length;
        this.pathState = pathState;
        this.debug = debug;
        this.maxDetourSize = pathState.maxDetourSize;
    }

    public InsertTwoDetours(int[] detours, PathState pathState) {
        this(detours, pathState, false);
    }

    @Override public String name() { return name; }

    @Override
    public void setNeighborhood(Integer demand) {
        this.demand = demand;
        source = pathState.source(demand);
        destination = pathState.destination(demand);
        position = 1;
        pDetour1 = -1;
        pDetour2 = 0;
        size = pathState.size(demand);
    }

    @Override
    public boolean hasNext() {
        return !(pDetour1 == nDetours - 1 && pDetour2 == nDetours - 1 && position == size - 1) && size < maxDetourSize - 1;
    }

    @Override
    public void next() {
        pDetour1++;
        if (pDetour1 == nDetours) {
            pDetour1 = 0;
            pDetour2++;
        }
        if (pDetour2 == nDetours) {
            pDetour1 = 0;
            pDetour2 = 0;
            position++;
        }
    }

    @Override
    public void apply() {
        pathState.insert(demand, detours[pDetour2], position);
        pathState.insert(demand, detours[pDetour1], position);
    }

    @Override
    public void saveBest() {
        storedPosition = position;
        storedPDetour1 = pDetour1;
        storedPDetour2 = pDetour2;
    }

    @Override
    public void applyBest() {
        position = storedPosition;
        pDetour1 = storedPDetour1;
        pDetour2 = storedPDetour2;
        apply();
    }
}
