package edu.repetita.solvers.sr.rls.state;

public class SavedPathState implements Trial {
    public final int nDemands;
    private final boolean[] changed;
    private final int[] changedStack;
    private int nChanged = 0;
    private final PathState pathState;

    private final int[][] paths;
    private final int[] lengthPaths;

    public SavedPathState(PathState pathState) {
        this.pathState = pathState;
        this.nDemands = pathState.nDemands;
        this.changed = new boolean[nDemands];
        this.changedStack = new int[nDemands];
        this.paths = new int[nDemands][pathState.maxDetourSize];
        this.lengthPaths = new int[nDemands];
        initialize();
    }

    private void changePath(int demand) {
        if (!changed[demand]) {
            changedStack[nChanged++] = demand;
            changed[demand] = true;
        }
    }

    private void initialize() {
        int demand = nDemands;
        while (demand > 0) {
            demand--;
            System.arraycopy(pathState.path(demand), 0, paths[demand], 0, pathState.size(demand));
            lengthPaths[demand] = pathState.size(demand);
        }
    }

    public int savePaths() {
        int count = 0;
        while (nChanged > 0) {
            nChanged--;
            int demand = changedStack[nChanged];
            int[] path = pathState.path(demand);
            int size = pathState.size(demand);
            changed[demand] = false;

            boolean pathChanged = size != lengthPaths[demand];
            if (!pathChanged) {
                int p = size;
                while (p > 0) {
                    p--;
                    if (path[p] != paths[demand][p]) {
                        pathChanged = true;
                        break;
                    }
                }
            }

            if (pathChanged) count++;

            System.arraycopy(path, 0, paths[demand], 0, size);
            lengthPaths[demand] = size;
        }
        return count;
    }

    public void restorePaths() {
        while (nChanged > 0) {
            nChanged--;
            int demand = changedStack[nChanged];
            changed[demand] = false;
            pathState.setPath(demand, paths[demand], lengthPaths[demand]);
        }
    }

    @Override
    public boolean check() { return true; }

    @Override
    public void revert() {}

    @Override
    public void update() {}

    @Override
    public void commit() {
        int[] currentChanged = pathState.changed;
        int p = pathState.nChanged();
        while (p > 0) {
            p--;
            int demand = currentChanged[p];
            changePath(demand);
        }
    }
}
