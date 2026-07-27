package edu.repetita.solvers.sr.rls.state;

import edu.repetita.solvers.sr.rls.ShortestPaths;
import edu.repetita.solvers.sr.rls.io.DemandsData;

public class FlowStateRecomputeDAG extends FlowStateChecker {
    private final ShortestPaths sp;
    private final double[] toRoute;

    public FlowStateRecomputeDAG(int nNodes, int nEdges, ShortestPaths sp, PathState pathState, DemandsData demandsData) {
        super(nNodes, nEdges, pathState, demandsData);
        this.sp = sp;
        this.toRoute = new double[nNodes];
        initialize();
        commitState();
    }

    @Override
    public void modify(int source, int destination, double bw) {
        int[][] successorNodes = sp.successorNodes(destination);
        int[][] successorEdges = sp.successorEdges(destination);
        int[] nSuccessors = sp.nSuccessors(destination);

        int source0 = source;
        while (source0 != destination && nSuccessors[source0] == 1) {
            int edge = successorEdges[source0][0];
            updateValue(edge, values[edge] + bw);

            int next = successorNodes[source0][0];
            source0 = next;
        }

        if (source0 != destination) {
            int[] ordering = sp.topologicalOrdering();
            int pOrdering = sp.makeTopologicalOrdering(source, destination);
            toRoute[source0] = bw;

            while (pOrdering > 0) {
                pOrdering--;
                int node = ordering[pOrdering];

                int pSucc = nSuccessors[node];
                double increment = toRoute[node] / pSucc;
                toRoute[node] = 0.0;

                while (pSucc > 0) {
                    pSucc--;
                    int succEdge = successorEdges[node][pSucc];
                    updateValue(succEdge, values[succEdge] + increment);

                    int succNode = successorNodes[node][pSucc];
                    toRoute[succNode] += increment;
                }
            }
        }
    }
}
