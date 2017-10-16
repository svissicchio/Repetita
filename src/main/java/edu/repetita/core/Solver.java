package edu.repetita.core;

import edu.repetita.io.IOConstants;

/**
 * Basic interface that any solvers must implement.
 * This interface can be extended to enable specific experiments and analyses on given families of algorithms
 * (e.g., see SRSolver and scenarios for algorithms based on Segment Routing)
 */
public abstract class Solver {
    protected int verbose = 1;
    protected int objective = IOConstants.SOLVER_OBJVALUES_DFLT;
    private String[] possibleObjectives = IOConstants.SOLVER_OBJVALUES;

    public Solver(){
        this.setObjective();
    }

    /**
     * Solvers must implement this method to specify the value of the objective variable
     * (as the index of the possibleObjectives array which reflects the optimization performed)
     */
    protected abstract void setObjective();

    /**
     * Sets the verbosity level
     * @param level the verbosity level to be set
     */
    public void setVerbose(int level) { verbose = level; }

	/**
	 * @return the name of the solver
	 */
	public abstract String name();

    /**
     * @return the intuitive description of what the solver optimizes and how
     */
	public abstract String getDescription();

    /**
     * @return a textual description of the optimization objective
     */
	public String getOptimizationObjective() { return possibleObjectives[objective]; }

	/**
	 *  Optimizes the routing configuration, modifying the setting object in-place
	 */
	public abstract void solve(Setting setting, long milliseconds);

	/**
	 * @return the time needed by the last call to solve(), in nanoseconds 
	 */
	public abstract long solveTime(Setting setting);
}
