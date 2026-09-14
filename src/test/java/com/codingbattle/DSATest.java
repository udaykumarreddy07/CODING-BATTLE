package com.codingbattle;

import com.codingbattle.dsa.EditorUndoStack;
import com.codingbattle.dsa.LeaderboardHeap;
import com.codingbattle.dsa.QuickSortRanking;
import com.codingbattle.model.LeaderboardEntry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DSATest {

    @Test
    public void testLeaderboardHeapTopK() {
        LeaderboardHeap heap = new LeaderboardHeap();
        heap.insert(new LeaderboardEntry(0, 1, "Alice", 500, 5, 1, 80.0));
        heap.insert(new LeaderboardEntry(0, 2, "Bob", 1200, 10, 2, 90.0));
        heap.insert(new LeaderboardEntry(0, 3, "Charlie", 800, 8, 0, 75.0));
        heap.insert(new LeaderboardEntry(0, 4, "Dave", 300, 3, 0, 60.0));

        assertEquals(4, heap.size());
        assertEquals("Bob", heap.peekMax().getUsername());

        List<LeaderboardEntry> top2 = heap.getTopK(2);
        assertEquals(2, top2.size());
        assertEquals("Bob", top2.get(0).getUsername());
        assertEquals(1, top2.get(0).getRank());
        assertEquals("Charlie", top2.get(1).getUsername());
        assertEquals(2, top2.get(1).getRank());
    }

    @Test
    public void testQuickSortRanking() {
        List<LeaderboardEntry> list = new ArrayList<>();
        list.add(new LeaderboardEntry(0, 1, "Alice", 500, 5, 0, 70.0));
        list.add(new LeaderboardEntry(0, 2, "Bob", 1500, 15, 0, 85.0));
        list.add(new LeaderboardEntry(0, 3, "Charlie", 900, 9, 0, 80.0));

        QuickSortRanking.sortAndAssignRanks(list);

        assertEquals("Bob", list.get(0).getUsername());
        assertEquals(1, list.get(0).getRank());
        assertEquals("Charlie", list.get(1).getUsername());
        assertEquals(2, list.get(1).getRank());
        assertEquals("Alice", list.get(2).getUsername());
        assertEquals(3, list.get(2).getRank());
    }

    @Test
    public void testEditorUndoStack() {
        EditorUndoStack<String> stack = new EditorUndoStack<>();
        assertTrue(stack.isEmpty());

        stack.push("v1");
        stack.push("v2");
        stack.push("v3");

        assertEquals(3, stack.size());
        assertEquals("v3", stack.peek());
        assertEquals("v3", stack.pop());
        assertEquals("v2", stack.pop());
        assertEquals("v1", stack.pop());
        assertTrue(stack.isEmpty());
    }
}
