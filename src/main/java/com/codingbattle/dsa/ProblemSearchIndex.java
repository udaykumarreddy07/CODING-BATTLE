package com.codingbattle.dsa;

import com.codingbattle.model.Category;
import com.codingbattle.model.Difficulty;
import com.codingbattle.model.Problem;
import java.util.*;

/**
 * High-performance search and filtering index for Coding Problems.
 * Demonstrates:
 * 1. HashMap for O(1) ID lookup and category index buckets.
 * 2. Binary Search for O(log N) points-based lookup over sorted arrays.
 * 3. Linear search with multiple filter criteria (category, difficulty, text).
 */
public class ProblemSearchIndex {
    private final Map<Integer, Problem> idMap;
    private final Map<Category, List<Problem>> categoryIndex;
    private final List<Problem> sortedByPoints;

    public ProblemSearchIndex(List<Problem> problems) {
        this.idMap = new HashMap<>();
        this.categoryIndex = new EnumMap<>(Category.class);
        this.sortedByPoints = new ArrayList<>();

        if (problems != null) {
            for (Problem p : problems) {
                idMap.put(p.getProblemId(), p);
                categoryIndex.computeIfAbsent(p.getCategory(), k -> new ArrayList<>()).add(p);
                sortedByPoints.add(p);
            }
        }
        // Sort by points for Binary Search
        sortedByPoints.sort(Comparator.comparingInt(Problem::getPoints));
    }

    /**
     * O(1) direct lookup by Problem ID.
     */
    public Problem findById(int problemId) {
        return idMap.get(problemId);
    }

    /**
     * Binary Search to find problems matching an exact points value.
     * Time Complexity: O(log N) to find match + O(K) to collect adjacent duplicates.
     */
    public List<Problem> findByPoints(int targetPoints) {
        List<Problem> result = new ArrayList<>();
        int low = 0;
        int high = sortedByPoints.size() - 1;
        int matchIndex = -1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int pts = sortedByPoints.get(mid).getPoints();
            if (pts == targetPoints) {
                matchIndex = mid;
                break;
            } else if (pts < targetPoints) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        if (matchIndex != -1) {
            // Expand left
            int left = matchIndex;
            while (left >= 0 && sortedByPoints.get(left).getPoints() == targetPoints) {
                result.add(sortedByPoints.get(left));
                left--;
            }
            // Expand right
            int right = matchIndex + 1;
            while (right < sortedByPoints.size() && sortedByPoints.get(right).getPoints() == targetPoints) {
                result.add(sortedByPoints.get(right));
                right++;
            }
        }
        return result;
    }

    /**
     * Multi-criteria filtering supporting text search, category, difficulty, and solved status.
     */
    public List<Problem> filter(String query, Category category, Difficulty difficulty, Boolean solvedFilter) {
        List<Problem> results = new ArrayList<>();
        String cleanQuery = (query != null) ? query.trim().toLowerCase() : "";

        Collection<Problem> pool = (category != null && categoryIndex.containsKey(category))
                ? categoryIndex.get(category)
                : idMap.values();

        for (Problem p : pool) {
            if (!cleanQuery.isEmpty()) {
                boolean matchesTitle = p.getTitle().toLowerCase().contains(cleanQuery);
                boolean matchesCategory = p.getCategory().getDisplayName().toLowerCase().contains(cleanQuery);
                boolean matchesDesc = p.getDescription().toLowerCase().contains(cleanQuery);
                if (!matchesTitle && !matchesCategory && !matchesDesc) {
                    continue;
                }
            }

            if (category != null && p.getCategory() != category) {
                continue;
            }

            if (difficulty != null && p.getDifficulty() != difficulty) {
                continue;
            }

            if (solvedFilter != null && p.isSolvedByCurrentUser() != solvedFilter) {
                continue;
            }

            results.add(p);
        }

        // Return sorted alphabetically by title
        results.sort(Comparator.comparing(Problem::getTitle, String.CASE_INSENSITIVE_ORDER));
        return results;
    }
}
