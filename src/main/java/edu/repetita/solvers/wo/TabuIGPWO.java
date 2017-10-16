package edu.repetita.solvers.wo;

import edu.repetita.core.Setting;
import edu.repetita.core.Topology;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.simulators.FlowSimulator;
import edu.repetita.solvers.IGPWOSolver;
import edu.repetita.paths.ShortestPaths;
import edu.repetita.solvers.wo.tabuLS.*;

import java.util.Random;

public class TabuIGPWO extends IGPWOSolver {

    // internal variables
    private FlowSimulator flowSimulator = FlowSimulator.getInstance();
    private Random random = new Random();
    private long solveTimeValue;
    private TabuTableWeightVectorArray tabuWeights;
    private TabuTableScore tabuScore = new TabuTableScore(2);

    // Tabu search parameters
    private double initialSamplingRate = 0.2;
    private boolean tabuAllMoves = false;
    private boolean resetStateWhenResetTabu = false;
    private int tabuSize = 16;


    @Override
    protected void setObjective() {
        this.objective = 0;
    }

    @Override
    public String name() {
        return "TabuIGPWO";
    }

    @Override
    public String getDescription() {
        return "An IGP weight optimizer inspired by \"B. Fortz and M. Thorup. Internet traffic engineering by" +
                "optimizing OSPF weights. In INFOCOM, 2000.\"";
    }

    @Override
    public void solve(Setting setting, long milliseconds) {
        this.solveTimeValue = optimizeWeights(setting, milliseconds);
    }

    @Override
    public long solveTime(Setting setting) {
        return this.solveTimeValue;
    }


    /* private methods */

    private long optimizeWeights(Setting setting, long timeMillis) {
        // time recording
        long startTime = System.nanoTime();
        long stopTime = startTime + 1000000 * timeMillis;

        // variable initialization
        Topology topology = setting.getTopology();
        int nEdges = topology.nEdges;
        double samplingRate = initialSamplingRate;

        // initialization of the tabu search states and objects
        State currentState = new State(topology.edgeWeight);
        State bestDelta = new State(topology.edgeWeight.clone());
        State bestState = new State(topology.edgeWeight.clone());

        this.flowSimulator.setup(setting);
        this.flowSimulator.computeFlows();
        double currentScore = flowSimulator.getMaxUtilization();
        double bestScore = currentScore;

        Neighborhood[] neighborhoods = {
                new NeighborhoodSingleWeightChange(currentState, nEdges, maxWeight),
                new NeighborhoodEvenlyBalancingFlows(currentState, topology, maxWeight, new ShortestPaths(topology))
        };

        int nNeighborhoods = neighborhoods.length;
        long nMoves = 0L;

        this.tabuWeights = new TabuTableWeightVectorArray(tabuSize, nEdges);


        // do exploring iterations
        for (int iteration = 1; System.nanoTime() < stopTime; iteration++) {
            for (int i = 0; i < nNeighborhoods; i++) {
                int nNeighbors = (int) (samplingRate * neighborhoods[i].size());
                double deltaScore = exploreNeighborhood(currentState, bestDelta, neighborhoods[i], nNeighbors, currentScore, bestScore);

                if (deltaScore < Double.MAX_VALUE) {
                    bestDelta.applyDeltaTo(currentState);
                    tabuWeights.forbid(currentState, true);
                    tabuScore.forbid(deltaScore, true);
                    currentState.save();
                    currentScore = deltaScore;
                } else {
                    RepetitaWriter.appendToOutput("Clearing tabu table, neighborhood " + i +
                                                    " found a score of " + deltaScore + " at iteration " + iteration,
                                                  1);
                    tabuWeights.reset();

                    if (resetStateWhenResetTabu && deltaScore > bestScore) {
                        if (random.nextInt(5) != 0) {
                            // switch back to the best solution
                            bestState.copyTo(currentState);
                            currentState.save();
                            currentScore = bestScore;
                        } else {
                            // randomize state
                            for (int edge = 0; edge < nEdges; edge++) {
                                currentState.set(edge, 1 + random.nextInt(maxWeight - 1));
                                currentState.save();
                                flowSimulator.computeFlows();
                                currentScore = flowSimulator.getMaxUtilization();
                            }
                        }
                    }
                }

                if (deltaScore < bestScore) {
                    currentState.copyTo(bestState);
                    bestScore = deltaScore;
                    long bestTime = (System.nanoTime() - startTime) / 1000000;
                    RepetitaWriter.appendToOutput("Current best is " + bestScore + " in " + bestTime + "ms",
                                                  1);
                }

                nMoves += nNeighbors;
            }
        }

        bestState.copyTo(currentState);
        currentState.save();

        // compute and return execution time
        long totalTime = (System.nanoTime() - startTime);
        RepetitaWriter.appendToOutput(((double) nMoves * 1000000 / totalTime) + " moves/ms " + nMoves + " in " +
                                        totalTime / 1000000 + " ms",
                                      1);

        return totalTime;
    }

    private double exploreNeighborhood(State currentState, State bestDelta, Neighborhood neighborhood, int nTrials, double currentScore, double bestScore) {
        double localScore = Double.MAX_VALUE;

        for (int trial = 0; trial < nTrials; trial++) {
            neighborhood.applyRandom();
            if (currentState.deltaSize() == 0) continue;

            // if tabu allows the move, evaluate the new state
            boolean tabuAllowed = tabuWeights.isAllowed(currentState);
            if (tabuAllowed) { // || (doScoreAspiration && score < bestScore)) {
                flowSimulator.computeFlows();
                double score = flowSimulator.getMaxUtilization();

                tabuWeights.forbid(currentState, false || tabuAllMoves);

                // remember if best yet
                if (score < localScore && tabuScore.isAllowed(score)) {
                    bestDelta.restore();
                    currentState.applyDeltaTo(bestDelta);
                    localScore = score;
                }
            }

            currentState.restore();
        }

        return localScore;
    }
}
