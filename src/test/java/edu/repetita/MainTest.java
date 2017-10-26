package edu.repetita;

import edu.repetita.io.IOConstants;
import edu.repetita.io.RepetitaWriter;
import edu.repetita.main.Main;
import edu.repetita.scenarios.ScenarioFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Permission;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainTest {

    /* Variables */
    private Warehouse warehouse = new Warehouse();
    private HashMap<String,String> args;
    private HashMap<String,String> customArgs;
    private PrintStream stdout = System.out;
    private String logFilename = "maintest.log";
    private String outFilename = "maintest.out";
    private Exception runtimeException = null;

    /* Support methods */

    // initialize arguments with default values
    private void initializeArgs(){
        this.args = new HashMap<>();
        this.args.put("-graph",this.warehouse.getDefaultTopologyFile());
        this.args.put("-demands",this.warehouse.getDefaultDemandFile());
        this.args.put("-t","1");
        this.args.put("-scenario",this.warehouse.getDefaultScenarioName());
        this.args.put("-solver",this.warehouse.getDefaultSolverName());
        this.args.put("-verbose","0");
    }

    // modifies the arguments variable with the specified options
    private void setArgs(Map<String,String> customArgs){
        this.args.putAll(customArgs);
    }

    // returns a String array with the set argument, as needed by the main method in the Main class
    private String[] getArgs(){
        List<String> argsList = new ArrayList<>();
        for (String k: this.args.keySet()) {
            argsList.add(k);
            Collections.addAll(argsList, this.args.get(k).split(" "));
        }
        String[] argsArray = new String[argsList.size()];
        return argsList.toArray(argsArray);
    }

    // classes to avoid that tests fail when they exit with errors
    protected static class ExitException extends SecurityException {
        public final int exitStatus;
        public ExitException(int status) {
            super("There is no escape!");
            this.exitStatus = status;
        }
    }

    final SecurityManager exitSecurityManager = new SecurityManager() {
        public void checkPermission(Permission perm)
        {
            // allow anything.
        }
        public void checkPermission(Permission perm, Object context)
        {
            // allow anything.
        }
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException(status);
        }
    } ;

    /* Tests */

    @Before
    public void setup (){
        this.customArgs = new HashMap<String,String>();
        this.initializeArgs();
    }

    @After
    public void tearDown(){
        // reset stdout
        System.setOut(stdout);

        //clear possible security managers
        System.setSecurityManager( null ) ;
    }

    @Test
    public void testMain_noErrors_noParameters () throws Exception {
        System.setSecurityManager( this.exitSecurityManager ) ;
        try {
            Main.main(new String[0]);
        }
        catch (ExitException e){
            assert(e.exitStatus == 1);
        }
    }

    @Test
    public void testMain_noErrors_Help () throws Exception {
        System.setSecurityManager( this.exitSecurityManager ) ;
        try {
            String[] options = {"-h"};
            Main.main(options);
        }
        catch (ExitException e){
            assert(e.exitStatus == 1);
        }
    }

    @Test
    public void testSetMultipleDemands_noErrors_defaultAdditionalDemandChanges () throws Exception {
        String changesFilelist = this.warehouse.getDefaultDemandChangeFiles().toString();
        changesFilelist = changesFilelist.replaceAll("\\[|\\]|,", "");
        this.customArgs.put("-demandchanges",changesFilelist);
        this.customArgs.put("-scenario","DemandChangeReoptimization");
        this.setArgs(this.customArgs);

        Main.main(this.getArgs());
    }

    @Test
    public void testPrintDoc_noErrorsAndWriteFiles () throws Exception {
        Main.main(new String[]{"-doc"});
        String readmeContent = new String(Files.readAllBytes(Paths.get(IOConstants.REPETITA_READMEFILE)));
        System.out.println("*** README file ***\n" + readmeContent);
        assert !readmeContent.isEmpty();
        String specsContent = new String(Files.readAllBytes(Paths.get(IOConstants.SOLVER_SPECSFILE)));
        System.out.println("\n*** External solvers spec file ***\n" + specsContent);
        assert !specsContent.isEmpty();
    }

    @Test
    public void testMain_noErrorsNoVerbose_runDefoWithFractionalTime () throws Exception {
        this.customArgs.put("-solver","defoCP");
        this.customArgs.put("-t","0.5");
        this.setArgs(this.customArgs);

        Main.main(this.getArgs());
    }

    @Test
    public void testMain_noErrorsNoVerbose_runMIPWeightOptimizer () throws Exception {
        this.customArgs.put("-solver","MIPWeightOptimizer");
        this.customArgs.put("-t","5");
        this.setArgs(this.customArgs);

        Main.main(this.getArgs());
    }

    @Test
    public void testMain_noErrorsVerbose_runDefaultSolver () throws Exception {
        this.customArgs.put("-verbose","2");
        this.setArgs(this.customArgs);

        // re-instantiate warehouse to log warning when objects are created for the first time
        this.warehouse = new Warehouse();

        // prepare (and clean) log file
        File f = new File(logFilename);
        RepetitaWriter.writeToFile("",logFilename);

        // redirect output to log file (while keeping a reference to the stdout)
        System.setOut(new PrintStream(f));

        // run main and analyze its output -- checking for strings that are not reported when not verbose
        Main.main(this.getArgs());
        String content = new String(Files.readAllBytes(Paths.get(logFilename)));
        // reset stdout to print content in a visible way
        System.setOut(stdout);
        System.out.println(content);
        Pattern regex = Pattern.compile(".*Selected a ratio of .* nodes as acceptable detours.*", Pattern.DOTALL);
        Matcher regexMatcher = regex.matcher(content);
        assert regexMatcher.find();
    }

    @Test
    public void testMain_sendOutputToFile_runDefaultSolverAllScenarios () throws Exception {
        this.customArgs.put("-verbose","0");
        this.customArgs.put("-out",outFilename);

        for(String s: ScenarioFactory.getAllScenarios().keySet()) {
            this.customArgs.put("-scenario",s);
            this.setArgs(this.customArgs);

            System.out.println("\nTrying scenario " + s);

            // redirect output to log file (while keeping a reference to the stdout)
            File f = new File(logFilename);
            RepetitaWriter.writeToFile("", logFilename);
            RepetitaWriter.writeToFile("", outFilename);
            System.setOut(new PrintStream(f));

            // run main and check that specific strings are reported in the output file while nothing is printed on stdout
            Main.main(this.getArgs());
            // reset stdout
            System.setOut(stdout);
            String logContent = new String(Files.readAllBytes(Paths.get(logFilename)));
            assert logContent.isEmpty();
            String outContent = new String(Files.readAllBytes(Paths.get(outFilename)));
            System.out.println(outContent);
            assert !outContent.isEmpty();
        }
    }

    @Test
    public void testMain_noErrors_runExternalSolver () throws Exception {
        this.customArgs.put("-solver","randomLinkWeights");
        this.setArgs(this.customArgs);

        // run it multiple times for statistical significance (since the solver provides randomized output)
        for(int i=0;i<10;i++) {
            Main.main(this.getArgs());
        }
    }

    @Test
    public void testMain_noErrors_runAnotherExternalSolver () throws Exception {
        this.customArgs.put("-solver","randomExplicitPaths");
        this.setArgs(this.customArgs);
        // run it multiple times for statistical significance (since the solver provides randomized output)
        for(int i=0;i<10;i++) {
            Main.main(this.getArgs());
        }
    }

}
