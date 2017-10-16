package edu.repetita.solvers;

import edu.repetita.core.Solver;

public abstract class IGPWOSolver extends Solver {
    protected int verbose = 1;
    protected int maxWeight = 100;

    public void setMaxWeight(int weight){
        this.maxWeight = weight;
    }

    public void setVerbose(int level) { this.verbose = level; }
}
