package edu.repetita.solvers.sr.defo.search;

import edu.repetita.solvers.sr.defo.constraints.paths.Visit;
import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class IncrPathBranchingSingle implements Supplier<Runnable[]> {
    public final IncrPathVar path;
    public final BiFunction<IncrPathVar, Integer, Integer> valSelect;

    public IncrPathBranchingSingle(IncrPathVar path, BiFunction<IncrPathVar, Integer, Integer> valSelect) {
        this.path = path;
        this.valSelect = valSelect;
    }

    @Override
    public Runnable[] get() {
        if (path.isBound()) return new Runnable[0];

        int[] poss = path.possible();
        Integer[] nodes = new Integer[poss.length];
        for (int k = 0; k < poss.length; k++) nodes[k] = poss[k];

        Arrays.sort(nodes, Comparator.comparingInt(n -> valSelect.apply(path, n)));

        Runnable[] alternatives = new Runnable[nodes.length];
        for (int k = 0; k < nodes.length; k++) {
            int node = nodes[k];
            alternatives[k] = () -> path.store.post(new Visit(path, node));
        }
        return alternatives;
    }
}
