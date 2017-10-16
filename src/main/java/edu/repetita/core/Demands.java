package edu.repetita.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *  A class made to contain a set of demands to be routed.
 *  <p>
 *  Each demand has a source, a destination, 
 *  an amount (in units that should agree with the corresponding topology),
 *  and an optional label.
 *  
 * @author Steven Gay
 */

public class Demands {
    /** number of demands */
    final public int nDemands;

    /** names of demands */
    final public String[] label;
    /** source nodes of demands */
    final public int[] source;
    /** destination nodes of demands */
    final public int[] dest;
    /** amounts of demands, typically in kbps */
    final public double[] amount;

    /**
     * The constructor uses the arrays that are passed internally.
     *
     * @param label names of demands, mostly optional
     * @param source source node of the demands
     * @param dest destination node of the demands
     * @param amount amount of the demands
     */
    public Demands(String[] label, int[] source, int[] dest, double[] amount) {
        this.nDemands = label.length;

        assert source.length == nDemands && dest.length == nDemands && amount.length == nDemands;
        for (double a: amount) assert a >= 0.0;

        this.label = label;
        this.source = source;
        this.dest = dest;
        this.amount = amount;
    }

    /**
     * Gives the demands as a matrix, thus merging demands with the same (source, destination) pair.
     *
     *  @param demands demands to be transformed
     *  @param nNodes number of nodes of the corresponding topology
     *  @return a nNodes * nNodes matrix with aggregated traffic demands
     */
    public static double[][] toTrafficMatrix(Demands demands, int nNodes) {
        return toTrafficMatrix(demands, nNodes, new HashSet<>());
    }

    public static double[][] toTrafficMatrix(Demands demands, int nNodes, Set<String> demandsToIgnore) {
        int nDemands = demands.nDemands;

        double[][] traffic = new double[nNodes][nNodes];
        for (int demand = 0; demand < nDemands; demand++) {
            if(demandsToIgnore.contains(demands.label[demand])){
                continue;
            }

            int source = demands.source[demand];
            int dest = demands.dest[demand];
            double amount = demands.amount[demand];

            traffic[source][dest] += amount;
        }

        return traffic;
    }

    public int getDemandIndex(String demandId) {
        return Arrays.asList(this.label).indexOf(demandId);
    }
}
