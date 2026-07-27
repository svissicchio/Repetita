package edu.repetita.solvers.sr.rls.state;

import edu.repetita.solvers.sr.rls.core.CapacityData;
import java.util.Random;

public class EdgeDemandStateTree extends EdgeDemandState {
    private final int logDemands;
    private final int baseDemands;
    private final double[][] treeFlowOnEdgeDemand;
    private final double epsilon = 1e-6;
    private final Random random = new Random();

    public EdgeDemandStateTree(int nDemands, int nEdges, CapacityData capacity) {
        super(nDemands, nEdges);
        this.logDemands = (int) Math.ceil(Math.log(nDemands) / Math.log(2));
        this.baseDemands = 1 << logDemands;
        this.treeFlowOnEdgeDemand = new double[nEdges][2 * baseDemands];
    }

    @Override
    public double flowOnEdgeDemand(int edge, int demand) {
        return treeFlowOnEdgeDemand[edge][baseDemands + demand];
    }

    private void modifyFlowDemand(int edge, int demand, double newFlow) {
        assert newFlow >= -epsilon;
        treeFlowOnEdgeDemand[edge][baseDemands + demand] = newFlow;
        modifyFlowDemandTree(treeFlowOnEdgeDemand[edge], (baseDemands + demand) >> 1);
    }

    private void modifyFlowDemandTree(double[] tree, int node) {
        int curr = node;
        while (curr >= 1) {
            int left = curr << 1;
            int right = left | 1;
            tree[curr] = tree[left] + tree[right];
            assert tree[curr] >= -epsilon;
            curr >>= 1;
        }
    }

    @Override
    public void updateEdgeDemand(int edge, int demand, double flowDelta) {
        double oldFlow = flowOnEdgeDemand(edge, demand);
        double newFlow = oldFlow + flowDelta;
        modifyFlowDemand(edge, demand, newFlow);
    }

    @Override
    public int selectRandomDemand(int edge) {
        double r = random.nextDouble() * treeFlowOnEdgeDemand[edge][1] - epsilon;
        int selectedNode = selectDemand(treeFlowOnEdgeDemand[edge], 1, 2 * baseDemands, r);
        return selectedNode - baseDemands;
    }

    private int selectDemand(double[] tree, int node, int limit, double r) {
        int currNode = node;
        double currR = r;
        while (true) {
            int left = currNode << 1;
            if (left >= limit) {
                return currNode;
            } else {
                if (currR <= tree[left]) {
                    currNode = left;
                } else {
                    int right = left | 1;
                    currR -= tree[left];
                    currNode = right;
                }
            }
        }
    }
}
