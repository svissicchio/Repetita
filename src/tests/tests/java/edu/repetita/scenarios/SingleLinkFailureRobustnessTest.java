package tests.java.edu.repetita.scenarios;

import edu.repetita.core.Scenario;
import edu.repetita.core.Setting;
import edu.repetita.core.Solver;
import edu.repetita.core.Topology;
import edu.repetita.analyses.Analysis;
import edu.repetita.analyses.Analyzer;
import edu.repetita.scenarios.SingleLinkFailureRobustness;
import edu.repetita.solvers.sr.MIPTwoSRNoSplit;
import tests.java.edu.repetita.Warehouse;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class SingleLinkFailureRobustnessTest {

    /* Variables */
    private Warehouse warehouse = new Warehouse();
    private Scenario scenario = new SingleLinkFailureRobustness();
    private long timeRun = 5 * 1000;    // 5 seconds (enough to compute a good solution on the default topology, which is small)

    /* Tests */

    @Before
    public void setup (){
        Setting setting = warehouse.getDefaultSetting();
        Solver solver = warehouse.getDeterministicSolver();
        solver.setVerbose(0);
        this.scenario.setup(setting,solver);
    }

    @Test
    public void testRun_noErrors_deterministicSolver () {
        scenario.run(this.timeRun);
    }

    @Test
    public void testRun_meaningfulLoadValues_deterministicSolver () {
        scenario.keepAnalysesWhileRun();
        scenario.run(this.timeRun);
        Map<String,Analysis> analyses = scenario.getAnalyses();

        Analysis initial = analyses.remove("-1: no broken edge");
        assert initial.maxLinkLoad >= initial.maxLinkLoadLowerBound;
        for (Analysis a: analyses.values()){
            assert a.maxLinkLoad >= a.maxLinkLoadLowerBound;
            assert a.maxLinkLoad >= initial.maxLinkLoad;
        }
    }

    @Test
    public void testRun_sameResultsAsManualReoptimization_deterministicSolver () {
        Analyzer analyzer = Analyzer.getInstance();
        Solver newSolver = new MIPTwoSRNoSplit();
        newSolver.setVerbose(0);
        Setting originalSetting = warehouse.getDefaultSetting();

        scenario.setup(originalSetting,newSolver);
        scenario.keepAnalysesWhileRun();
        scenario.run(this.timeRun * 100);     // the deterministic solver needs more time
        Map<String,Analysis> analyses = scenario.getAnalyses();

        for (int edge=0; edge < originalSetting.getTopology().nEdges; edge++) {
            String key = Integer.toString(edge);
            if (analyses.containsKey(key)) {
                // recreate setting with the failed link (keeping the same routing configuration computed before)
                Setting newSetting = originalSetting.clone();
                Topology newTopology = newSetting.getTopology();
                newTopology.edgeWeight[edge] = Topology.INFINITE_DISTANCE;
                int symmetricEdge = newTopology.findSymmetricEdge(edge);
                newTopology.edgeWeight[symmetricEdge] = Topology.INFINITE_DISTANCE;

                // assert that results are the same
                Analysis newAnalysis = analyzer.analyze(newSetting);
                System.out.println("considering link " + edge + " (stored maxLinkLoad " + analyses.get(key).maxLinkLoad + ", freshly computed maxLinkLoad " + newAnalysis.maxLinkLoad + ")");
                assert newAnalysis.maxLinkLoad == analyses.get(key).maxLinkLoad;
            }
        }
    }
}

