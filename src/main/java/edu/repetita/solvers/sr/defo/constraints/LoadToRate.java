package edu.repetita.solvers.sr.defo.constraints;

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPIntVar;

public class LoadToRate extends AbstractCPConstraint {
    public final CPIntVar load;
    public final CPIntVar rate;
    public final int step;

    public LoadToRate(CPIntVar load, CPIntVar rate, int step) {
        super(load.getSolver());
        this.load = load;
        this.rate = rate;
        this.step = step;
    }

    @Override
    public void post() {
        load.propagateOnBoundChange(this);
        rate.propagateOnBoundChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        updatedRate();
        updatedLoad();
    }

    private void updatedRate() {
        int min = load.min() / step;
        if (min < rate.min()) {
            load.removeBelow(step * rate.min());
        } else {
            int max = load.max() / step;
            if (max > rate.max()) {
                load.removeAbove(step * (rate.max() + 1) - 1);
            }
        }
    }

    private void updatedLoad() {
        int min = load.min() / step;
        rate.removeBelow(min);
        int max = load.max() / step;
        rate.removeAbove(max);
    }
}
