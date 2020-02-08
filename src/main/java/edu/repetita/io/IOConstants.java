package edu.repetita.io;

import java.util.*;

public class IOConstants {
    /* Filenames */
    public final static String REPETITA_READMEFILE = "README.txt";
    public static final String SOLVER_SPECSFILE = "external_solvers/solvers-specs.txt";

    /* Necessary CLI commands to run the external solvers */
    public static final String[] CLI_TIMEOUT_COMMANDS = {"timeout","gtimeout"};

    /* External solver spec file keywords */
    public static final String SOLVER_STARTDEF = "identifier";
    public static final String SOLVER_NAME = "name";
    public static final String SOLVER_RUNCOMMAND = "run command";
    public static final String SOLVER_TIMECOMMAND = "gettime command";
    public static final String SOLVER_OBJ = "optimization objective";
    public static final String[] SOLVER_OBJVALUES = {"undefined", "minimize max link utilization"};
    public static final int SOLVER_OBJVALUES_DFLT = 0;
    public static final int SOLVER_OBJVALUES_MINMAXLINKUSAGE = 1;

    public static final String FLOWSIMULATOR_NAME = "routing model";

    public static final String INTERPRETER_NAME = "optimization effect";
    public static final String INTERPRETER_FIELDSEPARATOR = "field separator";
    public static final String INTERPRETER_FIELDSEPARATOR_DFLT = " ";
    public static final String INTERPRETER_KEYFIELD = "key field";
    public static final int INTERPRETER_KEYFIELD_DFLT = 0;
    public static final String INTERPRETER_VALUEFIELD = "value field";
    public static final int INTERPRETER_VALUEFIELD_DFLT = 1;

    /* Other constants in the external solver spec file */
    public static final String SOLVER_COMMENTCHAR = "#";
    public static final String SOLVER_KEYVALUESEPARATOR = " = ";
    public static final String SOLVER_TOPOLOGYFILEPLACEHOLDER = "$TOPOFILE";
    public static final String SOLVER_DEMANDFILEPLACEHOLDER = "$DEMANDFILE";
    public static final String SOLVER_OUTPUTFILEPLACEHOLDER = "$OUTFILE";

    public static List<String> getSolverSpecConstantsInOrder(){
        return Arrays.asList(SOLVER_NAME, SOLVER_RUNCOMMAND, SOLVER_TIMECOMMAND, INTERPRETER_NAME,
                INTERPRETER_FIELDSEPARATOR, INTERPRETER_KEYFIELD, INTERPRETER_VALUEFIELD, SOLVER_OBJ);
    }

    private static String getObjectivesDescription(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        String prefix = "";
        for(int i = 0; i< SOLVER_OBJVALUES.length; i++){
            sb.append(prefix).append(i).append(" for \'").append(SOLVER_OBJVALUES[i]).append("\'");
            prefix = "; ";
        }
        sb.append("}");
        return sb.toString();
    }

    public static List<String> getSolverSpecDescriptionsInOrder() {
        return Arrays.asList("Name of the solver",
                "Command to run the solver from CLI (use strings \'"+ SOLVER_TOPOLOGYFILEPLACEHOLDER + "\' " +
                        "and \'" + SOLVER_DEMANDFILEPLACEHOLDER + "\' to specify " + "topology and demand files " +
                        "to be taken as input; use string \'" + SOLVER_OUTPUTFILEPLACEHOLDER + "\' for output file)",
                "Command to output the time taken by the solver during its last run",
                "How to modify the routing configuration according to the output of \'" + SOLVER_RUNCOMMAND + "\' ",
                "The field separator in the output of '" + SOLVER_RUNCOMMAND + "\' (default \'" +
                        INTERPRETER_FIELDSEPARATOR_DFLT + "\', see below for more details)",
                "The field number in the output of '" + SOLVER_RUNCOMMAND + "\' that has to be used as keys by the " +
                        IOConstants.INTERPRETER_NAME + " (default \'" + INTERPRETER_KEYFIELD_DFLT +
                        "\', see below for more details)",
                "The field number in the output of '" + SOLVER_RUNCOMMAND + "\' that has to be used as value by the " +
                        IOConstants.INTERPRETER_NAME + " (default \'" + INTERPRETER_VALUEFIELD_DFLT +
                        "\', see below for more details)",
                "An integer indicating the solver objective among " + getObjectivesDescription());
    }

    public static String getFormattedSolverSpecConstantsWithDescription(){
        return RepetitaWriter.formatAsListTwoColumns(getSolverSpecConstantsInOrder(),getSolverSpecDescriptionsInOrder());
    }
}
