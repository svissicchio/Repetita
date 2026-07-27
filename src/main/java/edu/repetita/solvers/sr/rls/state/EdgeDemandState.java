package edu.repetita.solvers.sr.rls.state;

import edu.repetita.solvers.sr.rls.core.CapacityData;
import java.util.Random;

public abstract class EdgeDemandState {
    protected final int nDemands;
    protected final int nEdges;

    public EdgeDemandState(int nDemands, int nEdges) {
        this.nDemands = nDemands;
        this.nEdges = nEdges;
    }

    public abstract void updateEdgeDemand(int edge, int demand, double flowDelta);
    public abstract int selectRandomDemand(int edge);
    public abstract double flowOnEdgeDemand(int edge, int demand);

    public void restrictDemands(boolean[] set) {
        int numDemands = set.length;
        int edge = nEdges;
        while (edge > 0) {
            edge--;
            int demand = numDemands;
            while (demand > 0) {
                demand--;
                double flow = flowOnEdgeDemand(edge, demand);
                if (!set[demand] && flow != 0.0) {
                    updateEdgeDemand(edge, demand, -flow);
                }
            }
        }
    }
}
