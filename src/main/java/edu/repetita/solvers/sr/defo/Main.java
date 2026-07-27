package edu.repetita.solvers.sr.defo;

import edu.repetita.solvers.sr.defo.core.Topology;
import edu.repetita.solvers.sr.defo.modeling.DEFOConstraint;
import edu.repetita.solvers.sr.defo.modeling.DEFODemand;
import edu.repetita.solvers.sr.defo.modeling.DEFOptimizer;
import edu.repetita.solvers.sr.defo.modeling.MRProblem;
import edu.repetita.solvers.sr.defo.modeling.units.LoadUnit;
import edu.repetita.solvers.sr.defo.modeling.units.RelativeUnit;
import edu.repetita.solvers.sr.defo.modeling.units.TimeUnit;
import edu.repetita.solvers.sr.defo.parsers.*;

import java.io.PrintWriter;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        solve(args);
    }

    public static void solve(String[] args) {
        TimeUnit time = TimeUnit.s(30);
        RelativeUnit load = RelativeUnit.pct(90);
        int scaling = 1;
        String graphFile = "";
        String demandsFile = "";
        boolean verbose = false;
        String statsFile = null;
        String pathsFile = null;

        try {
            if (args.length <= 1) {
                printHelp();
                System.exit(1);
            } else {
                int i = 0;
                while (i < args.length) {
                    String arg = args[i];
                    switch (arg) {
                        case "-h":
                            printHelp();
                            break;
                        case "-l":
                            i++;
                            load = RelativeUnit.pct(parseInt(args, i, "-l", s -> s + " is not a valid maxLinkLoad rate."));
                            break;
                        case "-t":
                            i++;
                            time = TimeUnit.s(parseInt(args, i, "-t", s -> s + " is not a valid time limit."));
                            break;
                        case "-s":
                            i++;
                            scaling = parseInt(args, i, "-s", s -> s + " is not a valid scaling coefficient.");
                            break;
                        case "-f":
                            i++;
                            graphFile = args[i] + ".graph";
                            demandsFile = args[i] + ".demands";
                            break;
                        case "-graph":
                            i++;
                            graphFile = args[i];
                            break;
                        case "-demands":
                            i++;
                            demandsFile = args[i];
                            break;
                        case "-stats":
                            i++;
                            statsFile = args[i];
                            break;
                        case "-paths":
                            i++;
                            pathsFile = args[i];
                            break;
                        case "-verbose":
                            verbose = true;
                            break;
                        default:
                            throw new IllegalArgumentException("unknown parameter " + arg + ".");
                    }
                    i++;
                }
            }

            solve(time, load, scaling, graphFile, demandsFile, verbose, pathsFile, statsFile);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static int parseInt(String[] args, int i, String param, java.util.function.Function<String, String> msg) {
        if (i >= args.length) System.err.println("missing value for parameter " + param + ".");
        String s = args[i];
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            throw new IllegalArgumentException(msg.apply(s));
        }
    }

    private static void printHelp() {
        System.out.println("Syntaxe: defo.jar [-parameters] <topologyFile> <demandsFile>");
        System.out.println("-h       print help message.");
        System.out.println("-l       maxLinkLoad rate to reach in percents (default: 90).");
        System.out.println("-t       optimization time limit in seconds (default: 30 secs).");
        System.out.println("-s       positive integer coefficient to rescale demands (default: 1).");
        System.out.println("-f       instance stem: will use stem.graph stem.demands ");
        System.out.println("-graph   instance.graph");
        System.out.println("-demands instance.demands");
        System.out.println("-verbose emit messages during computation");
        System.out.println("-stats   instance.stats put progress info in a file");
    }

    public static void solve(TimeUnit time, RelativeUnit load, int scaling, String topologyFile, String demandsFile,
                             boolean verbose, String pathsFilename, String statsFilename) throws Exception {
        TopologyData topologyDataParsed = TopologyParser.parse(topologyFile);
        DemandsData demandsDataParsed = DemandParser.parse(demandsFile);

        int[] scaledTraffics = new int[demandsDataParsed.demandTraffics.length];
        for (int i = 0; i < scaledTraffics.length; i++) scaledTraffics[i] = demandsDataParsed.demandTraffics[i] * scaling;

        DemandsData demandsData = new DemandsData(
                demandsDataParsed.demandLabels,
                demandsDataParsed.demandSrcs,
                demandsDataParsed.demandDests,
                scaledTraffics
        );

        int[] scaledCapacities = new int[topologyDataParsed.edgeCapacities.length];
        for (int i = 0; i < scaledCapacities.length; i++) scaledCapacities[i] = topologyDataParsed.edgeCapacities[i] * scaling;

        TopologyData topologyData = new TopologyData(
                topologyDataParsed.nodeLabels,
                topologyDataParsed.nodeCoordinates,
                topologyDataParsed.edgeLabels,
                topologyDataParsed.edgeSrcs,
                topologyDataParsed.edgeDests,
                topologyDataParsed.edgeWeights,
                scaledCapacities,
                topologyDataParsed.edgeLatencies
        );

        Topology topology = Topology.apply(topologyData.edgeSrcs, topologyData.edgeDests, topologyData.nodeLabels, topologyData.edgeLabels);

        MRProblem problem = new MRProblem(topology);
        for (int demandId = 0; demandId < demandsData.demandTraffics.length; demandId++) {
            problem.newDemand(
                    demandsData.demandLabels[demandId],
                    demandsData.demandSrcs[demandId],
                    demandsData.demandDests[demandId],
                    LoadUnit.kbps(demandsData.demandTraffics[demandId])
            );
        }

        PrintWriter statsWriter = null;
        if (statsFilename != null) {
            statsWriter = new PrintWriter(statsFilename);
        }

        DEFOptimizer solver = DEFOptimizer.apply(problem, topologyData.edgeWeights, topologyData.edgeCapacities, topologyData.edgeLatencies, verbose, statsWriter);
        solver.solve(time, load);

        if (statsWriter != null) {
            statsWriter.close();
        }

        if (pathsFilename != null) {
            try (PrintWriter file = new PrintWriter(pathsFilename)) {
                double worstLoad = 0.0;
                int[] finalRates = solver.core.bestRates();
                for (int edge = 0; edge < topology.nEdges; edge++) {
                    worstLoad = Math.max(worstLoad, (double) finalRates[edge] / solver.core.grain);
                }
                file.println("MAXLOAD");
                file.println(worstLoad);
                file.println();

                int nPaths = 0;
                file.println("PATHS");
                for (DEFODemand demand : problem.demands()) {
                    int[] path = problem.assignedPath(demand, solver);
                    if (path.length > 2) {
                        nPaths++;
                        StringBuilder sb = new StringBuilder();
                        sb.append(demand).append(" ");
                        for (int k = 0; k < path.length; k++) {
                            if (k > 0) sb.append(" ");
                            sb.append(topologyData.nodeLabels[path[k]]);
                        }
                        file.println(sb.toString());
                    }
                }
                file.println();

                file.println("NPATHS");
                file.println(nPaths);
                file.println();
            }
        }
    }
}
