package edu.repetita.solvers;

import edu.repetita.core.Setting;
import edu.repetita.simulators.FlowSimulator;
import edu.repetita.paths.SRPaths;
import edu.repetita.solvers.sr.MIPTwoSRNoSplit;
import org.junit.Test;

import edu.repetita.Warehouse;

public class MIPTwoSRNoSplitTest {
    /* Variables */

    private final Warehouse warehouse = new Warehouse();
    private final MIPTwoSRNoSplit solver = new MIPTwoSRNoSplit();
    private final FlowSimulator simulator = FlowSimulator.getInstance();

    /* Support methods */
    private double getMaxLinkUtilization(Setting setting){
        this.simulator.setup(setting);
        this.simulator.computeFlows();
        return this.simulator.getMaxUtilization();
    }

    /* Tests */

    @Test
    public void testSolve_setSRPathsAndDecreaseLinkUtilization_max10sec () {
        Setting setting = warehouse.getDefaultSetting();

        SRPaths initialPaths = setting.getSRPaths();
        double initialLoad = this.getMaxLinkUtilization(setting);
        this.solver.solve(setting, 10 * 1000);
        double loadAfter10 = this.getMaxLinkUtilization(setting);
        SRPaths pathsAfter10 = setting.getSRPaths();

        System.out.println("\n*** Optimization results ***");
        System.out.println("initial link utilization: " + initialLoad);
        System.out.println("initial SR paths: " + (initialPaths == null ? "none": "\n" + SRPaths.toString(setting.getTopology(),setting.getDemands(),initialPaths)));
        System.out.println("link utilization after 10 seconds: " + loadAfter10);
        System.out.println("SR paths after 10 seconds (" + pathsAfter10.getPathsWithIntermediateSegments().keySet().size() +
                           " with intermediate segments):\n" + SRPaths.toString(setting.getTopology(),setting.getDemands(),pathsAfter10));

        assert(loadAfter10 < initialLoad);
        assert pathsAfter10.getPathsWithIntermediateSegments().keySet().size() > 0;
    }
}
