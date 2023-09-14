package edu.repetita.solvers.sr.cg4sr.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Data structure to index value of type V by the order in which they are added.
 * Allows to get the index of a given value and get a value by giving its index.
 *
 * Usefully for instance to associate labels to the nodes of a graph.
 *
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class Index<V> implements Iterable<V> {

	private HashMap<V, Integer> M;
	private int index;
	private ArrayList<V> Mi;

	/*
	 * Create an empty index.
	 */
	public Index() {
		M = new HashMap<>();
		Mi = new ArrayList<>();
		index = 0;
	}

	/*
	 * Retrieve the number of elements in the index.
	 */
	public int size() {
		return M.size();
	}

	/*
	 * Add value v to the index.
	 */
	public int add(V v) {
		Integer idxV = M.get(v);
		if(idxV == null) {
			Mi.add(v);
			M.put(v, index);
			return index++;
		}
		return idxV;
	}

	/*
	 * Get the index of value v.
	 */
	public int get(V v) {
		return M.get(v);
	}

	/*
	 * Get the value with the given index.
	 */
	public V get(int index) {
		return Mi.get(index);
	}

	/*
	 * Create a string representation of the index.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for(int i = 0; i < size(); i++) {
			sb.append(i + " <-> " + Mi.get(i));
			if(i  < size() - 1) {
				sb.append(", ");
			}
		}
		sb.append(']');
		return sb.toString();
	}

	public Iterator<V> iterator() {
		return Mi.iterator();
	}

}
