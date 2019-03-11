package edu.repetita.solvers.sr;

import edu.repetita.core.Demands;
import edu.repetita.core.Setting;
import edu.repetita.core.Topology;
import edu.repetita.paths.SRPaths;
import edu.repetita.settings.SRSetting;
import edu.repetita.solvers.SRSolver;
import be.ac.ucl.ingi.rls.LoadOptimizer;
import be.ac.ucl.ingi.rls.io.DemandsData;
import be.ac.ucl.ingi.rls.io.TopologyData;
import be.ac.ucl.ingi.rls.state.PathState;


public class SRLS extends SRSolver {

    /* Variables */
    private TopologyData rlsTopology;
    private DemandsData rlsDemands;
    private LoadOptimizer loadOptimizer;
    private long solveTime = 0;


    /* Interface methods */
    @Override
    protected void setObjective() {
        this.objective = 0;
    }

    @Override
    public String name() {
        return "SRLS";
    }

    @Override
    public String getDescription() {
        return "A Segment Routing path optimizer approximating the Local Search algorithm described in " +
                "\"Gay et al., Expect the Unexpected: Sub-Second Optimization for Segment Routing. In INFOCOM, 2017.\""
                + "(full version at https://github.com/rhartert/defo-ls)";
    }

    @Override
    public void solve(Setting setting, long milliseconds) {
        // convert setting into variables expected by the Scala solver
        this.initializeScalaVariables(setting);

        // solve
        long timeBefore = System.nanoTime();
        PathState pathState = loadOptimizer.solve(milliseconds,this.getTargetObjectiveValue());
        long timeAfter = System.nanoTime();
        this.solveTime = timeAfter - timeBefore;

        // initialize new segment routing paths
        SRPaths paths = new SRPaths(setting.getDemands(),setting.getTopology().nNodes);

        // convert Scala output to SRPaths
        for (int demand = 0; demand < rlsDemands.nDemands(); demand++) {
            paths.setPath(demand, pathState.path(demand));
        }

        // add new paths to setting
        setting.setSRPaths(paths);
    }

    @Override
    public long solveTime(Setting setting) {
        return this.solveTime;
    }


    /* Helper method */
    private void initializeScalaVariables(Setting setting){
        Topology topology = setting.getTopology();
        Demands demands = setting.getDemands();
        SRSetting srSetting = (SRSetting) setting;

        rlsTopology = TopologyData.apply(
                topology.nodeLabel,
                topology.edgeLabel,
                topology.edgeSrc,
                topology.edgeDest,
                topology.edgeWeight,
                topology.edgeCapacity,
                topology.edgeLatency
        );

        rlsDemands = DemandsData.apply(demands.label, demands.source, demands.dest, demands.amount);
        loadOptimizer = new LoadOptimizer(rlsTopology, rlsDemands, srSetting.getMaxSeg(), this.verbose > 0);
    }
}
