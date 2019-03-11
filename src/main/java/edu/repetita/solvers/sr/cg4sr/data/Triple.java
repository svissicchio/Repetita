package edu.repetita.solvers.sr.cg4sr.data;

/**
 * @author  Francois Aubry f.aubry@uclouvain.be
 */
public class Triple<X, Y, Z> {

    private X x;
    private Y y;
    private Z z;

    public Triple(X x, Y y, Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public X x() {
        return x;
    }

    public Y y() {
        return y;
    }

    public Z z() {
        return z;
    }

    public String toString() {
        return String.format("(%s, %s, %s)", x.toString(), y.toString(), z.toString());
    }

}
