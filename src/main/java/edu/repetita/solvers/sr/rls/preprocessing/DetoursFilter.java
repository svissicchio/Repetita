package edu.repetita.solvers.sr.rls.preprocessing;

import edu.repetita.solvers.sr.rls.ShortestPaths;
import java.util.Arrays;
import java.util.Comparator;

public class DetoursFilter {
    private static final double ratio = 1.0;

    public static int[] apply(ShortestPaths sp, boolean debug) {
        int nNodes = sp.nSuccessors().length;

        int[] usageCounter = new int[nNodes];
        int[] pathCount = new int[nNodes];

        for (int dest = 0; dest < nNodes; dest++) {
            Arrays.fill(pathCount, 1);

            sp.makeTopologicalOrdering(dest);
            int[] ordering = sp.topologicalOrdering();
            int[][] successors = sp.successorNodes(dest);

            int pOrder = nNodes;
            while (pOrder > 0) {
                pOrder--;
                int node = ordering[pOrder];
                int pSucc = sp.nSuccessors(dest, node);
                while (pSucc > 0) {
                    pSucc--;
                    int succ = successors[node][pSucc];
                    pathCount[succ] += pathCount[node];
                }
            }

            for (int node = 0; node < nNodes; node++) {
                usageCounter[node] += pathCount[node];
            }
        }

        Integer[] sortedDetours = new Integer[nNodes];
        for (int i = 0; i < nNodes; i++) sortedDetours[i] = i;
        Arrays.sort(sortedDetours, Comparator.comparingInt(i -> usageCounter[i]));

        int nFiltered = (int) (ratio * nNodes);
        int[] filteredDetours = new int[nFiltered];
        for (int i = 0; i < nFiltered; i++) {
            filteredDetours[i] = sortedDetours[i];
        }

        if (debug) {
            System.out.println("Selected a ratio of " + ((double) filteredDetours.length / nNodes) + " nodes as acceptable detours");
        }

        return filteredDetours;
    }
}
