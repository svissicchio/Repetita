package edu.repetita.scenarios;

import edu.repetita.core.*;
import edu.repetita.analyses.Analyzer;

import java.util.List;

/*
 *  Change demands and ask the configured solver to re-optimize
 */

public class DemandChangeReoptimization extends Scenario {
    private Analyzer analyzer = Analyzer.getInstance();

    @Override
    public String getDescription() {
        return "Change demands and ask the configured solver to re-optimize";
    }

    @Override
    public void run(long timeMillis) {
        // extract useful information from setting
        List<Demands> demandsList = this.setting.getDemandChanges();
        demandsList.add(0,this.setting.getDemands());
        Topology topology = this.setting.getTopology();

        // print initial stats
        this.print("NODES " + topology.nNodes + " EDGES " + topology.nEdges + " INITIAL DEMANDS " + this.setting.getDemands().nDemands);

        int nIterations = Math.max(this.setting.getNumberReoptimizations(),demandsList.size());

        // iterate for nIterations
        RoutingConfiguration lastConfig = setting.getRoutingConfiguration();
        for (int iteration = 0; iteration < nIterations; iteration++) {
            // extract the new demand from demandsList
            Demands currentDemands = demandsList.get(iteration % demandsList.size());

            // create a new (minimalistic) setting and analyze it
            Long timeBeforeChange = System.nanoTime();
            Setting currentSetting = new Setting();
            currentSetting.setTopology(topology);
            currentSetting.setDemands(currentDemands);
            currentSetting.setRoutingConfiguration(lastConfig);
            analyses.put("Iteration " + Integer.toString(iteration) + " pre-optimization",analyzer.analyze(currentSetting,"pre-optimization"));
            Long timeAfterChange = System.nanoTime();

            // run solver to optimize configuration
            Long timeBeforeSolve = System.nanoTime();
            solver.solve(currentSetting, timeMillis);
            Long timeAfterSolve = System.nanoTime();

            // store the post-optimization analysis
            analyses.put("Iteration " + Integer.toString(iteration) + " post-optimization",analyzer.analyze(currentSetting,"post-optimization"));

            // keep the post-optimization configuration as the last one produced
            lastConfig = currentSetting.getRoutingConfiguration();

            // print some stats
            this.print("CHANGETIME " + (timeAfterChange - timeBeforeChange) / 1000000 +
                    " SOLVETIME " + (timeAfterSolve - timeBeforeSolve) / 1000000);
        }

        // print results
        for (int it = 0; it < nIterations; it++) {
            this.print("\ndemand change " + it);
            analyses.get("Iteration " + it + " pre-optimization").printTrafficSummary();
            analyses.get("Iteration " + it + " post-optimization").printTrafficSummary();
        }
    }
}
