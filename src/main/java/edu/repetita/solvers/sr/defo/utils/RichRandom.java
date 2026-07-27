package edu.repetita.solvers.sr.defo.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;

public class RichRandom {
    private final Random random;

    public RichRandom(Random random) {
        this.random = random;
    }

    public RichRandom(int seed) {
        this(new Random(seed));
    }

    public RichRandom() {
        this(new Random());
    }

    public <B> B weightedSelect(B[] array, ToIntFunction<B> prob) {
        B elem = array[1];
        int acc = prob.applyAsInt(elem);
        int i = 1;
        while (i < array.length) {
            B e = array[i];
            int p = prob.applyAsInt(e);
            acc += p;
            if (random.nextInt(acc) < p) elem = e;
            i++;
        }
        return elem;
    }

    public <B> B weightedSelect(Iterable<B> col, ToIntFunction<B> prob) {
        B elem = null;
        int acc = 0;
        boolean first = true;

        for (B e : col) {
            if (first) {
                elem = e;
                acc = prob.applyAsInt(e);
                first = false;
            } else {
                int p = prob.applyAsInt(e);
                acc += p;
                if (random.nextInt(acc) < p) elem = e;
            }
        }
        return elem;
    }

    public int[] weightedShuffle(int[] array, IntUnaryOperator f) {
        double[] values = new double[array.length];
        Integer[] sorted = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            values[i] = f.applyAsInt(array[i]) * random.nextFloat();
            sorted[i] = i;
        }

        Arrays.sort(sorted, Comparator.comparingDouble(i -> values[i]));

        int[] res = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            res[i] = array[sorted[i]];
        }
        return res;
    }

    public int[] weightedTake(int[] array, int k, IntUnaryOperator f, int alpha) {
        if (k >= array.length) return array;
        else {
            int[] selected = new int[k];
            Integer[] sorted = new Integer[array.length];
            for (int i = 0; i < array.length; i++) sorted[i] = array[i];
            Arrays.sort(sorted, Comparator.comparingInt(f::applyAsInt));

            boolean[] contained = new boolean[array.length];
            Arrays.fill(contained, true);

            int nSelected = 0;
            while (nSelected < k) {
                int r = (int) Math.floor(Math.pow(random.nextDouble(), alpha) * sorted.length);
                while (!contained[r]) {
                    if (r > 0) r--;
                    else r = array.length - 1;
                }
                int elem = sorted[r];
                contained[r] = false;
                selected[nSelected++] = elem;
            }
            return selected;
        }
    }

    public int[] weightedTake(int[] array, int k, IntUnaryOperator f) {
        return weightedTake(array, k, f, 2);
    }
}
