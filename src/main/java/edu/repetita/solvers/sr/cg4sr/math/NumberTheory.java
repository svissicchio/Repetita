package edu.repetita.solvers.sr.cg4sr.math;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class NumberTheory {

	public static long gcd(long a, long b) {
		if(a < 0) return gcd(-a, b);
		if(b < 0) return gcd(a, -b);
		if(b == 0) return a;
		return gcd(b, a % b);
	}
	
}
