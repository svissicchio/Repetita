package edu.repetita.solvers.sr;

import edu.repetita.core.Demands;
import edu.repetita.core.Setting;
import edu.repetita.core.Topology;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.paths.SRPaths;
import edu.repetita.solvers.SRSolver;
import edu.repetita.solvers.sr.cg4sr.*;
import edu.repetita.solvers.sr.cg4sr.data.*;
import edu.repetita.solvers.sr.cg4sr.segmentRouting.SrPath;

import java.util.ArrayList;

import static edu.repetita.io.IOConstants.SOLVER_OBJVALUES_MINMAXLINKUSAGE;

/**
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class CG4SR extends SRSolver {

	private long solveTime = 0;

	@Override
	protected void setObjective() {
		objective = SOLVER_OBJVALUES_MINMAXLINKUSAGE;
	}

	@Override
	public String name() {
		return "CG4SR";
	}

	@Override
	public String getDescription() {
		return "A Segment Routing path optimizer implementing a Column Generation algorithm described in" +
				" \"Mathieu Jadin, Francois Aubry, Pierre Schaus, and Olivier Bonaventure." +
				" CG4SR: Near Optimal Traffic Engineering for Segment Routing with Column Generation." +
				" In INFOCOM, 2019.\"";
	}

	private static Graph<NetworkEdge> convertTopo(Topology topology) {
		Index<String> nodelabels = new Index<>();
		for (int i = 0; i < topology.nNodes; i++) {
			nodelabels.add(topology.nodeLabel[i]);
		}
		Graph<NetworkEdge> g = new Graph<>(nodelabels);
		for (int i = 0; i < topology.nEdges; i++) {
			if (topology.edgeWeight[i] != Topology.INFINITE_DISTANCE)
				g.addEdge(new NetworkEdge(topology.edgeSrc[i], topology.edgeDest[i], topology.edgeWeight[i],
						topology.edgeLatency[i], (int) topology.edgeCapacity[i]), false);
		}
		return g;
	}

	private static ArrayList<Demand> convertDemands(Demands demands) {
		ArrayList<Demand> demandArrayList = new ArrayList<>();
		for (int i = 0; i < demands.label.length; i++) {
			demandArrayList.add(new Demand(demands.label[i], demands.source[i], demands.dest[i],
					(int) demands.amount[i], i));
		}
		return demandArrayList;
	}

	@Override
	public void solve(Setting setting, long milliseconds) {
		boolean adjacency = false; // TODO Parametrise
		int maxSegments = 4; // TODO Parametrise
		int maxAddColumns = 10; // TODO Parametrise

		Topology topology = setting.getTopology();
		Graph<NetworkEdge> g = convertTopo(topology);
		Demands demands = setting.getDemands();
		ArrayList<Demand> demandArrayList = convertDemands(demands);

		SRTEInstance colgen_instance = new SRTEInstance(g, demandArrayList, maxSegments, 1);

		RepetitaWriter.appendToOutput("Model generated\n", 1);
		ECMPSplit<NetworkEdge> split = new EqualSplit<>(g);
		InitialColumnGen colInit = new SourceDestColInit();
		DemandSelector demandSelector = new StochasticRatioSelector();

		// Put Integer.MAX_VALUE if you want the lower bound
		// A lower value if you want to speed things up
		BinarySearch binSearch = new BinarySearch(colgen_instance, colInit, demandSelector, maxAddColumns,
				Integer.MAX_VALUE, adjacency, milliseconds);
		long timeBefore = System.nanoTime();
		binSearch.run();
		long timeAfter = System.nanoTime();
		this.solveTime = timeAfter - timeBefore;

        SRTESolution sol = binSearch.getSolution();
		SRPaths paths = new SRPaths(setting.getDemands(),setting.getTopology().nNodes);
		for (int demand = 0; demand < demands.nDemands; demand++) {
		    for (Pair<SrPath, Double> pair_path: sol.findDemandPath(demands.source[demand], demands.dest[demand], (int) demands.amount[demand])) {
		        if (pair_path.y() == 1.0) {
                    paths.setPath(demand, pair_path.x().toArray());
                    break;
                }
            }
		}
		setting.setSRPaths(paths);
	}

	@Override
	public long solveTime(Setting setting) {
		return solveTime;
	}
}
