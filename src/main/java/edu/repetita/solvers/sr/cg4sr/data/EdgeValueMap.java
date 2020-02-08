package edu.repetita.solvers.sr.cg4sr.data;

import java.util.TreeMap;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class EdgeValueMap<E extends Edge> {

    private TreeMap<Integer, Double> values;

    public EdgeValueMap() {
        values = new TreeMap<>();
    }

    public void add(E e, double value) {
        values.put(e.id(), value);
    }

    public boolean contains(E e) {
        return values.containsKey(e.id());
    }

    public double get(E e) {
        return values.get(e.id());
    }

}
