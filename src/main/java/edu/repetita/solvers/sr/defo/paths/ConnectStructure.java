package edu.repetita.solvers.sr.defo.paths;

import edu.repetita.solvers.sr.defo.core.Topology;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class ConnectStructure {
    public final Topology graph;
    private int nStrong = 0;
    @SuppressWarnings("unchecked")
    private final Set<Integer>[] reach;

    @SuppressWarnings("unchecked")
    public ConnectStructure(Topology graph) {
        this.graph = graph;
        this.reach = new Set[graph.nNodes];

        for (int n = 0; n < graph.nNodes; n++) {
            Set<Integer> reachable = new HashSet<>();
            Deque<Integer> buffer = new ArrayDeque<>();
            reachable.add(n);
            buffer.add(n);

            while (!buffer.isEmpty()) {
                int curr = buffer.poll();
                int[] succ = graph.outNodes(curr);
                for (int s : succ) {
                    if (!reachable.contains(s)) {
                        buffer.add(s);
                        reachable.add(s);
                    }
                }
            }

            if (reachable.size() == graph.nNodes) nStrong++;
            reach[n] = reachable;
        }
    }

    public static ConnectStructure apply(Topology graph, boolean strongCheck) {
        return new ConnectStructure(graph);
    }

    public static ConnectStructure apply(Topology graph) {
        return new ConnectStructure(graph);
    }

    public boolean isStronglyConnected() {
        return nStrong == graph.nNodes;
    }

    public boolean reachable(int fromId, int toId) {
        return reach[fromId].contains(toId);
    }
}
