package edu.repetita.solvers.sr.cg4sr.data;

/**
 * Class that represented a weighted directed edge.
 * 
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class WeightedEdge extends Edge {

	private int weight;
	private WeightedEdge reverseWeightedEdge;
	
	/*
	 * Create an edge from orig to dest with given weight.
	 */
	public WeightedEdge(int orig, int dest, int weight) {
		super(orig, dest);
		this.weight = weight;
	}
	
	/*
	 * Get the weight of the edge.
	 */
	public int weight() {
		return weight;
	}
	
	/*
	 * Create and edge from orig to dest with given weight and id.
	 */
	public WeightedEdge(int orig, int dest, int weight, int id) {
		super(orig, dest, id);
		this.weight = weight;
	}
	
	/*
	 * Create a copy of the edge.
	 */
	public WeightedEdge copy() {
		return new WeightedEdge(orig, dest, weight, id);
	}
	
	/*
	 * Set the reverse of this edge.
	 */
	public void setReverse(WeightedEdge e) {
		this.reverseWeightedEdge = e;
		e.reverseWeightedEdge = this;
	}
	
	/*
	 * Get the reverse of this edge. 
	 */
	public WeightedEdge getReverse() {
		return reverseWeightedEdge;
	}
	
	/*
	 * Get a string representation of the edge.
	 */
	public String toString() {
		return super.toString() + " w=" + weight;
	}
}
