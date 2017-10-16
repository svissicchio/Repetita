package edu.repetita.simulators;

import edu.repetita.io.IOConstants;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.simulators.specialized.SpecializedFlowSimulator;
import edu.repetita.utils.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulatorFactory {
    private static SimulatorFactory instance = null;

    private Map<String,SpecializedFlowSimulator> simulators;

    public static SimulatorFactory getInstance(){
        if (instance == null){
            instance = new SimulatorFactory();
        }
        return instance;
    }

    // private to defeat direct instantiation
	private SimulatorFactory() {
        this.loadAllFlowSimulators();
	}

	private void loadAllFlowSimulators() {
        this.simulators = new HashMap<>();
        Package pkg = SimulatorFactory.class.getPackage();

        for (String className : Reflections.getClassesForPackage(pkg)) {
            if (className.equals(this.getClass().getName())) {
                continue;
            }
            try {
                SpecializedFlowSimulator s = (SpecializedFlowSimulator) Class.forName(className).newInstance();
                this.simulators.put(s.name(), s);
            } catch (Exception e) {
                RepetitaWriter.appendToOutput("Class " + className + " in package " + pkg.getName() +
                                                " is not a SpecializedFlowSimulator",2);
            }
        }

    }

    public Map<String,SpecializedFlowSimulator> getAllFlowSimulators(){
	    return this.simulators;
    }

    public SpecializedFlowSimulator getSimulator(String name){
        return this.simulators.get(name);
    }

    public String getSimulatorsDescription() {
        List<String> names = new ArrayList<>(this.simulators.keySet());
        List<String> descriptions = new ArrayList<>();
        this.simulators.values().forEach((i) -> descriptions.add(i.getDescription()));

        return "Implemented " + IOConstants.FLOWSIMULATOR_NAME + "\n" +
                RepetitaWriter.formatAsListTwoColumns(names,descriptions);
    }
}
