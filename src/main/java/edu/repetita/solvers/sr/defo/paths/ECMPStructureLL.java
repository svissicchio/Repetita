package edu.repetita.solvers.sr.defo.paths;

import edu.repetita.solvers.sr.defo.core.Topology;
import edu.repetita.solvers.sr.defo.utils.BinaryHeap;
import edu.repetita.solvers.sr.defo.utils.Dijkstra;

import java.util.*;

public class ECMPStructureLL extends ECMPStructure {
    private final Topology topology_;
    public final int[] weights;
    private final Set<Integer>[][] paths;
    private final double[][][] flows;
    private final int[][] latencyMatrix;

    public final int nSegments_;
    private final int[][] segmentIds;
    private final int[] segmentSrcs;
    private final int[] segmentDests;
    @SuppressWarnings("unchecked")
    private final List<Integer>[] reversedPaths;

    @SuppressWarnings("unchecked")
    public ECMPStructureLL(Topology topology, int[] weights, Set<Integer>[][] paths, double[][][] flows, int[][] latencyMatrix) {
        this.topology_ = topology;
        this.weights = weights;
        this.paths = paths;
        this.flows = flows;
        this.latencyMatrix = latencyMatrix;

        this.nSegments_ = topology.nNodes * topology.nNodes - topology.nNodes;
        this.segmentIds = new int[topology.nNodes][topology.nNodes];
        this.segmentSrcs = new int[nSegments_];
        this.segmentDests = new int[nSegments_];
        this.reversedPaths = new List[topology.nEdges];
        for (int i = 0; i < topology.nEdges; i++) {
            reversedPaths[i] = new ArrayList<>();
        }

        int id = 0;
        for (int src = 0; src < topology.nNodes; src++) {
            for (int dest = 0; dest < topology.nNodes; dest++) {
                if (src != dest) {
                    segmentIds[src][dest] = id;
                    segmentSrcs[id] = src;
                    segmentDests[id] = dest;
                    for (int edge = 0; edge < topology.nEdges; edge++) {
                        if (paths[src][dest].contains(edge)) {
                            reversedPaths[edge].add(0, id);
                        }
                    }
                    id++;
                }
            }
        }
    }

    @Override public Topology topology() { return topology_; }
    @Override public int weight(int linkId) { return weights[linkId]; }
    @Override public int nSegments() { return nSegments_; }
    @Override public int segmentSrc(int segmentId) { return segmentSrcs[segmentId]; }
    @Override public int segmentDest(int segmentId) { return segmentDests[segmentId]; }
    @Override public int segmentId(int src, int dest) { return segmentIds[src][dest]; }
    @Override public int linkSrc(int linkId) { return topology_.edgeSrc(linkId); }
    @Override public int linkDest(int linkId) { return topology_.edgeDest(linkId); }
    @Override public int linkId(int src, int dest) { throw new UnsupportedOperationException(); }
    @Override public int latency(int segmentId) { return latencyMatrix[segmentSrc(segmentId)][segmentDest(segmentId)]; }
    @Override public int latency(int segmentSrc, int segmentDest) { return latencyMatrix[segmentSrc][segmentDest]; }
    @Override public List<Integer> segments(int linkId) { return reversedPaths[linkId]; }
    @Override public Set<Integer> links(int segmentId) { return paths[segmentSrcs[segmentId]][segmentDests[segmentId]]; }
    @Override public Set<Integer> links(int segmentSrc, int segmentDest) { return paths[segmentSrc][segmentDest]; }
    @Override public double flow(int segmentId, int linkId) { return flows[segmentSrcs[segmentId]][segmentDests[segmentId]][linkId]; }
    @Override public double flow(int segmentSrc, int segmentDest, int linkId) { return flows[segmentSrc][segmentDest][linkId]; }

