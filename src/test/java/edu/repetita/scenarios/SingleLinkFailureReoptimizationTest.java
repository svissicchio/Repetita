package edu.repetita.scenarios;

import edu.repetita.core.Scenario;
import edu.repetita.core.Setting;
import edu.repetita.core.Solver;
import edu.repetita.core.Topology;
import edu.repetita.analyses.Analysis;
import edu.repetita.analyses.Analyzer;
import edu.repetita.scenarios.SingleLinkFailureReoptimization;
import edu.repetita.solvers.sr.MIPTwoSRNoSplit;
import edu.repetita.Warehouse;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class SingleLinkFailureReoptimizationTest {
    /* Variables */
    private Warehouse warehouse = new Warehouse();
    private Scenario scenario = new SingleLinkFailureReoptimization();
    private Setting originalSetting = warehouse.getDefaultSetting();
    private long timeRun = 2000;    // 2 seconds should be enough to compute an almost-optimal solution on the default topology (Abilene), which is small

    /* Tests */

    @Before
    public void setup (){
        Setting setting = warehouse.getDefaultSetting();
        Solver solver = warehouse.getDefaultSolver();
        this.scenario.setup(setting,solver);
    }

    @Test
    public void testRun_meaningfulLoadValues_defaultSolver () {
        scenario.keepAnalysesWhileRun();
        scenario.run(this.timeRun);
        Map<String,Analysis> analyses = scenario.getAnalyses();

        for (int edge=0; edge<originalSetting.getTopology().nEdges; edge++) {
            if (analyses.containsKey("Failed link " + edge + " pre-optimization")) {
                double preOptLoad = analyses.get("Failed link " + edge + " pre-optimization").maxLinkLoad;
                double postOptLoad = analyses.get("Failed link " + edge + " post-optimization").maxLinkLoad;
                assert (postOptLoad <= preOptLoad) ||
                        (Math.abs(preOptLoad - 0.9) < 1e-3 && Math.abs(preOptLoad - postOptLoad) < 1e-3);   // this second check is meant to avoid numerical errors
            }
        }
    }

    @Test
    public void testRun_sameResultsAsManualReoptimization_deterministicSolver () {
        Analyzer analyzer = Analyzer.getInstance();
        Topology topology = originalSetting.getTopology();
        Solver newSolver = new MIPTwoSRNoSplit();
        newSolver.setVerbose(0);

        long longtime = this.timeRun * 100;         // give a lot of time for the deterministic solver to compute a solution
        scenario.setup(originalSetting,newSolver);
        scenario.run(longtime);
        Map<String,Analysis> analyses = scenario.getAnalyses();

        for (int edge=0; edge<originalSetting.getTopology().nEdges; edge++) {
            if (analyses.containsKey("Failed link " + edge + " post-optimization")) {
                // simulate the link failure by setting the weight of the edge to infinite (on both directions)
                int oldWeight = topology.edgeWeight[edge];
                topology.edgeWeight[edge] = Topology.INFINITE_DISTANCE;
                int symmetricEdge = topology.findSymmetricEdge(edge);
                int oldSymmetricWeight = topology.edgeWeight[symmetricEdge];
                topology.edgeWeight[symmetricEdge] = Topology.INFINITE_DISTANCE;

                // assert that results are the same
                newSolver.solve(originalSetting,longtime);
                Analysis newAnalysis = analyzer.analyze(originalSetting);
                System.out.println("broke edge " + edge + " stored maxLinkLoad " + analyses.get("Failed link " + edge + " post-optimization").maxLinkLoad + " manual maxLinkLoad " + newAnalysis.maxLinkLoad + " (difference " + (newAnalysis.maxLinkLoad - analyses.get("Failed link " + edge + " post-optimization").maxLinkLoad) + ")");
                assert Math.abs(newAnalysis.maxLinkLoad - analyses.get("Failed link " + edge + " post-optimization").maxLinkLoad) < 1e-3;

                // repair the failure by setting the original weight
                topology.edgeWeight[edge] = oldWeight;
                topology.edgeWeight[symmetricEdge] = oldSymmetricWeight;
            }
        }
    }
}
