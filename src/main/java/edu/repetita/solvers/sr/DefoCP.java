package edu.repetita.solvers.sr;

import be.ac.ucl.ingi.defo.core.DEFOInstance;
import be.ac.ucl.ingi.defo.modeling.DEFOConstraint;
import be.ac.ucl.ingi.defo.modeling.DEFOptimizer;
import be.ac.ucl.ingi.defo.modeling.units.RelativeUnit;
import be.ac.ucl.ingi.defo.modeling.units.TimeUnit;
import edu.repetita.core.Demands;
import edu.repetita.core.Setting;
import edu.repetita.core.Topology;
import edu.repetita.paths.SRPaths;
import edu.repetita.settings.SRSetting;
import edu.repetita.solvers.SRSolver;

import java.io.PrintWriter;

public class DefoCP extends SRSolver {

    private long solveTime = 0;

    @Override
    protected void setObjective() {
        this.objective = 0;
    }

    @Override
    public String name() {
        return "defoCP";
    }

    @Override
    public String getDescription() {
        return "A Segment Routing path optimizer implementing the Constraint Programming algorithm described in " +
                "\"Hartert et al., A Declarative and Expressive Approach to Control Forwarding Paths in " +
                "Carrier-Grade Networks. In SIGCOMM, 2015.\"";
    }

    @Override
    public long solveTime(Setting setting) {
        return this.solveTime;
    }

    @Override
    public void solve(Setting setting, long timeMillis) {
        SRSetting srSetting = (SRSetting) setting;
        // extract information from setting
        Topology topology = srSetting.getTopology();
        int nEdges = topology.nEdges;
        Demands demands = srSetting.getDemands();

        // translate in scala data structures
        be.ac.ucl.ingi.defo.core.Topology defoTopology = be.ac.ucl.ingi.defo.core.Topology.apply(topology.edgeSrc, topology.edgeDest);

        int[] edgeCapacities = new int[nEdges];
        for (int edge = 0; edge < nEdges; edge++) edgeCapacities[edge] = (int) topology.edgeCapacity[edge];

        int[] demandTraffic = new int[demands.nDemands];
        for (int demand = 0; demand < demands.nDemands; demand++) demandTraffic[demand] = (int) demands.amount[demand];
        DEFOConstraint[][] demandConstraints = new DEFOConstraint[demands.nDemands][0];
        DEFOConstraint[] topologyConstraints = new DEFOConstraint[0];

        DEFOInstance instance = new DEFOInstance(defoTopology, topology.edgeWeight, demandTraffic, demands.source, demands.dest, demandConstraints, topologyConstraints, edgeCapacities, topology.edgeLatency, srSetting.getMaxSeg());
        DEFOptimizer optimizer = new DEFOptimizer(instance, this.verbose > 0, scala.Option.apply((PrintWriter) null));

        TimeUnit timeLimit = new TimeUnit((int) timeMillis, timeMillis + "ms");

        RelativeUnit maxLoad = new RelativeUnit((int) (this.getTargetObjectiveValue() * 100));

        // solve and track execution time
        long timeBefore = System.nanoTime();
        optimizer.solve(timeLimit, maxLoad);
        long timeAfter = System.nanoTime();
        this.solveTime = timeAfter - timeBefore;

        // write results back
        int[][] bestPaths = optimizer.core().bestPaths();
        SRPaths paths = new SRPaths(srSetting.getDemands(),srSetting.getTopology().nNodes);
        for (int demand = 0; demand < demands.nDemands; demand++) {
            paths.setPath(demand, bestPaths[demand]);
        }
        srSetting.setSRPaths(paths);
    }

}
