package edu.repetita.solvers.sr.rls.constraint;

import edu.repetita.solvers.sr.rls.state.Objective;
import edu.repetita.solvers.sr.rls.state.Trial;

public class Lexicographic implements Trial {
    public interface TrialObjective extends Trial, Objective {}

    private final TrialObjective[] trials_;
    private final int nTrials;
    private final double[] scores;
    private final double[] oldScores;

    public Lexicographic(TrialObjective... trials) {
        this.trials_ = trials;
        this.nTrials = trials.length;
        this.scores = new double[nTrials];
        this.oldScores = new double[nTrials];
        for (int i = 0; i < nTrials; i++) {
            scores[i] = trials_[i].score();
            oldScores[i] = scores[i];
        }
    }

    private void updateFrom(int index) {
        int p = index;
        while (p < nTrials) {
            trials_[p].update();
            scores[p] = trials_[p].score();
            p++;
        }
    }

    @Override
    public void update() {
        updateFrom(0);
    }

    private void revertBefore(int index) {
        int p = index;
        while (p > 0) {
            p--;
            trials_[p].revert();
            scores[p] = oldScores[p];
        }
    }

    @Override
    public void revert() {
        revertBefore(nTrials);
    }

    @Override
    public boolean check() {
        boolean continueCheck = true;
        int p = 0;
        while (p < nTrials && continueCheck) {
            trials_[p].update();
            scores[p] = trials_[p].score();
            continueCheck = scores[p] == oldScores[p];
            p++;
        }

        if (continueCheck) {
            return false;
        } else {
            if (scores[p - 1] < oldScores[p - 1]) {
                if (p > 1) {
                    System.out.println("accepted on secondary objective");
                }
                updateFrom(p);
                return true;
            } else {
                revertBefore(p);
                return false;
            }
        }
    }

    @Override
    public void commit() {
        int p = 0;
        while (p != nTrials) {
            trials_[p].commit();
            oldScores[p] = scores[p];
            p++;
        }
    }
}
