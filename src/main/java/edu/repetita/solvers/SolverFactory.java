package edu.repetita.solvers;

import java.util.*;

import edu.repetita.core.Solver;
import edu.repetita.io.ExternalSolverInterpreter;
import edu.repetita.io.IOConstants;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.io.interpreters.InterpreterFactory;
import edu.repetita.utils.reflections.Reflections;

public class SolverFactory {

    // private constructor, to defeat direct instantiation
	private SolverFactory() {}


    public static Map<String,Solver> getAllInternalSolvers(){
        Map<String,Solver> solvers = new HashMap<>();
        Package pkg = SolverFactory.class.getPackage();

        for (String subpkg: Reflections.getPackagesInPackage(pkg)) {
            for (String className : Reflections.getClassesForPackage(subpkg)) {
                try {
                    Solver s = (Solver) Class.forName(className).newInstance();
                    solvers.put(s.name(), s);
                } catch (Exception e) {
                    RepetitaWriter.appendToOutput("Class " + className + " in package " + pkg.getName() +
                                                    " is not a Solver",2);
                }
            }
        }

        return solvers;
    }

    private static boolean areSolverFeaturesComplete(Map<String,String> solverFeatures){
        return solverFeatures.containsKey(IOConstants.SOLVER_NAME) && solverFeatures.containsKey(IOConstants.SOLVER_RUNCOMMAND)
                && solverFeatures.containsKey(IOConstants.SOLVER_TIMECOMMAND);
    }

    public static Solver getExternalSolver(Map<String,String> solverFeatures){
        ExternalSolverInterpreter interpreter = InterpreterFactory.getInstance().getInterpreter(solverFeatures);
        ExternalSolver external = null;
        
        try {
            if (interpreter != null && areSolverFeaturesComplete(solverFeatures)) {
                external = new ExternalSolver(solverFeatures.get(IOConstants.SOLVER_NAME), interpreter,
                        solverFeatures.get(IOConstants.SOLVER_RUNCOMMAND), solverFeatures.get(IOConstants.SOLVER_TIMECOMMAND),
                        IOConstants.CLI_TIMEOUT_COMMANDS);
                if (solverFeatures.containsKey(IOConstants.SOLVER_OBJ)){
                    external.setObjective(solverFeatures.get(IOConstants.SOLVER_OBJ));
                }
            }
        }
        catch (Exception e){
            RepetitaWriter.appendToOutput(e.getMessage(),0);
        }

		return external;
	}

    public static String getSolversDescription() {
        Map<String,Solver> solvers = getAllInternalSolvers();

        List<String> names = new ArrayList<>(solvers.keySet());
        names.add("ExternalSolvers");

        List<String> descriptions = new ArrayList<>();
        solvers.values().forEach((solver) -> descriptions.add(solver.getDescription()));
        descriptions.add("Any algorithm described in " + IOConstants.SOLVER_SPECSFILE);

        return "Supported traffic engineering algorithms (solvers)\n" +
                RepetitaWriter.formatAsListTwoColumns(names,descriptions);
    }
}
