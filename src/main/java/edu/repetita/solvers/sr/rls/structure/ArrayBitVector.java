package edu.repetita.solvers.sr.rls.structure;

/**
 * Fast array-based bit vector.
 * This class does not check the validity of the user inputs.
 *
 * @author Renaud Hartert ren.hartert@gmail.com
 */
public final class ArrayBitVector {
    private final long[] words;

    public ArrayBitVector(int nBits) {
        this.words = new long[(nBits >> 6) + 1];
    }

    public final void insert(int bitId) {
        int wordId = bitId >> 6;
        words[wordId] |= (1L << bitId);
    }

    public final void remove(int bitId) {
        int wordId = bitId >> 6;
        words[wordId] &= ~(1L << bitId);
    }

    public final boolean get(int bitId) {
        int wordId = bitId >> 6;
        long word = words[wordId];
        return (word & (1L << bitId)) != 0;
    }

    public final void set(int bitId, boolean value) {
        int wordId = bitId >> 6;
        if (value) {
            words[wordId] |= (1L << bitId);
        } else {
            words[wordId] &= ~(1L << bitId);
        }
    }
}
