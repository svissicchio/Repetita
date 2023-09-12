package edu.repetita.simulators.specialized;

import edu.repetita.core.Setting;
import edu.repetita.core.Topology;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class SpecializedFlowSimulator {
    Setting setting;
    double[] flow;
    Set<String> demandsToIgnore = new HashSet<>();
    String nextHops;

    /**
     * Must return the name of the specific flow simulator.
     */
    public abstract String name();

    /**
     * Must returns the description of the specific flow simulator.
     */
    public abstract String getDescription();

    /**
     * Must compute traffic distribution for the setting provided in input through
     * the setup method.
     * Such traffic distribution must be stored in the double[] flow variable.
     * Also, next hops computed here are stored in the nextHops variable.
     *
     * @return a collection of identifiers for the demands whose traffic distribution has been simulated
     */
    public abstract Collection<String> computeFlows();

    /**
     * Sets the topology to be used for computing traffic distribution.
     */
    public void setup(Setting setting, Set<String> demandsToIgnore){
        this.setting = setting;
        this.demandsToIgnore = demandsToIgnore;
    }

    /**
     * Sets the topology to be used for computing traffic distribution.
     */
    public void setup(Setting setting){
        this.setting = setting;
    }


    /**
     * Returns the configured setting (e.g., used by visualizers).
     */
    public Topology getTopology(){
        return this.setting.getTopology();
    }

    /**
     * Returns the array of all link utilizations
     */
    public double[] getFlow() {return this.flow;}

    /**
     * Resets the traffic (maxLinkLoad) on every edge to zero.
     *
     * @param load vector containing information about the maxLinkLoad per edge
     */
    void resetEdgeLoad(double[] load) { for (int edge = 0; edge < load.length; edge++) load[edge] = 0; }

    /**
     * Returns a StringBuffer description of the computed paths
     */
    public String getNextHops() {return this.nextHops; }
}
