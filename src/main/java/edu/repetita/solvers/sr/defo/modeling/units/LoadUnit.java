package edu.repetita.solvers.sr.defo.modeling.units;

public class LoadUnit {
    public final int value;
    public final String label;

    public LoadUnit(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static LoadUnit kbps(int load) {
        if (load < 0) throw new IllegalArgumentException("maxLinkLoad has to be >= 0");
        return new LoadUnit(load, load + " kbps");
    }

    public static LoadUnit Mbps(int load) {
        if (load < 0) throw new IllegalArgumentException("maxLinkLoad has to be >= 0");
        return new LoadUnit(load << 10, load + " Mbps");
    }

    public static LoadUnit Gbps(int load) {
        if (load < 0) throw new IllegalArgumentException("maxLinkLoad has to be >= 0");
        return new LoadUnit(load << 20, load + " Gbps");
    }
}
