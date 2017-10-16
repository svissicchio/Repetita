package edu.repetita.solvers;

import java.util.UUID;

import edu.repetita.core.Setting;
import edu.repetita.core.Solver;
import edu.repetita.io.ExternalSolverInterpreter;
import edu.repetita.io.IOConstants;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.io.ShellCommandExecutor;


public class ExternalSolver extends Solver {
	
	private String name;
	private ExternalSolverInterpreter interpreter;
	private String runCommand;
	private String getTimeCommand;
	private ShellCommandExecutor executor;
	private String outputFilename;
	private String cliTimeoutCommand = null;

	public ExternalSolver(String name, ExternalSolverInterpreter externalSolverInterpreter, String runCommand, String getTimeCommand, String[] possibleTimeoutCommands) throws Exception{
		this.name = name;
        this.interpreter = externalSolverInterpreter;
        this.runCommand = runCommand;
        this.getTimeCommand = getTimeCommand;
        this.executor = ShellCommandExecutor.getInstance();
        this.outputFilename = "/tmp/repetita-" + UUID.randomUUID().toString();

        for (String cmd: possibleTimeoutCommands){
            if (this.executor.isCommandExecutable(cmd)){
                this.cliTimeoutCommand = cmd;
                break;
            }
        }
        if (this.cliTimeoutCommand == null){
            throw new Exception("External solver " + name + " not created: No command available on the operative system to bound the time taken by external solvers");
        }
	}

    public void setObjective(String objectiveId) {
        this.objective = new Integer(objectiveId);
    }

	@Override

    // implement the method declared abstract in Solver.
    // This implementation is empty since the objective is already set in the ExternalSolver constructor.
	protected void setObjective() {}

	@Override
	public String name() {
		return this.name;
	}

    @Override
    public String getDescription() {
        return "";
    }

	@Override
	public void solve(Setting setting, long milliseconds) {
		String timeLimit = Long.toString(milliseconds);
        String transformedCommand = this.specializeCommand(this.runCommand, setting.getTopologyFilename(), setting.getDemandsFilename());

		String finalCommand =  this.cliTimeoutCommand + " " + timeLimit + "s " + transformedCommand;
        RepetitaWriter.appendToOutput(finalCommand,2);
		String runOutput = this.executor.executeCommand(finalCommand);
		if (runOutput.isEmpty()){
			runOutput = this.executor.executeCommand("cat " +  this.outputFilename);
		}
        this.elaborateRunOutput(runOutput,setting);
	}

    @Override
	public long solveTime(Setting setting) {
		String finalCommand = this.specializeCommand(this.getTimeCommand, setting.getTopologyFilename(), setting.getDemandsFilename());
		RepetitaWriter.appendToOutput(finalCommand,2);
        String output = this.executor.executeCommand(finalCommand);
		return (long) Double.parseDouble(output.replaceAll("[^\\d.]", ""));
	}

    /**
     * Specifies the file to be used for registering output from the solvers.
     */
    public void setOutputFile(String outputFilename) {
        this.outputFilename = outputFilename;
    }

	/* private methods */
	private String specializeCommand(String parametricCommand, String graphFilename, String demandFilename){
		String transformedCommand = parametricCommand;
        transformedCommand = transformedCommand.replace(IOConstants.SOLVER_TOPOLOGYFILEPLACEHOLDER, graphFilename);
        transformedCommand = transformedCommand.replace(IOConstants.SOLVER_DEMANDFILEPLACEHOLDER, demandFilename);
        return transformedCommand.replace(IOConstants.SOLVER_OUTPUTFILEPLACEHOLDER, this.outputFilename);
	}

	private void elaborateRunOutput(String outputString, Setting setting){
		this.interpreter.elaborateRunCommandOutput(outputString, setting);
    }
}
