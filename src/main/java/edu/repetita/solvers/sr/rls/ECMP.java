package edu.repetita.solvers.sr.rls;

/*
 * To route traffic from src to dest, ECMP uses all shortest paths.
 * At every node, it splits the upcoming traffic equally among successors on the DAG of shortest paths to dest. 
 */
public class ECMP {
    public final int[][] nEdgesToModify;
    public final int[][][] edgesToModify;
    public final double[][][] fractionToModify;

    public ECMP(int nNodes, int nEdges, ShortestPaths sp) {
        this.nEdgesToModify = new int[nNodes][nNodes];
        this.edgesToModify = new int[nNodes][nNodes][];
        this.fractionToModify = new double[nNodes][nNodes][];

        initialize(nNodes, nEdges, sp);
    }

    private void initialize(int nNodes, int nEdges, ShortestPaths sp) {
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
}
