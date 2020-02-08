package edu.repetita.solvers.sr.cg4sr.data;

/**
 * Class that represents an link with both an IGP weight and a latency.
 * 
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class NetworkEdge extends WeightedEdge {

	private double lat;
	private int cap;
	private NetworkEdge reverseNetworkEdge;
	
	/*
	 * Create an edge between orig and dest with given igp cost and latency.
	 */
	public NetworkEdge(int orig, int dest, int igp, double lat) {
		super(orig, dest, igp);
		this.lat = lat;
		cap = 0;
	}
	
	/*
	 * Create an edge between orig and dest with given igp cost and latency.
	 */
	public NetworkEdge(int orig, int dest, int igp, double lat, int cap) {
		super(orig, dest, igp);
		this.lat = lat;
		this.cap = cap;
	}
	
	/*
	 * Create an edge between orig and dest with given igp cost, latency and id.
	 */
	public NetworkEdge(int orig, int dest, int igp, double lat, int cap, int id) {
		super(orig, dest, igp, id);
		this.lat = lat;
		this.cap = cap;
	}
	
	/*
	 * Get the latency of the edge.
	 */
	public double lat() {
		return lat;
	}
	
	/*
	 * Get the capacity of the edge.
	 */
	public int cap() {
		return cap;
	}
	
	/*
	 * Set the reverse edge.
	 */
	public void setReverse(NetworkEdge e) {
		this.reverseNetworkEdge = e;
		e.reverseNetworkEdge = this;
	}
	
	/*
	 * Get the reverse edge. 
	 */
	public NetworkEdge getReverse() {
		return reverseNetworkEdge;
	}
	
	/*
	 * Get a string representation of the edge.
	 */
	public String toString() {
		return String.format("(%d, %d, igp=%d, lat=%.3f, cap=%d)", orig, dest, super.weight(), lat, cap);
	}
	
	/*
	 * Get a string representation of the edge.
	 */
	public String toString(Graph<NetworkEdge> g) {
		return String.format("(%s, %s, igp=%d, lat=%.3f, cap=%d, id=%d)", g.getLabel(orig), g.getLabel(dest), super.weight(), lat, cap, id);
	}

}
