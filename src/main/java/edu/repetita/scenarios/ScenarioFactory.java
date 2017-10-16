package edu.repetita.scenarios;

import edu.repetita.core.Scenario;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.utils.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioFactory {

    // exists only to defeat instantiation
	private ScenarioFactory() {
	}

    public static Map<String,Scenario> getAllScenarios(){
        Map<String,Scenario> scenarios = new HashMap<>();
        Package pkg = ScenarioFactory.class.getPackage();

        for (String className: Reflections.getClassesForPackage(pkg)){
            try{
                Scenario s = (Scenario) Class.forName(className).newInstance();
                scenarios.put(s.name(),s);
            }
            catch(Exception e){
                RepetitaWriter.appendToOutput("Class " + className + " in package " + pkg.getName() + " is not a Scenario",2);
            }
        }

        return scenarios;
    }

    public static String getScenariosDescription() {
        Map<String,Scenario> scenarios= getAllScenarios();

        List<String> descriptions = new ArrayList<>();
        scenarios.values().forEach((scenario) -> descriptions.add(scenario.getDescription()));

        return "Scenarios to evaluate the solutions computed by the above algorithms\n" +
                RepetitaWriter.formatAsListTwoColumns(new ArrayList<>(scenarios.keySet()),descriptions);
    }
}
