package edu.repetita.core;

import edu.repetita.analyses.Analysis;
import edu.repetita.analyses.Analyzer;
import edu.repetita.io.RepetitaWriter;

import java.util.HashMap;
import java.util.Map;

/*
 * Interface for an object defining specific analyses to be performed across given TE algorithms.
 */
public abstract class Scenario {
    protected Setting setting;
    protected Solver solver;
    protected Analyzer analyzer = Analyzer.getInstance();

    protected boolean keepAnalyses = false;
    protected Map<String,Analysis> analyses = new HashMap<>();

    /**
     * Returns the name of this scenario
     */
    public String name() {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns the description of what this scenario does
     */
    public abstract String getDescription();

    /**
	 * Sets basic data needed by all scenarios.
	 */
	public void setup(Setting setting, Solver solver){
        this.setting = setting;
        this.solver = solver;
    }

    /**
     * Internal method that all sub-classes of Scenario *should* use to print
     */
    protected void print(String content){
        RepetitaWriter.appendToOutput(content);
    }

    /**
     * Used to activate tracking of the analyses performed by a scenario (for testing)
     */
    public void keepAnalysesWhileRun(){
        this.keepAnalyses = true;
    }

    /**
     * Returns the analyses performed by a scenario if keepAnalysesWhileRun was called before run (for testing)
     */
    public Map<String,Analysis> getAnalyses(){
        return this.analyses;
    }

	/**
	 * Runs experiments and performs analyses defined by the scenario.
	 */
	public abstract void run(long timeStabilizationMillis);

}
