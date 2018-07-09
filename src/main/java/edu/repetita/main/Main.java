package edu.repetita.main;

import edu.repetita.core.Scenario;
import edu.repetita.core.Setting;
import edu.repetita.core.Solver;
import edu.repetita.analyses.Analyzer;
import edu.repetita.io.IOConstants;
import edu.repetita.io.RepetitaParser;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.io.interpreters.InterpreterFactory;
import edu.repetita.scenarios.ScenarioFactory;
import edu.repetita.solvers.SolverFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Main {

	private static RepetitaStorage storage;

	/* Private print methods */
	private static String getUsage(){
        return "Typical usage: repetita " +
                "-graph topology_file -demands demands_filename -demandchanges list_demands_filename " +
                "-solver algorithm_id -scenario scenario_id -t max_execution_time -outpaths path_filename " +
                "-out output_filename -verbose debugging_level\n";
    }

	private static String getUsageOptions(){
	    ArrayList<String> options = new ArrayList<>();
        ArrayList<String> descriptions = new ArrayList<>();

        options.addAll(Arrays.asList("h","doc","graph","demands","demandchanges","solver",
                                     "scenario","t","outpaths","out","verbose"));

        descriptions.addAll(Arrays.asList(
                "only prints this help message",
                "only prints the README.txt file",
                "file.graph",
                "file.demands",
                "list of file.demands",
                "identifier of the algorithm to run, to be chosen among " + storage.getSolverIDs().toString(),
                "identifier of the scenario to simulate, to be chosen among " + storage.getScenarioIDs().toString(),
                "maximum time in seconds allowed to the solver",
                "name of the file collecting information of paths",
                "name of the file collecting all the information (standard output by default)",
                "level of debugging (default 0, only results reported)"
        ));

	    return "All options:\n" + RepetitaWriter.formatAsListTwoColumns(options, descriptions, "  -");
    }


	private static void printHelp(String additional) {
	    if (additional != null && !additional.equals("")) {
            System.out.println("\n" + additional + "\n");
        }

        System.out.println(getUsage());
        System.out.println(getUsageOptions());

		System.exit(1);
	}

	private static void printReadme(){
        // introduction
        String content = "Framework for repeatable experiments in Traffic Engineering.\n\n" +
                "Features:\n" +
                "- dataset with most instances from the Topology Zoo\n" +
                "- a collection of traffic engineering algorithms and analyses of their results\n" +
                "- libraries to simulate traffic distribution induced by ECMP, static (MPLS tunnels or OpenFlow rules)" +
                " and Segment Routing paths, compute Multicommodity Flow solutions, and much more!\n\n";

        content += getUsage() + "\n";

        // adding available solvers and scenarios
        content += SolverFactory.getSolversDescription() + "\n";
        content += ScenarioFactory.getScenariosDescription() + "\n";
        content += Analyzer.getInstance().getDescription();

        // write on the README file
        RepetitaWriter.writeToFile(content,IOConstants.REPETITA_READMEFILE);
    }

    private static void printExternalSolverSpecs() {
	    // prepare the new introduction to the file
        String doc = "For each external solver, specify how to use it within REPETITA\n\n" +
                "The definition of each solver must start with an identifier within square brackets.\n" +
                "It must also include the definition of the following features:\n";
        doc += IOConstants.getFormattedSolverSpecConstantsWithDescription() + "\n";
        doc += InterpreterFactory.getInstance().getInterpretersDescription() + "\n";
        StringBuilder content = new StringBuilder(RepetitaWriter.formatAsDocumentation(doc) + "\n");

        // read the information on the already configured external solvers, and append it to the content to write
        Map<String,Map<String,String>> features = RepetitaParser.parseExternalSolverFeatures(IOConstants.SOLVER_SPECSFILE);
        for (String configuredSolver: features.keySet()){
            content.append(features.get(configuredSolver).get(IOConstants.SOLVER_STARTDEF)).append("\n");
            for (String feat: IOConstants.getSolverSpecConstantsInOrder()){
                content.append(feat).append(IOConstants.SOLVER_KEYVALUESEPARATOR).append(features.get(configuredSolver).get(feat)).append("\n");
            }
            content.append("\n");
        }

        RepetitaWriter.writeToFile(content.toString(),IOConstants.SOLVER_SPECSFILE);
    }

	private static void print_doc() {
	    printReadme();
        printExternalSolverSpecs();
    }


	/* Main method */
	public static void main(String[] args) throws Exception {
		String graphFilename = null;
		String demandsFilename = null;
        ArrayList<String> demandChangesFilenames = new ArrayList<>();
		double timeLimit = 10;
		int verboseLevel = 0;
		boolean help = false;

		String solverChoice = "tabuLS";
		String scenarioChoice = "SingleSolverRun";

		// parse command line arguments
		int i = 0;
		while (i < args.length) {
			switch(args[i]) {
			case "-h": 
				help=true;
				break;

            case "-doc":
                print_doc();
                return;

			case "-graph":
				graphFilename = args[++i];
				break;

			case "-demands":
				demandsFilename = args[++i];
				break;

            case "-demandchanges":
                String next = args[++i];
                while (! next.startsWith("-")){
                    demandChangesFilenames.add(next);
                    next = args[++i];
                }
                i--;
                break;

			case "-solver":
				solverChoice = args[++i];
				break;

			case "-scenario":
				scenarioChoice = args[++i];
				break;

            case "-t":
				timeLimit = Double.parseDouble(args[++i]);
                break;

            case "-outpaths":
                RepetitaWriter.setOutpathsFilename(args[++i]);
                break;

            case "-out":
                RepetitaWriter.setOutputFilename(args[++i]);
                break;

			case "-verbose":
                verboseLevel = Integer.parseInt(args[++i]);
			    RepetitaWriter.setVerbose(verboseLevel);
				break;

			default: 
				printHelp("Unknown option " + args[i]);
			}    
			i++;
		}

        // create storage (after having set the verbose level)
        storage = RepetitaStorage.getInstance();

        // check that the strictly necessary information has been provided in input (after having creating the storage)
        if (args.length < 1 || help) printHelp("");
		if (graphFilename == null) printHelp("Needs an input topology file");
		if (demandsFilename == null) printHelp("Needs an input demands file");

		// check if solver and scenario choices are meaningful
        if (! storage.getSolverIDs().contains(solverChoice)) printHelp("Unknown solver: " + solverChoice);
        if (! storage.getScenarioIDs().contains(scenarioChoice)) printHelp("Unknown scenario: " + scenarioChoice);

        // run an experiment according to command line parameters
        Setting setting = storage.newSetting(graphFilename, demandsFilename, demandChangesFilenames);

		Solver solver = storage.getSolver(solverChoice);
		solver.setVerbose(verboseLevel);

		Scenario scenario = storage.newScenario(scenarioChoice, setting, solver);
		scenario.run((long) timeLimit * 1000);
	}
}
