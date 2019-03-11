package edu.repetita.main;

import java.util.*;

import edu.repetita.core.Scenario;
import edu.repetita.core.Setting;
import edu.repetita.core.Solver;
import edu.repetita.io.RepetitaParser;
import edu.repetita.io.IOConstants;
import edu.repetita.scenarios.ScenarioFactory;
import edu.repetita.solvers.SolverFactory;

public class RepetitaStorage {

	private static RepetitaStorage instance = null;

    private Map<String,Solver> solvers;
	private Map<String,Scenario> scenarios;

	/* Constructor methods */

	// private constructor to defeat instantiation
	private RepetitaStorage(){
        this.loadSolvers();
        this.loadScenarios();
	}
	
	public static RepetitaStorage getInstance() {
		if(instance == null) {
			instance = new RepetitaStorage();
		}
		return instance;
	}

    /* Getter and setter methods */

    public Set<String> getSolverIDs(){
        return this.solvers.keySet();
    }

    public Solver getSolver(String solverID){
        return this.solvers.get(solverID);
    }

    public Set<String> getScenarioIDs() {
        return this.scenarios.keySet();
    }

    public Scenario newScenario(String scenarioID) {
        return this.scenarios.get(scenarioID);
    }

    public Scenario newScenario(String scenarioName, Setting setting, Solver solver){
        Scenario scenario = this.newScenario(scenarioName);
        scenario.setup(setting, solver);
        return scenario;
    }

    public Setting newSetting(Solver solver, String graphFilename, String demandsFilename){
        Setting setting = solver.getSetting();
        setting.setTopologyFilename(graphFilename);
        setting.setDemandsFilename(demandsFilename);
        return setting;
    }

    public Setting newSetting(Solver solver, String graphFilename, String demandsFilename,
                              List<String> otherDemandsFilenames, Map<String, Object> extras){
        Setting setting = this.newSetting(solver, graphFilename,demandsFilename);
        setting.setDemandChangesFilenames(otherDemandsFilenames);
        setting.setExtras(extras);
        return setting;
    }

	private void loadSolvers(){

        this.solvers = SolverFactory.getAllInternalSolvers();

        Map<String,Map<String,String>> featureMap = RepetitaParser.parseExternalSolverFeatures(IOConstants.SOLVER_SPECSFILE);
        for (String extName: featureMap.keySet()){
            Solver extSolver = SolverFactory.getExternalSolver(featureMap.get(extName));
            if (extSolver != null) {
                this.solvers.put(extName, extSolver);
            }
        }
	}

	private void loadScenarios() {
	    this.scenarios = ScenarioFactory.getAllScenarios();
	}
}
