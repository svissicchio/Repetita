package edu.repetita.solvers.sr.cg4sr.data;

import java.util.*;

/**
 * Implementation of Dijkstra's algorithm.
 * 
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class Dijkstra<E extends WeightedEdge> {

	private Graph<E> g;
	private double[] w;

	/*
	 * Build Dijsktra from a given weighted graph.
	 */
	public Dijkstra(Graph<E> g) {
		this.g = g;
		w = new double[g.E()];
		for(int x = 0; x < g.V(); x++) {
			for(E e : g.outEdges(x)) {
				w[e.id] = e.weight();
			}
		}
	}

	/*
	 * Build Dijkstra from a given graph and edge weights.
	 * Used in case we want to use different weights.
	 */
	public Dijkstra(Graph<E> g, double[] w) {
		this.g = g;
		this.w = w;
	}

	/*
	 * Compute the shortest paths from a given source.
	 * O[E log(V)]
	 */
	@SuppressWarnings("unchecked")
	public DijkstraData<E> shortestPathFrom(int source) {
		// initialize distance array
		double[] distance = new double[g.V()];
		Arrays.fill(distance, Double.POSITIVE_INFINITY);
		distance[source] = 0;
		// initialize parent array
		List<E>[] parent = new LinkedList[g.V()];
		for(int i = 0; i < g.V(); i++) {
			parent[i] = new LinkedList<>();
		}
		// initialize path length (edges)
		int[] length = new int[g.V()];
		Arrays.fill(length, -1);
		length[source] = 0;
		// compute shortest paths
		TreeSet<Integer> Q = new TreeSet<>(new VertexCmp(distance));
		Q.add(source);
		while(!Q.isEmpty()) {
			int v = Q.pollFirst();	
			for(E e : g.outEdges(v)) {
				int u = e.dest;
				if(!e.isActive) continue;
				if(distance[v] + w[e.id] < distance[u]) {
					Q.remove(u);
					distance[u] = distance[v] + w[e.id];
					length[u] = length[v] + 1;
					parent[u].clear();
					parent[u].add(e);
					Q.add(u);
				} else if(distance[v] + w[e.id] == distance[u]) {
					parent[u].add(e);
				}
			} 
		}
		return new DijkstraData<>(source, distance, length, parent);
	}


	public static DijkstraData<WeightedEdge> shortestPathFrom(Graph<? extends WeightedEdge> g, int source) {
		// initialize distance array
		double[] distance = new double[g.V()];
		Arrays.fill(distance, Double.POSITIVE_INFINITY);
		distance[source] = 0;
		// initialize parent array
		List<WeightedEdge>[] parent = new LinkedList[g.V()];
		for(int i = 0; i < g.V(); i++) {
			parent[i] = new LinkedList<>();
		}
		// initialize path length (edges)
		int[] length = new int[g.V()];
		Arrays.fill(length, -1);
		length[source] = 0;
		// compute shortest paths
		TreeSet<Integer> Q = new TreeSet<>(new VertexCmp(distance));
		Q.add(source);
		while(!Q.isEmpty()) {
			int v = Q.pollFirst();	
			for(WeightedEdge e : g.outEdges(v)) {
				int u = e.dest;
				if(!e.isActive) continue;
				if(distance[v] + e.weight() < distance[u]) {
					Q.remove(u);
					distance[u] = distance[v] + e.weight();
					length[u] = length[v] + 1;
					parent[u].clear();
					parent[u].add(e);
					Q.add(u);
				} else if(distance[v] + e.weight() == distance[u]) {
					parent[u].add(e);
				}
			} 
		}
		return new DijkstraData<WeightedEdge>(source, distance, length, parent);
	}


	public static Path shortetPath(Graph<? extends WeightedEdge> g, int orig, int dest) {
		// initialize distance array
		double[] distance = new double[g.V()];
		Arrays.fill(distance, Double.POSITIVE_INFINITY);
		distance[orig] = 0;
		// initialize parent array
		WeightedEdge[] parent = new WeightedEdge[g.V()];
		// initialize path length (edges)
		int[] length = new int[g.V()];
		Arrays.fill(length, -1);
		length[orig] = 0;
		// compute shortest paths
		TreeSet<Integer> Q = new TreeSet<>(new VertexCmp(distance));
		Q.add(orig);
		while(!Q.isEmpty()) {
			int v = Q.pollFirst();	
			for(WeightedEdge e : g.outEdges(v)) {
				int u = e.dest;
				if(!e.isActive) continue;
				if(distance[v] + e.weight() < distance[u]) {
					Q.remove(u);
					distance[u] = distance[v] + e.weight();
					length[u] = length[v] + 1;
					parent[u] = e;
					Q.add(u);
				}
			} 
		}
		// check if path exists
		if(parent[dest] == null) return null;
		// build the path
		Path path = new Path();
		int cur = dest;
		while(parent[cur] != null) {
			path.add(cur);
			cur = parent[cur].orig;
		}
		path.add(cur);
		path.setCost(distance[dest]);
		path.reverse();
		return path;
	}

	/*
	 * Compute the shortest path dag from s to t.
	 */
	public Graph<E> spDag(int s, int t) {
		Graph<E> dags = shortestPathFrom(s).dag;
		return subDag(dags, t);
	}

	/*
	 * Given a dag and a node t, compute a subdag
	 * that contains only edges that belong to a path
	 * leading to t.
	 */
	public Graph<E> subDag(Graph<E> dag, int t) {
		Graph<E> sub = new Graph<E>(dag.V());
		Queue<Integer> Q = new LinkedList<>();
		Q.add(t);
		BitSet visited = new BitSet();
		visited.set(t);
		while(!Q.isEmpty()) {
			int cur = Q.poll();
			for(E e : dag.inEdges(cur)) {
				int y = e.orig;
				sub.addEdge(e, true);
				if(!visited.get(y)) {
					visited.set(y);
					Q.add(y);
				}
			}
		}
		return sub;
	}

	/*
	 * Compute all shortest path dags.
	 */
	@SuppressWarnings("unchecked")
	public Graph<E>[] shortestPathDags() {
		Graph<E>[] spDag = new Graph[g.V()];
		for(int x = 0; x < g.V(); x++) {
			spDag[x] = shortestPathFrom(x).dag;
		}
		return spDag;
	}


	/*
	 * Compute all pairs shortest paths.
	 */
	public double[][] apspDijkstra() {
		double[][] d = new double[g.V()][g.V()];
		for(int x = 0; x < g.V(); x++) {
			DijkstraData<E> data = shortestPathFrom(x);
			d[x] = data.distance;
		}
		return d;
	}

	public static Path shortestPath(double[][] weight, int orig, int dest) {
		int n = weight.length;
		// initialize distance array
		double[] distance = new double[n];
		Arrays.fill(distance, Double.POSITIVE_INFINITY);
		distance[orig] = 0;
		// initialize parent array
		Integer[] parent = new Integer[n];
		// initialize path length (edges)
		int[] length = new int[n];
		Arrays.fill(length, -1);
		length[orig] = 0;
		// compute shortest paths
		TreeSet<Integer> Q = new TreeSet<>(new VertexCmp(distance));
		Q.add(orig);
		while(!Q.isEmpty()) {
			int v = Q.pollFirst();	
			for(int u = 0; u < n; u++) {
				if(distance[v] + weight[v][u] < distance[u]) {
					Q.remove(u);
					distance[u] = distance[v] + weight[v][u];
					length[u] = length[v] + 1;
					parent[u] = v;
					Q.add(u);
				}
			} 
		}
		// check if path exists
		if(parent[dest] == null) return null;
		// build the path
		Path path = new Path();
		int cur = dest;
		while(parent[cur] != null) {
			path.add(cur);
			cur = parent[cur];
		}
		path.add(cur);
		path.setCost(distance[dest]);
		path.reverse();
		return path;
	}


	/*
	 * Class used to compare vertices by distance.
	 */
	private static class VertexCmp implements Comparator<Integer> {

		private double[] distance;

		public VertexCmp(double[] distance) {
			this.distance = distance;
		}

		public int compare(Integer o1, Integer o2) {
			int dcmp = Double.compare(distance[o1], distance[o2]);
			if(dcmp == 0) return o1 - o2;
			return dcmp;
		}
	}

}
