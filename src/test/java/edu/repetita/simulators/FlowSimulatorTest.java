package edu.repetita.simulators;

import edu.repetita.core.Demands;
import edu.repetita.core.Setting;
import edu.repetita.core.Topology;
import edu.repetita.simulators.FlowSimulator;
import edu.repetita.simulators.specialized.ECMPFlowSimulator;
import edu.repetita.paths.ExplicitPaths;
//import edu.repetita.solvers.wo.MIPWeightOptimizer;
import edu.repetita.ToyTopologies;
import edu.repetita.Warehouse;
import org.junit.Test;

import java.util.Arrays;

public class FlowSimulatorTest {
    private Warehouse warehouse = new Warehouse();
    private FlowSimulator simulator = FlowSimulator.getInstance();

    @Test
    public void computeFlows_consistentWithEcmpFlowSimulator_withNoExplicitPathAndNoSRPaths() {
        // simulate on IGP-only default setting
        Setting setting = warehouse.getDefaultSetting();
        setting.setExplicitPaths(new ExplicitPaths(setting.getTopology()));
        setting.setSRPaths(null);
        this.simulator.setup(setting);
        this.simulator.computeFlows();

        // maximum link utilization is not zero
        System.out.println(Arrays.toString(this.simulator.flow));
        System.out.println(this.simulator.getMaxUtilization());
        assert this.simulator.getMaxUtilization() > 0;

        // check that the link utilization is the same as running only the ECMP flow simulator
        ECMPFlowSimulator ecmp = new ECMPFlowSimulator();
        ecmp.setup(setting);
        ecmp.computeFlows();
        System.out.println(FlowSimulator.getMaxUtilization(ecmp.getFlow(),setting));
        assert this.simulator.getMaxUtilization() == FlowSimulator.getMaxUtilization(ecmp.getFlow(),setting);
    }

    /*
    @Test
    public void testComputeFlows_consistentWithLpResult_withNoExplicitPathAndNoSRPaths() {
        // optimize default setting with a MIP deterministic optimizer
        Setting setting = warehouse.getDefaultSetting();
        setting.setExplicitPaths(new ExplicitPaths(setting.getTopology()));
        setting.setSRPaths(null);
        MIPWeightOptimizer solver = new MIPWeightOptimizer();

        // optimized and get the objective function as computed by the MIP solver
        solver.solve(setting,5*1000);
        double solverLoad = solver.getOptimizedLoad();

        // get the max link maxLinkLoad as computed by the FlowSimulator
        this.simulator.setup(setting);
        this.simulator.computeFlows();
        double simulatedLoad = simulator.getMaxUtilization();

        // compare the extracted values
        System.out.println("Maximum link maxLinkLoad computed by the solver: " + solverLoad);
        System.out.println("Maximum link maxLinkLoad computed by the Repetita simulator: " + simulatedLoad);
        assert solverLoad >= 0.0 && Math.abs(solverLoad - simulatedLoad) <= 1e-6;
    }*/

    @Test
    public void testInteractionOfExplicitPathsAndEcmp_expectedLinkUtilization_withBasicSquareTwoDemandsOneExplicitPath(){
        Topology square = ToyTopologies.getSquare();
        Demands demands = ToyTopologies.getDiagonalDemandsOnSquare();
        ExplicitPaths paths = new ExplicitPaths(square);
        paths.setPath(0,new int[]{square.getEdgeId("ca"), square.getEdgeId("ab")});

        Setting setting = new Setting();
        setting.setTopology(square);
        setting.setDemands(demands);
        setting.setExplicitPaths(paths);

        this.simulator.setup(setting);
        this.simulator.computeFlows();

        for (int e=0; e<square.nEdges; e++){
            double trafficOnCurrEdge = this.simulator.flowOnEdge(e);
            if (e == square.getEdgeId("ab")) {
                assert trafficOnCurrEdge == 1.5;
            }
            else if (e == square.getEdgeId("ca")) {
                assert trafficOnCurrEdge == 1;
            }
            else if (e == square.getEdgeId("ac") || e == square.getEdgeId("cd") || e == square.getEdgeId("bd")) {
                assert trafficOnCurrEdge == 0.5;
            }
            else{
                assert trafficOnCurrEdge == 0.0;
            }
        }
    }

}
