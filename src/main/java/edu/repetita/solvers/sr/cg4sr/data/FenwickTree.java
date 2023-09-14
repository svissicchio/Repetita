package edu.repetita.solvers.sr.cg4sr.data;

/**
 * @author Francois Aubry f.aubry@uclouvain.be
 */
public class FenwickTree {

    private long[] ft;

    public FenwickTree(int n) {
        ft = new long[n + 1];
    }

    // 0-indexed methods

    public long totalsum() {
        return acsum1idx(1, ft.length - 1);
    }

    public long acsum(int i) {
        return acsum1idx(i + 1);
    }

    public long acsum(int i, int j) {
        return acsum1idx(i + 1, j + 1);
    }

    public void add(int i, int v) {
        add1dx(i + 1, v);
    }

    public void set(int i, int v) {
        int val = (int)acsum(i, i);
        add(i, v - val);

    }

    // 1-indexed methods

    private long acsum1idx(int i) {
        long sum = 0;
        for(; i > 0; i -= (i & -i)) {
            sum += ft[i];
        }
        return sum;
    }

    private long acsum1idx(int i, int j) {
        return acsum1idx(j) - (i == 1 ? 0 : acsum1idx(i - 1));
    }

    private void add1dx(int i, int v) {
        for(; i < ft.length; i += (i & -i)) {
            ft[i] += v;
        }
    }

    public String toString(String left, String middle, String right) {
        StringBuilder sb = new StringBuilder();
        sb.append(left);
        for(int i = 0; i < ft.length - 1; i++) {
            sb.append(acsum(i, i));
            if(i < ft.length - 2) {
                sb.append(middle);
            }
        }
        sb.append(right);
        return sb.toString();
    }

    public String toString() {
        return toString("[", ", ", "]");
    }

}
