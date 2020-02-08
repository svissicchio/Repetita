package edu.repetita.solvers.sr.cg4sr.data;

import java.util.*;

/**
 * 
 * @author Francois Aubry f.aubry@uclouvain.be
 *
 * @param <E>
 */
public class AllPairsShortestPaths<E extends WeightedEdge> {
	
	private Graph<E> g;
	private int n;
	private double[][] distance;
	private BitSet[][] next;
	
	/**
	 * Build an instance of all pairs shortest paths of a given graph.
	 * 
	 * It allows to retrieve the shortest path distance between any two
	 * given nodes and also the shortest path DAG between them.
	 * 
	 * @param g the graph on which to compute all pairs shortest paths.
	 */
	public AllPairsShortestPaths(Graph<E> g) {
		this.g = g;
		n = g.V();
		distance = new double[n][n];
		for(int x = 0; x < n; x++) {
			Arrays.fill(distance[x], Double.POSITIVE_INFINITY);
			distance[x][x] = 0;
		}
		next = new BitSet[n][n];
		for(int x = 0; x < n; x++) {
			for(int y = 0; y < n; y++) {
				next[x][y] = new BitSet();
			}
		}
		// base case
		for(E e : g.edges()) {
			distance[e.orig][e.dest] = e.weight();
			next[e.orig][e.dest].set(e.id);
		}
		// general case
		for(int k = 0; k < n; k++) {
			for(int i = 0; i < n; i++) {
				for(int j = 0; j < n; j++) {
					double dikj = distance[i][k] + distance[k][j];
					if(dikj == Double.POSITIVE_INFINITY) continue;
					if(Cmp.gr(distance[i][j], dikj)) {
						distance[i][j] = dikj;
						next[i][j].clear();
						next[i][j].or(next[i][k]);
					} else if(Cmp.eq(distance[i][j], dikj)) {
						next[i][j].or(next[i][k]);		
					}
				}
			}
		}
	}

	/**
	 * Get the number of nodes in the graph.
	 * 
	 * @return the number of nodes in the graph.
	 */
	public int getV() {
		return g.V();
	}
	
	/**
	 * Get the number of edges in the graph.
	 * 
	 * @return the number of edges in the graph.
	 */
	public int getE() {
		return g.E();
	}
	
	/**
	 * Get the graph.
	 * 
	 * @return the graph.
	 */
	public Graph<E> getGraph() {
		return g;
	}
	
	/**
	 * Get the shortest path length between two given nodes.
	 * 
	 * @param orig the source node.
	 * @param dest the destination node.
	 * @return the shortest path length between orig and dest.
	 */
	public double getDistance(int orig, int dest) {
		return distance[orig][dest];
	}
	
	/**
	 * Check whether a given edge belong to a shortest path between a given 
	 * origin and destination.
	 * @param orig the origin.
	 * @param dest the destination.
	 * @param e the edge to check.
	 * @return whether e belongs to a shortest path from orig to dest.
	 */
	public boolean isInShortestPath(int orig, int dest, E e) {
		return Cmp.eq(distance[orig][dest], distance[orig][e.orig] + e.weight() + distance[e.dest][dest]);
	}
	
	/**
	 * Compute a BitSet with all edges on the shortest path DAG 
	 * between a given source and destination.
	 * 
	 * The edges are indexed as the graph.
	 * 
	 * @param orig the source node.
	 * @param dest the destination node.
	 * @return a BitSet containing all edges on a shortest path
	 *         between orig and dest.
	 */
	public BitSet getDagEdges(int orig, int dest) {
		Queue<Integer> Q = new LinkedList<>();
		Q.add(orig);
		BitSet visited = new BitSet();
		visited.set(orig);
		visited.set(dest);
		BitSet dagEdges = new BitSet();
		while(!Q.isEmpty()) {
			int cur = Q.poll();
			dagEdges.or(next[cur][dest]);
			for(E e : g.outEdges(cur)) {
				if(!visited.get(e.dest)) {
					visited.set(e.dest);
					Q.add(e.dest);
				}
			}
		}
		return dagEdges;
	}
	
	/**
	 * Compute the shortest path DAG between a given source
	 * and destination.
	 * 
	 * @param orig the source node.
	 * @param dest the destination node.
	 * @return a graph containing all edges on a shortest path
	 *         between orig and dest.
	 */
	public Graph<E> getDag(int orig, int dest) {
		Queue<Integer> Q = new LinkedList<>();
		Q.add(orig);
		BitSet visited = new BitSet();
		visited.set(orig);
		visited.set(dest);
		Graph<E> dag = new Graph<>(g.V());
		ArrayList<E> edges = g.edges();
		while(!Q.isEmpty()) {
			int cur = Q.poll();
			int i = -1;
			while((i = next[cur][dest].nextSetBit(i + 1)) != -1) {
				dag.addEdge(edges.get(i), true);
				int next = edges.get(i).dest;
				if(!visited.get(next)) {
					visited.set(next);
					Q.add(next);
				}
			}
		}
		return dag;
	}

}

class Cmp {

	public static double eps = 1e-8;

	public static boolean eq(double x, double y) {
		return Math.abs(x - y) <= eps;
	}

	public static boolean geq(double x, double y) {
		return x >= y - eps;
	}

	public static boolean gr(double x, double y) {
		return x > y + eps;
	}

}
