package edu.repetita.solvers.sr.defo.utils;

import java.util.ArrayList;
import java.util.List;

public class DGraph {
    public static class Node {
        public final int id;
        public final String label;

        public Node(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static class Edge {
        public final int id;
        public final Node src;
        public final Node dest;

        public Edge(int id, Node src, Node dest) {
            this.id = id;
            this.src = src;
            this.dest = dest;
        }

        @Override
        public String toString() {
            return src + " -> " + dest;
        }
    }

    public final int nNodes;
    public final int nEdges;
    private final Node[] _nodes;
    private final Edge[] _edges;

    private final int[][] _outEdges;
    private final int[][] _inEdges;
    private final int[][] _adjEdges;

    private final int[][] _outNodes;
    private final int[][] _inNodes;
    private final int[][] _adjNodes;

    public DGraph(List<String> nodeNames, List<int[]> capacitedEdges) {
        this.nNodes = nodeNames.size();
        this.nEdges = capacitedEdges.size();

        this._nodes = new Node[nNodes];
        for (int i = 0; i < nNodes; i++) {
            _nodes[i] = new Node(i, nodeNames.get(i));
        }

        this._edges = new Edge[nEdges];
        for (int i = 0; i < nEdges; i++) {
            int[] edge = capacitedEdges.get(i);
            _edges[i] = new Edge(i, _nodes[edge[0]], _nodes[edge[1]]);
        }

        List<Integer>[] outE = new List[nNodes];
        List<Integer>[] inE = new List[nNodes];
        for (int i = 0; i < nNodes; i++) {
            outE[i] = new ArrayList<>();
            inE[i] = new ArrayList<>();
        }

        for (int e = 0; e < nEdges; e++) {
            outE[_edges[e].src.id].add(e);
            inE[_edges[e].dest.id].add(e);
        }

        this._outEdges = new int[nNodes][];
        this._inEdges = new int[nNodes][];
        this._adjEdges = new int[nNodes][];
        this._outNodes = new int[nNodes][];
        this._inNodes = new int[nNodes][];
        this._adjNodes = new int[nNodes][];

        for (int n = 0; n < nNodes; n++) {
            _outEdges[n] = outE[n].stream().mapToInt(x -> x).toArray();
            _inEdges[n] = inE[n].stream().mapToInt(x -> x).toArray();

            int[] adjE = new int[_outEdges[n].length + _inEdges[n].length];
            System.arraycopy(_inEdges[n], 0, adjE, 0, _inEdges[n].length);
            System.arraycopy(_outEdges[n], 0, adjE, _inEdges[n].length, _outEdges[n].length);
            _adjEdges[n] = adjE;

            _outNodes[n] = new int[_outEdges[n].length];
            for (int i = 0; i < _outEdges[n].length; i++) {
                _outNodes[n][i] = _edges[_outEdges[n][i]].dest.id;
            }

            _inNodes[n] = new int[_inEdges[n].length];
            for (int i = 0; i < _inEdges[n].length; i++) {
                _inNodes[n][i] = _edges[_inEdges[n][i]].src.id;
            }

            int[] adjN = new int[_inNodes[n].length + _outNodes[n].length];
            System.arraycopy(_inNodes[n], 0, adjN, 0, _inNodes[n].length);
            System.arraycopy(_outNodes[n], 0, adjN, _inNodes[n].length, _outNodes[n].length);
            _adjNodes[n] = adjN;
        }
    }

    public Edge[] edges() { return _edges; }
    public Node[] nodes() { return _nodes; }

    public Edge edge(int edgeId) { return _edges[edgeId]; }
    public Node node(int nodeId) { return _nodes[nodeId]; }

    public Edge[] outEdges(Node node) { return outEdges(node.id); }
    public Edge[] inEdges(Node node) { return inEdges(node.id); }
    public Edge[] adjEdges(Node node) { return adjEdges(node.id); }

    public Node[] outNodes(Node node) { return outNodes(node.id); }
    public Node[] inNodes(Node node) { return inNodes(node.id); }
    public Node[] adjNodes(Node node) { return adjNodes(node.id); }

    public Edge[] outEdges(int nodeId) {
        Edge[] res = new Edge[_outEdges[nodeId].length];
        for (int i = 0; i < res.length; i++) res[i] = _edges[_outEdges[nodeId][i]];
        return res;
    }

    public Edge[] inEdges(int nodeId) {
        Edge[] res = new Edge[_inEdges[nodeId].length];
        for (int i = 0; i < res.length; i++) res[i] = _edges[_inEdges[nodeId][i]];
        return res;
    }

    public Edge[] adjEdges(int nodeId) {
        Edge[] res = new Edge[_adjEdges[nodeId].length];
        for (int i = 0; i < res.length; i++) res[i] = _edges[_adjEdges[nodeId][i]];
        return res;
    }

    public Node[] outNodes(int nodeId) {
        Node[] res = new Node[_outNodes[nodeId].length];
        for (int i = 0; i < res.length; i++) res[i] = _nodes[_outNodes[nodeId][i]];
        return res;
    }

    public Node[] inNodes(int nodeId) {
        Node[] res = new Node[_inNodes[nodeId].length];
        for (int i = 0; i < res.length; i++) res[i] = _nodes[_inNodes[nodeId][i]];
        return res;
    }

    public Node[] adjNodes(int nodeId) {
        Node[] res = new Node[_adjNodes[nodeId].length];
        for (int i = 0; i < res.length; i++) res[i] = _nodes[_adjNodes[nodeId][i]];
        return res;
    }

    public int[] outEdgesId(Node node) { return _outEdges[node.id]; }
    public int[] inEdgesId(Node node) { return _inEdges[node.id]; }
    public int[] adjEdgesId(Node node) { return _adjEdges[node.id]; }

    public int[] outNodesId(Node node) { return _outNodes[node.id]; }
    public int[] inNodesId(Node node) { return _inNodes[node.id]; }
    public int[] adjNodesId(Node node) { return _adjNodes[node.id]; }

    public int[] outEdgesId(int nodeId) { return _outEdges[nodeId]; }
    public int[] inEdgesId(int nodeId) { return _inEdges[nodeId]; }
    public int[] adjEdgesId(int nodeId) { return _adjEdges[nodeId]; }

    public int[] outNodesId(int nodeId) { return _outNodes[nodeId]; }
    public int[] inNodesId(int nodeId) { return _inNodes[nodeId]; }
    public int[] adjNodesId(int nodeId) { return _adjNodes[nodeId]; }
}
