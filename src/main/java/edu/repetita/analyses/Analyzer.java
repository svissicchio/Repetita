package edu.repetita.analyses;

import edu.repetita.core.Setting;
import edu.repetita.analyses.specialized.OverheadSpecializedAnalyzer;
import edu.repetita.analyses.specialized.FlowSpecializedAnalyzer;
import edu.repetita.analyses.specialized.SpecializedAnalyzer;
import edu.repetita.io.RepetitaWriter;

import java.util.*;

/**
 * Object that analyzes settings, extracting several metrics of interest
 */

public class Analyzer {

    // Singleton implementation
    private static Analyzer instance = new Analyzer();

    private Analyzer(){}

    public static Analyzer getInstance(){
        return instance;
    }

    // private variables of the Analyzer instance
    private FlowSpecializedAnalyzer flowAnalyzer = new FlowSpecializedAnalyzer();
    private List<SpecializedAnalyzer> specializedAnalyzers = new ArrayList(){{
        add(flowAnalyzer);
        add(new OverheadSpecializedAnalyzer());
    }};

    public void addComparisonWithMCF(){
        flowAnalyzer.setCompareWithMCF();
    }

    public String getDescription(){
        List<String> descriptions = new ArrayList<>();
        for (SpecializedAnalyzer sa: specializedAnalyzers){
            descriptions.addAll(sa.getDescription());
        }
        return "Performed analyses:\n" + RepetitaWriter.formatAsListOneColumn(descriptions);
    }

    /**
     * Extracts important information from an input setting, and store it into an Analysis object
     *
     * @param setting the setting to be analyzed
     * @return the Analysis object
     */
    public Analysis analyze(Setting setting){
        Analysis result = new Analysis();

        // lets the specialized analyzers fill the analysis
        for (SpecializedAnalyzer sa: this.specializedAnalyzers){
            sa.analyze(setting,result);
        }

        return result;
    }

    /**
     * Extracts important information from an input setting, and store it into an Analysis object.
     * It also sets the id of the Analysis object.
     *
     * @param setting the setting to be analyzed
     * @param analysisId a string that acts as identifier of the returned Analysis object
     * @return the Analysis object
     */
    public Analysis analyze(Setting setting, String analysisId){
        Analysis result = this.analyze(setting);
        result.setId(analysisId);
        return result;
    }

    /**
     * Compares two Analyses objects -- e.g., in terms of changed paths and link weights
     *
     * @param firstAnalysis the first Analysis object to be compared
     * @param secondAnalysis the second Analysis object to be compared
     * @return a string describing the differences between the input Analysis objects
     */
    public String compare(Analysis firstAnalysis, Analysis secondAnalysis){
        String firstTag = this.getAnalysisId(firstAnalysis,"First");
        String secondTag = this.getAnalysisId(secondAnalysis,"Second");
        StringBuilder sb = new StringBuilder();

        for (SpecializedAnalyzer sa: this.specializedAnalyzers){
            sb.append(sa.compare(firstAnalysis,secondAnalysis,firstTag,secondTag));
        }

        return sb.toString();
    }

    /* Helper methods */
    private String getAnalysisId(Analysis analysis, String defaultId) {
        String tag = defaultId;
        if (analysis.id != null){
            tag = analysis.id;
        }
        return tag;
    }

}
