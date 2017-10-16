package edu.repetita.core;

import edu.repetita.io.RepetitaWriter;

import java.util.*;

/**
 * Represents a network as a digraph, with a weight, latency and capacity for each edge.
 * <p>
 * This class is meant to be used as a read-write object.
 *
 * @author Steven Gay, Stefano Vissicchio
 */
public class Topology {
    /**
     * in relation with the edgeWeight field, if a weight is equal or greater than this, then routing must consider it absent
     */
    final static public int INFINITE_DISTANCE = 1000000000;
    /**
     * number of nodes of the topology
     */
    final public int nNodes;
    /**
     * number of edges of the topology
     */
    final public int nEdges;
    /**
     * name of an edge
     */
    public String[] edgeLabel;
    /**
     * source node of an edge
     */
    public int[] edgeSrc;
    /**
     * destination node of an edge
     */
    public int[] edgeDest;
    /**
     * weight of an edge, used in routing protocols
     */
    public int[] edgeWeight;
    /**
     * latency of an edge, typically in microseconds
     */
    public int[] edgeLatency;
    /**
     * capacity of an edge, typically in Kbps
     */
    public double[] edgeCapacity;
    /**
     * name of a node
     */
    public String[] nodeLabel;
    /**
     * incoming edges of a node
     */
    public int[][] inEdges;
    /**
     * outgoing edges of a node
     */
    public int[][] outEdges;

    /**
     * Takes the characteristics of a topology to regroup it in one place.
     * <p>
     * This constructor does no copy, instances use arrays passed at construction time.
     * Some verification is made at construction, but if the user modifies any array, it is at their own risk.
     *
     * @param nodeLabel    node names
     * @param edgeLabel    edge names
     * @param edgeSrc      edge source
     * @param edgeDest     edge destination
     * @param edgeWeight   edge weight
     * @param edgeCapacity edge capacity
     * @param edgeLatency  edge latency
     */
    public Topology(String[] nodeLabel, String[] edgeLabel, int[] edgeSrc, int[] edgeDest, int[] edgeWeight, double[] edgeCapacity, int[] edgeLatency) {
        this.nNodes = nodeLabel.length;
        this.nEdges = edgeLabel.length;

        // check input is valid, not perfect since the user can change it in our back
        assert nEdges == edgeSrc.length;
        assert nEdges == edgeDest.length;
        assert nEdges == edgeWeight.length;
        assert nEdges == edgeCapacity.length;
        assert nEdges == edgeLatency.length;

        for (int node : edgeSrc) assert 0 <= node && node < nEdges;
        for (int node : edgeDest) assert 0 <= node && node < nEdges;
        for (int weight : edgeWeight) assert 0 < weight;
        for (double capacity : edgeCapacity) assert 0 <= capacity;
        for (int latency : edgeLatency) assert 0 <= latency;

        // take arrays without making a copy
        this.nodeLabel = nodeLabel;
        this.edgeLabel = edgeLabel;

        this.edgeSrc = edgeSrc;
        this.edgeDest = edgeDest;
        for(int w: edgeWeight){
            if (w < 1){
                RepetitaWriter.appendToOutput("ERROR: Asked to set a negative weight (" + w + "), " +
                        "but all weights must be positive integers!",0);
                System.exit(1);
            }
        }
        this.edgeWeight = edgeWeight;
        this.edgeCapacity = edgeCapacity;
        this.edgeLatency = edgeLatency;

        // compute inEdges and outEdges, those are node -> edge adjacency lists, graphs are supposed to be sparse, so no int[][] involved yet
        ArrayList<ArrayList<Integer>> incoming = new ArrayList<ArrayList<Integer>>(nNodes);
        ArrayList<ArrayList<Integer>> outgoing = new ArrayList<ArrayList<Integer>>(nNodes);

        for (int node = 0; node < nNodes; node++) {
            incoming.add(new ArrayList<Integer>());
            outgoing.add(new ArrayList<Integer>());
        }

        for (int edge = 0; edge < nEdges; edge++) {
            outgoing.get(edgeSrc[edge]).add(edge);
            incoming.get(edgeDest[edge]).add(edge);
        }

        // transcribe to int[][] for speed
        inEdges = new int[nNodes][];
        outEdges = new int[nNodes][];

        for (int node = 0; node < nNodes; node++) {
            ArrayList<Integer> nodeIncoming = incoming.get(node);
            int incomingSize = nodeIncoming.size();
            inEdges[node] = new int[incomingSize];
            for (int pEdge = 0; pEdge < incomingSize; pEdge++) inEdges[node][pEdge] = nodeIncoming.get(pEdge);

            ArrayList<Integer> nodeOutgoing = outgoing.get(node);
            int outgoingSize = nodeOutgoing.size();
            outEdges[node] = new int[outgoingSize];
            for (int pEdge = 0; pEdge < outgoingSize; pEdge++) outEdges[node][pEdge] = nodeOutgoing.get(pEdge);
        }
    }

