package edu.repetita.paths;

import edu.repetita.core.Demands;
import edu.repetita.core.Topology;
import org.apache.commons.lang.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

public class SRPaths {
    Demands demands;
    int nDemands;
    int[] lengths;
    int[][] paths;

    public SRPaths(Demands demands, int maxLength) {
        this.demands = demands;
        this.nDemands = demands.nDemands;
        lengths = new int[nDemands];
        paths = new int[nDemands][maxLength];

        // initially all segment routing paths are set with source and destination only (no intermediate segment)
        for (int demand = 0; demand < demands.nDemands; demand++) {
            int source = demands.source[demand];
            int dest = demands.dest[demand];
            this.setPath(demand, new int[]{source, dest});
        }
    }

    /**
     * Makes a String serializing an SRPaths
     *
     * @param topology the topology of paths
     * @param demands  the demands of paths
     * @param paths    the paths to be serialized
     * @return a String reprenseting paths
     */
    static public String toString(Topology topology, Demands demands, SRPaths paths) {
        StringBuilder output = new StringBuilder();

        for (int demand = 0; demand < demands.nDemands; demand++) {
            output.append(demands.label[demand] + " ");

            int len = paths.getPathLength(demand);
            for (int position = 0; position < len; position++) {
                int node = paths.getPathElement(demand, position);
                String nodeName = topology.nodeLabel[node];
                output.append(nodeName + " ");
            }
            output.append("\n");
        }

        return output.toString();
    }

    public void setPath(int demand, int[] path) {
        // compute the actual length of the new path, excluding possible starting or trailing padding from path variable
        int source = this.demands.source[demand];
        int sourceIndex = ArrayUtils.indexOf(path, source);
        int destination = this.demands.dest[demand];
        int actualPathLength = ArrayUtils.indexOf(path, destination) - sourceIndex + 1;

        // update data structures for the given demand
        System.arraycopy(path, sourceIndex, paths[demand], 0, actualPathLength);
        lengths[demand] = actualPathLength;
    }

    /*
     *  Returns the current length of the SR-path of demand
     *  @param demand      a valid demand identifier, should be in [0, nDemands[
     *  @return            the length of the path assigned to demand
     */
    public int getPathLength(int demand) {
        return lengths[demand];
    }

    /*
     *  Returns the current k-th element in the SR-path of demand
     *  @param demand      a valid demand identifier, should be in [0, nDemands[
     *  @param k           a node position, should be in [0, length[
     *  @return            the node at the k-th position of the path of demand
     */
    public int getPathElement(int demand, int position) {
        return paths[demand][position];
    }

    /*
     *  Returns the current SR-path of demand
     *  @param demand      a valid demand identifier, should be in [0, nDemands[
     *  @return            the path of demand, elements are those with index in [0, getPathLength(demand)[
     */
    public int[] getPath(int demand) {
        return paths[demand];
    }

    /*
     *  Returns the paths with at least one intermediate segment (beyond source and destination)
     *  @return            the path of demand, elements are those with index in [0, getPathLength(demand)[
     */
    public Map<Integer,int[]> getPathsWithIntermediateSegments() {
        HashMap<Integer,int[]> segmentedPaths = new HashMap<>();
        for (int dem=0; dem < this.paths.length; dem++){
            if (this.getPathLength(dem) > 2){
                segmentedPaths.put(dem,this.paths[dem].clone());
            }
        }
        return segmentedPaths;
    }
}
