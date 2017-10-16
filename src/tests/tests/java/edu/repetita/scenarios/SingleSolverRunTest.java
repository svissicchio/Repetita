package tests.java.edu.repetita.scenarios;

import edu.repetita.analyses.Analysis;
import edu.repetita.core.*;
import edu.repetita.paths.SRPaths;
import edu.repetita.scenarios.SingleSolverRun;
import tests.java.edu.repetita.ToyTopologies;
import tests.java.edu.repetita.Warehouse;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class SingleSolverRunTest {
    /* Variables */
    private Warehouse warehouse = new Warehouse();
    private Scenario scenario = new SingleSolverRun();
    private long timeLimitInMillis = 1000;  // running time limit: 1 second by default

    /* Tests */

    @Before
    public void setup (){
        Setting setting = warehouse.getDefaultSetting();
        Solver solver = warehouse.getDefaultSolver();
        this.scenario.setup(setting,solver);
    }

    @Test
    public void testRun_noErrors_defaultWarehouseObjects () {
        scenario.run(this.timeLimitInMillis);
    }

    @Test
    public void testRun_noErrors_deterministicSolver () {
        scenario.setup(warehouse.getDefaultSetting(),warehouse.getDeterministicSolver());
        scenario.run(this.timeLimitInMillis * 3);
    }

    @Test
    public void testRun_expectedLoads_withBottleneckedSquareJustFittingDemandsDefaultSolver() {
        Topology topology = ToyTopologies.getSquareWithBottomBottleneck();
        Demands demands = ToyTopologies.getBottomAndTopLinkDemandsJustFittingOnSquareWithBottleneck(false);

        Setting setting = new Setting();
        setting.setTopology(topology);
        setting.setDemands(demands);
        setting.setSRPaths(new SRPaths(demands,topology.nNodes));

        this.scenario.keepAnalysesWhileRun();
        this.scenario.setup(setting,warehouse.getDefaultSolver());
        this.scenario.run(this.timeLimitInMillis);
        Map<String,Analysis> analyses = this.scenario.getAnalyses();
        Set<String> keys = analyses.keySet();

        assert keys.size() == 2;
        assert analyses.get("pre-optimization").maxLinkLoad == 2;
        assert analyses.get("post-optimization").maxLinkLoad == 1;
    }

    @Test
    public void testRun_expectedLoads_withBottleneckedSquareLargelyFittingDemandsDefaultSolver() {
        Topology topology = ToyTopologies.getSquareWithBottomBottleneck();
        Demands demands = ToyTopologies.getBottomAndTopLinkDemandsLargelyFittingOnSquareWithBottleneck(false);

        Setting setting = new Setting();
        setting.setTopology(topology);
        setting.setDemands(demands);
        setting.setSRPaths(new SRPaths(demands,topology.nNodes));

        this.scenario.keepAnalysesWhileRun();
        this.scenario.setup(setting,warehouse.getDefaultSolver());
        this.scenario.run(this.timeLimitInMillis);
        Map<String,Analysis> analyses = this.scenario.getAnalyses();
        Set<String> keys = analyses.keySet();

        assert keys.size() == 2;
        assert analyses.get("pre-optimization").maxLinkLoad == 1;
        assert analyses.get("post-optimization").maxLinkLoad == .5;
    }
}
