package edu.repetita;

import edu.repetita.core.Demands;
import edu.repetita.core.Topology;

public class ToyTopologies {

    /*
     * a -- b
     * |    |
     * c -- d
     *
     * weights, capacities and delays initially set to 1 on all links
     */
    public static Topology getSquare(){
        String[] nodeLabel = {"a","b","c","d"};
        String[] edgeLabel = {"ab","ba","ac","ca","bd","db","cd","dc"};
        int[] edgeSrc = {0,1,0,2,1,3,2,3};
        int[] edgeDest = {1,0,2,0,3,1,3,2};
        int[] edgeWeight = {1,1,1,1,1,1,1,1};
        double[] edgeCapacity = {1,1,1,1,1,1,1,1};
        int[] edgeLatency = {1,1,1,1,1,1,1,1};
        return new Topology(nodeLabel, edgeLabel, edgeSrc, edgeDest, edgeWeight, edgeCapacity, edgeLatency);
    }

    /*
     * Returns a Demands object with a single demand from node c to node d in the square gadget -- see getSquare()
     */
    public static Demands getBottomLinkUnitaryDemandOnSquare(){
        Topology square = getSquare();
        Demands demands = new Demands(new String[]{"demand_cd"}, new int[]{square.getEdgeSource("cd")},
                new int[]{square.getEdgeDest("cd")},new double[]{1.0});
        return demands;
    }

    /*
     * Returns a Demands object with two demands on the square gadget, from node c to node d and from node a to node b,
     * respectively -- see getSquare() for details about the topology
     */
    public static Demands getBottomAndTopLinkDemandsOnSquare(){
        Topology square = getSquare();
        Demands demands = new Demands(new String[]{"demand_cd","demand_ab"},
                new int[]{square.getEdgeSource("cd"),square.getEdgeSource("ab")},
                new int[]{square.getEdgeDest("cd"),square.getEdgeDest("ab")},
                new double[]{1.0,1.0});
        return demands;
    }

    /*
     * Returns a Demands object with two demands on the square gadget, from node c to node d and from node a to node b,
     * respectively -- see getSquare() for details about the topology
     */
    public static Demands getDiagonalDemandsOnSquare(){
        Topology square = getSquare();
        Demands demands = new Demands(new String[]{"demand_cb","demand_ad"},
                new int[]{square.getEdgeSource("cd"),square.getEdgeSource("ab")},
                new int[]{square.getEdgeDest("ab"),square.getEdgeDest("cd")},
                new double[]{1.0,1.0});
        return demands;
    }

    /*
     * a -- b
     * |    |
     * c -- d
     *
     * weights and delays initially set to 1 on all links, capacities all equal to 2
     * but on link (c,d) which has capacity equal to 1.
     */
    public static Topology getSquareWithBottomBottleneck(){
        String[] nodeLabel = {"a","b","c","d"};
        String[] edgeLabel = {"ab","ba","ac","ca","bd","db","cd","dc"};
        int[] edgeSrc = {0,1,0,2,1,3,2,3};
        int[] edgeDest = {1,0,2,0,3,1,3,2};
        int[] edgeWeight = {1,1,1,1,1,1,1,1};
        double[] edgeCapacity = {2,2,2,2,2,2,1,1};
        int[] edgeLatency = {1,1,1,1,1,1,1,1};
        return new Topology(nodeLabel, edgeLabel, edgeSrc, edgeDest, edgeWeight, edgeCapacity, edgeLatency);
    }

    /*
     * Returns a Demands object with two demands on the square gadget, from node c to node d and from node a to node b,
     * respectively: One of the two demands just fit to top path -- see getSquareWithBottomBottleneck() for details
     * about the topology
     */
    public static Demands getBottomAndTopLinkDemandsJustFittingOnSquareWithBottleneck(boolean topDemandBigger){
        Topology square = getSquareWithBottomBottleneck();
        double[] demandVolumes = {1.0,2.0};
        if (!topDemandBigger){
            demandVolumes[0] = 2.0;
            demandVolumes[1] = 1.0;
        }
        Demands demands = new Demands(new String[]{"demand_cd","demand_ab"},
                new int[]{square.getEdgeSource("cd"),square.getEdgeSource("ab")},
                new int[]{square.getEdgeDest("cd"),square.getEdgeDest("ab")},
                demandVolumes);
        return demands;
    }

    /*
     * Returns a Demands object with two demands on the square gadget, from node c to node d and from node a to node b,
     * respectively: Both demands largely fit the top path -- see getSquareWithBottomBottleneck() for details
     * about the topology
     */
    public static Demands getBottomAndTopLinkDemandsLargelyFittingOnSquareWithBottleneck(boolean topDemandBigger){
        Topology square = getSquareWithBottomBottleneck();
        double[] demandVolumes = {0.5,1.0};
        if (!topDemandBigger){
            demandVolumes[0] = 1.0;
            demandVolumes[1] = 0.5;
        }
        Demands demands = new Demands(new String[]{"demand_cd","demand_ab"},
                new int[]{square.getEdgeSource("cd"),square.getEdgeSource("ab")},
                new int[]{square.getEdgeDest("cd"),square.getEdgeDest("ab")},
                demandVolumes);
        return demands;
    }
}
