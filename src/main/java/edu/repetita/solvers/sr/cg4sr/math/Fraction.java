package edu.repetita.solvers.sr.cg4sr.math;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class Fraction {
	
	private long a, b;
	
	public Fraction(long a) {
		this.a = a;
		this.b = 1;
	}
	
	public Fraction(long a, long b) {
		this.a = a;
		this.b = b;
		normalize();
	}
	
	public Fraction copy() {
		return new Fraction(a, b);
	}
	
	public void normalize() {
		if(b < 0) {
			a = -a;
			b = -b;
		}
		long d = NumberTheory.gcd(a, b);
		a /= d;
		b /= d;
	}
	
	public void add(Fraction other) {
		a = a * other.b + b * other.a;
		b = b * other.b;
		normalize();
	}
	
	public void subtract(Fraction other) {
		a = a * other.b - b * other.a;
		b = b * other.b;
		normalize();
	}
	
	public void negate() {
		a = -a;
		normalize();
	}
	
	public void divide(long x) {
		b *= x;
		normalize();
	}
	
	public void multiply(long x) {
		a *= x;
		normalize();
	}
	
	public static Fraction ONE() {
		return new Fraction(1, 1);
	}
	
	public static Fraction ZERO() {
		return new Fraction(0, 1);
	}
	
	public void multiply(Fraction other) {
		a *= other.a;
		b *= other.b;
		normalize();
	}
	
	public String toString() {
		if(b == 1) return Long.toString(a);
		return String.format("%d/%d", a, b);
	}

}
