package edu.repetita.solvers.sr.defo.core;

import java.util.ArrayList;
import java.util.List;

public class Topology {
    public final String[] nodeLabels;
    public final String[] edgeLabels;
    public final int[] _edgeSrc;
    public final int[] _edgeDest;
    public final int[][] _outEdges;
    public final int[][] _inEdges;
    public final int[][] _outNodes;
    public final int[][] _inNodes;
    public final int nNodes;
    public final int nEdges;

    public Topology(String[] nodeLabels, String[] edgeLabels,
                    int[] edgeSrc, int[] edgeDest,
                    int[][] outEdges, int[][] inEdges,
                    int[][] outNodes, int[][] inNodes) {
        this.nodeLabels = nodeLabels;
        this.edgeLabels = edgeLabels;
        this._edgeSrc = edgeSrc;
        this._edgeDest = edgeDest;
        this._outEdges = outEdges;
        this._inEdges = inEdges;
        this._outNodes = outNodes;
        this._inNodes = inNodes;
        this.nNodes = nodeLabels.length;
        this.nEdges = edgeLabels.length;
    }

    public int edgeSrc(int edgeId) { return _edgeSrc[edgeId]; }
    public int edgeDest(int edgeId) { return _edgeDest[edgeId]; }
    public int[] outEdges(int nodeId) { return _outEdges[nodeId]; }
    public int[] inEdges(int nodeId) { return _inEdges[nodeId]; }
    public int[] outNodes(int nodeId) { return _outNodes[nodeId]; }
    public int[] inNodes(int nodeId) { return _inNodes[nodeId]; }

    @SuppressWarnings("unchecked")
    public static Topology apply(int[] edgeSrcs, int[] edgeDests) {
        int nEdges = edgeSrcs.length;
        int maxNode = 0;
        for (int i = 0; i < nEdges; i++) {
            if (edgeSrcs[i] > maxNode) maxNode = edgeSrcs[i];
            if (edgeDests[i] > maxNode) maxNode = edgeDests[i];
        }
        int nNodes = maxNode + 1;

        List<Integer>[] outEdges = new List[nNodes];
        List<Integer>[] inEdges = new List[nNodes];
        List<Integer>[] outNodes = new List[nNodes];
        List<Integer>[] inNodes = new List[nNodes];
        for (int i = 0; i < nNodes; i++) {
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

        String[] nodeLabels = new String[nNodes];
        for (int i = 0; i < nNodes; i++) {
            nodeLabels[i] = "N" + i;
        }

        int[][] outE = new int[nNodes][];
        int[][] inE = new int[nNodes][];
        int[][] outN = new int[nNodes][];
        int[][] inN = new int[nNodes][];
        for (int i = 0; i < nNodes; i++) {
            outE[i] = outEdges[i].stream().mapToInt(x -> x).toArray();
            inE[i] = inEdges[i].stream().mapToInt(x -> x).toArray();
            outN[i] = outNodes[i].stream().mapToInt(x -> x).toArray();
            inN[i] = inNodes[i].stream().mapToInt(x -> x).toArray();
        }

        return new Topology(nodeLabels, edgeLabels, edgeSrcs, edgeDests, outE, inE, outN, inN);
    }

    @SuppressWarnings("unchecked")
    public static Topology apply(int[] edgeSrcs, int[] edgeDests, String[] nodeLabels, String[] edgeLabels) {
        int nEdges = edgeSrcs.length;
        int nNodes = nodeLabels.length;

        List<Integer>[] outEdges = new List[nNodes];
        List<Integer>[] inEdges = new List[nNodes];
        List<Integer>[] outNodes = new List[nNodes];
        List<Integer>[] inNodes = new List[nNodes];
        for (int i = 0; i < nNodes; i++) {
            outEdges[i] = new ArrayList<>();
            inEdges[i] = new ArrayList<>();
            outNodes[i] = new ArrayList<>();
            inNodes[i] = new ArrayList<>();
        }

        for (int i = 0; i < nEdges; i++) {
            int src = edgeSrcs[i];
            int dest = edgeDests[i];
            outEdges[src].add(i);
            inEdges[dest].add(i);
            outNodes[src].add(dest);
            inNodes[dest].add(src);
        }

        int[][] outE = new int[nNodes][];
        int[][] inE = new int[nNodes][];
        int[][] outN = new int[nNodes][];
        int[][] inN = new int[nNodes][];
        for (int i = 0; i < nNodes; i++) {
            outE[i] = outEdges[i].stream().mapToInt(x -> x).toArray();
            inE[i] = inEdges[i].stream().mapToInt(x -> x).toArray();
            outN[i] = outNodes[i].stream().mapToInt(x -> x).toArray();
            inN[i] = inNodes[i].stream().mapToInt(x -> x).toArray();
        }

        return new Topology(nodeLabels, edgeLabels, edgeSrcs, edgeDests, outE, inE, outN, inN);
    }
}
