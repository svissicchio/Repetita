package edu.repetita.solvers.sr.cg4sr.data;

/**
 * Class that represents a generic pair.
 * 
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class Pair<X, Y> {

	private X x;
	private Y y;
	
	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	public X x() {
		return x;
	}
	
	public Y y() {
		return y;
	}
	
	public String toString() {
		return String.format("(%s, %s)", x.toString(), y.toString());
	}
	
}
