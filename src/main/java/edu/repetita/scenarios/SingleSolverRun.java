package edu.repetita.scenarios;

import edu.repetita.core.Scenario;
import edu.repetita.analyses.Analysis;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.simulators.FlowSimulator;

/*
 *  Basic scenario: we run the solvers for a set amount of time,
 *  and check the maximum link utilization of the returned paths.
 */

public class SingleSolverRun extends Scenario {
    @Override
    public String getDescription() {
        return "Runs the given solver on an input setting";
    }

    @Override
    public void run(long timeMillis) {
        // perform pre-optimization analyses
        Analysis preOpt = analyzer.analyze(this.setting);
        preOpt.setId("pre-optimization");
        if (this.keepAnalyses){
            this.analyses.put("pre-optimization",preOpt);
        }

        // ask the solver to optimize
        this.solver.solve(this.setting,timeMillis);
        long optTime = this.solver.solveTime(this.setting);

        // perform post-optimization analyses
        Analysis postOpt = analyzer.analyze(this.setting);
        postOpt.setId("post-optimization");
        if (this.keepAnalyses){
            this.analyses.put("post-optimization",postOpt);
        }

        // print results
        this.print(analyzer.compare(preOpt,postOpt));
        this.print("Optimization time (in seconds): " + optTime / 1000000000.0);

        // save on paths file (if asked by the user)
        RepetitaWriter.writeToPathFile(FlowSimulator.getInstance().getNextHops());
    }
}
