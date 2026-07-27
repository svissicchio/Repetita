package edu.repetita.solvers.sr.rls.state;

public interface Trial {
    void update();
    boolean check();
    void revert();
    void commit();
}
