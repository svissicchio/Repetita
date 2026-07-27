package edu.repetita.solvers.sr.rls.core;

public abstract class Neighborhood<T> {
    public abstract void setNeighborhood(T substate);

    public abstract boolean hasNext();
    public abstract void next();

    public abstract void apply();

    public abstract void saveBest();
    public abstract void applyBest();

    public abstract String name();
}
