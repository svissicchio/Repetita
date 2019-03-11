package edu.repetita.solvers.sr.cg4sr.data;

/**
 * @author  Francois Aubry f.aubry@uclouvain.be
 */
public class Tuple4<A, B, C, D> {

    public A a;
    public B b;
    public C c;
    public D d;

    public Tuple4(A a, B b, C c, D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public String toString() {
        return String.format("(%s, %s, %s, %s)", a.toString(), b.toString(), c.toString(), d.toString());
    }

}
