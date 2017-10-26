package edu.repetita.solvers;

import edu.repetita.core.Setting;
import edu.repetita.simulators.FlowSimulator;
import edu.repetita.solvers.wo.TabuIGPWO;
import org.junit.Test;

import edu.repetita.Warehouse;

public class TabuLSTest {

    /* Variables */
    private final Warehouse warehouse = new Warehouse();
    private final TabuIGPWO solver = new TabuIGPWO();
    private final FlowSimulator simulator = FlowSimulator.getInstance();

    /* Support methods */
    private double getMaxLinkUtilization(Setting setting){
        this.simulator.setup(setting);
        this.simulator.computeFlows();
        return this.simulator.getMaxUtilization();
    }

    /* Tests */

    @Test
    public void testSolve_decreaseLinkUtilization_1and5and10sec () {
        Setting setting = warehouse.getDefaultSetting();

        double initialLoad = this.getMaxLinkUtilization(setting);
        this.solver.solve(setting, 1 * 1000);
        double loadAfter1 = this.getMaxLinkUtilization(setting);
        this.solver.solve(setting, 5 * 1000);
        double loadAfter5 = this.getMaxLinkUtilization(setting);
        this.solver.solve(setting, 10 * 1000);
        double loadAfter10 = this.getMaxLinkUtilization(setting);

        System.out.println("\n*** Optimization results ***");
        System.out.println("initial link utilization: " + initialLoad);
        System.out.println("link utilization after 1 second: " + loadAfter1);
        System.out.println("link utilization after 5 seconds: " + loadAfter5);
        System.out.println("link utilization after 10 seconds: " + loadAfter10);

        assert(loadAfter1 < initialLoad);
        assert(loadAfter5 <= loadAfter1);
        assert(loadAfter10 <= loadAfter5);
    }

}
