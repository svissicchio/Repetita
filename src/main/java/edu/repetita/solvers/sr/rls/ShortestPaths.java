package edu.repetita.solvers.sr.rls;

import edu.repetita.solvers.sr.rls.core.Topology;
import edu.repetita.solvers.sr.rls.structure.ArrayHeapInt;

public class ShortestPaths {
    public static final int InfDistance = 1000000000;

    private final Topology topology;
    private final int[] weights;

    private final int nNodes;
    private final int nEdges;

    private final int[][][] successorNodes;
    private final int[][][] successorEdges;
    private final int[][] nSuccessors;

    private final int[][][] predecessorNodes;
    private final int[][][] predecessorEdges;
    private final int[][] nPredecessors;

    private final int[][] distance;

    private final ArrayHeapInt heap;
    private final boolean[] inHeap;

    private final int[] toVisitStack;
    private final boolean[] visited;
    private final boolean[] visiting;
    private final int[] topologicalOrdering;
    private final int[] degree;

    public ShortestPaths(Topology topology, int[] weights) {
        assert topology.nEdges == weights.length;
        this.topology = topology;
        this.weights = weights;
        this.nNodes = topology.nNodes;
        this.nEdges = topology.nEdges;

        this.successorNodes = new int[nNodes][nNodes][];
        this.successorEdges = new int[nNodes][nNodes][];
        this.nSuccessors = new int[nNodes][nNodes];

        this.predecessorNodes = new int[nNodes][nNodes][];
        this.predecessorEdges = new int[nNodes][nNodes][];
        this.nPredecessors = new int[nNodes][nNodes];

        for (int dest = 0; dest < nNodes; dest++) {
            for (int node = 0; node < nNodes; node++) {
                successorNodes[dest][node] = new int[topology.outEdges(node).length];
                successorEdges[dest][node] = new int[topology.outEdges(node).length];
                predecessorNodes[dest][node] = new int[topology.inEdges(node).length];
                predecessorEdges[dest][node] = new int[topology.inEdges(node).length];
            }
        }

        this.distance = new int[nNodes][nNodes];
        this.heap = new ArrayHeapInt(nNodes);
        this.inHeap = new boolean[nNodes];

        this.toVisitStack = new int[nEdges + nNodes];
        this.visited = new boolean[nNodes];
        this.visiting = new boolean[nNodes];
        this.topologicalOrdering = new int[nNodes];
        this.degree = new int[nNodes];

        computeShortestPaths();
    }

    public int[][][] successorNodes() { return successorNodes; }
    public int[][] successorNodes(int dest) { return successorNodes[dest]; }
    public int[] successorNodes(int dest, int node) { return successorNodes[dest][node]; }

    public int[][][] successorEdges() { return successorEdges; }
    public int[][] successorEdges(int dest) { return successorEdges[dest]; }
    public int[] successorEdges(int dest, int node) { return successorEdges[dest][node]; }

    public int[][] nSuccessors() { return nSuccessors; }
    public int[] nSuccessors(int dest) { return nSuccessors[dest]; }
    public int nSuccessors(int dest, int node) { return nSuccessors[dest][node]; }

    public int[][][] predecessorNodes() { return predecessorNodes; }
    public int[][] predecessorNodes(int dest) { return predecessorNodes[dest]; }

    public int[][][] predecessorEdges() { return predecessorEdges; }
    public int[][] predecessorEdges(int dest) { return predecessorEdges[dest]; }

    public int[][] nPredecessors() { return nPredecessors; }
    public int[] nPredecessors(int dest) { return nPredecessors[dest]; }
    public int nPredecessors(int dest, int node) { return nPredecessors[dest][node]; }

    public int[] topologicalOrdering() { return topologicalOrdering; }

    public boolean canReach(int src, int dest) {
        return distance[src][dest] < InfDistance;
    }

