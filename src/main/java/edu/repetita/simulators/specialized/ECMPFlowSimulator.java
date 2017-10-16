package edu.repetita.simulators.specialized;

import edu.repetita.core.Demands;
import edu.repetita.core.Topology;
import edu.repetita.paths.ShortestPaths;

import java.util.Arrays;
import java.util.Collection;

public class ECMPFlowSimulator extends SpecializedFlowSimulator {

    /*
     * Implements abstract method in SpecializedFlowSimulator
     */
    @Override
    public String name() {
        return "ecmp";
    }

    @Override
    public String getDescription() {
        return "For each pair of traffic source and destination, simulates the traffic " +
                "distribution corresponding to equal load balancing packets across all " +
                "shortest paths from the source to the destination.";
    }

    /*
     * Implements abstract method in SpecializedFlowSimulator, feeding the computeTrafficDistribution method
     */
    @Override
    public Collection<String> computeFlows() {
        Topology topology = this.setting.getTopology();
        Demands demands = this.setting.getDemands();
        double[][] traffic = Demands.toTrafficMatrix(demands, topology.nNodes, this.demandsToIgnore);
        this.flow = this.computeTrafficDistribution(topology, traffic);
        return Arrays.asList(demands.label);
    }

    /*
     * Simulates distribution of traffic on shortest paths, equally splitting demands in the case
     * of multiple shortest paths for the same source-destination pair.
     */
    double[] computeTrafficDistribution(Topology topology, double[][] traffic) {
        int nNodes = topology.nNodes;
        int nEdges = topology.nEdges;

        // initialize variables to store traffic distribution
        double[] toRoute = new double[nNodes];
        double[] distribution = new double[nEdges];
        this.resetEdgeLoad(distribution);

        // computing paths
        ShortestPaths sp = new ShortestPaths(topology);
        sp.computeShortestPaths();

        // simulate flows per destination: for every dest, add passing flows on edges to the flow variable
        for (int dest = 0; dest < nNodes; dest++) {
            // put amounts to route at every node
            for (int node = 0; node < nNodes; node++) {
                toRoute[node] = traffic[node][dest];
            }

            // push flow by topological order from farthest to closest using all shortest paths
            int[] ordering = sp.topologicalOrdering;
            int nOrdering = sp.makeTopologicalOrdering(dest);

            // visit nodes in far -> dest order, which is reverse of ordering.
            while (--nOrdering >= 0) {
                int node = ordering[nOrdering];

                int pSucc = sp.nSuccessors[dest][node];
                double amountToRoute = toRoute[node] / pSucc;  // spread flow evenly, if pSucc == 0 then amountToRoute is NaN
                while (--pSucc >= 0) {
                    int succNode = sp.successorNodes[dest][node][pSucc];
                    toRoute[succNode] += amountToRoute;

                    int succEdge = sp.successorEdges[dest][node][pSucc];
                    distribution[succEdge] += amountToRoute;
                }

                toRoute[node] = 0.0;
            }
        }

        return distribution;
    }
}
