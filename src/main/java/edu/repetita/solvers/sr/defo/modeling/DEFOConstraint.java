package edu.repetita.solvers.sr.defo.modeling;

public abstract class DEFOConstraint {

    public static abstract class DEFODemandConstraint extends DEFOConstraint {
        public abstract int demandId();
    }

    public static class DEFOAvoidNode extends DEFODemandConstraint {
        private final int demandId;
        public final int nodeId;

        public DEFOAvoidNode(int demandId, int nodeId) {
            this.demandId = demandId;
            this.nodeId = nodeId;
        }

        @Override public int demandId() { return demandId; }
    }

    public static class DEFOAvoidEdge extends DEFODemandConstraint {
        private final int demandId;
        public final int edgeId;

        public DEFOAvoidEdge(int demandId, int edgeId) {
            this.demandId = demandId;
            this.edgeId = edgeId;
        }

        @Override public int demandId() { return demandId; }
    }

    public static class DEFOPassThrough extends DEFODemandConstraint {
        private final int demandId;
        public final int[] nodes;

        public DEFOPassThrough(int demandId, int[] nodes) {
            this.demandId = demandId;
            this.nodes = nodes;
        }

        @Override public int demandId() { return demandId; }
    }

    public static class DEFOPassThroughSeq extends DEFODemandConstraint {
        private final int demandId;
        public final int[][] seqNodes;

        public DEFOPassThroughSeq(int demandId, int[][] seqNodes) {
            this.demandId = demandId;
            this.seqNodes = seqNodes;
        }

        @Override public int demandId() { return demandId; }
    }

    public static class DEFOLowerLatency extends DEFODemandConstraint {
        private final int demandId;
        public final int latency;
        public final boolean relative;

        public DEFOLowerLatency(int demandId, int latency, boolean relative) {
            this.demandId = demandId;
            this.latency = latency;
            this.relative = relative;
        }

        @Override public int demandId() { return demandId; }
    }

    public static class DEFOLowerEqLatency extends DEFODemandConstraint {
        private final int demandId;
        public final int latency;
        public final boolean relative;

        public DEFOLowerEqLatency(int demandId, int latency, boolean relative) {
            this.demandId = demandId;
            this.latency = latency;
            this.relative = relative;
        }

        @Override public int demandId() { return demandId; }
    }

    public static class DEFOLowerLoad extends DEFOConstraint {
        public final int edgeId;
        public final int load;
        public final boolean relative;

        public DEFOLowerLoad(int edgeId, int load, boolean relative) {
            this.edgeId = edgeId;
            this.load = load;
            this.relative = relative;
        }
    }

    public static class DEFOLowerEqLoad extends DEFOConstraint {
        public final int edgeId;
        public final int load;
        public final boolean relative;

        public DEFOLowerEqLoad(int edgeId, int load, boolean relative) {
            this.edgeId = edgeId;
            this.load = load;
            this.relative = relative;
        }
    }

    public static class DEFOLowerLength extends DEFODemandConstraint {
        private final int demandId;
        public final int length;

        public DEFOLowerLength(int demandId, int length) {
            this.demandId = demandId;
            this.length = length;
        }

        @Override public int demandId() { return demandId; }
    }

    public static class DEFOLowerEqLength extends DEFODemandConstraint {
        private final int demandId;
        public final int length;

        public DEFOLowerEqLength(int demandId, int length) {
            this.demandId = demandId;
            this.length = length;
        }

        @Override public int demandId() { return demandId; }
    }
}
