package edu.repetita.solvers;

import edu.repetita.core.Setting;
import edu.repetita.core.Solver;
import edu.repetita.settings.SRSetting;

/**
 * Abstract class implementing methods common to all segment-routing based solvers.
 */

public abstract class SRSolver extends Solver {
    private double targetObjectiveValue = 0; // by default, there is no target objective value

    public double getTargetObjectiveValue() { return this.targetObjectiveValue; }

    public void setTargetObjectiveValue(double value) { this.targetObjectiveValue = value; }

    @Override
    public Setting getSetting() {
        return new SRSetting();
    }
}
