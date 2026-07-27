package edu.repetita.solvers.sr.rls.state;

import edu.repetita.solvers.sr.rls.io.DemandsData;

public class PathState extends TrialState {
    public final int nDemands;
    public final int maxDetourSize = 6;
    private final DemandsData demands;

    private final Path[] paths;

    public final int[] changed;
    private int nChanged_ = 0;
    private final boolean[] markedChanged;

    public PathState(DemandsData demands) {
        this.demands = demands;
        this.nDemands = demands.nDemands;
        this.paths = new Path[nDemands];
        for (int d = 0; d < nDemands; d++) {
            paths[d] = new Path(d, demands.demandSrcs[d], demands.demandDests[d], maxDetourSize);
        }
        this.changed = new int[nDemands];
        this.markedChanged = new boolean[nDemands];
    }

    public int source(int demand) {
        return demands.demandSrcs[demand];
    }

    public int destination(int demand) {
        return demands.demandDests[demand];
    }

    public int size(int demand) {
        return paths[demand].size();
    }

    public int[] path(int demand) {
        return paths[demand].path();
    }

    public int oldSize(int demand) {
        return paths[demand].oldSize();
    }

    public int[] oldPath(int demand) {
        return paths[demand].oldPath();
    }

    public void insert(int demand, int node, int position) {
        addChanged(demand);
        paths[demand].insert(node, position);
    }

    public void replace(int demand, int node, int position) {
        addChanged(demand);
        paths[demand].replace(node, position);
    }

    public void remove(int demand, int position) {
        addChanged(demand);
        paths[demand].remove(position);
    }

    public void reset(int demand) {
        addChanged(demand);
        paths[demand].reset();
    }

    public void setPath(int demand, int[] path, int size) {
        addChanged(demand);
        paths[demand].setPath(path, size);
    }

    public int nChanged() {
        return nChanged_;
    }

    private void addChanged(int demand) {
        if (!markedChanged[demand]) {
            paths[demand].save();
            markedChanged[demand] = true;
            changed[nChanged_++] = demand;
        }
    }

    @Override
    public void updateState() {}

    @Override
    public void commitState() {
        while (nChanged_ > 0) {
            nChanged_--;
            int demand = changed[nChanged_];
            markedChanged[demand] = false;
        }
    }

    @Override
    public void revertState() {
        while (nChanged_ > 0) {
            nChanged_--;
            int demand = changed[nChanged_];
            paths[demand].restore();
            markedChanged[demand] = false;
        }
    }

    private static class Path {
        public final int demand;
        public final int source;
        public final int destination;
        public final int maxSize;

        private int currentSize = 2;
        private final int[] currentPath;

        private int savedSize = 2;
        private final int[] savedPath;

        public Path(int demand, int source, int destination, int maxSize) {
            if (maxSize < 2) throw new IllegalArgumentException("maxSize >= 2 required");
            this.demand = demand;
            this.source = source;
            this.destination = destination;
            this.maxSize = maxSize;

            this.currentPath = new int[maxSize];
            currentPath[0] = source;
            currentPath[1] = destination;

            this.savedPath = new int[maxSize];
            savedPath[0] = source;
            savedPath[1] = destination;
        }

        public int size() { return currentSize; }
        public int[] path() { return currentPath; }
        public int oldSize() { return savedSize; }
        public int[] oldPath() { return savedPath; }

        public void insert(int node, int position) {
            assert 0 < position && position < currentSize && currentSize < maxSize;
            System.arraycopy(currentPath, position, currentPath, position + 1, currentSize - position);
            currentPath[position] = node;
            currentSize++;
        }

        public void replace(int node, int position) {
            assert position > 0 && position < currentSize - 1;
            currentPath[position] = node;
        }

        public void remove(int position) {
            assert 0 < position && position < currentSize - 1;
            System.arraycopy(currentPath, position + 1, currentPath, position, currentSize - position - 1);
            currentSize--;
        }

        public void reset() {
            currentPath[0] = source;
            currentPath[1] = destination;
            currentSize = 2;
        }

        public void setPath(int[] newPath, int newSize) {
            assert newSize <= maxSize;
            assert newPath[0] == source;
            assert newPath[newSize - 1] == destination;
            System.arraycopy(newPath, 0, currentPath, 0, newSize);
            currentSize = newSize;
        }

        public void save() {
            savedSize = currentSize;
            System.arraycopy(currentPath, 0, savedPath, 0, savedSize);
        }

        public void restore() {
            currentSize = savedSize;
            System.arraycopy(savedPath, 0, currentPath, 0, savedSize);
        }
    }
}
