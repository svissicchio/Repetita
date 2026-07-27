package edu.repetita.solvers.sr.rls.constraint;

import edu.repetita.solvers.sr.rls.ShortestPaths;
import edu.repetita.solvers.sr.rls.core.CapacityData;
import edu.repetita.solvers.sr.rls.core.Topology;
import edu.repetita.solvers.sr.rls.state.FlowStateChecker;
import edu.repetita.solvers.sr.rls.state.Objective;
import edu.repetita.solvers.sr.rls.state.Trial;
import edu.repetita.solvers.sr.rls.constraint.Lexicographic.TrialObjective;

public class MaxLoad implements TrialObjective {
    private final int nNodes;
    private final int nEdges;
    private final CapacityData capacityData;
    private final FlowStateChecker flowState;
    private final ShortestPaths sp;
    private final boolean debug;

    private int nMaxLoad = 0;
    private double maxLoad = 0.0;
    private int maxEdge = 0;
    private int oldNMaxLoad = nMaxLoad;
    private double oldMaxLoad = maxLoad;
    private int oldMaxEdge = maxEdge;

    public boolean active = true;
    public boolean relaxed = false;

    public MaxLoad(Topology topology, CapacityData capacityData, FlowStateChecker flowState, ShortestPaths sp, boolean debug) {
        this.topology = topology;
        this.capacityData = capacityData;
        this.flowState = flowState;
        this.sp = sp;
        this.debug = debug;
        this.nNodes = topology.nNodes;
        this.nEdges = topology.nEdges;
        initialize();
        commit();
    }

    private final Topology topology;

    @Override
    public double score() {
        return maxLoad;
    }

    public void initialize() {
        maxLoad = 0.0;
        nMaxLoad = 0;

        double[] flow = flowState.values;
        double[] invcapa = capacityData.invCapacity();

        int edge = nEdges;
        while (edge > 0) {
            edge--;
            double load = flow[edge] * invcapa[edge];
            if (load > maxLoad) {
                maxEdge = edge;
                maxLoad = load;
                nMaxLoad = 1;
            } else if (load == maxLoad) {
                nMaxLoad++;
            }
        }
    }

    public int selectRandomMaxEdge() {
        return maxEdge;
    }

    @Override
    public void update() {
        double[] flow = flowState.values;
        double[] oldFlow = flowState.oldValues;
        double[] invcapa = capacityData.invCapacity();

        int[] changed = flowState.deltaElements();
        int p = flowState.nDelta();
        while (p > 0) {
            p--;
            int edge = changed[p];
            if (flow[edge] != oldFlow[edge]) {
                double load = flow[edge] * invcapa[edge];
                double oldLoad = oldFlow[edge] * invcapa[edge];

                if (load > maxLoad) {
                    maxEdge = edge;
                    maxLoad = load;
                    nMaxLoad = 1;
                } else if (load == maxLoad) {
                    nMaxLoad++;
                } else if (oldLoad == maxLoad) {
                    nMaxLoad--;
                }
            }
        }

        if (nMaxLoad == 0) {
            initialize();
        }
    }

    @Override
    public boolean check() {
        update();
        boolean improved = !active || (relaxed && maxLoad <= oldMaxLoad) || maxLoad < oldMaxLoad;
        if (!improved) {
            revert();
        }
        return improved;
    }

    @Override
    public void commit() {
        oldMaxLoad = maxLoad;
        oldNMaxLoad = nMaxLoad;
        oldMaxEdge = maxEdge;
    }

    @Override
    public void revert() {
        maxLoad = oldMaxLoad;
        nMaxLoad = oldNMaxLoad;
        maxEdge = oldMaxEdge;
    }
}
