package edu.repetita.solvers;

import edu.repetita.core.Setting;
import edu.repetita.core.Solver;
import edu.repetita.core.Topology;
import edu.repetita.io.IOConstants;
import edu.repetita.simulators.FlowSimulator;
import edu.repetita.solvers.SolverFactory;
import org.junit.Test;

import edu.repetita.Warehouse;
import java.util.HashMap;
import java.util.Map;

public class SolversTest {
    private Warehouse warehouse = new Warehouse();

    // Helper methods
    private double getMaxLoad(Setting setting){
        FlowSimulator simulator = FlowSimulator.getInstance();
        simulator.setup(setting);
        simulator.computeFlows();
        return simulator.getMaxUtilization();
    }

    private Map<String,Setting> runAllInternalLoadOptimizationSolvers(long timeLimitInSec){
        Setting setting = warehouse.getDefaultSetting();
        Map<String,Setting> optimizedSettings = new HashMap<>();

        for(Solver solver: SolverFactory.getAllInternalSolvers().values()){
            // skip all the solvers that have an optimization objective different from min max link utilization
            if(solver.getOptimizationObjective().equals(IOConstants.SOLVER_OBJVALUES[1])){
                // clone the setting, to avoid that changes made by one solver propagate to the following
                Setting settingCopy = setting.clone();
                System.out.println("\nTesting solver " + solver.name());
                solver.solve(settingCopy,timeLimitInSec * 1000);

                // assert that IGP weights are integers
                Topology topology = settingCopy.getTopology();
                for (int edge = 0; edge < topology.nEdges; edge++) {
                    int weight = topology.edgeWeight[edge];
                    assert weight >= 0;
                }

                // store the optimized setting
                optimizedSettings.put(solver.name(),settingCopy);
            }
        }

        return optimizedSettings;
    }

    @Test
    public void testRun_noErrors_defaultSettingAllInternalSolvers () {
        this.runAllInternalLoadOptimizationSolvers(1);
    }

    @Test
    public void testRun_checkSolversReduceMaxLinkUtilization_defaultSettingAllInternalLoadOptimizationSolvers () {
        double initLoad = this.getMaxLoad(warehouse.getDefaultSetting());
        System.out.println("\n\nOptimization results\ninitial max link utilization: " + initLoad);
        Map<String,Setting> optimizedSettings = this.runAllInternalLoadOptimizationSolvers(5);
        for (String solverName: optimizedSettings.keySet()){
            Setting optSetting = optimizedSettings.get(solverName);
            double optLoad = this.getMaxLoad(optSetting);
            System.out.println("Max link utilization after optimization from solver " + solverName + ": " + optLoad);
            assert optLoad < initLoad;
        }
    }

}
