package edu.repetita.solvers.sr.cg4sr;

import edu.repetita.solvers.sr.cg4sr.data.Demand;
import edu.repetita.solvers.sr.cg4sr.data.FenwickTree;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class StochasticRatioSelector implements DemandSelector {

    private ArrayList<Demand> demands;
    private FenwickTree ft;
    private Random random;

    public StochasticRatioSelector() {
        random = new Random();
    }

    public void init(ArrayList<Demand> demands) {
        this.demands = demands;
        // initialize the Fenwick tree with the demand values
        ft = new FenwickTree(demands.size());
        for(int i = 0 ; i < demands.size(); i++) {
            ft.set(i, demands.get(i).volume);
        }
    }

    public int getNextDemand() {
        // compute the total demand volume currently in the tree
        long maxvalue = ft.totalsum();
        // generate a random target
        long target = (long)Math.ceil(random.nextDouble() * maxvalue);
        // perform a binary search to find the demand
        int lb = 0;
        int ub = demands.size() - 1;
        while(lb != ub) {
            int mid = (lb + ub) / 2;
            long sum = ft.acsum(0, mid);
            if(sum < target) {
                lb = mid + 1;
            } else if(sum >= target) {
                ub = mid;
            }
        }
        ft.set(lb, 0);
        return lb;
    }

    public boolean hasNext() {
        return ft.totalsum() > 0;
    }

    public void endSelectionStep() {
        for(int i = 0 ; i < demands.size(); i++) {
            ft.set(i, demands.get(i).volume);
        }
    }


}
