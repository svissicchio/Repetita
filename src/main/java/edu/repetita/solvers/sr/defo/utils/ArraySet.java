package edu.repetita.solvers.sr.defo.utils;

public class ArraySet {
    private final int[] elements;
    private final int[] positions;
    private int nInside = 0;

    public ArraySet(int nElems) {
        this.elements = new int[nElems];
        this.positions = new int[nElems];
        for (int i = 0; i < nElems; i++) {
            elements[i] = i;
            positions[i] = i;
        }
    }

    public int size() { return nInside; }
    public boolean isEmpty() { return nInside == 0; }

    public int get(int idx) {
        if (idx < nInside) return elements[idx];
        else throw new IndexOutOfBoundsException();
    }

    public void add(int elem) {
        int position = positions[elem];
        if (position >= nInside) {
            int elem2 = elements[nInside];
            elements[position] = elem2;
            elements[nInside] = elem;
            positions[elem2] = position;
            positions[elem] = nInside;
            nInside++;
        }
    }

    public boolean contains(int elem) {
        return positions[elem] < nInside;
    }
}
