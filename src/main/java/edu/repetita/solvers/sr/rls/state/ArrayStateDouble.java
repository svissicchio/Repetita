package edu.repetita.solvers.sr.rls.state;

public abstract class ArrayStateDouble extends TrialState {
    public final double[] values;
    public final double[] oldValues;
    private final boolean[] deltaMarker;
    private int nDelta_ = 0;
    private final int[] deltaElements_;

    public ArrayStateDouble(int nElements) {
        this.values = new double[nElements];
        this.oldValues = new double[nElements];
        this.deltaMarker = new boolean[nElements];
        this.deltaElements_ = new int[nElements];
    }

    protected void updateValue(int element, double newValue) {
        if (!deltaMarker[element]) {
            deltaMarker[element] = true;
            deltaElements_[nDelta_] = element;
            nDelta_++;
        }
        values[element] = newValue;
    }

    public int[] deltaElements() {
        return deltaElements_;
    }

    public int nDelta() {
        return nDelta_;
    }

    @Override
    public void revertState() {
        while (nDelta_ > 0) {
            nDelta_--;
            int element = deltaElements_[nDelta_];
            deltaMarker[element] = false;
            values[element] = oldValues[element];
        }
    }

    @Override
    public void commitState() {
        while (nDelta_ > 0) {
            nDelta_--;
            int element = deltaElements_[nDelta_];
            deltaMarker[element] = false;
            oldValues[element] = values[element];
        }
    }
}
