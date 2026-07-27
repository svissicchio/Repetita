package edu.repetita.solvers.sr.rls.neighborhood;

import edu.repetita.solvers.sr.rls.core.Neighborhood;
import edu.repetita.solvers.sr.rls.state.PathState;

public class ReplaceDetours extends Neighborhood<Integer> {
    public final String name = "ReplaceDetours";

    private final int[] detours;
    private final int nDetours;
    private final PathState pathState;
    private final boolean debug;

    private int demand = -1;
    private int source = -1;
    private int destination = -1;
    private int position = 0;
    private int pDetour = 0;
    private int size = 0;

    private int storedPosition = position;
    private int storedPDetour = pDetour;

    public ReplaceDetours(int[] detours, PathState pathState, boolean debug) {
        this.detours = detours;
        this.nDetours = detours.length;
        this.pathState = pathState;
        this.debug = debug;
    }

    public ReplaceDetours(int[] detours, PathState pathState) {
        this(detours, pathState, false);
    }

    @Override public String name() { return name; }

    @Override
    public void setNeighborhood(Integer demand) {
        this.demand = demand;
        source = pathState.source(demand);
        destination = pathState.destination(demand);
        position = 0;
        pDetour = nDetours - 1;
        size = pathState.size(demand);
    }

    @Override
    public boolean hasNext() {
        return !(pDetour == nDetours - 1 && position == size - 2);
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
        if (pathState.path(demand)[position] != detours[pDetour]) {
            pathState.replace(demand, detours[pDetour], position);
        }
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
            System.out.println("Replacing " + pathState.path(demand)[position] + " by " + detours[pDetour] + " at position " + position + " for demand " + demand);
        }
        apply();
    }
}
