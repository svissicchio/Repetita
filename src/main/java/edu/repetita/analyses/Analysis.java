package edu.repetita.analyses;

import edu.repetita.io.RepetitaWriter;

import java.util.Map;

/**
 * Class that stores the results of the Analyzer on a given Setting
 */

public class Analysis {
    public String id = null;
    public int totNodes;
    public int totEdges;
    public int totDemands;
    public double maxLinkLoad;
    public double maxLinkLoadLowerBound = -1;
    public int[] igpWeights;
    public Map<Integer,int[]> demands2SRPaths;
    public Map<Integer,int[]> demands2ExplicitPaths;

    public void setId(String identifier){
        this.id = identifier;
    }

    // Prints the most important characteristics of traffic from a traffic engineering viewpoint (only link maxLinkLoad, for the moment)
    public void printTrafficSummary() {
        RepetitaWriter.appendToOutput(this.getTrafficSummary());
    }

    public String getTrafficSummary(){
        StringBuilder sb = new StringBuilder();

        String identifier = "";
        if (this.id != null){
            identifier = this.id + " ";
        }
        sb.append(identifier);

        sb.append("max link utilization: ").append(this.maxLinkLoad);
        if (this.maxLinkLoadLowerBound != -1){
            sb.append(", ").append(identifier).append("mcf: ").append(this.maxLinkLoadLowerBound);
        }

        return sb.toString();
    }
}
