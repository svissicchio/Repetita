package edu.repetita.solvers.sr.defo.utils;

import java.util.NoSuchElementException;

public final class ArrayHeapInt {
    private final int[] values;
    private final int[] keys;
    private final int maxSize;
    private int heapSize = 0;

    public ArrayHeapInt(int maxSize) {
        this.maxSize = maxSize;
        this.values = new int[maxSize + 1];
        this.keys = new int[maxSize + 1];
    }

    public int minValue() {
        if (heapSize != 0) return values[1];
        else throw new NoSuchElementException("empty");
    }

    public int minKey() {
        if (heapSize != 0) return keys[1];
        else throw new NoSuchElementException("empty");
    }

    public int size() {
        return heapSize;
    }

    public boolean isEmpty() {
        return heapSize == 0;
    }

    public void clear() {
        heapSize = 0;
    }

    public void enqueue(int key, int value) {
        if (heapSize == maxSize) throw new IllegalStateException("the heap is full");
        else {
            heapSize++;
            keys[heapSize] = key;
            values[heapSize] = value;
            heapifyBottomUp(heapSize);
        }
    }

    public int dequeue() {
        if (heapSize == 0) throw new NoSuchElementException("empty");
        else {
            int value = values[1];
            values[1] = values[heapSize];
            keys[1] = keys[heapSize];
            heapSize--;
            heapifyTopDown(1);
            return value;
        }
    }

    public boolean remove(int key, int value) {
        if (heapSize == 0) return false;
        else {
            int id = search(1, key, value);
            if (id > heapSize) return false;
            else {
                values[id] = values[heapSize];
                keys[id] = keys[heapSize];
                heapSize--;
                heapifyTopDown(id);
                return true;
            }
        }
    }

    public boolean changeKey(int oldKey, int newKey, int value) {
        if (heapSize == 0) return false;
        else {
            int i = search(1, oldKey, value);
            if (i > heapSize) return false;
            else {
                keys[i] = newKey;
                if (i == 1) heapifyTopDown(1);
                else if (newKey < keys[i >> 1]) heapifyBottomUp(i);
                else heapifyTopDown(i);
                return true;
            }
        }
    }

    private int search(int id, int key, int value) {
        int curr = id;
        while (curr <= heapSize) {
            if (keys[curr] == key && values[curr] == value) return curr;
            curr++;
        }
        return curr;
    }

    private void heapifyTopDown(int i) {
        int curr = i;
        while (true) {
            int min = minChild(curr);
            if (min != curr) {
                int tmpValue = values[curr];
                int tmpKey = keys[curr];
                values[curr] = values[min];
                keys[curr] = keys[min];
                values[min] = tmpValue;
                keys[min] = tmpKey;
                curr = min;
            } else {
                break;
            }
        }
    }

    private void heapifyBottomUp(int i) {
        int curr = i;
        while (curr > 1) {
            int p = curr >> 1;
            if (keys[p] > keys[curr]) {
                int tmpValue = values[curr];
                int tmpKey = keys[curr];
                values[curr] = values[p];
                keys[curr] = keys[p];
                values[p] = tmpValue;
                keys[p] = tmpKey;
                curr = p;
            } else {
                break;
            }
        }
    }

    private int minChild(int i) {
        int l = i << 1;
        int r = l + 1;
        int min = i;
        if (l <= heapSize) {
            if (keys[l] < keys[i]) min = l;
            if (r <= heapSize && keys[r] < keys[min]) {
                min = r;
            }
        }
        return min;
    }
}
