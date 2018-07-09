package edu.repetita.paths;

import edu.repetita.core.Topology;
import edu.repetita.utils.datastructures.ArrayHeapInt;
import javafx.util.Pair;
import org.apache.commons.collections15.map.HashedMap;

import java.util.*;

/**
 * Computes shortest paths for all destinations of the topology given in the constructor.
 * <p>
 * The weight used for edges is the Topology::edgeWeight array;
 * if a weight is {@code Topology.infiniteDistance} or greater, we consider that the edge does not exist.
 * <p>
 * The main method of this class is {@code computeShortestPaths()}.
 * Given weights on every edge, it computes the shortest paths for all destinations and stores them as follows:  
 * for every destination node {@code dest}, {@code successorNodes[dest][node][k]} is
 * a successor of node in the DAG of {@code dest}, for every k in 0 .. {@code nSuccessors[dest][node]} - 1.
 * Same for successorEdges, and predecessorNodes/Edges with nPredecessors instead.
 * <p>
 * Distance is also computed in {@code distance[][]}, if there is no path from a to b,
 * {@code distance[a][b]} will be {@code Topology::infiniteDistance}. 
 *   
 *  @author Renaud Hartert ren.hartert@gmail.com
 *  @author Steven Gay aashcrahin@gmail.com
 */
final public class ShortestPaths {
    final private int infiniteDistance;

    final Topology topology;
    final private int nNodes;
    final private int nEdges;

    // representation of the shortest path DAGs
    final public int[][][] successorNodes;
    final public int[][][] successorEdges;
    final public int[][] nSuccessors;

    final public int[][][] predecessorNodes;
    final public int[][][] predecessorEdges;
    final public int[][] nPredecessors;

    // Distance matrix and heap are used in Dijkstra's algorithm
    final public int[][] distance;
    final private ArrayHeapInt heap;
    private boolean graphIsDisconnected = false;
    private boolean graphIsDisconnectedFilled = false;

    // Some facilities to generate topological ordering of shortest path DAGs from source to destination
    final private int[] toVisitStack;
    final private boolean[] visited;
    final private boolean[] visiting;
    final private int[] degree;

    /**
     * Filled on demand by makeTopologicalOrdering methods
     */
    final public int[] topologicalOrdering;

    /**
     * Creates a ShortestPaths instance, that will use {@code topology} internally
     *
     * @param topology the topology on which shortest paths are to be computed
     */
    public ShortestPaths(Topology topology) {
        this.topology = topology;
        this.infiniteDistance = Topology.INFINITE_DISTANCE;

        nNodes = topology.nNodes; // copy for performance
        nEdges = topology.nEdges;

        // Shortest path DAGs, per destination
        // dest, node => array of successors of node in DAG of dest
        successorNodes = new int[nNodes][nNodes][];
        for (int dest = 0; dest < nNodes; dest++)
            for (int node = 0; node < nNodes; node++)
                successorNodes[dest][node] = new int[topology.outEdges[node].length];

        successorEdges = new int[nNodes][nNodes][];
        for (int dest = 0; dest < nNodes; dest++)
            for (int node = 0; node < nNodes; node++)
                successorEdges[dest][node] = new int[topology.outEdges[node].length];

        nSuccessors = new int[nNodes][nNodes];

        // dest, node => array of predecessors of node in DAG of dest
        predecessorNodes = new int[nNodes][nNodes][];
        for (int dest = 0; dest < nNodes; dest++)
            for (int node = 0; node < nNodes; node++) {
                predecessorNodes[dest][node] = new int[topology.inEdges[node].length];
                for (int index = 0; index < predecessorNodes[dest][node].length; index++) {
                    predecessorNodes[dest][node][index] = -1;
                }
            }

        predecessorEdges = new int[nNodes][nNodes][];
        for (int dest = 0; dest < nNodes; dest++)
            for (int node = 0; node < nNodes; node++)
                predecessorEdges[dest][node] = new int[topology.inEdges[node].length];

        nPredecessors = new int[nNodes][nNodes];

        distance = new int[nNodes][nNodes];

        heap = new ArrayHeapInt(nNodes);

        computeShortestPaths();

        toVisitStack = new int[nEdges + nNodes];
        visited = new boolean[nNodes];
        visiting = new boolean[nNodes];
        topologicalOrdering = new int[nNodes];
        degree = new int[nNodes];
    }


