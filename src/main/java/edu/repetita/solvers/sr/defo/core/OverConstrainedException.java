package edu.repetita.solvers.sr.defo.core;

public class OverConstrainedException extends DEFOException {
    public final int demandId;

    public OverConstrainedException(int demandId, String message) {
        super(message);
        this.demandId = demandId;
    }
}
