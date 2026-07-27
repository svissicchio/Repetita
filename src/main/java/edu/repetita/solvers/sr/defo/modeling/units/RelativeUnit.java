package edu.repetita.solvers.sr.defo.modeling.units;

public class RelativeUnit {
    public final int value;

    public RelativeUnit(int value) {
        this.value = value;
    }

    public static RelativeUnit pct(int percent) {
        return new RelativeUnit(percent);
    }
}
