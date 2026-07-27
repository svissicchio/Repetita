package edu.repetita.solvers.sr.rls.core;

import edu.repetita.solvers.sr.rls.io.TopologyData;
import java.util.ArrayList;
import java.util.List;

public class Topology {
    public final String[] nodeLabels;
    public final String[] edgeLabels;
    public final int[] edgeSrc;
    public final int[] edgeDest;
    private final int[] _edgeSrc;
    private final int[] _edgeDest;
    private final int[][] _outEdges;
    private final int[][] _inEdges;
    private final int[][] _outNodes;
    private final int[][] _inNodes;
    public final int nNodes;
    public final int nEdges;

    public Topology(String[] nodeLabels, String[] edgeLabels,
                    int[] edgeSrc, int[] edgeDest,
                    int[][] outEdges, int[][] inEdges,
                    int[][] outNodes, int[][] inNodes) {
        this.nodeLabels = nodeLabels;
        this.edgeLabels = edgeLabels;
        this.edgeSrc = edgeSrc;
        this.edgeDest = edgeDest;
        this._edgeSrc = edgeSrc;
        this._edgeDest = edgeDest;
        this._outEdges = outEdges;
        this._inEdges = inEdges;
        this._outNodes = outNodes;
        this._inNodes = inNodes;
        this.nNodes = nodeLabels.length;
        this.nEdges = edgeLabels.length;
    }

    public static Topology apply(TopologyData data) {
        return create(data);
    }

    public final int edgeSrc(int edgeId) {
        return _edgeSrc[edgeId];
    }

    public final int edgeDest(int edgeId) {
        return _edgeDest[edgeId];
    }

    public final int[] outEdges(int nodeId) {
        return _outEdges[nodeId];
    }

    public final int[] inEdges(int nodeId) {
        return _inEdges[nodeId];
    }

    public final int[] outNodes(int nodeId) {
        return _outNodes[nodeId];
    }

    public final int[] inNodes(int nodeId) {
        return _inNodes[nodeId];
    }

    @SuppressWarnings("unchecked")
    public static Topology create(int[] edgeSrcs, int[] edgeDests) {
        int nEdges = edgeSrcs.length;
        int maxNode = 0;
        for (int i = 0; i < nEdges; i++) {
            if (edgeSrcs[i] > maxNode) maxNode = edgeSrcs[i];
            if (edgeDests[i] > maxNode) maxNode = edgeDests[i];
        }
        int numNodes = maxNode + 1;

        List<Integer>[] outEdges = new List[numNodes];
        List<Integer>[] inEdges = new List[numNodes];
        List<Integer>[] outNodes = new List[numNodes];
        List<Integer>[] inNodes = new List[numNodes];
        for (int i = 0; i < numNodes; i++) {
            outEdges[i] = new ArrayList<>();
            inEdges[i] = new ArrayList<>();
            outNodes[i] = new ArrayList<>();
            inNodes[i] = new ArrayList<>();
        }

        String[] edgeLabels = new String[nEdges];
        for (int i = 0; i < nEdges; i++) {
            int src = edgeSrcs[i];
            int dest = edgeDests[i];
            outEdges[src].add(i);
            inEdges[dest].add(i);
            outNodes[src].add(dest);
            inNodes[dest].add(src);
            edgeLabels[i] = "Link_N" + src + "_N" + dest;
        }

        String[] nodeLabels = new String[numNodes];
        for (int i = 0; i < numNodes; i++) {
            nodeLabels[i] = "N" + i;
        }

        return create(edgeSrcs, edgeDests, nodeLabels, edgeLabels);
    }

    @SuppressWarnings("unchecked")
    public static Topology create(int[] edgeSrcs, int[] edgeDests, String[] nodeLabels, String[] edgeLabels) {
        int nEdges = edgeSrcs.length;
        int numNodes = nodeLabels.length;

        List<Integer>[] outEdgesList = new List[numNodes];
        List<Integer>[] inEdgesList = new List[numNodes];
        List<Integer>[] outNodesList = new List[numNodes];
        List<Integer>[] inNodesList = new List[numNodes];
        for (int i = 0; i < numNodes; i++) {
            outEdgesList[i] = new ArrayList<>();
            inEdgesList[i] = new ArrayList<>();
            outNodesList[i] = new ArrayList<>();
            inNodesList[i] = new ArrayList<>();
        }

        for (int i = 0; i < nEdges; i++) {
            int src = edgeSrcs[i];
            int dest = edgeDests[i];
            outEdgesList[src].add(i);
            inEdgesList[dest].add(i);
            outNodesList[src].add(dest);
            inNodesList[dest].add(src);
        }

        int[][] outEdges = new int[numNodes][];
        int[][] inEdges = new int[numNodes][];
        int[][] outNodes = new int[numNodes][];
        int[][] inNodes = new int[numNodes][];
        for (int i = 0; i < numNodes; i++) {
            outEdges[i] = outEdgesList[i].stream().mapToInt(x -> x).toArray();
            inEdges[i] = inEdgesList[i].stream().mapToInt(x -> x).toArray();
            outNodes[i] = outNodesList[i].stream().mapToInt(x -> x).toArray();
            inNodes[i] = inNodesList[i].stream().mapToInt(x -> x).toArray();
        }

        return new Topology(nodeLabels, edgeLabels, edgeSrcs, edgeDests, outEdges, inEdges, outNodes, inNodes);
    }

    public static Topology create(TopologyData data) {
        return create(data.edgeSrcs, data.edgeDests, data.nodeLabels, data.edgeLabels);
    }
}
