package edu.repetita.solvers.sr.rls.structure;

import java.util.ArrayList;
import java.util.List;

public class BucketHeap<V> implements LightPriorityQueue<V> {
    public static class Node<V> {
        public final int key;
        public final V value;

        public Node(int key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public final int maxKey;
    public final int bucketSize;

    @SuppressWarnings("unchecked")
    public final List<Node<V>>[] topBucket;
    private int topNEntries = 0;
    private int topBucketId = 0;

    @SuppressWarnings("unchecked")
    public final List<Node<V>>[] lowBucket;
    private int lowNEntries = 0;
    private int lowBucketId = 0;

    @SuppressWarnings("unchecked")
    public BucketHeap(int maxKey) {
        this.maxKey = maxKey;
        this.bucketSize = (int) Math.ceil(Math.sqrt(maxKey + 1));

        this.topBucket = new List[bucketSize];
        this.lowBucket = new List[bucketSize];
        for (int i = 0; i < bucketSize; i++) {
            topBucket[i] = new ArrayList<>();
            lowBucket[i] = new ArrayList<>();
        }
    }

    @Override
    public int size() {
        return topNEntries;
    }

    @Override
    public boolean isEmpty() {
        return topNEntries == 0;
    }

    @Override
    public void enqueue(int key, V value) {
        topNEntries++;
        int topId = key / bucketSize;
        topBucketId = Math.min(topBucketId, topId);

        if (topId != topBucketId) {
            topBucket[topId].add(0, new Node<>(key, value));
        } else {
            lowNEntries++;
            int lowId = key % bucketSize;
            lowBucket[lowId].add(0, new Node<>(key, value));
            lowBucketId = Math.min(lowBucketId, lowId);
        }
    }

    @Override
    public V head() {
        if (isEmpty()) {
            throw new IllegalStateException("Empty");
        } else {
            if (lowNEntries == 0) {
                nextTopId();
                expand(topBucket[topBucketId]);
            } else if (lowBucket[lowBucketId].isEmpty()) {
                nextLowId();
            }
            return lowBucket[lowBucketId].get(0).value;
        }
    }

    @Override
    public V dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Empty");
        } else {
            V entry = head();
            lowBucket[lowBucketId].remove(0);
            lowNEntries--;
            topNEntries--;
            if (isEmpty()) {
                topBucketId = 0;
                lowBucketId = 0;
            }
            return entry;
        }
    }

    @Override
    public void removeAll() {
        topBucketId = 0;
        lowBucketId = 0;
        topNEntries = 0;
        lowNEntries = 0;
        for (int i = 0; i < bucketSize; i++) {
            topBucket[i].clear();
            lowBucket[i].clear();
        }
    }

    @Override
    public boolean remove(int key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean changeKey(int oldKey, int newKey, V value) {
        throw new UnsupportedOperationException();
    }

    private void nextTopId() {
        while (topBucket[topBucketId].isEmpty()) {
            topBucketId++;
        }
    }

    private void nextLowId() {
        while (lowBucket[lowBucketId].isEmpty()) {
            lowBucketId++;
        }
    }

    private void expand(List<Node<V>> entries) {
        topBucket[topBucketId] = new ArrayList<>();
        lowBucketId = Integer.MAX_VALUE;
        for (Node<V> entry : entries) {
            int lowId = entry.key % bucketSize;
            lowBucket[lowId].add(0, entry);
            lowNEntries++;
            lowBucketId = Math.min(lowBucketId, lowId);
        }
    }
}
