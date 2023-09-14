package edu.repetita.solvers.sr.cg4sr;

import edu.repetita.io.RepetitaWriter;
import edu.repetita.solvers.sr.cg4sr.data.Demand;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class BinarySearch {

    private SRTEInstance instance;
    private InitialColumnGen colInit;
    private DemandSelector demSelector;
    private int maxAddColumns, maxEqValueIterations;
    private boolean adjacency;
    private long timeout;
    private SRTESolution sol;

    public BinarySearch(SRTEInstance instance, InitialColumnGen colInit, DemandSelector demSelector, int maxAddColumns,
                        int maxEqValueIterations, boolean adjacency, long timeout) {
        this.instance = instance;
        this.colInit = colInit;
        this.demSelector = demSelector;
        this.maxAddColumns = maxAddColumns;
        this.maxEqValueIterations = maxEqValueIterations;
        this.adjacency = adjacency;
        this.timeout = timeout;
    }

    public SRTESolution getSolution() {
        return sol;
    }

    public void run() {
        double totalDemandVol = 0;
        for(Demand demand : instance.getDemands()) {
            totalDemandVol += demand.volume;
        }
        RepetitaWriter.appendToOutput("Total demand is " + totalDemandVol, 1);


        RepetitaWriter.appendToOutput("Solving Column Generation with full capacity", 1);

        double lb = 0.88;  // XXX Lower bound of MCF of Repetita instances TODO Derive it from MCF
        double ub = 1.5;  // Already too high TODO Parametrise

        SRTEColGen colGen = new SRTEColGen(instance, colInit, demSelector, maxAddColumns, maxEqValueIterations, ub,
                adjacency,  this.timeout * 1000);

        double value = colGen.run();

        RepetitaWriter.appendToOutput(String.format("Found a solution at %.3f", value), 1);
        while(Math.abs(ub - lb) > 1e-2 && !colGen.isTimedOut()) {
            double mid = (lb + ub) / 2;
            RepetitaWriter.appendToOutput(String.format("Binary search interval: [%.3f, %.3f]", lb, ub), 1);
            RepetitaWriter.appendToOutput(String.format("Solving Column Generation with factor %.3f of link capacities", mid), 1);

            colGen.updateCapacityFactor(mid);
            double curValue = colGen.run();
            RepetitaWriter.appendToOutput(String.format("Found a solution at value=%.3f\n", curValue), 1);

            if (curValue < value - 1e-5) {
                lb = mid;
            } else {
                ub = mid;
            }
        }
        RepetitaWriter.appendToOutput("The number of paths explored during the binary search is " + colGen.getModel().getDemandPathMap().size(), 2);

        colGen.stopTimeoutThread();

        SRTESolution sol_temp = colGen.getModel().getSolution();
        RepetitaWriter.appendToOutput("Solution before enforcing integrity constraint", 2);
        sol_temp.print();

        RepetitaWriter.appendToOutput("Solving the model with integrity constraints", 1);
        MinFactorColGenModel mip = new MinFactorColGenModel(instance, colGen.getModel().getDemandPathMap(),
                instance.getTotalDemand(), true);

        double obj = mip.optimize();

        sol = mip.getSolution();

        RepetitaWriter.appendToOutput("Solution: ", 2);
        sol.print();
    }
}
