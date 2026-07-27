package edu.repetita.solvers.sr.defo.paths;

import edu.repetita.solvers.sr.defo.core.Topology;
import java.util.List;
import java.util.Set;

public abstract class ECMPStructure {
    public abstract Topology topology();
    public abstract int weight(int linkId);
    public abstract int nSegments();
    public abstract int segmentSrc(int segmentId);
    public abstract int segmentDest(int segmentId);
    public abstract int segmentId(int src, int dest);
    public abstract int linkSrc(int linkId);
    public abstract int linkDest(int linkId);
    public abstract int linkId(int src, int dest);
    public abstract int latency(int segmentId);
    public abstract int latency(int segmentSrc, int segmentDest);
    public abstract List<Integer> segments(int linkId);
    public abstract Set<Integer> links(int segmentId);
    public abstract Set<Integer> links(int segmentSrc, int segmentDest);
    public abstract double flow(int segmentId, int linkId);
    public abstract double flow(int segmentSrc, int segmentDest, int linkId);

    public static ECMPStructure apply(Topology topology, int[] weights, int[] latencies) {
        return ECMPStructureLL.apply(topology, weights, latencies);
    }
}
