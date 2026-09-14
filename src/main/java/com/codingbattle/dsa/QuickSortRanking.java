package com.codingbattle.dsa;

import com.codingbattle.model.LeaderboardEntry;
import java.util.Comparator;
import java.util.List;

/**
 * Custom QuickSort implementation demonstrating DSA sorting algorithms.
 * Time Complexity: Average O(N log N), Worst O(N^2)
 * Space Complexity: O(log N) recursion stack
 */
public class QuickSortRanking {

    /**
     * Sorts a list of LeaderboardEntry objects using QuickSort and assigns correct 1-based ranks.
     */
    public static void sortAndAssignRanks(List<LeaderboardEntry> list) {
        if (list == null || list.size() <= 1) {
            if (list != null && list.size() == 1) {
                list.get(0).setRank(1);
            }
            return;
        }
        quickSort(list, 0, list.size() - 1, LeaderboardEntry::compareTo);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }
    }

    /**
     * Generic QuickSort with custom comparator.
     */
    public static <T> void quickSort(List<T> list, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            int pi = partition(list, low, high, comparator);
            quickSort(list, low, pi - 1, comparator);
            quickSort(list, pi + 1, high, comparator);
        }
    }

    private static <T> int partition(List<T> list, int low, int high, Comparator<T> comparator) {
        // Median-of-three pivot selection to prevent worst-case on sorted data
        int mid = low + (high - low) / 2;
        if (comparator.compare(list.get(mid), list.get(low)) < 0) {
            swap(list, low, mid);
        }
        if (comparator.compare(list.get(high), list.get(low)) < 0) {
            swap(list, low, high);
        }
        if (comparator.compare(list.get(high), list.get(mid)) < 0) {
            swap(list, mid, high);
        }
        swap(list, mid, high);

        T pivot = list.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}
