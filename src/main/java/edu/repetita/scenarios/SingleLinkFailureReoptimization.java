package edu.repetita.scenarios;

import edu.repetita.core.Scenario;
import edu.repetita.paths.ShortestPaths;
import edu.repetita.core.Setting;
import edu.repetita.core.Topology;

/*
 *  From a given state, the solver is called to re-optimize the forwarding after every link failure in a randomly generated series.
 *  Link failures are modeled by setting weight to infinity.
 *  The solver has a set time to re-optimize after link failures, and we count the number of events where the network's maxLinkLoad is higher than 0.9 after an event.
 */

public class SingleLinkFailureReoptimization extends Scenario {
    private ShortestPaths sp;

    @Override
    public String getDescription() {
        return "The solver is called to re-optimize the forwarding after every link failure in a randomly generated series";
    }

    @Override
    public void run(long timeMillis) {
        // run the solver and assess the post-optimization state
        this.solver.solve(this.setting, timeMillis);
        analyses.put("Initial ", analyzer.analyze(this.setting));

        // compute max link utilization after every single link failure
        Topology topology = this.setting.getTopology();
        int nEdges = topology.nEdges;
        boolean[] breakable = new boolean[nEdges]; // edge can be broken without disconnecting graph

        // for every link, if breaking the link does not disconnect the network, run the solver and store the analyses
        // before and after the optimization
        for (int edge = 0; edge < nEdges; edge++) {
            Topology newTopology = topology.clone().removeUndirectedEdge(edge);
            Setting newSetting = this.setting.clone();
            newSetting.setTopology(newTopology);

            sp = new ShortestPaths(newTopology);
            sp.computeShortestPaths();
            if (!sp.isGraphDisconnected()) {
                breakable[edge] = true;
                this.analyses.put(String.format("Failed link %d pre-optimization",edge),analyzer.analyze(newSetting,"pre-optimization"));
                this.solver.solve(newSetting, timeMillis);
                this.analyses.put(String.format("Failed link %d post-optimization",edge),analyzer.analyze(newSetting, "post-optimization"));
            }
        }

        // print results
        for (int edge = 0; edge < nEdges; edge++) {
            if (breakable[edge]){
                this.print("broke edge " + edge);
                analyses.get(String.format("Failed link %d pre-optimization",edge)).printTrafficSummary();
                analyses.get(String.format("Failed link %d post-optimization",edge)).printTrafficSummary();
                this.print("");
            }
        }
    }
}
