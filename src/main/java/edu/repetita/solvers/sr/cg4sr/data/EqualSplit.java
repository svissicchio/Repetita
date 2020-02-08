package edu.repetita.solvers.sr.cg4sr.data;

import java.util.Arrays;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class EqualSplit<E extends NetworkEdge> implements ECMPSplit<E> {
	
	private double[][][] nodeSplit;
	private EdgeValueMap[][] edgeSplit;
	private double[][] maxRatio;
	private NetworkEdge[][] criticalEdge;
	private Graph<E>[][] dags;
	private AllPairsShortestPaths<E> apsp;
	
	@SuppressWarnings("unchecked")
	public EqualSplit(Graph<E> g) {
		AllPairsShortestPaths<E> apsp = new AllPairsShortestPaths<>(g);
		this.apsp = apsp;
		nodeSplit = new double[apsp.getV()][apsp.getV()][apsp.getV()];
		edgeSplit = new EdgeValueMap[apsp.getV()][apsp.getV()];
		//edgeSplit = new double[apsp.getV()][apsp.getV()][apsp.getE()];

		maxRatio = new double[apsp.getV()][apsp.getV()];
		criticalEdge = new NetworkEdge[apsp.getV()][apsp.getV()];
		dags = new Graph[apsp.getV()][apsp.getV()];
		// loop over all pairs to compute the forwarding graphs
		TopologicalSort<E> toposort;
		for(int x = 0; x < apsp.getV(); x++) {
			for(int y = 0; y < apsp.getV(); y++) {
				//  compute forw(x, y)
				edgeSplit[x][y] = new EdgeValueMap();
				dags[x][y] = apsp.getDag(x, y);
				nodeSplit[x][y][x] = 1.0;
				maxRatio[x][y] = Double.NEGATIVE_INFINITY;
				criticalEdge[x][y] = null;
				toposort = new TopologicalSort<>(dags[x][y]);
				for(int z : toposort.order) {
					int outdeg = dags[x][y].outDeg(z);
					for(E e : dags[x][y].outEdges(z)) {
						double split = nodeSplit[x][y][e.orig()] / outdeg;
						edgeSplit[x][y].add(e, split);
						if(split > maxRatio[x][y] * e.cap()) {
							maxRatio[x][y] = split / e.cap();
							criticalEdge[x][y] = e;
						}
						nodeSplit[x][y][e.dest()] += split;
					}
				}
			}
		}
	}

	public Graph<E> getDag(int x, int y) {
		return dags[x][y];
	}

	public double minEdgeSplit(int x, int y) {
		return maxRatio[x][y];
	}

	public NetworkEdge getCriticalEdge(int x, int y) {
		return criticalEdge[x][y];
	}

	public double getValue(int x, int y, E edge) {
		if(edgeSplit[x][y].contains(edge)) {
			return edgeSplit[x][y].get(edge);
		}
		return 0;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Index<String> nodeLabels = apsp.getGraph().getNodeLabels();
		for(int x = 0; x < apsp.getV(); x++) {
			for(int y = 0; y < apsp.getV(); y++) {
				sb.append(String.format("%s %s: ", nodeLabels.get(x), nodeLabels.get(y)));
				for(E e : dags[x][y].edges()) {
					sb.append(String.format("(%s, %s, %.3f) ", nodeLabels.get(e.orig()), nodeLabels.get(e.dest()), edgeSplit[x][y].get(e)));
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}

}