package tests.java.edu.repetita;

import edu.repetita.core.Setting;
import edu.repetita.core.Solver;
import edu.repetita.main.RepetitaStorage;

import java.util.Arrays;
import java.util.List;

/**
 * Class collecting default objects (filenames, settings, scenarios, etc.) ready to use for tests.
 */
public class Warehouse {

    private RepetitaStorage storage;

    private String defaultTopologyFile = "data/2016TopologyZooUCL_inverseCapacity/Abilene.graph";
    private String defaultDemandFile = "data/2016TopologyZooUCL_inverseCapacity/Abilene.0000.demands";
    private String defaultScenarioName = "SingleSolverRun";
    private String defaultSolverName = "SRLS";
    private String deterministicSolverName = "MIPTwoSRNoSplit";

    private List<String> defaultDemandChangeFiles = Arrays.asList(
            "data/2016TopologyZooUCL_inverseCapacity/Abilene.0001.demands",
            "data/2016TopologyZooUCL_inverseCapacity/Abilene.0002.demands"
    );

    public String getDefaultTopologyFile() {
        return defaultTopologyFile;
    }

    public String getDefaultDemandFile() {
        return defaultDemandFile;
    }

    public String getDefaultScenarioName() {
        return defaultScenarioName;
    }

    public String getDefaultSolverName() {
        return defaultSolverName;
    }

    public List<String> getDefaultDemandChangeFiles() { return this.defaultDemandChangeFiles; }

    public Solver getDefaultSolver() {
        this.initializeStorage();
        return this.storage.getSolver(this.defaultSolverName);
    }

    public Solver getDeterministicSolver() {
        this.initializeStorage();
        return this.storage.getSolver(this.deterministicSolverName);
    }

    public Setting getDefaultSetting() {
        this.initializeStorage();
        return this.storage.newSetting(this.defaultTopologyFile, this.defaultDemandFile, this.defaultDemandChangeFiles);
    }

    // initialize storage only when strictly necessary
    // _Note_ that this allows tests to check verbosity of storage creation
    private void initializeStorage(){
        if (this.storage == null){
            this.storage = RepetitaStorage.getInstance();
        }
    }
}
