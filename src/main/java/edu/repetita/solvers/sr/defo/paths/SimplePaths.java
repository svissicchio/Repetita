package edu.repetita.solvers.sr.defo.paths;

import edu.repetita.solvers.sr.defo.core.Topology;
import edu.repetita.solvers.sr.defo.utils.Dijkstra;
import java.util.List;

public class SimplePaths {
    private final Topology topology;
    private final int[] weights;
    private final int nNodes;
    private final int[][] nPaths;

    public SimplePaths(Topology topology, int[] weights) {
        if (weights.length != topology.nEdges) {
            throw new IllegalArgumentException("the number of weights does not correspond to the topology.");
        }
        this.topology = topology;
        this.weights = weights;
        this.nNodes = topology.nNodes;
        this.nPaths = new int[nNodes][];

        for (int dest = 0; dest < nNodes; dest++) {
            this.nPaths[dest] = computeNPath(dest);
        }
    }

    public int nPaths(int src, int dest) {
        return nPaths[dest][src];
    }

    private int[] computeNPath(int dest) {
        List<Integer>[] successors = Dijkstra.shortestPathTo(dest, topology, weights).prevs;
        int[] resNPaths = new int[nNodes];
        resNPaths[dest] = 1;

        int i = nNodes;
        while (i > 0) {
            i--;
            computeNPath0(resNPaths, successors, i);
        }
        return resNPaths;
    }

    private int computeNPath0(int[] resNPaths, List<Integer>[] successors, int node) {
        if (resNPaths[node] > 0) {
            return resNPaths[node];
        } else {
            for (int edge : successors[node]) {
                int n = topology.edgeDest(edge);
                resNPaths[node] += computeNPath0(resNPaths, successors, n);
            }
            return resNPaths[node];
        }
    }
}
