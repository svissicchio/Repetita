package edu.repetita.solvers;

import edu.repetita.core.Setting;
import edu.repetita.simulators.FlowSimulator;
import edu.repetita.paths.SRPaths;
import edu.repetita.solvers.sr.DefoCP;
import org.junit.Test;

import edu.repetita.Warehouse;

public class DefoCPTest {

    /* Variables */
    private final Warehouse warehouse = new Warehouse();
    private final DefoCP solver = new DefoCP();
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
        this.solver.solve(setting, 5 * 1000);
        double loadAfter5 = this.getMaxLinkUtilization(setting);
        SRPaths pathsAfter5 = setting.getSRPaths();

        System.out.println("\n*** Optimization results ***");
        System.out.println("initial link utilization: " + initialLoad);
        System.out.println("initial SR paths: " + (initialPaths == null ? "none": "\n" + SRPaths.toString(setting.getTopology(),setting.getDemands(),initialPaths)));
        System.out.println("link utilization after 5 seconds: " + loadAfter5);
        System.out.println("SR paths after 5 seconds (" + pathsAfter5.getPathsWithIntermediateSegments().keySet().size() +
                           " with intermediate segments):\n" + SRPaths.toString(setting.getTopology(),setting.getDemands(),pathsAfter5));

        assert(loadAfter5 < initialLoad);
        assert(pathsAfter5 != null && pathsAfter5.getPathsWithIntermediateSegments().keySet().size() > 0);
    }

    @Test
    public void testSolve_efficientSolution_targetObjectiveEquals1 () {
        Setting setting = warehouse.getDefaultSetting();
        long maxTimeMillis = 5 * 1000;
        long maxTimeNano = maxTimeMillis * 1000000;

        this.solver.setTargetObjectiveValue(1);
        this.solver.solve(setting, maxTimeMillis);

        long solveTime = this.solver.solveTime(setting);
        double optLoad = this.getMaxLinkUtilization(setting);

        System.out.println("\n*** Optimization results with target objective value set to 1 ***");
        System.out.println("link utilization after 5 seconds: " + optLoad);
        System.out.println("optimization time: " + solveTime / 1000000 + " ms (maximum allowed " + maxTimeMillis + " ms)");

        assert optLoad <= 1;
        assert optLoad > 0.9;
        assert solveTime < maxTimeNano;
    }
}
