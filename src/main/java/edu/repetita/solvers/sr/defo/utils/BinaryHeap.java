package edu.repetita.solvers.sr.defo.utils;

import java.util.NoSuchElementException;

public class BinaryHeap<V> {
    public static class Node<V> {
        public final int key;
        public final V value;

        public Node(int key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public final int maxSize;
    @SuppressWarnings("unchecked")
    private final Node<V>[] heap;
    private int heapSize = 0;

    @SuppressWarnings("unchecked")
    public BinaryHeap(int maxSize) {
        this.maxSize = maxSize;
        this.heap = new Node[maxSize + 1];
    }

    public V head() { return heap[1].value; }
    public int size() { return heapSize; }
    public boolean isEmpty() { return heapSize == 0; }
    public void removeAll() { heapSize = 0; }

    public void enqueue(int key, V value) {
        if (heapSize == maxSize) throw new IllegalStateException("the heap is full");
        else {
            heapSize++;
            heap[heapSize] = new Node<>(key, value);
            heapifyBottomUp(heapSize);
        }
    }

    public V dequeue() {
        if (isEmpty()) throw new NoSuchElementException("empty");
        else {
            Node<V> min = heap[1];
            heap[1] = heap[heapSize];
            heap[heapSize] = null;
            heapSize--;
            heapifyTopDown(1);
            return min.value;
        }
    }

    public boolean remove(int key, V value) {
        if (isEmpty()) return false;
        else {
            int id = search(1, key, value);
            if (id > heapSize) return false;
            else {
                heap[id] = heap[heapSize];
                heap[heapSize] = null;
                heapSize--;
                heapifyTopDown(id);
                return true;
            }
        }
    }

    public boolean changeKey(int oldKey, int newKey, V value) {
        if (isEmpty()) return false;
        else {
            int i = search(1, oldKey, value);
            if (i > heapSize) return false;
            else {
                heap[i] = new Node<>(newKey, value);
                if (i == 1) heapifyTopDown(1);
                else if (newKey < heap[parent(i)].key) heapifyBottomUp(i);
                else heapifyTopDown(i);
                return true;
            }
        }
    }

    private int search(int id, int key, V value) {
        int curr = id;
        while (curr <= heapSize) {
            if (heap[curr] != null && heap[curr].key == key && heap[curr].value.equals(value)) {
                return curr;
            }
            curr++;
        }
        return curr;
    }

    private void heapifyTopDown(int i) {
        int curr = i;
        while (true) {
            int min = minSon(curr);
            if (min != curr) {
                Node<V> temp = heap[curr];
                heap[curr] = heap[min];
                heap[min] = temp;
                curr = min;
            } else {
                break;
            }
        }
    }

    private void heapifyBottomUp(int i) {
        int curr = i;
        while (curr > 1) {
            int p = parent(curr);
            if (heap[p].key > heap[curr].key) {
                Node<V> temp = heap[curr];
                heap[curr] = heap[p];
                heap[p] = temp;
                curr = p;
            } else {
                break;
            }
        }
    }

    private int minSon(int i) {
        int l = left(i);
        int r = right(i);
        int min = i;
        if (l <= heapSize) {
            if (heap[l].key < heap[i].key) min = l;
            if (r <= heapSize && heap[r].key < heap[min].key) {
                min = r;
            }
        }
        return min;
    }

    private int parent(int i) { return i / 2; }
    private int left(int i) { return 2 * i; }
    private int right(int i) { return 2 * i + 1; }
}
