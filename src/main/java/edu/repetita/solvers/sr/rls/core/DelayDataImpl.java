package edu.repetita.solvers.sr.rls.core;

import edu.repetita.solvers.sr.rls.ShortestPaths;
import edu.repetita.solvers.sr.rls.io.TopologyData;

/**
 * For every (source, destination), computes the longest path in the DAG of shortest paths from source to destination with the latencies as weights.
 */
public class DelayDataImpl implements DelayData {
    private final int nNodes;
    private final int[][] delay_;
    private final int[] ordering;
    private final int[] degrees;

    public DelayDataImpl(int nNodes, ShortestPaths sp, TopologyData topologyData) {
        this.nNodes = nNodes;
        this.delay_ = new int[nNodes][nNodes];
        this.ordering = new int[nNodes];
        this.degrees = new int[nNodes];
        initialize(sp, topologyData);
    }

    @Override
    public int delay(int source, int destination) {
        return delay_[source][destination];
    }

    private void initialize(ShortestPaths sp, TopologyData topologyData) {
        int destination = nNodes;
        while (destination > 0) {
            destination--;
            int pOrdering = 0;

            // Copy degrees to temporary structure, while adding 0-degree nodes to ordering
            int node = nNodes;
            while (node > 0) {
                node--;
                degrees[node] = sp.nPredecessors(destination, node);
                if (degrees[node] == 0) {
                    ordering[pOrdering++] = node;
                }
            }

            // Visit nodes in ordering from first inserted to last, decreasing degree of all successors by 1
            int pNode = 0;
            while (pNode < pOrdering) {
                int currNode = ordering[pNode];
                int[] successors = sp.successorNodes(destination, currNode);
                int pSucc = sp.nSuccessors(destination, currNode);
                while (pSucc > 0) {
                    pSucc--;
                    int eNode = successors[pSucc];
                    degrees[eNode]--;
                    if (degrees[eNode] == 0) {
                        ordering[pOrdering++] = eNode;
                    }
                }
                pNode++;
            }

            assert (ordering[pNode - 1] == destination);

            delay_[destination][destination] = 0; // skip destination
            pNode--;

            // Visit nodes in reverse order to compute max paths
            while (pNode > 0) {
                pNode--;
                int currNode = ordering[pNode];
                int m = Integer.MIN_VALUE;

                int[] successorNodes = sp.successorNodes(destination, currNode);
                int[] successorEdges = sp.successorEdges(destination, currNode);
                int pSucc = sp.nSuccessors(destination, currNode);
                while (pSucc > 0) {
                    pSucc--;
                    int succNode = successorNodes[pSucc];
                    int edge = successorEdges[pSucc];

                    m = Math.max(m, topologyData.edgeLatencies[edge] + delay_[succNode][destination]);
                }
                delay_[currNode][destination] = m;
            }
        }
    }
}
