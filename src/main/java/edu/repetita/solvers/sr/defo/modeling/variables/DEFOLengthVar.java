package edu.repetita.solvers.sr.defo.modeling.variables;

import edu.repetita.solvers.sr.defo.modeling.DEFOConstraint;

public class DEFOLengthVar {
    public final int demandId;

    public DEFOLengthVar(int demandId) {
        this.demandId = demandId;
    }

    public DEFOConstraint lt(int length) {
        return new DEFOConstraint.DEFOLowerLength(demandId, length + 2);
    }

    public DEFOConstraint le(int length) {
        return new DEFOConstraint.DEFOLowerEqLength(demandId, length + 2);
    }
}
