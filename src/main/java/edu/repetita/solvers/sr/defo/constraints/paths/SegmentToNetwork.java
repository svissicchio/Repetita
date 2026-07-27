package edu.repetita.solvers.sr.defo.constraints.paths;

import edu.repetita.solvers.sr.defo.constraints.PathConstraint;
import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;
import edu.repetita.solvers.sr.defo.paths.ECMPStructure;
import org.maxicp.cp.engine.core.CPIntVar;

import java.util.Set;

public class SegmentToNetwork extends PathConstraint {
    public final IncrPathVar path;
    public final CPIntVar[] flows;
    public final ECMPStructure ecmpStruct;
    public final int traffic;

    public SegmentToNetwork(IncrPathVar path, CPIntVar[] flows, ECMPStructure ecmpStruct, int traffic) {
        super(path.store.getCPSolver());
        this.path = path;
        this.flows = flows;
        this.ecmpStruct = ecmpStruct;
        this.traffic = traffic;
    }

    private int intFlow(int from, int to, int link) {
        return (int) Math.ceil(ecmpStruct.flow(from, to, link) * traffic);
    }

    @Override
    public void post() {
        init();
        path.callVisitedWhenVisit(this);
    }

    @Override
    public void propagate() {}

    private void init() {
        int nVisited = path.nVisited();
        int from = path.nodeAt(0);
        for (int i = 1; i < nVisited; i++) {
            int to = path.nodeAt(i);
            visited(path, from, to);
            from = to;
        }
    }

    @Override
    public void visited(IncrPathVar path, int from, int to) {
        Set<Integer> links = ecmpStruct.links(from, to);
        for (int l : links) {
            int flow = intFlow(from, to, l);
            flows[l].fix(flow);
        }
        if (path.isBound()) {
            for (CPIntVar f : flows) {
                if (!f.isFixed()) {
                    f.fix(0);
                }
            }
        }
    }
}
