package edu.repetita.solvers.sr.rls.constraint;

import edu.repetita.solvers.sr.rls.state.DelayState;
import edu.repetita.solvers.sr.rls.state.Trial;

public class MaxDelayIncrease implements Trial {
    private final double increaseFactor = 2.0;
    private final double[] limit;
    private final DelayState delayState;

    public MaxDelayIncrease(int nDemands, DelayState delayState) {
        this.delayState = delayState;
        this.limit = new double[nDemands];
        for (int demand = 0; demand < nDemands; demand++) {
            limit[demand] = delayState.delay[demand] * increaseFactor;
        }
    }

    @Override
    public boolean check() {
        int[] changed = delayState.changed;
        int pChanged = delayState.nChanged();
        boolean ok = true;
        while (pChanged > 0 && ok) {
            pChanged--;
            int demand = changed[pChanged];
            ok &= delayState.delay[demand] < limit[demand];
        }
        return ok;
    }

    @Override public void revert() {}
    @Override public void commit() {}
    @Override public void update() {}
}
