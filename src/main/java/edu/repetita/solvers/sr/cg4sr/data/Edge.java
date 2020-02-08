package edu.repetita.solvers.sr.cg4sr.data;

/**
 * Class that represents a directed unweighted edge.
 * 
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class Edge implements Comparable<Edge> {
	
	protected int orig, dest, id;
	protected Edge reverseEdge;
	protected boolean isActive;
	
	/*
	 * Create an edge between orig and dest without id.
	 */
	public Edge(int orig, int dest) {
		this.orig = orig;
		this.dest = dest;
		id = -1;
		reverseEdge = null;
		isActive = true;
	}
	
	/*
	 * Create an edge between orig and dest with a given id.
	 */
	public Edge(int orig, int dest, int id) {
		this.orig = orig;
		this.dest = dest;
		this.id = id;
		reverseEdge = null;
		isActive = true;
	}
	
	
	/*
	 * Get the origin of the edge.
	 */
	public int orig() {
		return orig;
	}
	
	/*
	 * Get the destination of the edge.
	 */
	public int dest() {
		return dest;
	}
	
	/*
	 * Get edge id.
	 */
	public int id() {
		return id;
	}
	
	/*
	 * Set the reverse of this edge.
	 */
	public void setReverse(Edge e) {
		this.reverseEdge = e;
		e.reverseEdge = this;
	}
	
	/*
	 * Get reverse edge (for undirected graphs).
	 */
	public Edge getReverse() {
		return reverseEdge;
	}
	
	/*
	 * Get whether the edge is active or not.
	 */
	public boolean isActive() {
		return isActive;
	}
	
	/*
	 * Set whether the edge is active or not.
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	/*
	 * Set the id of this edge.
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/*
	 * Create a copy of the edge.
	 */
	public Edge copy() {
		return new Edge(orig, dest, id);
	}
	
	/*
	 * Get a hashcode for the edge.
	 */
	public int hashCode() {
		return orig + 31 * dest;
	}
	
	/*
	 * Check whether this edge is equal to other. 
	 */
	public boolean equals(Object other) {
		if(other instanceof Edge) {
			Edge e = (Edge) other;
			return orig == e.orig && dest == e.dest;
		}
		return false;
	}
	
    /*
     * Compare edges to sort by id first, orig second and dest third.
     */
	public int compareTo(Edge o) {
		int deltaId = id - o.id;
		if(deltaId != 0) return deltaId;
		int deltaOrig = orig - o.orig;
		if(deltaOrig != 0) return deltaOrig;
		return dest - o.dest;
	}

	/*
	 * Create a string representation of the edge. 
	 */
	public String toString() {
		return String.format("(%d, %d):%d", orig, dest, id);
	}

}
