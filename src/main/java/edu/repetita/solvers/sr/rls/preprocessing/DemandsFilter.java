package edu.repetita.solvers.sr.rls.preprocessing;

import edu.repetita.solvers.sr.rls.core.CapacityData;
import edu.repetita.solvers.sr.rls.io.DemandsData;
import java.util.Arrays;
import java.util.Comparator;

public class DemandsFilter {
    public static DemandsData[] apply(DemandsData input, CapacityData capacity) {
        return doNothing(input);
    }

    public static DemandsData[] doNothing(DemandsData input) {
        DemandsData emptyDemandsData = new DemandsData(new String[0], new int[0], new int[0], new double[0]);
        return new DemandsData[]{input, emptyDemandsData};
    }

    public static DemandsData[] fixedSmallest(DemandsData input, CapacityData capacity, int nRemoved) {
        Integer[] indices = new Integer[input.nDemands];
        for (int i = 0; i < input.nDemands; i++) indices[i] = i;
        Arrays.sort(indices, Comparator.comparingDouble(i -> input.demandTraffics[i]));

        int nUnder = Math.min(nRemoved, input.nDemands);
        int nAbove = input.nDemands - nUnder;

        String[] labelsAbove = new String[nAbove];
        int[] srcsAbove = new int[nAbove];
        int[] destsAbove = new int[nAbove];
        double[] trafficsAbove = new double[nAbove];

        for (int i = 0; i < nAbove; i++) {
            int idx = indices[nUnder + i];
            labelsAbove[i] = input.demandLabels[idx];
            srcsAbove[i] = input.demandSrcs[idx];
            destsAbove[i] = input.demandDests[idx];
            trafficsAbove[i] = input.demandTraffics[idx];
        }

        String[] labelsUnder = new String[nUnder];
        int[] srcsUnder = new int[nUnder];
        int[] destsUnder = new int[nUnder];
        double[] trafficsUnder = new double[nUnder];

        for (int i = 0; i < nUnder; i++) {
            int idx = indices[i];
            labelsUnder[i] = input.demandLabels[idx];
            srcsUnder[i] = input.demandSrcs[idx];
            destsUnder[i] = input.demandDests[idx];
            trafficsUnder[i] = input.demandTraffics[idx];
        }

        return new DemandsData[]{
                new DemandsData(labelsAbove, srcsAbove, destsAbove, trafficsAbove),
                new DemandsData(labelsUnder, srcsUnder, destsUnder, trafficsUnder)
        };
    }
}
