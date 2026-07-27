package edu.repetita.solvers.sr.rls.io;

public class TopologyData {
    public final String[] nodeLabels;
    public final double[][] nodeCoordinates;
    public final String[] edgeLabels;
    public final int[] edgeSrcs;
    public final int[] edgeDests;
    public final int[] edgeWeights;
    public final double[] edgeCapacities;
    public final int[] edgeLatencies;

    public TopologyData(String[] nodeLabels, double[][] nodeCoordinates,
                        String[] edgeLabels, int[] edgeSrcs, int[] edgeDests,
                        int[] edgeWeights, double[] edgeCapacities, int[] edgeLatencies) {
        this.nodeLabels = nodeLabels;
        this.nodeCoordinates = nodeCoordinates;
        this.edgeLabels = edgeLabels;
        this.edgeSrcs = edgeSrcs;
        this.edgeDests = edgeDests;
        this.edgeWeights = edgeWeights;
        this.edgeCapacities = edgeCapacities;
        this.edgeLatencies = edgeLatencies;
    }

    public TopologyData(String[] nodeLabels, String[] edgeLabels,
                        int[] edgeSrcs, int[] edgeDests,
                        int[] edgeWeights, double[] edgeCapacities, int[] edgeLatencies) {
        this(nodeLabels, new double[nodeLabels.length][2], edgeLabels, edgeSrcs, edgeDests, edgeWeights, edgeCapacities, edgeLatencies);
    }

    public static TopologyData apply(String[] nodeLabels, double[][] nodeCoordinates,
                                    String[] edgeLabels, int[] edgeSrcs, int[] edgeDests,
                                    int[] edgeWeights, double[] edgeCapacities, int[] edgeLatencies) {
        return new TopologyData(nodeLabels, nodeCoordinates, edgeLabels, edgeSrcs, edgeDests, edgeWeights, edgeCapacities, edgeLatencies);
    }

    public static TopologyData apply(String[] nodeLabels, String[] edgeLabels,
                                    int[] edgeSrcs, int[] edgeDests,
                                    int[] edgeWeights, double[] edgeCapacities, int[] edgeLatencies) {
        return new TopologyData(nodeLabels, edgeLabels, edgeSrcs, edgeDests, edgeWeights, edgeCapacities, edgeLatencies);
    }
}
