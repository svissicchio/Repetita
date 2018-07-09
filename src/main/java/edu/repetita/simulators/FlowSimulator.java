package edu.repetita.simulators;

import edu.repetita.core.Setting;
import edu.repetita.simulators.specialized.ECMPFlowSimulator;
import edu.repetita.simulators.specialized.ExplicitPathFlowSimulator;
import edu.repetita.simulators.specialized.SegmentRoutingFlowSimulator;
import edu.repetita.simulators.specialized.SpecializedFlowSimulator;

import java.util.*;

public class FlowSimulator {
    // class variable
    private static FlowSimulator instance = new FlowSimulator();

    private FlowSimulator() {
        // add all specialized flow simulators, in order of priority
        this.simulators.add(new ExplicitPathFlowSimulator());
        this.simulators.add(new SegmentRoutingFlowSimulator());
        this.simulators.add(new ECMPFlowSimulator());
    }

    public static FlowSimulator getInstance(){
        return instance;
    }

    // instance variables
    private List<SpecializedFlowSimulator> simulators = new ArrayList<>();
    private Setting setting;
    public double[] flow;
    public StringBuffer nextHops;

    // getter methods
    public Setting getSetting(){ return this.setting; }

    // to make priority and support for technologies configurable
    public void setSimulators(List<SpecializedFlowSimulator> simulators) {
        this.simulators = simulators;
    }

    /**
     * Assign the input setting to this object.
     * From now on, this setting is the one considered when calling all the main methods (computeFlows, getMaxUtilization, etc.) of this FlowSimulator.
     * @param setting the setting on which to simulate how traffic flows
     */
    public void setup(Setting setting){
        this.setting = setting;
        this.computeFlows();
    }

    /**
     * Returns the next-hops for each source-destination pair.
     */
    public String getNextHops(){
        return this.nextHops.toString();
    }

    /**
     * Internally computes and stores in private variables traffic distribution and next-hops
     * for the setting provided in input through the setup method.
     */
    public void computeFlows(){
        Set<String> simulatedDemands = new HashSet<>();
        this.flow = new double[this.setting.getTopology().nEdges];
        this.nextHops = new StringBuffer();
        int priority = 1;

        // simulate the different parts of the configuration using the specialized simulators one by one
        for (SpecializedFlowSimulator sim: this.simulators){
            // compute flows
            sim.setup(this.setting, simulatedDemands);
            Collection<String> justSimulated = sim.computeFlows();
            simulatedDemands.addAll(justSimulated);

            // update maxLinkLoad on each edge
            for(int e = 0; e < this.flow.length; e++){
                this.flow[e] += sim.getFlow()[e];
            }

            // update next-hop string
            this.nextHops.append("\n***Next hops priority " + priority + " (" + sim.name() + " paths)***\n" + sim.getNextHops());
            priority++;
        }
    }

    /**
     * Returns the traffic on the input edge.
     *
     * @param edge an edge of the topology, should be {@code 0 <= edge < topology.nEdges}
     * @return a non-negative amount of flow on edge
     */
    public double flowOnEdge(int edge) {
        return this.flow[edge];
    }

    /**
     * Returns the traffic on the input edge according to the flow structure.
     *
     * @param flow an array of link utilization per edge
     * @param edge an edge of the topology, should be {@code 0 <= edge < topology.nEdges}
     * @return a non-negative amount of flow on edge
     */
    public static double flowOnEdge(double[] flow, int edge) {
        return flow[edge];
    }

    /**
     * Returns the value of the maximally utilized edges.
     *
     * @return a non-negative utilization of edge
     */
    public double getMaxUtilization(){
        return getMaxUtilization(this.flow, this.setting);
    }

    /**
     * Returns the value of the maximally utilized edges.
     *
     * @return a non-negative utilization of edge
     */
    public static double getMaxUtilization(double[] flow, Setting setting){
        double maxUtil = 0.0;
        for (int edge = 0; edge < flow.length; edge++) {
            maxUtil = Math.max(maxUtil, flowOnEdge(flow,edge) / setting.getTopology().edgeCapacity[edge]);
        }
        return maxUtil;
    }

}
