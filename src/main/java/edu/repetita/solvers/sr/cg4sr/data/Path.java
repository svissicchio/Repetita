package edu.repetita.solvers.sr.cg4sr.data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

/**
 * Class to represent a path in the network.
 * 
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class Path implements Iterable<Integer> {
	
	private ArrayList<Integer> nodes;
	private BitSet nodesInPath;
	private int size;
	private double cost;
	private ArrayList<? extends Edge> edges;
	
	/*
	 * Initialize an empty path.
	 */
	public Path() {
		nodes = new ArrayList<>();
		nodesInPath = new BitSet();
		size = 0;
		cost = 0;
	}
	
	/*
	 * Function to create a path from an array.
	 */
	public static Path createFromArray(int[] v) {
		Path p = new Path();
		for(int x : v) p.add(x);
		return p;
	}
	
	/*
	 * Function to create a path from the first size elements of an
	 * array.
	 */
	public static Path createFromArray(int[] v, int size) {
		Path p = new Path();
		for(int i = 0; i < size; i++) p.add(v[i]);
		return p;
	}
	
	/*
	 * Set the cost of the path to cost.
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}
	 
	/*
	 * Get the cost of a path.
	 */
	public double getCost() {
		return cost;
	}
	
	/*
	 * Make a copy of the path.
	 */
	public Path copy() {
		Path p = new Path();
		for(int i = 0; i < size; i++) {
			p.add(get(i));
		}
		return p;
	}
	
	/*
	 * Get the size of the path.
	 */
	public int size() {
		return size;
	}
	
	/*
	 * Get the last node of the path.
	 */
	public int getLast() {
		return nodes.get(size - 1);
	}
	
	/*
	 * Get the first node of the path.
	 */
	public int getFirst() {
		return nodes.get(0);
	}
	
	/*
	 * Remove the last node of the path.
	 */
	public void removeLast() {
		nodesInPath.clear(getLast());
		size = Math.max(size - 1, 0);
	}
	
	/*
	 * Get the i-th node of the path.
	 */
	public int get(int i) {
		return nodes.get(i);
	}
	
	/*
	 * Reverse the path node order.
	 */
	public void reverse() {
		for(int i = 0; i < size / 2; i++) {
			int tmp = nodes.get(i);
			nodes.set(i, nodes.get(size - i - 1));
			nodes.set(size - i - 1, tmp);
		}
	}
	
	/*
	 * Add node v to the path.
	 */
	public void add(int v) {
		if(nodes.size() > size) {
			nodes.set(size, v);
		} else {
			nodes.add(v);
		}
		size++;
		nodesInPath.set(v);
	}
	
	/*
	 * Check whether the path contains node v.
	 */
	public boolean contains(int v) {
		return nodesInPath.get(v);
	}
	
	/*
	 * Create a string representation of the path
	 * replacing the node indexes by the given labels.
	 */
	public String toString(Index<String> labels) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(int i = 0; i < size; i++) {
			sb.append(labels.get(nodes.get(i)));
			if(i < size - 1) sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}

	/*
	 * Create a Repetita-compatible string representation
	 * of the path replacing the node indexes by the given labels.
	 */
	public String toRepetitaString(Index<String> labels) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < size; i++) {
			sb.append(labels.get(nodes.get(i)));
			if(i < size - 1) sb.append("-");
		}
		return sb.toString();
	}
	
	/*
	 * Get a hash code for this path.
	 * 
	 * p[0] + 31 p[1] + 31^2 p[2] + ... + 31^n-1 p[n-1]
	 */
	public int hashCode() {
		int h = 0;
		for(int i = 0; i < nodes.size(); i++) {
			h = 31 * h + nodes.get(i);
		}
		return 0;
	}
	
	/*
	 * Equals function. 
	 */
	public boolean equals(Object other) {
		if(other instanceof Path) {
			Path o = (Path)other;
			return nodes.equals(o.nodes);
		}
		return false;
	}
	
	public void setEdges(ArrayList<? extends Edge> edges) {
		this.edges = edges;
	}
	
	public ArrayList<? extends Edge> getEdges() {
		return edges;
	}
	
	/*
	 * Create a string representation of the path.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(int i = 0; i < size; i++) {
			sb.append(nodes.get(i));
			if(i  < size - 1) sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}

	/*
	 * Provide an iterator for nodes in the path.
	 */
	public Iterator<Integer> iterator() {
		return nodes.iterator();
	}
	
}
