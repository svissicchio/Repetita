package edu.repetita.solvers.sr.cg4sr;

import edu.repetita.solvers.sr.cg4sr.data.Demand;
import edu.repetita.solvers.sr.cg4sr.segmentRouting.SrPath;

import java.util.ArrayList;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class SourceDestColInit implements InitialColumnGen {

    public SourceDestColInit() {}

    public ArrayList<SrPath> getInitialColumns(ArrayList<Demand> demands) {
        ArrayList<SrPath> paths = new ArrayList<>();
        for(Demand demand : demands) {
            SrPath path = new SrPath();
            path.add(demand.src);
            path.add(demand.dst);
            paths.add(path);
        }
        return paths;
    }

}
