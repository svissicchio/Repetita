package edu.repetita.analyses.specialized;

import edu.repetita.core.Setting;
import edu.repetita.analyses.Analysis;

import java.util.*;

public class OverheadSpecializedAnalyzer implements SpecializedAnalyzer {

    @Override
    public List<String> getDescription() {
        return new ArrayList(){{
            add("number of demands with configured segment routing paths");
            add("number of demands with configured explicit paths");
            add("number of modified segment routing paths between two configurations");
            add("number of modified explicit paths between two configurations");
        }};
    }

    @Override
    public void analyze(Setting setting, Analysis analysis) {
        analysis.totNodes = setting.getTopology().nNodes;
        analysis.totEdges = setting.getTopology().nEdges;
        analysis.totDemands = setting.getDemands().nDemands;

        Map<Integer,int[]> srPaths = new HashMap<>();
        if (setting.getSRPaths() != null){
            srPaths = setting.getSRPaths().getPathsWithIntermediateSegments();
        }
        analysis.demands2SRPaths = srPaths;

        Map<Integer,int[]> explicitPaths = new HashMap<>();
        if (setting.getExplicitPaths() != null){
            explicitPaths = setting.getExplicitPaths().getAllPaths();
        }
        analysis.demands2ExplicitPaths = explicitPaths;

        int[] edgeWeights = setting.getTopology().edgeWeight;
        analysis.igpWeights = new int[edgeWeights.length];
        System.arraycopy(edgeWeights,0,analysis.igpWeights,0, edgeWeights.length);
    }

    @Override
    public String compare(Analysis firstAnalysis, Analysis secondAnalysis, String firstTag, String secondTag){
        StringBuilder sb = new StringBuilder();

        // compare overhead and routing configurations
        Collection<Integer> changedExplicitPaths = this.getDemandsWithChangedExplicitPaths(firstAnalysis,secondAnalysis);
        if(!changedExplicitPaths.isEmpty()){
            sb.append(changedExplicitPaths.size() + " demands over " + firstAnalysis.totDemands + " (" +
                    (100.0*changedExplicitPaths.size())/firstAnalysis.totDemands + "%) " +
                    "have a different explicit path between " + firstTag + " and " + secondTag + "\n");
        }

        Collection<Integer> changedSrPaths = this.getDemandsWithChangedSRPaths(firstAnalysis,secondAnalysis);
        if(!changedSrPaths.isEmpty()){
            sb.append(changedSrPaths.size() + " demands over " + firstAnalysis.totDemands + " (" +
                    (100.0*changedSrPaths.size())/firstAnalysis.totDemands + "%) " +
                    "have a different segment routing path between " + firstTag + " and " + secondTag + "\n");
        }

        Collection<Integer> linksWithChangedWeights = this.getLinksWithChangedWeight(firstAnalysis,secondAnalysis);
        if(!linksWithChangedWeights.isEmpty()){
            sb.append(linksWithChangedWeights.size() + " links over " + firstAnalysis.totEdges + " (" +
                    (100.0*linksWithChangedWeights.size())/firstAnalysis.totEdges + "%) " +
                    "have a different IGP weight between " + firstTag + " and " + secondTag + "\n");
        }

        return sb.toString();
    }

    /* Analyses on mechanisms used for routing */
    private Collection<Integer> getDemandsWithSRPaths(Analysis analysis) {
        return analysis.demands2SRPaths.keySet();
    }

    private Collection<Integer> getDemandsWithExplicitPaths(Analysis analysis) {
        return analysis.demands2ExplicitPaths.keySet();
    }

    /* Analyses on configuration changes */
    /* The following methods assume that firstAnalysis and secondAnalysis are defined on
    /* (i) the same topology (only link weights can be different)
    /* (ii) the same set of demands (only demand volumes can be different) */

    private Collection<Integer> getLinksWithChangedWeight(Analysis firstAnalysis, Analysis secondAnalysis) {
        Collection<Integer> linksWithNewWeights = new ArrayList<>();

        // check that the two analyses have the same number of link weights
        assert firstAnalysis.igpWeights.length == secondAnalysis.igpWeights.length;

        // check which weights are different
        for(int ind = 0; ind < firstAnalysis.igpWeights.length; ind++){
            if(firstAnalysis.igpWeights[ind] != secondAnalysis.igpWeights[ind]){
                linksWithNewWeights.add(ind);
            }
        }

        return linksWithNewWeights;
    }

    private Collection<Integer> getDemandsWithChangedSRPaths(Analysis firstAnalysis, Analysis secondAnalysis){
        return this.getDemandsWithChangedPaths(firstAnalysis.demands2SRPaths,secondAnalysis.demands2SRPaths);
    }

    private Collection<Integer> getDemandsWithChangedExplicitPaths(Analysis firstAnalysis, Analysis secondAnalysis){
        return this.getDemandsWithChangedPaths(firstAnalysis.demands2ExplicitPaths,secondAnalysis.demands2ExplicitPaths);
    }

    // generic method to compute path differences, taking as input two maps, each associating: demand -> path by edges
    private Collection<Integer> getDemandsWithChangedPaths(Map<Integer,int[]> firstPaths, Map<Integer,int[]> secondPaths){
        Collection<Integer> demandsWithChangedPaths = new ArrayList<>();
        Set<Integer> demandsWithEqualPaths = new HashSet<>();

        // check whether both first and second paths are empty
        if ((firstPaths == null || firstPaths.isEmpty()) && (secondPaths == null || secondPaths.isEmpty())){
            return  new LinkedList<>();
        }

        // if they are not empty, iterate on both of them and store the differences
        for(int dem = 0; dem < firstPaths.keySet().size(); dem++){
            if(!Arrays.equals(firstPaths.get(dem),secondPaths.get(dem))){
                demandsWithChangedPaths.add(dem);
            }
            else{
                demandsWithEqualPaths.add(dem);
            }
        }
        for(int dem = 0; dem < secondPaths.keySet().size(); dem++) {
            if (demandsWithEqualPaths.contains(dem)){
                continue;
            }
            if (!Arrays.equals(firstPaths.get(dem), secondPaths.get(dem))) {
                demandsWithChangedPaths.add(dem);
            }
        }

        return demandsWithChangedPaths;
    }
}
