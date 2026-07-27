package edu.repetita.solvers.sr.rls;

import edu.repetita.solvers.sr.rls.core.Topology;
import edu.repetita.solvers.sr.rls.io.DemandParser;
import edu.repetita.solvers.sr.rls.io.DemandsData;
import edu.repetita.solvers.sr.rls.io.PathsData;
import edu.repetita.solvers.sr.rls.io.PathsParser;
import edu.repetita.solvers.sr.rls.io.TopologyData;
import edu.repetita.solvers.sr.rls.io.TopologyParser;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Checker {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: Checker input_stem [timeLimit in seconds]");
            System.out.println("Needs .graph, .demands, and .paths files.");
            System.exit(1);
        }

        boolean debug = true;
        String fileName = args[0];
        int timeLimit = (args.length > 1) ? Integer.parseInt(args[1]) : 10;

        TopologyData topologyData = TopologyParser.parse(fileName + ".graph");
        Topology topology = Topology.apply(topologyData);
        DemandsData demandsData = DemandParser.parse(fileName + ".demands");
        PathsData pathsData = PathsParser.parse(fileName + ".paths", topologyData, demandsData);
        int nNodes = topology.nNodes;

        ShortestPaths sp = new ShortestPaths(topology, topologyData.edgeWeights);

        @SuppressWarnings("unchecked")
        Deque<Integer>[] demandSourcePerDestination = new Deque[nNodes];
        @SuppressWarnings("unchecked")
        Deque<Double>[] demandAmountPerDestination = new Deque[nNodes];
        for (int i = 0; i < nNodes; i++) {
            demandSourcePerDestination[i] = new ArrayDeque<>();
            demandAmountPerDestination[i] = new ArrayDeque<>();
        }

        int demand = demandsData.nDemands;
        while (demand > 0) {
            demand--;
            double amount = demandsData.demandTraffics[demand];

            if (pathsData.hasPath(demand)) {
                int[] path = pathsData.pathOf(demand);
                int p = path.length;
                while (p > 1) {
                    p--;
                    int src = path[p - 1];
                    int dest = path[p];

                    demandSourcePerDestination[dest].push(src);
                    demandAmountPerDestination[dest].push(amount);
                }
            } else {
                int src = demandsData.demandSrcs[demand];
                int dest = demandsData.demandDests[demand];
                demandSourcePerDestination[dest].push(src);
                demandAmountPerDestination[dest].push(amount);
            }
        }

        double[] flow = new double[topology.nEdges];
        double[] toRoute = new double[nNodes];

        int dest = nNodes;
        while (dest > 0) {
            dest--;

            for (int source : demandSourcePerDestination[dest]) {
                double amount = demandAmountPerDestination[dest].pop();
                toRoute[source] += amount;
            }
            demandSourcePerDestination[dest].clear();

            int p = sp.makeTopologicalOrdering(dest);
            int[] ordering = sp.topologicalOrdering();
            while (p > 0) {
                p--;
                int src = ordering[p];

                int nSucc = sp.nSuccessors(dest, src);
                double increment = toRoute[src] / nSucc;
                toRoute[src] = 0.0;

                int[] successorEdges = sp.successorEdges(dest, src);
                int pSucc = nSucc;
                while (pSucc > 0) {
                    pSucc--;
                    int edge = successorEdges[pSucc];
                    flow[edge] += increment;
                }

                int[] successorNodes = sp.successorNodes(dest, src);
                pSucc = nSucc;
                while (pSucc > 0) {
                    pSucc--;
                    int node = successorNodes[pSucc];
                    toRoute[node] += increment;
                }
            }
        }

        double maxLoad = 0.0;
        int edge = topology.nEdges;
        while (edge > 0) {
            edge--;
            maxLoad = Math.max(maxLoad, flow[edge] / topologyData.edgeCapacities[edge]);
        }

        System.out.println("maxLoad is " + maxLoad + ", announced " + pathsData.maxLoad);
    }
}
