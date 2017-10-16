package tests.java.edu.repetita.solvers;

import edu.repetita.core.Solver;
import edu.repetita.io.IOConstants;
import edu.repetita.solvers.SolverFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SolverFactoryTest {
    private Map<String,String> solverFeatures = new HashMap<>();

    @Before
    public void setup(){
        this.solverFeatures.put(IOConstants.SOLVER_NAME,"randomExplicitPaths");
        this.solverFeatures.put(IOConstants.SOLVER_RUNCOMMAND,"python external_solvers/compute_random_paths.py $TOPOFILE $DEMANDFILE $OUTFILE");
        this.solverFeatures.put(IOConstants.SOLVER_TIMECOMMAND,"cat $OUTFILE | grep 'execution time' | awk -v FS=': ' '{print $2}'");
        this.solverFeatures.put(IOConstants.INTERPRETER_NAME,"setExplicitPaths");
        this.solverFeatures.put(IOConstants.INTERPRETER_FIELDSEPARATOR,"; ");
        this.solverFeatures.put(IOConstants.INTERPRETER_KEYFIELD,"0");
        this.solverFeatures.put(IOConstants.INTERPRETER_VALUEFIELD,"2");
    }

    @Test
    public void testGetExternalSolver_raiseException_notEnoughSolverFeatures () {
        this.solverFeatures.remove(IOConstants.SOLVER_TIMECOMMAND);
        Solver solver = SolverFactory.getExternalSolver(this.solverFeatures);
        assert solver == null;
    }

    @Test
    public void testGetExternalSolver_returnsSolver_notAllInterpreterFeatures () {
        // interpreters have default values for their features; hence, not explicitly specifying all interpreter
        // features should not impede the instantiation of external solvers
        this.solverFeatures.remove(IOConstants.INTERPRETER_KEYFIELD);
        Solver solver = SolverFactory.getExternalSolver(this.solverFeatures);
        assert solver != null;
    }
}
