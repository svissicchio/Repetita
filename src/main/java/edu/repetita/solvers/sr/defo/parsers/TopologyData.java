package edu.repetita.solvers.sr.defo.parsers;

public class TopologyData {
    public final String[] nodeLabels;
    public final double[][] nodeCoordinates;
    public final String[] edgeLabels;
    public final int[] edgeSrcs;
    public final int[] edgeDests;
    public final int[] edgeWeights;
    public final int[] edgeCapacities;
    public final int[] edgeLatencies;

    public TopologyData(String[] nodeLabels, double[][] nodeCoordinates,
                        String[] edgeLabels, int[] edgeSrcs, int[] edgeDests,
                        int[] edgeWeights, int[] edgeCapacities, int[] edgeLatencies) {
        this.nodeLabels = nodeLabels;
        this.nodeCoordinates = nodeCoordinates;
        this.edgeLabels = edgeLabels;
        this.edgeSrcs = edgeSrcs;
        this.edgeDests = edgeDests;
        this.edgeWeights = edgeWeights;
        this.edgeCapacities = edgeCapacities;
        this.edgeLatencies = edgeLatencies;
    }
}
