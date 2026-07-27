package edu.repetita.solvers.sr.rls.neighborhood;

import edu.repetita.solvers.sr.rls.core.Neighborhood;
import edu.repetita.solvers.sr.rls.state.PathState;

public class InsertDetours extends Neighborhood<Integer> {
    public final String name = "InsertDetours";

    private final int[] detours;
    private final int nDetours;
    private final PathState pathState;
    private final boolean debug;

    private int demand = -1;
    private int position = 0;
    private int pDetour = 0;
    private int size = 0;
    private final int maxDetourSize;

    private int storedPosition = 0;
    private int storedPDetour = 0;

    public InsertDetours(int[] detours, PathState pathState, boolean debug) {
        this.detours = detours;
        this.nDetours = detours.length;
        this.pathState = pathState;
        this.debug = debug;
        this.maxDetourSize = pathState.maxDetourSize;
    }

    public InsertDetours(int[] detours, PathState pathState) {
        this(detours, pathState, false);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void setNeighborhood(Integer demand) {
        this.demand = demand;
        this.position = 1;
        this.pDetour = -1;
        this.size = pathState.size(demand);
    }

    @Override
    public boolean hasNext() {
        return !(pDetour == nDetours - 1 && position == size - 1) && size < maxDetourSize;
    }

    @Override
    public void next() {
        pDetour++;
        if (pDetour >= nDetours) {
            pDetour = 0;
            position++;
        }
    }

    @Override
    public void apply() {
        pathState.insert(demand, detours[pDetour], position);
    }

    @Override
    public void saveBest() {
        storedPosition = position;
        storedPDetour = pDetour;
    }

    @Override
    public void applyBest() {
        position = storedPosition;
        pDetour = storedPDetour;
        if (debug) {
            System.out.println("Inserting " + detours[pDetour] + " at position " + position + " for demand " + demand);
        }
        apply();
    }
}
