package com.comp2042.model;

import java.io.Serializable;

/**
 * Represents a single score entry in the leaderboard.
 * Implements Serializable for persistence and Comparable for sorting.
 */
public class ScoreEntry implements Serializable, Comparable<ScoreEntry> {
    
    private static final long serialVersionUID = 1L;
    
    private final String name;
    private final int score;
    
    /**
     * Constructs a new score entry.
     * 
     * @param name The player's name
     * @param score The player's score
     */
    public ScoreEntry(String name, int score) {
        this.name = name;
        this.score = score;
    }
    
    /**
     * Gets the player's name.
     * 
     * @return The player's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the player's score.
     * 
     * @return The player's score
     */
    public int getScore() {
        return score;
    }
    
    /**
     * Returns a formatted string representation of this score entry.
     * 
     * @return A string in the format "Name: Score"
     */
    @Override
    public String toString() {
        return name + ": " + score;
    }
    
    /**
     * Compares this score entry to another by score in descending order
     * (highest score first).
     * 
     * @param other The other score entry to compare
     * @return A negative integer if this score is greater than other's,
     *         zero if they are equal, or a positive integer if this score is less
     */
    @Override
    public int compareTo(ScoreEntry other) {
        // Descending order: higher score comes first
        return Integer.compare(other.score, this.score);
    }
}

