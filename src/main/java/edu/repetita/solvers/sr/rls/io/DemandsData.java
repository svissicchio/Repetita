package edu.repetita.solvers.sr.rls.io;

public class DemandsData {
    public final String[] demandLabels;
    public final int[] demandSrcs;
    public final int[] demandDests;
    public final double[] demandTraffics;
    public final int nDemands;

    public DemandsData(String[] demandLabels, int[] demandSrcs, int[] demandDests, double[] demandTraffics) {
        this.demandLabels = demandLabels;
        this.demandSrcs = demandSrcs;
        this.demandDests = demandDests;
        this.demandTraffics = demandTraffics;
        this.nDemands = demandLabels.length;
    }

    public int nDemands() {
        return nDemands;
    }

    public static DemandsData apply(String[] demandLabels, int[] demandSrcs, int[] demandDests, double[] demandTraffics) {
        return new DemandsData(demandLabels.clone(), demandSrcs.clone(), demandDests.clone(), demandTraffics.clone());
    }
}
