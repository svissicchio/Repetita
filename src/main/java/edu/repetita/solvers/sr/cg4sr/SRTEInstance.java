package edu.repetita.solvers.sr.cg4sr;

import edu.repetita.solvers.sr.cg4sr.data.Demand;
import edu.repetita.solvers.sr.cg4sr.data.EqualSplit;
import edu.repetita.solvers.sr.cg4sr.data.Graph;
import edu.repetita.solvers.sr.cg4sr.data.NetworkEdge;

import java.util.ArrayList;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class SRTEInstance {

    private Graph<NetworkEdge> g;
    private ArrayList<Demand> initialDemands, demands;
    private int maxSeg;
    private double keepPercentage;
    private EqualSplit<NetworkEdge> split;
    private long totalDemand;

    public SRTEInstance(Graph<NetworkEdge> g, ArrayList<Demand> initialDemands, int maxSeg, double keepPercentage) {
        this.g = g;
        this.initialDemands = initialDemands;
        this.maxSeg = maxSeg;
        this.keepPercentage = keepPercentage;
        split = new EqualSplit<>(g);
        // pre-process the demands by removing infeasible demands and keeping only keepPercetange of demands
        totalDemand = 0;
        ArrayList<Demand> tmp = new ArrayList<>();
        for(Demand demand : initialDemands) {
            totalDemand += demand.volume;
            tmp.add(demand);
        }
        demands = new ArrayList<>();
        demands.addAll(initialDemands);

        for(int i = 0; i < demands.size(); i++) {
            demands.get(i).index = i;
        }
    }

    public long getTotalDemand() {
        return totalDemand;
    }

    public int nbDemands() {
        return demands.size();
    }

    public int getMaxSeg() {
        return maxSeg;
    }

    public NetworkEdge getCriticalEdge(int x, int y) {
        return split.getCriticalEdge(x, y);
    }

    public ArrayList<Demand> getDemands() {
        return demands;
    }

    public ArrayList<NetworkEdge> getEdges() {
        return g.edges();
    }

    public int nbEdges() {
        return g.edges().size();
    }

    public int nbNodes() {
        return g.V();
    }

    public Graph<NetworkEdge> getGraph() {
        return g;
    }

    public EqualSplit<NetworkEdge> getSplit() {
        return split;
    }

    public double getSplitValue(int x, int y, NetworkEdge e) {
        return split.getValue(x, y, e);
    }

    public Demand getDemand(int demandIndex) {
        return demands.get(demandIndex);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----INSTANCE-----\n");
        sb.append(String.format("topology: %s\n", g.name()));
        sb.append(String.format("V=%d, E=%d\n", g.V(), g.E()));
        sb.append(String.format("%d / %d demands kept (%d%% of total feasible demand)\n", demands.size(), initialDemands.size(), (int)(100 * keepPercentage)));
        sb.append("-----------------\n");
        return sb.toString();
    }

}
