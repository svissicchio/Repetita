package edu.repetita.solvers.sr.rls.state;

public abstract class TrialState implements Trial {
    public abstract void updateState();
    public abstract void commitState();
    public abstract void revertState();

    private int nTrial = 0;
    private int maxTrial = 16;
    private Trial[] trials = new Trial[maxTrial];

    public void addTrial(Trial trial) {
        if (nTrial == maxTrial) {
            maxTrial <<= 1;
            Trial[] newTrials = new Trial[maxTrial];
            System.arraycopy(trials, 0, newTrials, 0, nTrial);
            trials = newTrials;
        }
        trials[nTrial] = trial;
        nTrial++;
    }

    @Override
    public void update() {
        updateState();
        int pTrial = 0;
        while (pTrial < nTrial) {
            trials[pTrial].update();
            pTrial++;
        }
    }

    @Override
    public boolean check() {
        int pTrial = 0;
        while (pTrial < nTrial && trials[pTrial].check()) {
            pTrial++;
        }

        boolean pass = pTrial == nTrial;
        if (!pass) {
            while (pTrial > 0) {
                pTrial--;
                trials[pTrial].revert();
            }
            revertState();
        }
        return pass;
    }

    @Override
    public void commit() {
        commitAll();
        commitState();
    }

    @Override
    public void revert() {
        revertAll();
        revertState();
    }

    private void revertAll() {
        int pTrial = nTrial;
        while (pTrial > 0) {
            pTrial--;
            trials[pTrial].revert();
        }
    }

    private void commitAll() {
        int p = 0;
        while (p < nTrial) {
            trials[p].commit();
            p++;
        }
    }
}
