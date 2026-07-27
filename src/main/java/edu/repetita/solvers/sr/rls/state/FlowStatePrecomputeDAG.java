package edu.repetita.solvers.sr.rls.state;

import edu.repetita.solvers.sr.rls.ShortestPaths;
import edu.repetita.solvers.sr.rls.io.DemandsData;

public class FlowStatePrecomputeDAG extends FlowStateChecker {
    private final ShortestPaths sp;
    private final int[][] nEdgesToModify;
    private final int[][][] edgesToModify;
    private final double[][][] fractionToModify;

    public FlowStatePrecomputeDAG(int nNodes, int nEdges, ShortestPaths sp, PathState pathState, DemandsData demandsData) {
        super(nNodes, nEdges, pathState, demandsData);
        this.sp = sp;
        this.nEdgesToModify = new int[nNodes][nNodes];
        this.edgesToModify = new int[nNodes][nNodes][];
        this.fractionToModify = new double[nNodes][nNodes][];

        initializeEdgesToModify();
        initialize();
        commitState();
    }

    private void initializeEdgesToModify() {
        double[] toRoute = new double[nNodes];
        double[] fractionDAG = new double[nEdges];
        int[] edgesDAG = new int[nEdges];

        for (int source = 0; source < nNodes; source++) {
            for (int destination = 0; destination < nNodes; destination++) {
                int nEdgesDAG = 0;
                int[] ordering = sp.topologicalOrdering();
                int nOrdering = sp.makeTopologicalOrdering(source, destination);

                toRoute[source] = 1.0;
                while (nOrdering > 0) {
                    nOrdering--;
                    int node = ordering[nOrdering];

                    int pSucc = sp.nSuccessors(destination, node);
                    double amountToRoute = toRoute[node] / pSucc;
                    while (pSucc > 0) {
                        pSucc--;
                        int succNode = sp.successorNodes(destination, node)[pSucc];
                        toRoute[succNode] += amountToRoute;

                        int succEdge = sp.successorEdges(destination, node)[pSucc];
                        edgesDAG[nEdgesDAG] = succEdge;
                        fractionDAG[nEdgesDAG] = amountToRoute;
                        nEdgesDAG++;
                    }
                    toRoute[node] = 0.0;
                }

                nEdgesToModify[source][destination] = nEdgesDAG;
                edgesToModify[source][destination] = new int[nEdgesDAG];
                System.arraycopy(edgesDAG, 0, edgesToModify[source][destination], 0, nEdgesDAG);

                fractionToModify[source][destination] = new double[nEdgesDAG];
                for (int pEdge = 0; pEdge < nEdgesDAG; pEdge++) {
                    fractionToModify[source][destination][pEdge] = fractionDAG[pEdge];
                }
            }
        }
    }

    @Override
    public void modify(int source, int destination, double bw) {
        int[] edges = edgesToModify[source][destination];
        double[] fractions = fractionToModify[source][destination];

        int pEdge = nEdgesToModify[source][destination];
        while (pEdge > 0) {
            pEdge--;
            int edge = edges[pEdge];
            updateValue(edge, values[edge] + bw * fractions[pEdge]);
        }
    }
}
