package edu.repetita.solvers.sr.defo.constraints.paths;

import edu.repetita.solvers.sr.defo.constraints.PathConstraint;
import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;
import edu.repetita.solvers.sr.defo.paths.ECMPStructure;
import org.maxicp.cp.engine.core.CPIntVar;

import java.util.HashSet;
import java.util.Set;

public class DAGPath extends PathConstraint {
    public final IncrPathVar path;
    public final CPIntVar[] edges;
    public final ECMPStructure ecmpStruct;

    public DAGPath(IncrPathVar path, CPIntVar[] edges, ECMPStructure ecmpStruct) {
        super(path.store.getCPSolver());
        this.path = path;
        this.edges = edges;
        this.ecmpStruct = ecmpStruct;
    }

    @Override
    public void post() {
        init();
        path.callVisitedWhenVisit(this);
    }

    @Override
    public void propagate() {}

    private void init() {
        assert path.nVisited() == 1;
    }

    @Override
    public void visited(IncrPathVar path, int from, int to) {
        Set<Integer> links = ecmpStruct.links(from, to);
        Set<Integer> nodes = new HashSet<>();
        for (int e : links) nodes.add(ecmpStruct.topology().edgeSrc(e));
        nodes.add(to);

        for (int n : nodes) {
            if (n != from) {
                int[] inEdges = ecmpStruct.topology().inEdges(n);
                for (int e : inEdges) {
                    if (!links.contains(e)) {
                        edges[e].fix(0);
                    }
                }
            }
        }
        for (int n : nodes) {
            if (n != to) {
                int[] outEdges = ecmpStruct.topology().outEdges(n);
                for (int e : outEdges) {
                    if (!links.contains(e)) {
                        edges[e].fix(0);
                    }
                }
            }
        }
    }
}
