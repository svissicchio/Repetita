package edu.repetita.solvers.sr.defo.modeling.variables;

import edu.repetita.solvers.sr.defo.modeling.DEFOConstraint;
import edu.repetita.solvers.sr.defo.modeling.units.LoadUnit;
import edu.repetita.solvers.sr.defo.modeling.units.RelativeUnit;

public class DEFOLoadVar {
    public final int edgeId;

    public DEFOLoadVar(int edgeId) {
        this.edgeId = edgeId;
    }

    public DEFOConstraint lt(LoadUnit load) {
        return new DEFOConstraint.DEFOLowerLoad(edgeId, load.value, false);
    }

    public DEFOConstraint le(LoadUnit load) {
        return new DEFOConstraint.DEFOLowerEqLoad(edgeId, load.value, false);
    }

    public DEFOConstraint lt(RelativeUnit percent) {
        return new DEFOConstraint.DEFOLowerLoad(edgeId, percent.value, true);
    }

    public DEFOConstraint le(RelativeUnit percent) {
        return new DEFOConstraint.DEFOLowerEqLoad(edgeId, percent.value, true);
    }
}
