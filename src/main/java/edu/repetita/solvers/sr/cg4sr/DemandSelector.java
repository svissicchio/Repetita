package edu.repetita.solvers.sr.cg4sr;

import edu.repetita.solvers.sr.cg4sr.data.Demand;

import java.util.ArrayList;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public interface DemandSelector {

	public void init(ArrayList<Demand> demands);

	/**
	 * @return The demand index to give to the pricing problem.
	 * If all the demands were considered, -1 is returned.
	 */
	public int getNextDemand();

	/**
	 * To be called at the end of the column generation step
	 */
	public void endSelectionStep();

	public boolean hasNext();

}
