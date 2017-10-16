package edu.repetita.analyses.specialized;

import edu.repetita.core.Setting;
import edu.repetita.analyses.Analysis;
import edu.repetita.simulators.FlowSimulator;
import edu.repetita.utils.MCF;

import java.util.ArrayList;
import java.util.List;

public class FlowSpecializedAnalyzer implements SpecializedAnalyzer {
    private FlowSimulator flowSimulator = FlowSimulator.getInstance();
    private boolean compareWithMCF = false;

    public void setCompareWithMCF(){
        this.compareWithMCF = true;
    }

    @Override
    public List<String> getDescription() {
        return new ArrayList(){{
            add("maximum link utilization, optionally including comparison with the theoretical lower bound (MCF solution)");
        }};
    }

    @Override
    public void analyze(Setting setting, Analysis analysis) {
        flowSimulator.setup(setting);
        flowSimulator.computeFlows();

        analysis.maxLinkLoad = this.getMaxLoad();

        double mcfLoad = -1;
        if (this.compareWithMCF){
            MCF mcfComputer = new MCF(setting.getTopology(), setting.getDemands(), false);
            mcfLoad = mcfComputer.computeMaxUtilization();
        }
        analysis.maxLinkLoadLowerBound = mcfLoad;
    }


    @Override
    public String compare(Analysis firstAnalysis, Analysis secondAnalysis, String firstTag, String secondTag){
        StringBuilder sb = new StringBuilder();

        // compare maxLinkLoad
        sb.append(firstTag).append(" max link utilization ").append(firstAnalysis.maxLinkLoad).append("\n");
        sb.append(secondTag).append(" max link utilization ").append(secondAnalysis.maxLinkLoad).append("\n");

        // future additional comparison to be added here...

        return sb.toString();
    }

    /* Analyses on objective function(s) */

    private double getMaxLoad(){
        return flowSimulator.getMaxUtilization();
    }
}
