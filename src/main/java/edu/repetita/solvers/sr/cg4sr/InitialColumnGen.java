package edu.repetita.solvers.sr.cg4sr;

import edu.repetita.solvers.sr.cg4sr.data.Demand;
import edu.repetita.solvers.sr.cg4sr.segmentRouting.SrPath;

import java.util.ArrayList;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public interface InitialColumnGen {

    ArrayList<SrPath> getInitialColumns(ArrayList<Demand> demands);

}