    public void computeShortestPaths() {
        int dest = nNodes;
        while (dest > 0) {
            dest--;
            computeShortestPathsTo(dest);
        }

        dest = nNodes;
        while (dest > 0) {
            dest--;
            int node = nNodes;
            while (node > 0) {
                node--;
                nPredecessors[dest][node] = 0;
            }
        }

        dest = nNodes;
        while (dest > 0) {
            dest--;
            int nodeA = nNodes;
            while (nodeA > 0) {
                nodeA--;
                int[] succNodes = successorNodes[dest][nodeA];
                int[] succEdges = successorEdges[dest][nodeA];
                int pSucc = nSuccessors[dest][nodeA];
                while (pSucc > 0) {
                    pSucc--;
                    int nodeB = succNodes[pSucc];
                    int edge = succEdges[pSucc];
                    predecessorNodes[dest][nodeB][nPredecessors[dest][nodeB]] = nodeA;
                    predecessorEdges[dest][nodeB][nPredecessors[dest][nodeB]] = edge;
                    nPredecessors[dest][nodeB]++;
                }
            }
        }
    }

    private void computeShortestPathsTo(int dest) {
        int i = nNodes;
        while (i > 0) {
            i--;
            inHeap[i] = false;
            distance[i][dest] = InfDistance;
            nSuccessors[dest][i] = 0;
        }

        distance[dest][dest] = 0;
        heap.enqueue(0, dest);

        while (!heap.isEmpty()) {
            int node = heap.dequeue();
            inHeap[node] = false;

            int[] inEdges = topology.inEdges(node);
            int idx = inEdges.length;
            while (idx > 0) {
                idx--;
                int edge = inEdges[idx];
                int src = topology.edgeSrc[edge];

                int oldDistance = distance[node][dest];
                int edgeWeight = weights[edge];
                int newDist = (oldDistance > InfDistance - edgeWeight) ? InfDistance : oldDistance + edgeWeight;

                int srcDist = distance[src][dest];
                int comp = newDist - srcDist;
                if (comp < 0) {
                    if (inHeap[src]) {
                        heap.changeKey(distance[src][dest], newDist, src);
                    } else {
                        heap.enqueue(newDist, src);
                    }
                    distance[src][dest] = newDist;
                    successorEdges[dest][src][0] = edge;
                    successorNodes[dest][src][0] = node;
                    nSuccessors[dest][src] = 1;
                    inHeap[src] = true;
                } else if (comp == 0) {
                    int nSucc = nSuccessors[dest][src];
                    successorEdges[dest][src][nSucc] = edge;
                    successorNodes[dest][src][nSucc] = node;
                    nSuccessors[dest][src]++;
                }
            }
        }
    }

    public int makeTopologicalOrdering(int dest) {
        int nToVisit = 0;
        int node = nNodes;
        while (node > 0) {
            node--;
            degree[node] = nPredecessors[dest][node];
            if (degree[node] == 0) {
                toVisitStack[nToVisit++] = node;
            }
        }

        int nOrder = nNodes;
        while (nToVisit > 0) {
            nToVisit--;
            int currNode = toVisitStack[nToVisit];
            nOrder--;
            topologicalOrdering[nOrder] = currNode;
            int pSucc = nSuccessors[dest][currNode];
            while (pSucc > 0) {
                pSucc--;
                int succ = successorNodes[dest][currNode][pSucc];
                degree[succ]--;
                if (degree[succ] == 0) {
                    toVisitStack[nToVisit++] = succ;
                }
            }
        }

        assert nOrder == 0 : "nOrder = " + nOrder + " != nNodes = " + nNodes;
        return nNodes;
    }

    public int makeTopologicalOrdering(int source, int destination) {
        int[][] successors = successorNodes[destination];
        int[] nSuccessorNodes = nSuccessors[destination];

        toVisitStack[0] = source;
        int pStack = 1;
        int pOrdering = 0;

        while (pStack > 0) {
            pStack--;
            int node = toVisitStack[pStack];

            if (visiting[node]) {
                visiting[node] = false;
                visited[node] = true;
                topologicalOrdering[pOrdering++] = node;
            } else if (!visited[node]) {
                visiting[node] = true;
                pStack++;

                int pSucc = nSuccessorNodes[node];
                while (pSucc > 0) {
                    pSucc--;
                    int succ = successors[node][pSucc];
                    toVisitStack[pStack++] = succ;
                }
            }
        }

        int nOrdering = pOrdering;
        while (pOrdering > 0) {
            pOrdering--;
            visited[topologicalOrdering[pOrdering]] = false;
        }

        return nOrdering;
    }
}
