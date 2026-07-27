package edu.repetita.solvers.sr.defo.utils;

import java.util.Arrays;
import java.util.Random;

public class TrafficMatrixGenerator {

    public static double exp(Random rng) {
        double d = rng.nextDouble();
        return -Math.log(d);
    }

    public static double[][] generate(int n, int seed) {
        return generate(n, new Random(seed));
    }

    public static double[][] generate(int n, Random rng) {
        double[] tIn = new double[n];
        double[] tOut = new double[n];

        for (int i = 0; i < n; i++) {
            tIn[i] = exp(rng);
            tOut[i] = exp(rng);
        }

        double sumInNminus1 = 0.0;
        double sumOutNminus1 = 0.0;
        for (int i = 0; i < n - 1; i++) {
            sumInNminus1 += tIn[i];
            sumOutNminus1 += tOut[i];
        }

        if (sumInNminus1 > sumOutNminus1) {
            tIn[n - 1] = exp(rng);
            tOut[n - 1] = (sumInNminus1 + tIn[n - 1]) - sumOutNminus1;
        } else {
            tOut[n - 1] = exp(rng);
            tIn[n - 1] = (sumOutNminus1 + tOut[n - 1]) - sumInNminus1;
        }

        double tInTot = Arrays.stream(tIn).sum();
        double tOutTot = Arrays.stream(tOut).sum();

        double[][] tm = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tm[i][j] = (tOut[i] * tIn[j]) / tInTot;
            }
        }

        return tm;
    }
}
