package edu.repetita.solvers.sr.defo.modeling;

import edu.repetita.solvers.sr.defo.core.CoreSolver;
import edu.repetita.solvers.sr.defo.core.DEFOInstance;
import edu.repetita.solvers.sr.defo.core.Topology;
import edu.repetita.solvers.sr.defo.modeling.units.RelativeUnit;
import edu.repetita.solvers.sr.defo.modeling.units.TimeUnit;
import edu.repetita.solvers.sr.defo.paths.ECMPStructure;
import edu.repetita.solvers.sr.defo.paths.SimplePaths;

import java.io.PrintWriter;
import java.util.*;

public class DEFOptimizer {
    public final CoreSolver core;
    private final Topology topology;
    private final DEFOInstance instance;
    private final boolean verbose;

    public DEFOptimizer(DEFOInstance instance, boolean verbose, PrintWriter statsFile) {
        this.instance = instance;
        this.verbose = verbose;
        this.core = new CoreSolver(instance, verbose, statsFile);
        this.topology = instance.topology;
    }

    public void firstSolution() {
        core.searchInitialSol();
    }

    public void solve(TimeUnit timeLimit, RelativeUnit maxLoad) {
        long t0 = System.currentTimeMillis();
        core.searchInitialSol();
        long preTime = System.currentTimeMillis() - t0;

        long t1 = System.currentTimeMillis();
        core.search(timeLimit.value, maxLoad.value);
        long optTime = System.currentTimeMillis() - t1;

        Integer[] sortedEdges = new Integer[topology.nEdges];
        for (int i = 0; i < topology.nEdges; i++) sortedEdges[i] = i;
        Arrays.sort(sortedEdges, Comparator.comparingInt(e -> -core.initialRates()[e]));

        int[][] bestPaths = core.bestPaths();
        List<int[]> filteredPath = new ArrayList<>();
        for (int[] path : bestPaths) {
            if (path.length > 2) filteredPath.add(path);
        }

        SimplePaths simplePaths = new SimplePaths(topology, instance.weights);

        ECMPStructure ecmpStruct = ECMPStructure.apply(topology, instance.capacities, instance.latencies);

        if (verbose) {
            System.out.println();
            System.out.println("Optimization completed");
            System.out.println("----------------------");
            System.out.println("number of nodes     : " + topology.nNodes);
            System.out.println("number of edges     : " + topology.nEdges);
            System.out.println("number of demands   : " + instance.demandDests.length);
            System.out.println("first solution time : " + preTime);
            System.out.println("optimization time   : " + optTime);

            int initMax = 0;
            for (int r : core.initialRates()) if (r > initMax) initMax = r;
            int bestMax = 0;
            for (int r : core.bestRates()) if (r > bestMax) bestMax = r;

            System.out.println("initial max maxLinkLoad    : " + initMax);
            System.out.println("final max maxLinkLoad      : " + bestMax);
            System.out.println("number of tunnels   : " + filteredPath.size());
        }
    }

    public static DEFOptimizer apply(MRProblem problem, int[] edgeWeights, int[] edgeCapacities, int[] edgeLatencies, boolean verbose, PrintWriter statsFile) {
        DEFOInstance instance = problem.toInstance(edgeWeights, edgeCapacities, edgeLatencies);
        return new DEFOptimizer(instance, verbose, statsFile);
    }
}
