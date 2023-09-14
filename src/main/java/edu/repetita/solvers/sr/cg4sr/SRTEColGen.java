package edu.repetita.solvers.sr.cg4sr;

import edu.repetita.io.RepetitaWriter;
import edu.repetita.solvers.sr.cg4sr.config.RunConfig;
import edu.repetita.solvers.sr.cg4sr.data.Demand;
import edu.repetita.solvers.sr.cg4sr.data.NetworkEdge;
import edu.repetita.solvers.sr.cg4sr.data.Pair;
import edu.repetita.solvers.sr.cg4sr.data.Tuple4;
import edu.repetita.solvers.sr.cg4sr.segmentRouting.SrPath;
import edu.repetita.solvers.sr.cg4sr.threading.PricingThread;
import edu.repetita.solvers.sr.cg4sr.threading.TimeoutThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class SRTEColGen {

    private SRTEInstance instance;
    private SRTEColGenModel model;
    private int maxAddColumns, maxEqualIterations;
    private double objVal;
    private boolean adjacency;

    private ArrayBlockingQueue<Tuple4<double[][], double[], Double, Integer>> processPricingQueue;
    private Semaphore mainSem;
    private int threadNumber;
    private AtomicInteger columnAdded = new AtomicInteger(0);
    private ArrayList<PricingThread> threads;

    private ArrayList<Demand> sortedDemands;

    private TimeoutThread timeoutThread;

    public SRTEColGen(SRTEInstance instance, InitialColumnGen initialColumnGen, DemandSelector demandSelector,
                      int maxAddColumns, int maxEqualIterations, double capacityFactor, boolean adjaceny,
                      long timeout) {
        this.instance = instance;
        demandSelector.init(instance.getDemands());
        this.maxAddColumns = maxAddColumns;
        this.maxEqualIterations = maxEqualIterations;
        this.adjacency = adjaceny;
        objVal = 0;
        RepetitaWriter.appendToOutput("Creating model", 1);
        model = new SRTEColGenModel(instance, initialColumnGen, capacityFactor, false);

        sortedDemands = new ArrayList<>();
        for(Demand d : instance.getDemands()) {
            sortedDemands.add(d);
        }

        if (timeout > 0) {
            startTimeoutThread(timeout);
        }
    }

    public double run() {
        RepetitaWriter.appendToOutput("Running column generation", 2);
        int nbColumnsAdded;
        int iteration = 1;
        double oldObjectiveValue = -1;
        int equalCount = 0;

        startPricingThreads();

        do {
            RepetitaWriter.appendToOutput(String.format("----- START OF ITERATION %d -----", iteration), 4);
            RepetitaWriter.appendToOutput("Solving LP", 4);
            double value = model.optimize();

            objVal = value;
            if (this.isTimedOut()) {
                break;
            }

            if(oldObjectiveValue < 0 || oldObjectiveValue != value) {
                equalCount = 1;
            } else  {
                equalCount += 1;
            }
	        oldObjectiveValue = value;
            RepetitaWriter.appendToOutput("LP Solved: objective = " + value, 4);

            RepetitaWriter.appendToOutput("Computing dual values", 4);
            Pair<double[], double[]> dualValues = model.getDualValues();
            double[] edgeDual = dualValues.x();
            double[] demandDual = dualValues.y();

            RepetitaWriter.appendToOutput("Finding columns", 4);
            nbColumnsAdded = findNewColumns(edgeDual, demandDual);

            RepetitaWriter.appendToOutput(String.format("----- END OF ITERATION %d -----\n", iteration), 4);
            iteration += 1;
        } while(equalCount < maxEqualIterations && nbColumnsAdded > 0);

        endPricingThreads();

        RepetitaWriter.appendToOutput("finished column generation", 2);
        return objVal;
    }

    private void startPricingThreads() {
        processPricingQueue = new ArrayBlockingQueue<>(maxAddColumns);
        mainSem = new Semaphore(0);
        threadNumber = Runtime.getRuntime().availableProcessors();
        columnAdded = new AtomicInteger(0);
        threads = new ArrayList<>();

        for (int i = 0; i < threadNumber; i++) {
            PricingThread t = new PricingThread(this, processPricingQueue, mainSem, columnAdded, threadNumber);
            threads.add(t);
            t.start();
        }

    }

    private class DemandCmp implements Comparator<Demand> {

        private double[] demandDual;

        public DemandCmp(double[] demandDual) {
            this.demandDual = demandDual;
        }

        public int compare(Demand d1, Demand d2) {
            double val1 = (d1.volume - demandDual[d1.index]) / d1.volume;
            double val2 = (d2.volume - demandDual[d2.index]) / d2.volume;
            return -Double.compare(val1, val2);
        }

    }

    private int findNewColumns(double[] edgeDual, double[] demandDual) {
        double[][] w = getWeights(edgeDual);
        columnAdded.set(0);
        sortedDemands.sort(new DemandCmp(demandDual));
        try {
            int demandIndex = 0;
            while (columnAdded.get() < maxAddColumns && demandIndex < sortedDemands.size()
                    && !this.isTimedOut()) {
                Demand demand = sortedDemands.get(demandIndex);
                Tuple4<double[][], double[], Double, Integer> elem = new Tuple4<>(w, edgeDual, demandDual[demand.index], demand.index);
                processPricingQueue.put(elem);
                demandIndex += 1;
            }
            /* Poison the bounded buffer */
            for (int i = 0; i < threadNumber; i++) {
                processPricingQueue.put(new Tuple4<>(null, null, 0.0, 0)); // Poison for one iteration end
            }
            /* Wait for all columns to be added */
            mainSem.acquire();
        } catch (InterruptedException e) {
            RepetitaWriter.appendToOutput("Finding new columns was interrupted", 0);
        }
        return columnAdded.get();
    }

    private void endPricingThreads() {

        /* Final poisoning of the bounded buffer because the stop variable is set to true */
        try {
            for (int i = 0; i < threadNumber; i++) {
                processPricingQueue.put(new Tuple4<>(null,null, null, null)); // Final poison
            }
            for (int i = 0; i < threadNumber; i++) {
                threads.get(i).join();
            }
        } catch (InterruptedException e) {
            RepetitaWriter.appendToOutput("Ending pricing threads was interrupted", 0);
        }
    }

    private void startTimeoutThread(long timeout) {
        timeoutThread = new TimeoutThread(timeout);
        timeoutThread.start();
    }

    public boolean isTimedOut() {

        return TimeoutThread.timedOut.get();
    }

    public void stopTimeoutThread() {
        if (timeoutThread != null) {
            TimeoutThread.stopMonitoring.set(true);
            try {
                timeoutThread.join();
            } catch (InterruptedException e) {

            }
            TimeoutThread.stopMonitoring.set(false);
        }
    }

    public SRTEColGenModel getModel() {
        return model;
    }

    public SRTEInstance getInstance() {
        return instance;
    }

    public void updateCapacityFactor(double capacityFactor) {
        model.updateCapacityFactor(capacityFactor);
    }

    public double getObjectiveValue() {
        return objVal;
    }

    public Pair<SrPath, Double> pricingProblemMaxSeg(double[][] w, double[] edgeDual, double demandDual, Demand demand) {
        int s = demand.src;
        int t = demand.dst;
        // compute the shortest s-t path with at most maxSegEdges
        int maxSeg = instance.getMaxSeg();
        double[][] dist = new double[maxSeg + 1][instance.nbNodes()];
        for (int i = 0; i <= maxSeg; i++) {
            Arrays.fill(dist[i], Double.POSITIVE_INFINITY);
        }
        dist[0][s] = 0;
        Parent[][] parent = new Parent[maxSeg + 1][instance.nbNodes()];
        for (int i = 1; i <= maxSeg; i++) {
            for (int cur = 0; cur < instance.nbNodes(); cur++) {
                // no not use a new segment (1)
                dist[i][cur] = dist[i - 1][cur];
                parent[i][cur] = parent[i - 1][cur];
                // can we reach cur by coming from prev (2)
                for (int prev = 0; prev < instance.nbNodes(); prev++) {
                    if (prev == cur) continue;
                    // check whether the segment (prev, cur) has enough capacity
                    // min_e split(e) / cap(e) <= capacityFactor / d.volume <=>
                    // split.minEdgeSplit(prev, cur) <= capacityFactor / d.volume
                    NetworkEdge ce = instance.getCriticalEdge(prev, cur);
                    if (instance.getSplitValue(prev, cur, ce) * demand.volume <= model.getCapacityFactor() * ce.cap()) {
                        // segment capacity is ok
                        double l = dist[i - 1][prev] + w[prev][cur];
                        if (l < dist[i][cur]) {
                            // found a better path
                            dist[i][cur] = l;
                            parent[i][cur] = new Parent(prev);
                        }
                    }
                }
                // check for adjacency segments
                if(adjacency) {
                    if(i < 2) continue;
                    for(NetworkEdge e : instance.getGraph().inEdges(cur)) {
                        // first wat to use adjacency segments dp[i - 2][orig] + edgeDual[e]
                        // check whether e has enough capacity
                        if(demand.volume <= model.getCapacityFactor() * e.cap()) {
                            double l = dist[i - 2][e.orig()] + edgeDual[e.id()];
                            if (l < dist[i][cur]) {
                                dist[i][cur] = l;
                                parent[i][cur] = new Parent(e);
                            }
                        }
                        // second way to use adjacency segments dp[i - 2][prev] + w[prev][orig] + edgeDual[e]
                        for(int prev = 0; prev < instance.nbNodes(); prev++) {
                            if(prev == e.orig()) continue;
                            NetworkEdge ce = instance.getCriticalEdge(prev, e.orig());
                            if (instance.getSplitValue(prev, e.orig(), ce) * demand.volume <= model.getCapacityFactor() * ce.cap()) {
                                double l = dist[i - 2][prev] + w[prev][e.orig()] + edgeDual[e.id()];
                                if (l < dist[i][cur]) {
                                    dist[i][cur] = l;
                                    parent[i][cur] = new Parent(prev, e);
                                }
                            }
                        }

                    }
                }
            }
        }
        // check whether a path was found
        if (parent[maxSeg][t] == null) return null;
        // build the path
        SrPath path = new SrPath();
        Integer cur = t;
        double pathWeight = 0;
        while (parent[maxSeg][cur] != null) {
            Parent p = parent[maxSeg][cur];
            if(p.type() == 0) {
                path.add(cur);
                pathWeight += w[p.node][cur];
                maxSeg -= 1;
                cur = p.node;
            } else if(p.type() == 1) {
                path.add(p.edge);
                pathWeight += edgeDual[p.edge.id()];
                maxSeg -= 2;
                cur = p.edge.orig();
            } else {
                path.add(p.edge);
                pathWeight += w[p.node][p.edge.orig()] + edgeDual[p.edge.id()];
                maxSeg -= 2;
                cur = p.node;
            }
        }

        if(path.get(path.size() - 1).x1() != s) {
            path.add(s);
        }

        // check whether the path satisfies the dual condition
        if (demandDual + (pathWeight - 1) * demand.volume < -RunConfig.PRICING_EPS) {
            path.reverse();
            return new Pair<>(path, demandDual + (pathWeight - 1) * demand.volume);
        }
        return null;
    }

    private double[][] getWeights(double[] edgeDual) {
        // compute the edge weights
        double[][] w = new double[instance.nbNodes()][instance.nbNodes()];
        for (int x = 0; x < w.length; x++) {
            for (int y = 0; y < w[x].length; y++) {
                for (NetworkEdge e : instance.getSplit().getDag(x, y).edges()) {
                    w[x][y] += instance.getSplitValue(x, y, e) * edgeDual[e.id()];
                }
            }
        }
        return w;
    }


    private class Parent {

        private Integer node;
        private NetworkEdge edge;

        public Parent(int node) {
            this.node = node;
        }

        public Parent(NetworkEdge edge) {
            this.edge = edge;
        }

        public Parent(int node, NetworkEdge edge) {
            this.node = node;
            this.edge = edge;
        }

        public int type() {
            if(this.node != null && this.edge != null) return 2;
            if(this.node != null) return 0;
            return 1;
        }

    }

}
