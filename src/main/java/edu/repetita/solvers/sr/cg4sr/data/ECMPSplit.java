package edu.repetita.solvers.sr.cg4sr.data;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public interface ECMPSplit<E extends NetworkEdge> {

	double getValue(int x, int y, E edge);

	NetworkEdge getCriticalEdge(int x, int y);

}
