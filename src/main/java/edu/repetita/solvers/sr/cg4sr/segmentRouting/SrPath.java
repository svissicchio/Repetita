package edu.repetita.solvers.sr.cg4sr.segmentRouting;

import edu.repetita.solvers.sr.cg4sr.data.ECMPSplit;
import edu.repetita.solvers.sr.cg4sr.data.NetworkEdge;

import java.util.ArrayList;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class SrPath {

    private ArrayList<Segment> seg;

    public SrPath() {
        seg = new ArrayList<>();
    }

    public void add(int v) {
        seg.add(new Segment(v));
    }

    public void add(NetworkEdge e) {
        seg.add(new Segment(e));
    }

    public int orig() {
        return seg.get(0).x1();
    }

    public int dest() {
        return seg.get(seg.size() - 1).x2();
    }

    public int size() {
        return seg.size();
    }

    public Segment get(int i) {
        return seg.get(i);
    }


    public int segCost() {
        int cost = 0;
        for(int i = 0; i < seg.size(); i++) {
            if(seg.get(i).isAdj()) cost += 2;
            else cost += 1;
        }
        return cost;
    }

    public void reverse() {
        ArrayList<Segment> rev = new ArrayList<>();
        for(int i = seg.size() - 1; i >= 0; i--) {
            rev.add(seg.get(i));
        }
        seg = rev;
    }

    public double ratio(ECMPSplit<NetworkEdge> split, NetworkEdge e) {
        double r = 0;
        for(int i = 0; i < seg.size(); i++)  {
            if(seg.get(i).isAdj() && seg.get(i).edge().id() == e.id()) {
                r += 1;
            }
        }
        for(int i = 1; i < seg.size(); i++) {
            r += split.getValue(seg.get(i - 1).x2(), seg.get(i).x1(), e);
        }
        return r;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i = 0; i < seg.size(); i++) {
            sb.append(seg.get(i).toString());
            if(i < seg.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    /* This command doesn't work in case of an adjacency segment */
    public int[] toArray() {
        int[] tab = new int[seg.size()];
        for (int i = 0; i < tab.length; i++) {
            tab[i] = seg.get(i).x1();
        }
        return tab;
    }
}
