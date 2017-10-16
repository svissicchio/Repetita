package edu.repetita.scenarios;

import edu.repetita.analyses.Analysis;
import edu.repetita.core.Scenario;
import edu.repetita.core.Setting;
import edu.repetita.core.Solver;
import edu.repetita.core.Topology;
import edu.repetita.paths.ShortestPaths;

/*
 *  We put the network in optimal state with given topology and demands,
 *  then we break links to see how the new max utilization compares to the MCF.
 */

public class SingleLinkFailureRobustness extends Scenario {
    private ShortestPaths sp;

    @Override
    public String getDescription() {
        return "Runs a solver on a topology, stores the routing configuration computed by the solver, " +
                "and evaluates how single link failures affect the computed configuration";
    }

    @Override
    public void setup(Setting setting, Solver solver) {
        super.setup(setting,solver);
        analyzer.addComparisonWithMCF();
        this.sp = new ShortestPaths(setting.getTopology());
    }

    @Override
    public void run(long timeMillis) {
        // run the solver and assess the post-optimization state
        this.solver.solve(this.setting,timeMillis);
        Analysis initialAnalysis = analyzer.analyze(this.setting,"initial");
        initialAnalysis.printTrafficSummary();
        if (this.keepAnalyses){
            this.analyses.put("-1: no broken edge",initialAnalysis);
        }

        // compute max link utilization after every single link failure
        Topology topology = this.setting.getTopology();
        int nEdges = topology.nEdges;
        boolean[] breakable = new boolean[nEdges]; // edges that can be broken without disconnecting graph


        // for every link, if breaking the link does not disconnect the network, compare minimum maxLinkLoad and maxLinkLoad deriving
        // from the initial optimization (storing the performed analyses if needed)
        for (int edge = 0; edge < nEdges; edge++) {
            Topology newTopology = topology.clone().removeUndirectedEdge(edge);
            Setting newSetting = this.setting.clone();
            newSetting.setTopology(newTopology);

            sp = new ShortestPaths(newTopology);
            sp.computeShortestPaths();
            if (!sp.isGraphDisconnected()) {
                breakable[edge] = true;
                Analysis currentAnalysis = analyzer.analyze(newSetting,"new");
                this.print("broke edge " + edge + ", " + currentAnalysis.getTrafficSummary());
                if (this.keepAnalyses){
                    this.analyses.put(Integer.toString(edge),currentAnalysis);
                }
            }
        }

        this.print("");
    }
}
