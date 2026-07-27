package edu.repetita.solvers.sr.defo.search;

import edu.repetita.solvers.sr.defo.constraints.paths.Visit;
import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

public class IncrPathBranching implements Supplier<Runnable[]> {
    public final IncrPathVar[] paths;
    public final IntUnaryOperator varSelect;
    public final BiFunction<Integer, Integer, Integer> valSelect;

    public IncrPathBranching(IncrPathVar[] paths, IntUnaryOperator varSelect, BiFunction<Integer, Integer, Integer> valSelect) {
        this.paths = paths;
        this.varSelect = varSelect;
        this.valSelect = valSelect;
    }

    @Override
    public Runnable[] get() {
        int i = selectMin();
        if (i == -1) return new Runnable[0];

        IncrPathVar path = paths[i];
        int[] poss = path.possible();
        Integer[] nodes = new Integer[poss.length];
        for (int k = 0; k < poss.length; k++) nodes[k] = poss[k];

        final int pathIdx = i;
        Arrays.sort(nodes, Comparator.comparingInt(n -> valSelect.apply(pathIdx, n)));

        Runnable[] alternatives = new Runnable[nodes.length];
        for (int k = 0; k < nodes.length; k++) {
            int node = nodes[k];
            alternatives[k] = () -> path.store.post(new Visit(path, node));
        }
        return alternatives;
    }

    private int selectMin() {
        int minId = -1;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < paths.length; i++) {
            if (!paths[i].isBound()) {
                int m = varSelect.applyAsInt(i);
                if (m < min) {
                    min = m;
                    minId = i;
                }
            }
        }
        return minId;
    }
}
