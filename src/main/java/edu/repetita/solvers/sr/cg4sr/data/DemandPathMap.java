package edu.repetita.solvers.sr.cg4sr.data;

import edu.repetita.solvers.sr.cg4sr.SRTEColGenModel;
import edu.repetita.solvers.sr.cg4sr.SRTEInstance;
import edu.repetita.solvers.sr.cg4sr.segmentRouting.SrPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class DemandPathMap {

    private SRTEColGenModel model;
    private SRTEInstance instance;
    private HashMap<Demand, ArrayList<SrPath>> M;
    private int size;

    public DemandPathMap(SRTEColGenModel model, SRTEInstance instance) {
        this.model = model;
        this.instance = instance;
        M = new HashMap<>();
        for(Demand demand : instance.getDemands()) {
            M.put(demand, new ArrayList<>());
        }
        size = 0;
    }

    public void addPaths(ArrayList<SrPath> paths) {
        for(SrPath path : paths) addPath(path, true);
    }

    public void addPath(SrPath path) {
        addPath(path, false);
    }

    public void addPath(SrPath path, boolean keepExceedingCapaPath) {
        for(Entry<Demand, ArrayList<SrPath>> e : M.entrySet()) {
            if(e.getKey().isCompatibleWith(path, instance.getEdges(), instance.getSplit(),
                    model.getCapacityFactor(), keepExceedingCapaPath)) {
                e.getValue().add(path);
                size++;
            }
        }
    }

    public int size() {
        return size;
    }

    public ArrayList<SrPath> getCompatiblePaths(Demand demand) {
        return M.get(demand);
    }

    public int nbCompatiblePaths(Demand demand) {
        return M.get(demand).size();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Entry<Demand, ArrayList<SrPath>> e : M.entrySet()) {
            sb.append(e.getKey().toString() + '\n');
            for(SrPath path : e.getValue()) {
                sb.append('\t' + path.toString() + '\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

}
