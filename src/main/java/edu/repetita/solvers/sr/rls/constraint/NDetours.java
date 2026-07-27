package edu.repetita.solvers.sr.rls.constraint;

import edu.repetita.solvers.sr.rls.state.PathState;

public class NDetours implements Lexicographic.TrialObjective {
    private int nDetours = 0;
    private int oldNDetours = nDetours;
    private final PathState pathState;
    private final int nDemands;

    public NDetours(PathState pathState) {
        this.pathState = pathState;
        this.nDemands = pathState.nDemands;
        initialize();
    }

    @Override
    public double score() {
        return nDetours;
    }

    private void initialize() {
        int counter = 0;
        int demand = nDemands;
        while (demand > 0) {
            demand--;
            if (pathState.size(demand) > 2) counter++;
        }
        nDetours = counter;
        oldNDetours = counter;
    }

    @Override
    public void update() {
        int[] demands = pathState.changed;
        int p = pathState.nChanged();
        while (p > 0) {
            p--;
            int demand = demands[p];
            boolean hadDetour = pathState.oldSize(demand) > 2;
            boolean hasDetour = pathState.size(demand) > 2;

            if (hadDetour && !hasDetour) nDetours--;
            if (!hadDetour && hasDetour) nDetours++;
        }
    }

    @Override
    public boolean check() {
        update();
        return oldNDetours >= nDetours;
    }

    @Override
    public void revert() {
        nDetours = oldNDetours;
    }

    @Override
    public void commit() {
        oldNDetours = nDetours;
    }
}