    public Topology clone() {
        return new Topology(this.nodeLabel.clone(), this.edgeLabel.clone(), this.edgeSrc.clone(),
                this.edgeDest.clone(), this.edgeWeight.clone(), this.edgeCapacity.clone(),
                this.edgeLatency.clone());
    }

    public void setWeight(String edgeId, int weight) {
        int edgeIndex = Arrays.asList(edgeLabel).indexOf(edgeId);
        if (weight < 1){
            RepetitaWriter.appendToOutput("ERROR: Given weight " + weight + " for link " + edgeId +
                    ", but all weights must be positive integers!",0);
            System.exit(1);
        }
        edgeWeight[edgeIndex] = weight;
    }

    public int getEdgeId(String edgeLabel){
        return Arrays.asList(this.edgeLabel).indexOf(edgeLabel);
    }

    public Topology removeUndirectedEdge(int edgeId){
        HashMap<Integer,Integer> mapEdges = new HashMap<>();
        mapEdges.put(edgeId,1);

        Integer symEdgeId = this.findSymmetricEdge(edgeId);
        if (symEdgeId != null){
            mapEdges.put(symEdgeId,1);
        }
        return this.removeEdges(mapEdges);
    }

    public Integer findSymmetricEdge(int edgeId) {
        int originalSrc = this.edgeSrc[edgeId];
        int originalDst = this.edgeDest[edgeId];
        for(int e=0; e < this.edgeLabel.length; e++){
            if(edgeSrc[e] == originalDst && edgeDest[e] == originalSrc){
                return e;
            }
        }
        return null;
    }

    public Topology removeDirectedEdge(int edgeId){
        HashMap<Integer,Integer> mapEdges = new HashMap<>();
        mapEdges.put(edgeId,1);
        return this.removeEdges(mapEdges);
    }

    // input is a map: id of edges to be removed -> any integer value
    public Topology removeEdges(HashMap<Integer,Integer> edgeIds) {
        // nodes remain the same
        String[] newNodeLabel = new String[this.nNodes];
        System.arraycopy(this.nodeLabel,0,newNodeLabel,0,this.nNodes);

        // initialize edge related arrays
        int newNumEdges = this.nEdges - edgeIds.size();
        String[] newEdgeLabel = new String[newNumEdges];
        int[] newEdgeSrc = new int[newNumEdges];
        int[] newEdgeDest = new int[newNumEdges];
        int[] newEdgeWeight = new int[newNumEdges];
        double[] newEdgeCapacity = new double[newNumEdges];
        int[] newEdgeLatency = new int[newNumEdges];

        // fill new edge arrays only for edges not to be removed
        int newIndex = 0;
        for(int e=0; e < this.edgeLabel.length; e++){
            if(! edgeIds.containsKey(e)){
                newEdgeLabel[newIndex] = this.edgeLabel[e];
                newEdgeSrc[newIndex] = this.edgeSrc[e];
                newEdgeDest[newIndex] = this.edgeDest[e];
                newEdgeWeight[newIndex] = this.edgeWeight[e];
                newEdgeCapacity[newIndex] = this.edgeCapacity[e];
                newEdgeLatency[newIndex] = this.edgeLatency[e];
                newIndex++;
            }
        }

        return new Topology(newNodeLabel, newEdgeLabel, newEdgeSrc, newEdgeDest, newEdgeWeight, newEdgeCapacity, newEdgeLatency);
    }

    public int getEdgeSource(String edgeLabel){
        int index = Arrays.asList(this.edgeLabel).indexOf(edgeLabel);
        return this.edgeSrc[index];
    }

    public int getEdgeDest(String edgeLabel){
        int index = Arrays.asList(this.edgeLabel).indexOf(edgeLabel);
        return this.edgeDest[index];
    }

    public String getEdgeLabel(int edgeId){
        return edgeLabel[edgeId];
    }
}
