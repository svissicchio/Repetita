package edu.repetita.solvers.sr.cg4sr.segmentRouting;

import edu.repetita.solvers.sr.cg4sr.data.NetworkEdge;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class Segment {


    private int node;
    private NetworkEdge edge;

    public Segment(int node) {
        this.node = node;
    }

    public Segment(NetworkEdge edge) {
        this.edge = edge;
    }

    public int x1() {
        if(isAdj()) return edge.orig();
        return node;
    }

    public int x2() {
        if(isAdj()) return edge.dest();
        return node;
    }

    public NetworkEdge edge() {
        return edge;
    }

    public boolean isAdj() {
        return edge != null;
    }

    public String toString() {
        if(isAdj()) return String.format("(%d, %d)", edge.orig(), edge.dest());
        return "" + node;
    }

}