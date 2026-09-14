package com.codingbattle.dsa;

import com.codingbattle.model.LeaderboardEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Custom Max-Heap (PriorityQueue) implementation for the Leaderboard.
 * Demonstrates DSA Heap operations: insert O(log N), extractMax O(log N),
 * and Top-K retrieval in O(K log N).
 */
public class LeaderboardHeap {
    private final List<LeaderboardEntry> heap;

    public LeaderboardHeap() {
        this.heap = new ArrayList<>();
    }

    public LeaderboardHeap(List<LeaderboardEntry> entries) {
        this.heap = new ArrayList<>();
        if (entries != null) {
            for (LeaderboardEntry entry : entries) {
                insert(entry);
            }
        }
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Inserts an entry into the max-heap and bubbles up to maintain heap invariant.
     * Time Complexity: O(log N)
     */
    public void insert(LeaderboardEntry entry) {
        heap.add(entry);
        siftUp(heap.size() - 1);
    }

    /**
     * Extracts the top performer with maximum score.
     * Time Complexity: O(log N)
     */
    public LeaderboardEntry extractMax() {
        if (isEmpty()) {
            throw new NoSuchElementException("Leaderboard heap is empty");
        }
        LeaderboardEntry max = heap.get(0);
        LeaderboardEntry last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return max;
    }

    /**
     * Peeks at the current rank 1 leader without removal.
     * Time Complexity: O(1)
     */
    public LeaderboardEntry peekMax() {
        if (isEmpty()) return null;
        return heap.get(0);
    }

    /**
     * Retrieves the top K entries in ranked descending order.
     * Time Complexity: O(K log N)
     */
    public List<LeaderboardEntry> getTopK(int k) {
        LeaderboardHeap copy = new LeaderboardHeap(this.heap);
        List<LeaderboardEntry> topList = new ArrayList<>();
        int limit = Math.min(k, copy.size());
        for (int i = 0; i < limit; i++) {
            LeaderboardEntry entry = copy.extractMax();
            entry.setRank(i + 1);
            topList.add(entry);
        }
        return topList;
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            // Higher score has higher priority
            if (heap.get(index).compareTo(heap.get(parent)) < 0) {
                swap(index, parent);
                index = parent;
            } else {
                break;
            }
        }
    }

    private void siftDown(int index) {
        int size = heap.size();
        while (index < size) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int largest = index;

            if (left < size && heap.get(left).compareTo(heap.get(largest)) < 0) {
                largest = left;
            }
            if (right < size && heap.get(right).compareTo(heap.get(largest)) < 0) {
                largest = right;
            }

            if (largest != index) {
                swap(index, largest);
                index = largest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        LeaderboardEntry temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
