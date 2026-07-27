package edu.repetita.solvers.sr.defo.utils;

import edu.repetita.solvers.sr.defo.core.Topology;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dijkstra {
    public static class Result {
        @SuppressWarnings("unchecked")
        public final List<Integer>[] prevs;
        public final int[] dist;

        public Result(List<Integer>[] prevs, int[] dist) {
            this.prevs = prevs;
            this.dist = dist;
        }
    }

    public static Result shortestPathTo(int nodeId, Topology topology, int[] weights) {
        int[] dist = new int[topology.nNodes];
        Arrays.fill(dist, Integer.MAX_VALUE);

        @SuppressWarnings("unchecked")
        List<Integer>[] prevs = new List[topology.nNodes];
        for (int i = 0; i < topology.nNodes; i++) {
            prevs[i] = new ArrayList<>();
        }

        BinaryHeap<Integer> queue = new BinaryHeap<>(topology.nNodes);
        dist[nodeId] = 0;

        Set<Integer> reachableNodes = reachable(nodeId, topology);
        for (int node : reachableNodes) {
            queue.enqueue(dist[node], node);
        }

        while (!queue.isEmpty()) {
            int currNodeId = queue.dequeue();
            int[] inEdges = topology.inEdges(currNodeId);

            for (int edgeId : inEdges) {
                int srcId = topology.edgeSrc(edgeId);

                int newDist = (dist[currNodeId] > Integer.MAX_VALUE - weights[edgeId]) ? Integer.MAX_VALUE : dist[currNodeId] + weights[edgeId];

                if (newDist < dist[srcId]) {
                    queue.changeKey(dist[srcId], newDist, srcId);
                    dist[srcId] = newDist;
                    prevs[srcId] = new ArrayList<>();
                    prevs[srcId].add(edgeId);
                } else if (newDist == dist[srcId]) {
                    prevs[srcId].add(0, edgeId);
                }
            }
        }

        return new Result(prevs, dist);
    }

    private static Set<Integer> reachable(int v, Topology topology) {
        Set<Integer> visited = new HashSet<>();
        dfs(v, topology, visited);
        return visited;
    }

    private static void dfs(int n, Topology topology, Set<Integer> visited) {
        visited.add(n);
        int[] inEdges = topology.inEdges(n);
        for (int e : inEdges) {
            int srcId = topology.edgeSrc(e);
            if (!visited.contains(srcId)) {
                dfs(srcId, topology, visited);
            }
        }
    }
}
