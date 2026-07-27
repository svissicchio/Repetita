package edu.repetita.solvers.sr.rls.state;

import edu.repetita.solvers.sr.rls.ShortestPaths;
import edu.repetita.solvers.sr.rls.io.DemandsData;

public class FlowStateRecomputeDAGOnCommit extends ArrayStateDouble {
    private final int nNodes;
    private final ShortestPaths sp;
    private final PathState pathState;
    private final DemandsData demandsData;
    private final EdgeDemandState edgeDemandState;
    private final double[] toRoute;

    public FlowStateRecomputeDAGOnCommit(int nNodes, int nEdges, ShortestPaths sp, PathState pathState, DemandsData demandsData, EdgeDemandState edgeDemandState) {
        super(nEdges);
        this.nNodes = nNodes;
        this.sp = sp;
        this.pathState = pathState;
        this.demandsData = demandsData;
        this.edgeDemandState = edgeDemandState;
        this.toRoute = new double[nNodes];
        initialize();
        super.commit();
    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void updateState() {}

    @Override
    public void commit() {
        updateFlowState();
        super.commit();
    }

    protected void initialize() {
        int demand = pathState.nDemands;
        while (demand > 0) {
            demand--;
            int[] path = pathState.path(demand);
            int pos = pathState.size(demand) - 1;
            while (pos > 0) {
                pos--;
                int source = path[pos];
                int destination = path[pos + 1];
                modify(demand, source, destination, demandsData.demandTraffics[demand]);
            }
        }
    }

    private void updateFlowState() {
        int pChanged = pathState.nChanged();
        int[] changed = pathState.changed;
        while (pChanged > 0) {
            pChanged--;
            int demand = changed[pChanged];
            double bandwidth = demandsData.demandTraffics[demand];

            int[] currentPath = pathState.path(demand);
            int currentSize = pathState.size(demand);
            int[] oldPath = pathState.oldPath(demand);
            int oldSize = pathState.oldSize(demand);

            int minSize = Math.min(currentSize, oldSize);
            int firstDiff = 1;
            while (firstDiff < minSize && currentPath[firstDiff] == oldPath[firstDiff]) {
                firstDiff++;
            }

            int endCurrent = currentSize - 2;
            int endOld = oldSize - 2;
            while (firstDiff < endCurrent && firstDiff < endOld && currentPath[endCurrent] == oldPath[endOld]) {
                endCurrent--;
                endOld--;
            }

            int p = firstDiff - 1;
            while (p <= endCurrent) {
                modify(demand, currentPath[p], currentPath[p + 1], bandwidth);
                p++;
            }

            int q = firstDiff - 1;
            while (q <= endOld) {
                modify(demand, oldPath[q], oldPath[q + 1], -bandwidth);
                q++;
            }
        }
    }

    public void modify(int demand, int source, int destination, double bw) {
        int[][] successorNodes = sp.successorNodes(destination);
        int[][] successorEdges = sp.successorEdges(destination);
        int[] nSuccessors = sp.nSuccessors(destination);

        int source0 = source;
        while (source0 != destination && nSuccessors[source0] == 1) {
            int edge = successorEdges[source0][0];
            edgeDemandState.updateEdgeDemand(edge, demand, bw);
            updateValue(edge, values[edge] + bw);

            int next = successorNodes[source0][0];
            source0 = next;
        }

        if (source0 != destination) {
            int[] ordering = sp.topologicalOrdering();
            int pOrdering = sp.makeTopologicalOrdering(source, destination);
            toRoute[source0] = bw;

            while (pOrdering > 0) {
                pOrdering--;
                int node = ordering[pOrdering];

                int pSucc = nSuccessors[node];
                double increment = toRoute[node] / pSucc;
                toRoute[node] = 0.0;

                while (pSucc > 0) {
                    pSucc--;
                    int succEdge = successorEdges[node][pSucc];
                    updateValue(succEdge, values[succEdge] + increment);
                    edgeDemandState.updateEdgeDemand(succEdge, demand, increment);

                    int succNode = successorNodes[node][pSucc];
                    toRoute[succNode] += increment;
                }
            }
        }
    }
}
