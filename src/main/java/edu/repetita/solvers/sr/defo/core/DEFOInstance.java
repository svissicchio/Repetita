package edu.repetita.solvers.sr.defo.core;

import edu.repetita.solvers.sr.defo.modeling.DEFOConstraint;
import edu.repetita.solvers.sr.defo.parsers.DemandsData;
import edu.repetita.solvers.sr.defo.parsers.TopologyData;

public class DEFOInstance {
    public final Topology topology;
    public final int[] weights;
    public final int[] demandTraffics;
    public final int[] demandSrcs;
    public final int[] demandDests;
    public final DEFOConstraint[][] demandConstraints;
    public final DEFOConstraint[] topologyConstraints;
    public final int[] capacities;
    public final int[] latencies;

    public DEFOInstance(Topology topology, int[] weights, int[] demandTraffics,
                        int[] demandSrcs, int[] demandDests,
                        DEFOConstraint[][] demandConstraints,
                        DEFOConstraint[] topologyConstraints,
                        int[] capacities, int[] latencies) {
        this.topology = topology;
        this.weights = weights;
        this.demandTraffics = demandTraffics;
        this.demandSrcs = demandSrcs;
        this.demandDests = demandDests;
        this.demandConstraints = demandConstraints;
        this.topologyConstraints = topologyConstraints;
        this.capacities = capacities;
        this.latencies = latencies;
    }

    public static DEFOInstance apply(TopologyData topologyData, DemandsData demandsData) {
        Topology topology = Topology.apply(topologyData.edgeSrcs, topologyData.edgeDests);
        DEFOConstraint[][] demandConstraints = new DEFOConstraint[demandsData.demandTraffics.length][0];
        DEFOConstraint[] topologyConstraints = new DEFOConstraint[0];
        return new DEFOInstance(
                topology,
                topologyData.edgeWeights,
                demandsData.demandTraffics,
                demandsData.demandSrcs,
                demandsData.demandDests,
                demandConstraints,
                topologyConstraints,
                topologyData.edgeCapacities,
                topologyData.edgeLatencies
        );
    }
}