    private void fillGraphIsDisconnected() {
        graphIsDisconnectedFilled = true;
        graphIsDisconnected = false;
        for (int src = 0; src < nNodes; src++) {
            for (int dest = 0; dest < nNodes; dest++) {
                if (distance[src][dest] == infiniteDistance) graphIsDisconnected = true;
            }
        }
    }

    /**
     * Checks and returns whether graph is connected.
     *
     * @return true iff the graph was not strongly connected when computeShortestPaths() was last called
     */
    public boolean isGraphDisconnected() {
        if (!graphIsDisconnectedFilled) fillGraphIsDisconnected();
        return graphIsDisconnected;
    }

    /**
     * Computes shortest path DAGs to all destinations, puts the resulting DAGs in predecessor/successor
     */
    public void computeShortestPaths() {
        // O(V E log V), main part of the cost
        for (int dest = 0; dest < nNodes; dest++) computeShortestPathsTo(dest);

        // Use successors to compute DAGs as predecessors
        // set all nPredecessors to 0, O(V^2)
        for (int dest = 0; dest < nNodes; dest++)
            for (int node = 0; node < nNodes; node++)
                nPredecessors[dest][node] = 0;

        // browse successor structure: when b successor of a in DAG dest, add a as predecessor of b in DAG dest
        // O(V E)
        for (int dest = 0; dest < nNodes; dest++) {
            for (int nodeA = 0; nodeA < nNodes; nodeA++) {
                int[] succNodes = successorNodes[dest][nodeA];
                int[] succEdges = successorEdges[dest][nodeA];

                for (int pSucc = nSuccessors[dest][nodeA] - 1; pSucc >= 0; pSucc--) {
                    int nodeB = succNodes[pSucc];
                    int edge = succEdges[pSucc];
                    int index = nPredecessors[dest][nodeB];
                    predecessorNodes[dest][nodeB][index] = nodeB;
                    predecessorEdges[dest][nodeB][index] = edge;
                    nPredecessors[dest][nodeB]++;
                }
            }
        }

        graphIsDisconnectedFilled = false;
    }

    // compute shortest paths to one destination
    private void computeShortestPathsTo(int dest) {
        int[] weights = topology.edgeWeight;  // shortcut access

        // Reset structures, heap is already empty from last run
        for (int i = 0; i < nNodes; i++) {
            distance[i][dest] = infiniteDistance;
            nSuccessors[dest][i] = 0;
        }

        // Initialize with first event
        distance[dest][dest] = 0;
        heap.enqueue(0, dest);

        // Run Dijkstra's algorithm for single destination shortest path
        while (!heap.isEmpty()) {
            // visit next (closest) node
            int node = heap.dequeue();
            int nodeDistance = distance[node][dest];
            int[] inEdges = topology.inEdges[node];

            // for every predecessor src, shorten distance[src][dest] if possible and add in heap
            for (int i = inEdges.length - 1; i >= 0; i--) {
                int edge = inEdges[i];
                int src = topology.edgeSrc[edge];

                // distance to predecessor by going through node
                int edgeWeight = weights[edge];
                if (edgeWeight == infiniteDistance) continue; // edge is not here
                int newDist = nodeDistance + edgeWeight;

                // update structures if this edge is on a shortest path
                int comp = newDist - distance[src][dest];
                if (comp < 0) {
                    // path shorter than any other seen, update distance, the set of successor now has exactly one element
                    if (heap.inHeap(src)) heap.decreaseKey(newDist, src);
                    else heap.enqueue(newDist, src);

                    distance[src][dest] = newDist;
                    successorEdges[dest][src][0] = edge;
                    successorNodes[dest][src][0] = node;
                    nSuccessors[dest][src] = 1;
                } else if (comp == 0) {
                    // another path with the same shortest distance, just add to successors
                    int k = nSuccessors[dest][src];
                    successorEdges[dest][src][k] = edge;
                    successorNodes[dest][src][k] = node;
                    nSuccessors[dest][src]++;
                }
            }
        }
    }


