package edu.repetita.solvers.sr.defo.modeling;

import edu.repetita.solvers.sr.defo.core.DEFOInstance;
import edu.repetita.solvers.sr.defo.core.Topology;
import edu.repetita.solvers.sr.defo.modeling.units.LoadUnit;
import edu.repetita.solvers.sr.defo.modeling.variables.DEFOLoadVar;
import edu.repetita.solvers.sr.defo.paths.ConnectStructure;

import java.util.*;

public class MRProblem {
    public final Topology topology;
    private final Map<String, DEFONode> labelToNode = new HashMap<>();
    private final Map<String, DEFOEdge> labelToEdge = new HashMap<>();
    private final Map<String, DEFODemand> labelToDemand = new HashMap<>();

    private final List<Integer> demandSrcs = new ArrayList<>();
    private final List<Integer> demandDests = new ArrayList<>();
    private final List<Integer> demandTraffics = new ArrayList<>();
    private final List<String> demandSymbols = new ArrayList<>();

    private final Stack<DEFOConstraint> constraints = new Stack<>();
    private final ConnectStructure reachStruct;

    public MRProblem(Topology topology) {
        this.topology = topology;
        this.reachStruct = ConnectStructure.apply(topology, false);

        for (int i = 0; i < topology.nodeLabels.length; i++) {
            String label = topology.nodeLabels[i];
            if (labelToNode.containsKey(label)) throw new IllegalStateException(label + " is already used");
            labelToNode.put(label, new DEFONode(i, label));
        }

        for (int i = 0; i < topology.edgeLabels.length; i++) {
            String label = topology.edgeLabels[i];
            if (labelToEdge.containsKey(label)) throw new IllegalStateException(label + " is already used");
            labelToEdge.put(label, new DEFOEdge(i, label));
        }
    }

    public DEFONode node(String label) {
        DEFONode n = labelToNode.get(label);
        if (n == null) throw new IllegalArgumentException("No node referenced by " + label);
        return n;
    }

    public DEFOEdge edge(String label) {
        DEFOEdge e = labelToEdge.get(label);
        if (e == null) throw new IllegalArgumentException("No edge referenced by " + label);
        return e;
    }

    public DEFODemand demand(String label) {
        DEFODemand d = labelToDemand.get(label);
        if (d == null) throw new IllegalArgumentException("No demand referenced by " + label);
        return d;
    }

    public boolean existsDemand(String label) {
        return labelToDemand.containsKey(label);
    }

    public void minimize(DEFOLoadVar objective) {}

    public DEFOLoadVar maxLoad() {
        return new DEFOLoadVar(-1);
    }

    public Collection<DEFODemand> demands() { return labelToDemand.values(); }
    public Collection<DEFONode> nodes() { return labelToNode.values(); }

    public void add(DEFOConstraint constraint) {
        constraints.push(constraint);
    }

    public void newDemand(String label, DEFONode src, DEFONode dest, LoadUnit traffic) {
        newDemand(label, src.nodeId, dest.nodeId, traffic);
    }

    public void newDemand(String label, int srcId, int destId, LoadUnit traffic) {
        int t = traffic.value;
        boolean reachable = reachStruct.reachable(srcId, destId);
        if (!reachable) {
            System.err.println("Demand " + label + " dropped: no path from node " + srcId + " to node " + destId + ".");
        } else if (t <= 0) {
            System.err.println("Demand " + label + " dropped: " + traffic + " <= 0.");
        } else {
            if (labelToDemand.containsKey(label)) {
                System.err.println("The label " + label + " is already used.");
            } else {
                DEFODemand demand = new DEFODemand(demandSrcs.size(), label);
                labelToDemand.put(label, demand);
                demandSrcs.add(srcId);
                demandDests.add(destId);
                demandTraffics.add(t);
                demandSymbols.add(label);
            }
        }
    }

    public int[] assignedPath(DEFODemand demand, DEFOptimizer defoptimizer) {
        return defoptimizer.core.bestPaths()[demand.demandId];
    }

    public DEFOInstance toInstance(int[] weights, int[] capacities, int[] latencies) {
        @SuppressWarnings("unchecked")
        List<DEFOConstraint>[] demandConstraints = new List[demandSrcs.size()];
        for (int i = 0; i < demandSrcs.size(); i++) demandConstraints[i] = new ArrayList<>();
        List<DEFOConstraint> topologyConstraints = new ArrayList<>();

        for (DEFOConstraint constraint : constraints) {
            if (constraint instanceof DEFOConstraint.DEFODemandConstraint) {
                DEFOConstraint.DEFODemandConstraint c = (DEFOConstraint.DEFODemandConstraint) constraint;
                demandConstraints[c.demandId()].add(constraint);
            } else {
                topologyConstraints.add(constraint);
            }
        }

        DEFOConstraint[][] dCons = new DEFOConstraint[demandSrcs.size()][];
        for (int i = 0; i < demandSrcs.size(); i++) {
            dCons[i] = demandConstraints[i].toArray(new DEFOConstraint[0]);
        }

        return new DEFOInstance(
                topology,
                weights,
                demandTraffics.stream().mapToInt(x -> x).toArray(),
                demandSrcs.stream().mapToInt(x -> x).toArray(),
                demandDests.stream().mapToInt(x -> x).toArray(),
                dCons,
                topologyConstraints.toArray(new DEFOConstraint[0]),
                capacities,
                latencies
        );
    }
}
