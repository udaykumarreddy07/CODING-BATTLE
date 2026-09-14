package com.codingbattle.service;

import com.codingbattle.dao.ScoreDAO;
import com.codingbattle.dsa.LeaderboardHeap;
import com.codingbattle.dsa.QuickSortRanking;
import com.codingbattle.model.LeaderboardEntry;
import com.codingbattle.model.Score;

import java.util.ArrayList;
import java.util.List;

/**
 * Service Layer for Leaderboards and Dynamic DSA Rankings.
 * Demonstrates:
 * - QuickSort ranking
 * - Heap-based Top-K extraction
 */
public class LeaderboardService {

    private final ScoreDAO scoreDAO;

    public LeaderboardService() {
        this.scoreDAO = new ScoreDAO();
    }

    /**
     * Returns full global leaderboard sorted via DSA QuickSort.
     */
    public List<LeaderboardEntry> getGlobalLeaderboard() {
        List<LeaderboardEntry> rawList = scoreDAO.getGlobalLeaderboardData();
        // Use custom QuickSort implementation
        QuickSortRanking.sortAndAssignRanks(rawList);
        return rawList;
    }

    /**
     * Returns Top-K leaders using custom Max-Heap (PriorityQueue).
     * Demonstrates O(K log N) Heap retrieval.
     */
    public List<LeaderboardEntry> getTopKLeaders(int k) {
        List<LeaderboardEntry> rawList = scoreDAO.getGlobalLeaderboardData();
        LeaderboardHeap heap = new LeaderboardHeap(rawList);
        return heap.getTopK(k);
    }

    /**
     * Filters leaderboard entries by student username.
     */
    public List<LeaderboardEntry> searchLeaderboard(String query) {
        List<LeaderboardEntry> all = getGlobalLeaderboard();
        if (query == null || query.trim().isEmpty()) {
            return all;
        }
        String q = query.trim().toLowerCase();
        List<LeaderboardEntry> filtered = new ArrayList<>();
        for (LeaderboardEntry e : all) {
            if (e.getUsername().toLowerCase().contains(q)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    public List<Score> getBattleScores(int battleId) {
        return scoreDAO.getScoresByBattleId(battleId);
    }
}
