package edu.repetita.simulators.specialized;

import edu.repetita.core.Demands;
import edu.repetita.core.Topology;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.paths.ExplicitPaths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExplicitPathFlowSimulator extends SpecializedFlowSimulator {

    /*
     * Implements abstract method in SpecializedFlowSimulator
     */
    @Override
    public String name() {
        return "explicit";
    }

    @Override
    public String getDescription() {
        return "Simulates how traffic is distributed over links in the presence of " +
                "explicitly defined paths, like MPLS tunnels or static/OpenFlow routes " +
                "(assumes exactly one explicit path for each demand).";
    }

    /*
     * Implements abstract method in SpecializedFlowSimulator
     */
    @Override
    public Collection<String> computeFlows() {
        List<String> simulatedDemands = new ArrayList<>();
        StringBuffer currNextHops = new StringBuffer("");

        // extract information from setting
        Topology topology = this.setting.getTopology();
        Demands demands = this.setting.getDemands();
        int nEdges = topology.nEdges;

        // reset per-edge maxLinkLoad
        this.flow = new double[nEdges];
        this.resetEdgeLoad(this.flow);

        // get explicit paths from setting
        ExplicitPaths paths = this.setting.getExplicitPaths();
        if (paths == null){
            RepetitaWriter.appendToOutput("No explicit paths set!",2);
            this.nextHops = "";
            return simulatedDemands;
        }

        // for each demand D, add traffic volume to every edge in the explicit path set for D
        for (int demand = 0; demand < demands.nDemands; demand++) {
            if(this.demandsToIgnore.contains(demands.label[demand])){
                continue;
            }

            int[] pathEdges = paths.getPath(demand);
            if (pathEdges == null || pathEdges.length == 0){
                continue;
            }

            currNextHops.append("\nDestination " + topology.nodeLabel[demands.dest[demand]] + "\n");

            double amount = demands.amount[demand];
            for (int edge : pathEdges) {
                this.flow[edge] += amount;
                currNextHops.append("node: " + topology.nodeLabel[topology.edgeSrc[edge]] + ", next hops: [" + topology.nodeLabel[topology.edgeDest[edge]] + "]\n");
            }

            simulatedDemands.add(demands.label[demand]);
        }

        this.nextHops = currNextHops.toString();

        return simulatedDemands;
    }
}
