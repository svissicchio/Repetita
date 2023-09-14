package edu.repetita.solvers.sr.cg4sr.data;

import java.util.List;

/**
 * Class used to encapsulate the result of Dijkstra's algorithm.
 * 
 * @author Francois Aubry f.aubry@uclouvian.be
 */
public class DijkstraData<E extends WeightedEdge> {
	
	public int source, V; // source vertex and number of vertices.
	public double[] distance; // distance[x] = distance from source to x
	public int[] length; // length[x] = number of edges in the shortest path from source to x
	public Graph<E> dag; // shortest path dag rooted at source
	public List<E>[] parent; // parent[x] = all predecessors of x in the shortest path dag
	
	public DijkstraData(int source, double[] distance, int[] length, List<E>[] parent) {
		this.V = distance.length;
		this.source = source;
		this.distance = distance;
		this.length = length;
		this.parent = parent;
		buildSPDag();
	}
	
	/*
	 * Build the shortest path dag from the parents.
	 */
	private void buildSPDag() {
		dag = new Graph<>(V);
		for(int v = 0; v < V; v++) {
			for(E e : parent[v]) {
				dag.addEdge(e, true);
			}
		}
	}

}
