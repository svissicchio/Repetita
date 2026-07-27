package edu.repetita.solvers.sr.rls.state;

import edu.repetita.solvers.sr.rls.core.CapacityData;
import java.util.Random;

public class EdgeDemandStateSimple extends EdgeDemandState {
    private final double[][] flowOnEdgeDemand_;
    private final double epsilon = 1e-6;
    private final Random random = new Random();

    public EdgeDemandStateSimple(int nDemands, int nEdges, CapacityData capacity) {
        super(nDemands, nEdges);
        this.flowOnEdgeDemand_ = new double[nEdges][nDemands];
    }

    @Override
    public double flowOnEdgeDemand(int edge, int demand) {
        return flowOnEdgeDemand_[edge][demand];
    }

    @Override
    public void updateEdgeDemand(int edge, int demand, double flowDelta) {
        flowOnEdgeDemand_[edge][demand] += flowDelta;
    }

    @Override
    public int selectRandomDemand(int edge) {
        int chosenDemand = -1;
        double totalFlow = 0.0;

        int demand = nDemands;
        while (demand > 0) {
            demand--;
            double flow = flowOnEdgeDemand_[edge][demand];
            totalFlow += flow;

            if (random.nextDouble() * totalFlow < flow) {
                chosenDemand = demand;
            }
        }
        return chosenDemand;
    }
}
