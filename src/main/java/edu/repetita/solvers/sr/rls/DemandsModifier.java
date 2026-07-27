package edu.repetita.solvers.sr.rls;

import edu.repetita.solvers.sr.rls.io.DemandsData;
import edu.repetita.solvers.sr.rls.state.FlowStateChecker;
import edu.repetita.solvers.sr.rls.state.FlowStateRecomputeDAGOnCommit;
import edu.repetita.solvers.sr.rls.state.PathState;

public class DemandsModifier {
    private final PathState pathState;
    private final FlowStateChecker flowState;
    private final FlowStateRecomputeDAGOnCommit flowState2;
    private final DemandsData demandsData;

    public DemandsModifier(PathState pathState, FlowStateChecker flowState, FlowStateRecomputeDAGOnCommit flowState2, DemandsData demandsData) {
        this.pathState = pathState;
        this.flowState = flowState;
        this.flowState2 = flowState2;
        this.demandsData = demandsData;
    }

    public void add(int demand, double diffTraffic) {
        demandsData.demandTraffics[demand] += diffTraffic;

        int[] path = pathState.path(demand);
        int pDetour = pathState.size(demand) - 1;

        while (pDetour > 0) {
            pDetour--;
            int source = path[pDetour];
            int destination = path[pDetour + 1];
            flowState.modify(source, destination, diffTraffic);
            flowState2.modify(demand, source, destination, diffTraffic);
        }

        flowState.update();
        flowState.commit();

        flowState2.update();
        flowState2.commit();
    }
}
