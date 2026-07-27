package edu.repetita.solvers.sr.rls.neighborhood;

import edu.repetita.solvers.sr.rls.core.Neighborhood;
import edu.repetita.solvers.sr.rls.state.PathState;

public class Insert extends Neighborhood<Integer> {
    public final String name = "Insert";

    private final int nNodes;
    private final PathState pathState;
    private final boolean debug;

    private int demand = -1;
    private int source = -1;
    private int destination = -1;
    private int position = 0;
    private int node = 0;
    private int size = 0;
    private final int maxDetourSize;

    private int storedPosition = 0;
    private int storedNode = 0;

    public Insert(int nNodes, PathState pathState, boolean debug) {
        this.nNodes = nNodes;
        this.pathState = pathState;
        this.debug = debug;
        this.maxDetourSize = pathState.maxDetourSize;
    }

    public Insert(int nNodes, PathState pathState) {
        this(nNodes, pathState, false);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void setNeighborhood(Integer demand) {
        this.demand = demand;
        this.source = pathState.source(demand);
        this.destination = pathState.destination(demand);
        this.position = 1;
        this.node = -1;
        this.size = pathState.size(demand);
    }

    @Override
    public boolean hasNext() {
        return !(node == nNodes - 1 && position == size - 1) && size < maxDetourSize;
    }

    @Override
    public void next() {
        node++;
        if (node >= nNodes) {
            node = 0;
            position++;
        }
    }

    @Override
    public void apply() {
        pathState.insert(demand, node, position);
    }

    @Override
    public void saveBest() {
        storedPosition = position;
        storedNode = node;
    }

    @Override
    public void applyBest() {
        position = storedPosition;
        node = storedNode;
        apply();
    }
}
