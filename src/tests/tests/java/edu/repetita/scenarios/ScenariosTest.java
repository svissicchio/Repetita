package tests.java.edu.repetita.scenarios;

import edu.repetita.core.Scenario;
import edu.repetita.core.Setting;
import edu.repetita.core.Solver;
import edu.repetita.scenarios.ScenarioFactory;
import org.junit.Before;
import org.junit.Test;
import tests.java.edu.repetita.Warehouse;

import java.util.*;

public class ScenariosTest {

    /* Variables */
    private Warehouse warehouse = new Warehouse();
    private Collection<Scenario> scenarios;

    /* Tests */

    @Before
    public void setup (){
        this.scenarios = ScenarioFactory.getAllScenarios().values();
    }

    @Test
    public void testRun_noErrors_defaultWarehouseObjects () {
        Solver solver = warehouse.getDefaultSolver();
        long timeLimitInSec = 1;

        // run all scenarios, one by one
        for(Scenario s: this.scenarios){
            // get a new default setting and setup the scenario
            Setting setting = warehouse.getDefaultSetting().clone();
            s.setup(setting, solver);

            // run the scenario
            System.out.println("\nTesting scenario " + s.getClass().getSimpleName());
            s.run(timeLimitInSec * 1000);
        }
    }

}
