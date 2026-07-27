package edu.repetita.solvers.sr.rls.state;

import edu.repetita.solvers.sr.rls.core.DelayData;

public class DelayState extends TrialState {
    public final int nDemands;
    public final int[] delay;
    public final int[] oldDelay;
    private final boolean[] markedDemands;
    public final int[] changed;
    private int nChanged_ = 0;
    private final DelayData delays;
    private final PathState pathState;

    public DelayState(int nDemands, DelayData delays, PathState pathState) {
        this.nDemands = nDemands;
        this.delays = delays;
        this.pathState = pathState;
        this.delay = new int[nDemands];
        this.oldDelay = new int[nDemands];
        this.markedDemands = new boolean[nDemands];
        this.changed = new int[nDemands];
        initialize();
    }

    public int nChanged() {
        return nChanged_;
    }

    private void initialize() {
        int demand = nDemands;
        while (demand > 0) {
            demand--;
            delay[demand] = computeDelay(demand);
        }
    }

    private int computeDelay(int demand) {
        int sumDelay = 0;
        int[] path = pathState.path(demand);
        int p = pathState.size(demand) - 1;
        while (p > 0) {
            p--;
            sumDelay += delays.delay(path[p], path[p + 1]);
        }
        return sumDelay;
    }

    private void update(int demand, int newDelay) {
        if (!markedDemands[demand]) {
            markedDemands[demand] = true;
            changed[nChanged_++] = demand;
            oldDelay[demand] = delay[demand];
        }
        delay[demand] = newDelay;
    }

    @Override
    public void updateState() {
        int[] changedDemand = pathState.changed;
        int p = pathState.nChanged();
        while (p > 0) {
            p--;
            int demand = changedDemand[p];
            int newDelay = computeDelay(demand);
            if (newDelay != delay[demand]) {
                update(demand, newDelay);
            }
        }
    }

    @Override
    public boolean check() {
        updateState();
        return super.check();
    }

    @Override
    public void commitState() {
        while (nChanged_ > 0) {
            nChanged_--;
            markedDemands[changed[nChanged_]] = false;
        }
    }

    @Override
    public void revertState() {
        while (nChanged_ > 0) {
            nChanged_--;
            int demand = changed[nChanged_];
            markedDemands[demand] = false;
            delay[demand] = oldDelay[demand];
        }
    }
}
