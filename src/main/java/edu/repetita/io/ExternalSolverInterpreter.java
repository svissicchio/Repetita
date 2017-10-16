package edu.repetita.io;

import edu.repetita.core.Setting;

/**
 * Interface of classes enabling ExternalSolver to interpret results from external algorithms
 * (provided as executable).
 */
public abstract class ExternalSolverInterpreter {
    /**
     * Variables common to all interpreters
     */
    private String fieldSeparator = IOConstants.INTERPRETER_FIELDSEPARATOR_DFLT;
    private int keyField = IOConstants.INTERPRETER_KEYFIELD_DFLT;
    private int valueField = IOConstants.INTERPRETER_VALUEFIELD_DFLT;

    /**
     * Instruct the interpreter on how to identify fields in the output of the external solver.
     *
     * @param fieldSeparator the string separating different fields (default " ")
     */
    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    /**
     * Sets the field of the object to be modified
     * (e.g., edge id if the solver is changing edge weights).
     *
     * @param keyField the field number of what to change (default 0)
     */
    public void setKeyField(int keyField) {
        this.keyField = keyField;
    }

    /**
     * Sets the field of the modification values
     * (e.g., edge weights if the solver is changing edge weights).
     *
     * @param valueField the field number of how to change the object specified by the keyField (default 1)
     */
    public void setValueField(int valueField) {
        this.valueField = valueField;
    }

    /**
     * @return the value of the field separator
     */
    public String getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * @return the value of the key field
     */
    public int getKeyField() {
        return keyField;
    }

    /**
     * @return the value of the value field
     */
    public int getValueField() {
        return valueField;
    }


    /* Abstract methods (to be implemented by sub-classes */

    /**
     * @return the name of the Interpreter
     */
    public abstract String name();

    /**
     * @return the description of what the Interpreter does
     */
    public abstract String getDescription();

    /**
     * Modifies the input setting according to the optimizations made by the external executable.
     * (assume that the setting collects all the information to compute paths)
     *
     * @param outputString the output of the external executable when asked to optimize a setting
     * @param setting the setting to optimize: it is expected to be modified according to outputString
     */
    public abstract void elaborateRunCommandOutput(String outputString, Setting setting);
}