    /**
     * Makes a topological ordering of the nodes in DAG of the destination.
     * The ordering is put in the topologicalOrdering field, with dest as first entry of topologicalOrdering.
     * Returns the number of nodes in the ordering, which is always nNodes in this case.
     *
     * @param dest the destination to which compute shortest paths
     * @return the number of nodes explored, will always be nNodes
     */
    // Uses a predecessor-counting technique: when a node has 0 remaining predecessors,
    // put it at the front of ordering, then decrease the counts of its successors.
    // Doing so on a DAG visits all nodes and yields a topological ordering.
    public int makeTopologicalOrdering(int dest) {
        int nToVisit = 0;

        // copy node degrees in DAG of dest; remember which nodes have degree 0
        for (int node = 0; node < nNodes; node++) {
            degree[node] = nPredecessors[dest][node];
            if (degree[node] == 0) toVisitStack[nToVisit++] = node;
        }

        // visit node and remove edges from node to neighbors; if it was the last incoming edge of a node, add node to visit stack
        int nOrder = nNodes;
        while (nToVisit > 0) {
            // visit top of stack
            int node = toVisitStack[--nToVisit];
            topologicalOrdering[--nOrder] = node;

            // remove edges
            for (int pSucc = nSuccessors[dest][node] - 1; pSucc >= 0; pSucc--) {
                int succ = successorNodes[dest][node][pSucc];
                degree[succ]--;
                if (degree[succ] == 0) toVisitStack[nToVisit++] = succ;
            }
        }

        assert nOrder == 0 : "nOrder should be 0, or graph is disconnected";
        return nNodes;
    }


    /**
     * Makes a topological ordering of the nodes in all shortest paths from source to destination.
     * The resulting ordering is in the topologicalOrdering field, with source as its first entry.
     * Returns the number of nodes k in the sub-DAG, thus topologicalOrdering's (k-1)-th entry is destination.
     *
     * @param source      the root of the sub-DAG
     * @param destination the only leaf of the sub-DAG
     * @return the number of nodes in the sub-DAG
     */
    // Uses a DFS-based algorithm
    public int makeTopologicalOrdering(int source, int destination) {
        int[][] successors = successorNodes[destination];
        int[] nSuccessorNodes = nSuccessors[destination];

        toVisitStack[0] = source;
        int pStack = 1;
        int pOrdering = 0;

        while (pStack > 0) {
            int node = toVisitStack[--pStack];

            if (visiting[node]) {
                visiting[node] = false;
                visited[node] = true;
                topologicalOrdering[pOrdering++] = node;
            } else if (!visited[node]) {
                visiting[node] = true;

                pStack++;  // push node back  // toVisitStack[pStack++] = node;

                for (int pSucc = nSuccessorNodes[node] - 1; pSucc >= 0; pSucc--) {
                    int succ = successors[node][pSucc]; // push
                    toVisitStack[pStack++] = succ;
                }
            }
        }

        // clear visited
        int nOrdering = pOrdering;
        while (pOrdering > 0) visited[topologicalOrdering[--pOrdering]] = false;

        return nOrdering;
    }

    public String getNextHops() {
        StringBuilder sb = new StringBuilder();

        for (int dest = 0; dest < successorNodes.length; dest++) {
            sb.append("\nDestination " + topology.nodeLabel[dest]);
            for (int src = 0; src < successorNodes[dest].length; src++) {
                Set<String> nextHopSet = new HashSet();
                for (int index = 0; index < nSuccessors[dest][src]; index++) {
                    nextHopSet.add(topology.nodeLabel[successorNodes[dest][src][index]]);
                }

                sb.append("\nnode: " + topology.nodeLabel[src] + ", next hops: " + nextHopSet.toString());
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}

