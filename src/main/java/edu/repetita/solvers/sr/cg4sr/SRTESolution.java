package edu.repetita.solvers.sr.cg4sr;

import edu.repetita.io.RepetitaWriter;
import edu.repetita.solvers.sr.cg4sr.data.Demand;
import edu.repetita.solvers.sr.cg4sr.data.DemandPathMap;
import edu.repetita.solvers.sr.cg4sr.data.Pair;
import edu.repetita.solvers.sr.cg4sr.segmentRouting.SrPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


/**
 * @author Francois Aubry f.aubry@uclouvain.be
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class SRTESolution {

    private HashMap<Demand, ArrayList<Pair<SrPath, Double>>> map;
    private double objectiveValue;
    private DemandPathMap demandPathMap;

    public SRTESolution(double objectiveValue) {
        this.objectiveValue = objectiveValue;
        map = new HashMap<>();
    }

    public void addPath(Demand demand, SrPath path, double factor) {
        ArrayList<Pair<SrPath, Double>> paths = map.get(demand);
        if(paths == null) {
            paths = new ArrayList<>();
            map.put(demand, paths);
        }
        paths.add(new Pair<>(path, factor));
    }

    public boolean isDemandCovered(Demand demand) {
        return map.containsKey(demand);
    }

    public ArrayList<SrPath> getPaths(Demand demand) {
        ArrayList<SrPath> paths = new ArrayList<>();
        for(Pair<SrPath, Double> path : map.get(demand)) {
            paths.add(path.x());
        }
        return paths;
    }

    public ArrayList<Pair<SrPath, Double>> getPathsAndFactors(Demand demand) {
        return map.get(demand);
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    public void setDemandPathMap(DemandPathMap demandPathMap) {
        this.demandPathMap = demandPathMap;
    }

    public DemandPathMap getDemandPathMap() {
        return this.demandPathMap;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int maxSegments = 0;
        for(Entry<Demand, ArrayList<Pair<SrPath, Double>>> e : map.entrySet()) {
            sb.append(e.getKey().toString() + "\n");
            for(Pair<SrPath, Double> paths : e.getValue()) {
                maxSegments = maxSegments < paths.x().size() - 2 ? paths.x().size() - 2 : maxSegments;
                sb.append(" " + paths.x() + "\t" + paths.y() + "\n");
            }
        }
        sb.append("max seg: " + maxSegments + "\n");
        sb.append("Number of demands found: " + map.entrySet().size() + "\n");
        return sb.toString();
    }

    public void print() {
        int maxSegments = 0;
        for(Entry<Demand, ArrayList<Pair<SrPath, Double>>> e : map.entrySet()) {
            RepetitaWriter.appendToOutput(e.getKey().toString(), 3);
            for(Pair<SrPath, Double> paths : e.getValue()) {
                maxSegments = maxSegments < paths.x().size() - 2 ? paths.x().size() - 2 : maxSegments;
                RepetitaWriter.appendToOutput(" " + paths.x() + "\t" + paths.y(), 3);
            }
        }
        RepetitaWriter.appendToOutput("At most " + maxSegments + " intermediate segments are used to represent the paths", 2);
    }

    public ArrayList<Pair<SrPath, Double>> findDemandPath(int src, int dst, int volume) {
        return map.get(new Demand(src, dst, volume, 0));
    }

}
