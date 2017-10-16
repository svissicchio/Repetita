package edu.repetita.io.interpreters;

import edu.repetita.io.ExternalSolverInterpreter;
import edu.repetita.io.IOConstants;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.utils.reflections.Reflections;

import java.util.*;

public class InterpreterFactory {
    private static InterpreterFactory instance = null;
    private Map<String,ExternalSolverInterpreter> interpreters = new HashMap<>();

    public static InterpreterFactory getInstance(){
        if (instance == null){
            instance = new InterpreterFactory();
        }
        return instance;
    }

    // private to defeat direct instantiation
    private InterpreterFactory() {
        this.loadAllInterpreters();
    }

    private void loadAllInterpreters() {
        Package pkg = InterpreterFactory.class.getPackage();

        for (String className: Reflections.getClassesForPackage(pkg)) {
            if (className.equals(this.getClass().getName())) {
                continue;
            }
            try{
                ExternalSolverInterpreter i = (ExternalSolverInterpreter)
                                                    Class.forName(className).newInstance();
                this.interpreters.put(i.name(),i);
            }
            catch(Exception e){
                RepetitaWriter.appendToOutput("Class " + className + " in package " + pkg.getName() + " is not a Scenario",2);
            }
        }
    }

    public ExternalSolverInterpreter getInterpreter(Map<String,String> interpreterFeatures) {
        // extract the type of interpreter to instantiate
        Class interpreterClass = this.interpreters.get(interpreterFeatures.get(IOConstants.INTERPRETER_NAME)).getClass();

        ExternalSolverInterpreter interpreter = null;
        try {
            // clone interpreter: different solvers may want to use the same type of interpreter (configured differently)
            interpreter = (ExternalSolverInterpreter) interpreterClass.newInstance();

            // configure the clone according to the intepreterFeatures
            if (interpreterFeatures.containsKey(IOConstants.INTERPRETER_FIELDSEPARATOR)){
                interpreter.setFieldSeparator(interpreterFeatures.get("field separator").replaceAll("'",""));
            }
            if (interpreterFeatures.containsKey(IOConstants.INTERPRETER_KEYFIELD)){
                interpreter.setKeyField(new Integer(interpreterFeatures.get("key field")));
            }
            if (interpreterFeatures.containsKey(IOConstants.INTERPRETER_VALUEFIELD)){
                interpreter.setValueField(new Integer(interpreterFeatures.get("value field")));
            }
        } catch (Exception e) {
            RepetitaWriter.appendToOutput("Error creating intepreter " + interpreterClass, 0);
        }

        return interpreter;
    }

    /* Description method */
    public String getInterpretersDescription() {
        List<String> names = new ArrayList<>(this.interpreters.keySet());
        List<String> descriptions = new ArrayList<>();
        this.interpreters.values().forEach((i) -> descriptions.add(i.getDescription()));

        return "Implemented " + IOConstants.INTERPRETER_NAME + "\n" +
                RepetitaWriter.formatAsListTwoColumns(names,descriptions);
    }
}
