package edu.repetita.simulators.specialized;

import edu.repetita.core.Demands;
import edu.repetita.core.Topology;
import edu.repetita.paths.SRPaths;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SegmentRoutingFlowSimulator extends SpecializedFlowSimulator {
    private final ECMPFlowSimulator ecmp = new ECMPFlowSimulator();

    /*
     * Implements abstract method in SpecializedFlowSimulator
     */
    @Override
    public String name() {
        return "sr";
    }

    @Override
    public String getDescription() {
        return "Simulates the traffic distribution induced by specified Segment Routing " +
                "paths with the ecmp routing model followed between consecutive segments " +
                "(assumes exactly one segment routing path for every demand)";
    }

    @Override
    public Collection<String> computeFlows() {
        // extract information from setting
        Topology topology = this.setting.getTopology();
        Demands demands = this.setting.getDemands();
        int nNodes = topology.nNodes;
        StringBuffer currNextHops = new StringBuffer();

        Set<String> simulatedDemands = new HashSet<>();

        // get SR paths from setting
        SRPaths paths = this.setting.getSRPaths();

        // re-initialize traffic matrix
        double[][] traffic = new double[nNodes][nNodes];
        for (int source = 0; source < nNodes; source++) {
            for (int dest = 0; dest < nNodes; dest++) {
                traffic[source][dest] = 0.0;
            }
        }

        // compute the new traffic matrix, splitting demand so as to match SR paths
        if(paths != null) {
            for (int demand = 0; demand < demands.nDemands; demand++) {
                // if the demand has to be ignored, do nothing
                if(this.demandsToIgnore.contains(demands.label[demand])){
                    continue;
                }

                // if there is an SR path, split the demand in sub-demands
                double amount = demands.amount[demand];
                currNextHops.append("\nDestination " + topology.nodeLabel[demands.dest[demand]] + "\nsequence of middlepoints: ");
                if (paths.getPath(demand) != null) {
                    int positions = paths.getPathLength(demand) - 1;
                    for (int position = 0; position < positions; position++) {
                        int subSrc = paths.getPathElement(demand, position);
                        int subDest = paths.getPathElement(demand, position + 1);
                        traffic[subSrc][subDest] += amount;
                        currNextHops.append(topology.nodeLabel[subSrc] + " -> ");
                        if (position == positions - 1){
                            currNextHops.append(topology.nodeLabel[demands.dest[demand]] + "\n");
                        }
                    }
                    simulatedDemands.add(demands.label[demand]);
                }
            }
        }

        // compute ECMP paths for the traffic matrix reflecting SR paths
        this.flow = ecmp.computeTrafficDistribution(topology, traffic);

        // store the current SR paths
        this.nextHops = currNextHops.toString();

        return simulatedDemands;
    }

}
