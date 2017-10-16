package edu.repetita.analyses.specialized;
import edu.repetita.core.Setting;
import edu.repetita.analyses.Analysis;

import java.util.List;

public interface SpecializedAnalyzer {

    public List<String> getDescription();

    public void analyze(Setting setting, Analysis result);

    public String compare(Analysis firstAnalysis, Analysis secondAnalysis, String firstTag, String secondTag);

}
