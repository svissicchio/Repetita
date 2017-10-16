package tests.java.edu.repetita.scenarios;

import edu.repetita.core.*;
import edu.repetita.analyses.Analysis;

import edu.repetita.scenarios.DemandChangeReoptimization;
import tests.java.edu.repetita.ToyTopologies;
import tests.java.edu.repetita.Warehouse;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DemandChangeReoptimizationTest {
    /* Variables */
    private Warehouse warehouse = new Warehouse();
    private Scenario scenario = new DemandChangeReoptimization();
    private long timeRun = 1000;    // 1 second (enough to compute a good solution on the default topology, which is small)

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

        int it = 0;
        while(analyses.containsKey("Iteration " + it + " pre-optimization")){
            assert analyses.get("Iteration " + it + " post-optimization").maxLinkLoad <
                    analyses.get("Iteration " + it + " pre-optimization").maxLinkLoad;
            it++;
        }
    }

    @Test
    public void testRun_expectedLoads_withSquareTopologyTwoDemandsDefaultSolver() {
        Topology topology = ToyTopologies.getSquareWithBottomBottleneck();
        Demands initialDemands = ToyTopologies.getBottomAndTopLinkDemandsJustFittingOnSquareWithBottleneck(false);
        List<Demands> changedDemands = new ArrayList<>();
        changedDemands.add(ToyTopologies.getBottomAndTopLinkDemandsLargelyFittingOnSquareWithBottleneck(true));

        Setting setting = new Setting();
        setting.setTopology(topology);
        setting.setDemands(initialDemands);
        setting.setDemandChanges(changedDemands);

        scenario.setup(setting,warehouse.getDefaultSolver());
        scenario.keepAnalysesWhileRun();
        scenario.run(this.timeRun);
        Map<String,Analysis> analyses = scenario.getAnalyses();

        assert analyses.get("Iteration 0 pre-optimization").maxLinkLoad == 2;
        assert analyses.get("Iteration 0 post-optimization").maxLinkLoad == 1;
        assert analyses.get("Iteration 1 pre-optimization").maxLinkLoad > 0.5;
        assert analyses.get("Iteration 1 post-optimization").maxLinkLoad == 0.5;

        Map<Integer, int[]> firstPathMap = analyses.get("Iteration 0 post-optimization").demands2SRPaths;
        Map<Integer, int[]> secondPathMap = analyses.get("Iteration 0 post-optimization").demands2SRPaths;
        assert firstPathMap.get(0) == secondPathMap.get(0);
        assert firstPathMap.get(1) == secondPathMap.get(1);
    }
}
