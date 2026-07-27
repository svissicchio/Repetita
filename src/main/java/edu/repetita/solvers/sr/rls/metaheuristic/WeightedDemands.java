package edu.repetita.solvers.sr.rls.metaheuristic;

import java.util.Random;

public class WeightedDemands {
    private final int nDemands;
    private final double[] partialSums;
    private final Random random = new Random();

    public WeightedDemands(double[] weights) {
        this.nDemands = weights.length;
        this.partialSums = new double[nDemands];
        initialize(weights);
    }

    private void initialize(double[] weights) {
        double sum = 0.0;
        partialSums[0] = 0.0;
        for (int p = 0; p < nDemands; p++) {
            sum += weights[p];
            partialSums[p] = sum;
        }
    }

    public int weightedChoice() {
        double point = random.nextDouble() * partialSums[nDemands - 1];

        int left = 0;
        int right = nDemands;
        while (left + 1 < right) {
            int middle = left + (right - left) / 2;
            if (partialSums[middle] <= point) {
                left = middle;
            } else {
                right = middle;
            }
        }
        return left;
    }
}
