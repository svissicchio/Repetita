package edu.repetita.solvers.sr.defo.modeling.units;

public class TimeUnit {
    public final int value;
    public final String label;

    public TimeUnit(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static TimeUnit minutes(int time) {
        if (time < 0) throw new IllegalArgumentException("time has to be >= 0");
        return new TimeUnit(time * 60000, time + " min");
    }

    public static TimeUnit s(int time) {
        if (time < 0) throw new IllegalArgumentException("time has to be >= 0");
        return new TimeUnit(time * 1000, time + " s");
    }

    public static TimeUnit ms(int time) {
        if (time < 0) throw new IllegalArgumentException("time has to be >= 0");
        return new TimeUnit(time, time + " ms");
    }
}