    @SuppressWarnings("unchecked")
    public static ECMPStructureLL apply(Topology topology, int[] weights, int[] latencies) {
        Object[] pf = computePathsAndFlows(topology, weights);
        Set<Integer>[][] paths = (Set<Integer>[][]) pf[0];
        double[][][] flows = (double[][][]) pf[1];

        int[][] latencyMatrix = new int[topology.nNodes][topology.nNodes];
        for (int s = 0; s < topology.nNodes; s++) {
            for (int d = 0; d < topology.nNodes; d++) {
                latencyMatrix[s][d] = computeLatency(topology, paths[s][d], s, d, latencies);
            }
        }
        return new ECMPStructureLL(topology, weights, paths, flows, latencyMatrix);
    }

    public static int computeLatency(Topology topology, Set<Integer> edges, int source, int dest, int[] latencies) {
        Map<String, Integer> results = new HashMap<>();
        return maxLatencyMemo(source, dest, topology, edges, latencies, results);
    }

    private static int maxLatencyMemo(int s, int d, Topology topology, Set<Integer> edges, int[] latencies, Map<String, Integer> results) {
        if (s == d) return 0;
        String key = s + "," + d;
        if (results.containsKey(key)) return results.get(key);

        int[] outEdges = topology.outEdges(s);
        List<Integer> validOutEdges = new ArrayList<>();
        for (int e : outEdges) {
            if (edges.contains(e)) validOutEdges.add(e);
        }

        if (validOutEdges.isEmpty()) {
            results.put(key, 0);
            return 0;
        }

        int maxLat = 0;
        for (int e : validOutEdges) {
            int nextNode = topology.edgeDest(e);
            int lat = latencies[e] + maxLatencyMemo(nextNode, d, topology, edges, latencies, results);
            maxLat = Math.max(maxLat, lat);
        }
        results.put(key, maxLat);
        return maxLat;
    }

    @SuppressWarnings("unchecked")
    public static Object[] computePathsAndFlows(Topology topology, int[] weights) {
        Set<Integer>[][] paths = new Set[topology.nNodes][topology.nNodes];
        double[][][] flows = new double[topology.nNodes][topology.nNodes][];

        for (int destId = 0; destId < topology.nNodes; destId++) {
            Dijkstra.Result res = Dijkstra.shortestPathTo(destId, topology, weights);
            for (int srcId = 0; srcId < topology.nNodes; srcId++) {
                paths[srcId][destId] = collectEdges(topology, srcId, destId, res.prevs);
                flows[srcId][destId] = buildFlow(topology, srcId, res.prevs, res.dist, topology.nEdges);
            }
        }
        return new Object[]{paths, flows};
    }

    private static Set<Integer> collectEdges(Topology topology, int srcId, int destId, List<Integer>[] pathsTo) {
        Set<Integer> edges = new HashSet<>();
        List<Integer> toVisit = new ArrayList<>();
        toVisit.add(srcId);

        while (!toVisit.isEmpty()) {
            int nextNodeId = toVisit.remove(0);
            for (int edge : pathsTo[nextNodeId]) {
                edges.add(edge);
                toVisit.add(topology.edgeDest(edge));
            }
        }
        return edges;
    }

    private static double[] buildFlow(Topology topology, int src, List<Integer>[] pathsTo, int[] distances, int nEdges) {
        double[] flows = new double[nEdges];
        double[] toSend = new double[distances.length];
        boolean[] visited = new boolean[distances.length];
        BinaryHeap<Integer> queue = new BinaryHeap<>(distances.length);

        queue.enqueue(-distances[src], src);
        toSend[src] = 1.0;
        visited[src] = true;

        while (!queue.isEmpty()) {
            int node = queue.dequeue();
            List<Integer> nextHops = pathsTo[node];
            if (!nextHops.isEmpty()) {
                double flow = toSend[node] / nextHops.size();
                for (int nextHop : nextHops) {
                    int nextDest = topology.edgeDest(nextHop);
                    toSend[nextDest] += flow;
                    if (toSend[nextDest] > 1.0) toSend[nextDest] = 1.0;
                    flows[nextHop] = flow;
                    if (!visited[nextDest]) {
                        queue.enqueue(-distances[nextDest], nextDest);
                        visited[nextDest] = true;
                    }
                }
            }
        }
        return flows;
    }
}
