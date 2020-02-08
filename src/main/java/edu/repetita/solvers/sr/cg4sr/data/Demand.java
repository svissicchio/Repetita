package edu.repetita.solvers.sr.cg4sr.data;

import edu.repetita.solvers.sr.cg4sr.segmentRouting.SrPath;

import java.util.ArrayList;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class Demand implements Comparable<Demand> {

	public String id;
	public int src, dst, volume, index;
	public double dual;

	public Demand(int src, int dst, int D, int index) {
		this.src = src;
		this.dst = dst;
		this.volume = D;
		this.index = index;
	}

	public Demand(String id, int src, int dst, int D, int index) {
		this.src = src;
		this.dst = dst;
		this.volume = D;
		this.id = id;
		this.index = index;
	}

	public void setDual(double dual) {
		this.dual = dual;
	}

	public double getDual() {
		return dual;
	}

	public Demand copy() {
		return new Demand(id, src, dst, volume, index);
	}

	public int hashCode() {
		return 23 * src + 31 * dst + 31 * 31 * volume;
	}

	public String toString() {
		return toString(null);
	}

	public String toString(Index<String> labels) {
		return String.format("%s(%s, %s, %d)", id != null ? id + " " : "",
				labels != null ? labels.get(src) : src,
				labels != null ? labels.get(dst) : dst, volume);
	}

	public boolean equals(Object other) {
		if (other instanceof Demand) {
			Demand o = (Demand) other;
			return (id == null && src == o.src && dst == o.dst && volume == o.volume) || (id != null && id.equals(o.id));
		}
		return false;
	}

	public int compareTo(Demand other) {
		return volume - other.volume;
	}

	public boolean incompatible(Path p) {
		return src != p.get(0) || dst != p.getLast();
	}

	public boolean isCompatibleWith(SrPath p, ArrayList<NetworkEdge> edges, ECMPSplit<NetworkEdge> split,
	                                double capacityFactor, boolean keepExceedingCapaPath) {
		// check origin and destination
		if(src != p.orig() || dst != p.dest()) return false;

		if (keepExceedingCapaPath || true)
			return true;

		// check the capacity of p
		for(NetworkEdge e : edges) {
			double ratio = 0;
			for (int i = 0; i < p.size(); i++) {
				if (p.get(i).isAdj()) {
					if (p.get(i).edge().id() == e.id()) {
						ratio += 1;
					}
				}
			}
			for (int i = 1; i < p.size(); i++) {
				ratio += split.getValue(p.get(i - 1).x2(), p.get(i).x1(), e);
			}
			if (ratio * volume > capacityFactor * e.cap()) return false;
		}
		return true;
	}

}
