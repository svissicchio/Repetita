package edu.repetita.solvers.sr.rls.neighborhood;

import edu.repetita.solvers.sr.rls.ECMP;
import edu.repetita.solvers.sr.rls.constraint.MaxLoad;
import edu.repetita.solvers.sr.rls.core.Neighborhood;
import edu.repetita.solvers.sr.rls.state.PathState;

public class ReplaceGuarded extends Neighborhood<Integer> {
    public final String name = "ReplaceGuarded";

    private final int nNodes;
    private final int nEdges;
    private final PathState pathState;
    private final MaxLoad maxLoad;
    private final boolean debug;

    private int demand = -1;
    private int source = -1;
    private int destination = -1;
    private int position = 0;
    private int node = 0;
    private int size = 0;

    private final double[][][] fractions;
    private int storedPosition = 0;
    private int storedNode = node;

    public ReplaceGuarded(int nNodes, int nEdges, ECMP ecmp, PathState pathState, MaxLoad maxLoad, boolean debug) {
        this.nNodes = nNodes;
        this.nEdges = nEdges;
        this.pathState = pathState;
        this.maxLoad = maxLoad;
        this.debug = debug;
        this.fractions = new double[nNodes][nNodes][nEdges];

        for (int src = 0; src < nNodes; src++) {
            for (int dest = 0; dest < nNodes; dest++) {
                int p = ecmp.nEdgesToModify[src][dest];
                while (p > 0) {
                    p--;
                    int edge = ecmp.edgesToModify[src][dest][p];
                    double frac = ecmp.fractionToModify[src][dest][p];
                    fractions[src][dest][edge] = frac;
                }
            }
        }
    }

    public ReplaceGuarded(int nNodes, int nEdges, ECMP ecmp, PathState pathState, MaxLoad maxLoad) {
        this(nNodes, nEdges, ecmp, pathState, maxLoad, false);
    }

    @Override public String name() { return name; }

    @Override
    public void setNeighborhood(Integer demand) {
        this.demand = demand;
        source = pathState.source(demand);
        destination = pathState.destination(demand);
        position = 0;
        node = nNodes - 1;
        size = pathState.size(demand);
    }

    @Override
    public boolean hasNext() {
        return size >= 3 && !(node == nNodes - 1 && position == size - 2);
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
        int maxEdge = maxLoad.selectRandomMaxEdge();
        int src = pathState.path(demand)[position - 1];
        int middle = pathState.path(demand)[position];
        int dest = pathState.path(demand)[position + 1];

        double loadSub = fractions[src][middle][maxEdge] + fractions[middle][dest][maxEdge];
        double loadAdd = fractions[src][node][maxEdge] + fractions[node][dest][maxEdge];

        if (loadSub > loadAdd) {
            pathState.replace(demand, node, position);
        }
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
