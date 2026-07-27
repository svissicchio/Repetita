package edu.repetita.solvers.sr.defo.constraints.paths;

import edu.repetita.solvers.sr.defo.constraints.PathConstraint;
import edu.repetita.solvers.sr.defo.core.variables.IncrPathVar;
import edu.repetita.solvers.sr.defo.paths.ECMPStructure;
import org.maxicp.cp.engine.core.CPIntVar;

import java.util.Set;

public class NetworkToSegment extends PathConstraint {
    public final IncrPathVar path;
    public final CPIntVar[] flows;
    public final ECMPStructure ecmpStruct;
    public final int traffic;

    public NetworkToSegment(IncrPathVar path, CPIntVar[] flows, ECMPStructure ecmpStruct, int traffic) {
        super(path.store.getCPSolver());
        this.path = path;
        this.flows = flows;
        this.ecmpStruct = ecmpStruct;
        this.traffic = traffic;
    }

    @Override
    public void post() {
        propagate();
        path.callVisitedWhenVisit(this);
        for (CPIntVar f : flows) {
            if (!f.isFixed()) {
                f.propagateOnBoundChange(this);
            }
        }
    }

    @Override
    public void propagate() {
        int from = path.lastVisited();
        int[] possible = path.possible();
        for (int to : possible) {
            Set<Integer> links = ecmpStruct.links(from, to);
            for (int link : links) {
                int flow = (int) Math.ceil(ecmpStruct.flow(from, to, link) * traffic);
                if (flow > flows[link].max()) {
                    path.remove(to);
                }
            }
        }
    }

    @Override
    public void visited(IncrPathVar path, int from, int to) {
        propagate();
    }
}
