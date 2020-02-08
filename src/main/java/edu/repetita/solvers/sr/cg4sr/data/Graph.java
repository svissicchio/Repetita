package edu.repetita.solvers.sr.cg4sr.data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;


/**
 * Class to represent a graph.
 * 
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class Graph<E extends Edge> {

	private int V, E, curId;
	private ArrayList<E>[] out;
	private ArrayList<E>[] in;
	private Index<String> nodeLabels;
	private String name;
	
	/*
	 * Create a graph with V nodes.
	 */
	@SuppressWarnings("unchecked")
	public Graph(int V) {
		this.V = V;
		out = new ArrayList[V];
		in = new ArrayList[V];
		for(int i = 0; i < V; i++) {
			out[i] = new ArrayList<>();
			in[i] = new ArrayList<>();
		}
		curId = 0;
		nodeLabels = null;
	}
	
	/*
	 * Create a graph with V nodes and a given list
	 * of edges. The edges must all have their origin
	 * and destination between 0 and V - 1 inclusive.
	 */
	@SuppressWarnings("unchecked")
	public Graph(int V, ArrayList<E> edges) {
		this.V = V;
		out = new ArrayList[V];
		in = new ArrayList[V];
		for(int i = 0; i < V; i++) {
			out[i] = new ArrayList<>();
			in[i] = new ArrayList<>();
		}
		nodeLabels = null;
		for(E e : edges) {
			addEdge(e, true);
		}
	}
	
	/*
	 * Create a graph from an index representing
	 * the labels of the nodes. The graph will
	 * contain no edges and the same amount of nodes
	 * as the size of the given index. The labels
	 * of the nodes will be set the the given labels.
	 */
	@SuppressWarnings("unchecked")
	public Graph(Index<String> nodeLabels) {
		this.nodeLabels = nodeLabels;
		V = nodeLabels.size();
		out = new ArrayList[V];
		in = new ArrayList[V];
		for(int i = 0; i < V; i++) {
			out[i] = new ArrayList<>();
			in[i] = new ArrayList<>();
		}
		curId = 0;
	}
	
	
	/*
	 * Set the label of the i-th node to be "i".
	 */
	public void createLabelsFromIndexes() {
		nodeLabels = new Index<>();
		for(int i = 0; i < V(); i++) {
			nodeLabels.add("" + i);
		}
	}
	
	public void setNodeLabels(Index<String> nodeLabels) {
		this.nodeLabels = nodeLabels;
	}
	
	public Index<String> getNodeLabels() {
		return nodeLabels;
	}
	
	/*
	 * Get the number of nodes.
	 */
	public int V() {
		return V;
	}
	
	/*
	 * Get the name of the graph.
	 */
	public String name() {
		return name;
	}
	
	/*
	 * Set the name of the graph.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/*
	 * Get the number of edges.
	 */
	public int E() {
		return E;
	}
	
	/*
	 * Get the out-degree of node x.
	 */
	public int outDeg(int x) {
		return out[x].size();
	}
	
	/*
	 * Get the in-degree of node x.
	 */
	public int inDeg(int x) {
		return in[x].size();
	}
	
	/*
	 * Add edge e to the graph. This edge
	 * will get an id equal to the id of the
	 * last added edge + 1.
	 */
	public void addEdge(E e) {
		addEdge(e, false);
	}
	
	/*
	 * Add edge e to the graph. If keepId is true
	 * then the edge will not be given a new id. Otherwise
	 * This edge will get an id equal to the id of the
	 * last added edge + 1.
	 */
	public void addEdge(E e, boolean keepId) {
		if(!keepId) e.id = curId++;
		out[e.orig].add(e);
		in[e.dest].add(e);
		E++;
	}
	
	/*
	 * Get the list of edge out of node x.
	 */
	public ArrayList<E> outEdges(int x) {
		return out[x];
	}
	
	/*
	 * Inefficient method to get an edge between x and  y.
	 * This is used only to test some stuff.
	 */
	public E getEdge(int x, int y) {
		for(E e : edges()) {
			if(e.orig == x && e.dest == y) {
				return e;
			}
		}
		return null;
	}
	
	
	/*
	 * Get the list of edges into node x.
	 */
	public ArrayList<E> inEdges(int x) {
		return in[x];
	}
	
	/*
	 * Get the sorted list of all edges in the
	 * graph.
	 */
	public ArrayList<E> edges() {
		ArrayList<E> edges = new ArrayList<>(E());
		for(int x = 0; x < V; x++) {
			for(E e : out[x]) {
				edges.add(e);
			}
		}
		Collections.sort(edges);
		return edges;
	}
	
	/*
	 * Get a Bitset with bits set to
	 * one on each id of an edge in the graph.
	 */
	public BitSet edgeSet() {
		BitSet s = new BitSet();
		for(int x = 0; x < V; x++) {
			for(Edge e : out[x]) {
				s.set(e.id);
			}
		}
		return s;
	}
	
	public void activateAll() {
		for(int x = 0; x < V(); x++) {
			for(Edge e : out[x]) {
				e.isActive = true;
			}
		}
	}

	
	public int getIndex(String s) {
		return nodeLabels.get(s);
	}
	
	public String getLabel(int x) {
		return nodeLabels.get(x);
	}
	
	public ArrayList<E> getEdges(BitSet s) {
		ArrayList<E> edges = new ArrayList<>();
		for(int x = 0; x < V; x++) {
			for(E e : out[x]) {
				if(s.get(e.id)) {
					edges.add(e);
				}
			}
		}
		return edges;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int x = 0; x < V(); x++) {
			sb.append(x + ":");
			for(Edge e : out[x]) {
				if(e.isActive) {
					sb.append(" " + e);
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
}
