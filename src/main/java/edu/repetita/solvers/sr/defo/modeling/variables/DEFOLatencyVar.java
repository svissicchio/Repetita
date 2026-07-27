package edu.repetita.solvers.sr.defo.modeling.variables;

import edu.repetita.solvers.sr.defo.modeling.DEFOConstraint;
import edu.repetita.solvers.sr.defo.modeling.units.RelativeUnit;
import edu.repetita.solvers.sr.defo.modeling.units.TimeUnit;

public class DEFOLatencyVar {
    public final int demandId;

    public DEFOLatencyVar(int demandId) {
        this.demandId = demandId;
    }

    public DEFOConstraint lt(TimeUnit latency) {
        return new DEFOConstraint.DEFOLowerLatency(demandId, latency.value, false);
    }

    public DEFOConstraint le(TimeUnit latency) {
        return new DEFOConstraint.DEFOLowerEqLatency(demandId, latency.value, false);
    }

    public DEFOConstraint lt(RelativeUnit percent) {
        return new DEFOConstraint.DEFOLowerLatency(demandId, percent.value, true);
    }

    public DEFOConstraint le(RelativeUnit percent) {
        return new DEFOConstraint.DEFOLowerEqLatency(demandId, percent.value, true);
    }
}
