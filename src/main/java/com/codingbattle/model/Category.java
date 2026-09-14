package com.codingbattle.model;

/**
 * Problem Categories for categorization and filtering.
 */
public enum Category {
    ARRAYS("Arrays"),
    STRINGS("Strings"),
    SEARCHING("Searching"),
    SORTING("Sorting"),
    STACK("Stack"),
    QUEUE("Queue"),
    LINKED_LIST("Linked List"),
    TREES("Trees"),
    GRAPHS("Graphs"),
    RECURSION("Recursion"),
    DYNAMIC_PROGRAMMING("Dynamic Programming"),
    MATHEMATICS("Mathematics");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Category fromString(String value) {
        if (value == null) return ARRAYS;
        String clean = value.trim().replace(" ", "_").toUpperCase();
        try {
            return Category.valueOf(clean);
        } catch (IllegalArgumentException e) {
            for (Category c : values()) {
                if (c.displayName.equalsIgnoreCase(value.trim())) {
                    return c;
                }
            }
            return ARRAYS;
        }
    }
}
