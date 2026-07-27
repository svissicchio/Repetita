package edu.repetita.solvers.sr.rls.state;

import edu.repetita.solvers.sr.rls.io.DemandsData;

public abstract class FlowStateChecker extends ArrayStateDouble {
    protected final PathState pathState;
    protected final DemandsData demandsData;
    protected final int nNodes;
    protected final int nEdges;

    public FlowStateChecker(int nNodes, int nEdges, PathState pathState, DemandsData demandsData) {
        super(nEdges);
        this.nNodes = nNodes;
        this.nEdges = nEdges;
        this.pathState = pathState;
        this.demandsData = demandsData;
    }

    @Override
    public boolean check() {
        updateState();
        return super.check();
    }

    public abstract void modify(int source, int destination, double bw);

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
                modify(source, destination, demandsData.demandTraffics[demand]);
            }
        }
    }

    @Override
    public void updateState() {
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
                modify(currentPath[p], currentPath[p + 1], bandwidth);
                p++;
            }

            int q = firstDiff - 1;
            while (q <= endOld) {
                modify(oldPath[q], oldPath[q + 1], -bandwidth);
                q++;
            }
        }
    }
}
