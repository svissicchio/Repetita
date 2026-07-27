package edu.repetita.solvers.sr.defo.parsers;

public class DemandsData {
    public final String[] demandLabels;
    public final int[] demandSrcs;
    public final int[] demandDests;
    public final int[] demandTraffics;
    public final int nDemands;

    public DemandsData(String[] demandLabels, int[] demandSrcs, int[] demandDests, int[] demandTraffics) {
        this.demandLabels = demandLabels;
        this.demandSrcs = demandSrcs;
        this.demandDests = demandDests;
        this.demandTraffics = demandTraffics;
        this.nDemands = demandLabels.length;
    }
}
