package com.comp2042.model;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages leaderboard persistence for different game modes.
 * Each game mode has its own leaderboard file storing the top 10 scores.
 */
public class HighScoreManager {
    
    private static final String HIGHSCORE_FILE = "highscore.dat";

    /**
     * Loads the leaderboard for the specified game mode.
     * 
     * @param mode The game mode to load the leaderboard for
     * @return A list of ScoreEntry objects, sorted by score (descending).
     *         Returns an empty list if the file doesn't exist or an error occurs.
     */
    public static List<ScoreEntry> loadLeaderboard(GameMode mode) {
        File file = new File(mode.getFileName());
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            List<ScoreEntry> leaderboard = (List<ScoreEntry>) ois.readObject();
            return leaderboard != null ? leaderboard : new ArrayList<>();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.err.println("Failed to load leaderboard for " + mode + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Saves a new score entry to the leaderboard for the specified game mode.
     * If a player with the same username (case-sensitive) already exists, their score
     * will be updated if the new score is higher. Otherwise, a new entry is added.
     * The leaderboard is automatically sorted and limited to the top 10 entries.
     * 
     * @param mode The game mode to save the entry for
     * @param entry The score entry to save
     */
    public static void saveEntry(GameMode mode, ScoreEntry entry) {
        // Load the current leaderboard
        List<ScoreEntry> leaderboard = loadLeaderboard(mode);
        
        // Check if a player with the same username (case-sensitive) already exists
        String newPlayerName = entry.getName();
        ScoreEntry existingEntry = null;
        int existingIndex = -1;
        
        for (int i = 0; i < leaderboard.size(); i++) {
            ScoreEntry currentEntry = leaderboard.get(i);
            // Case-sensitive comparison
            if (currentEntry.getName().equals(newPlayerName)) {
                existingEntry = currentEntry;
                existingIndex = i;
                break;
            }
        }
        
        if (existingEntry != null) {
            // Player exists - update score if new score is higher
            if (entry.getScore() > existingEntry.getScore()) {
                // Replace the old entry with the new one (higher score)
                leaderboard.set(existingIndex, entry);
            }
            // If new score is lower or equal, do nothing (keep the existing higher score)
        } else {
            // New player - add the entry
            leaderboard.add(entry);
        }
        
        // Sort the list (descending by score)
        Collections.sort(leaderboard);
        
        // Keep only the top 10 entries
        while (leaderboard.size() > 10) {
            leaderboard.remove(leaderboard.size() - 1);
        }
        
        // Save the updated leaderboard back to the file
        File file = new File(mode.getFileName());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(leaderboard);
        } catch (IOException e) {
            System.err.println("Failed to save leaderboard entry for " + mode + ": " + e.getMessage());
        }
    }
    
    /**
     * Checks if a score qualifies for the top 10 leaderboard.
     * 
     * @param mode The game mode to check
     * @param score The score to check
     * @return true if the leaderboard has fewer than 10 entries OR 
     *         if the score is higher than the last entry's score, false otherwise
     */
    public static boolean isTopScore(GameMode mode, int score) {
        List<ScoreEntry> leaderboard = loadLeaderboard(mode);
        
        // If there are fewer than 10 entries, any score qualifies
        if (leaderboard.size() < 10) {
            return true;
        }
        
        // If there are 10 entries, check if the score beats the lowest one
        ScoreEntry lastEntry = leaderboard.get(leaderboard.size() - 1);
        return score > lastEntry.getScore();
    }
    
    /**
     * Resets the leaderboard for the specified game mode by clearing all entries.
     * 
     * @param mode The game mode to reset the leaderboard for
     */
    public static void resetLeaderboard(GameMode mode) {
        File file = new File(mode.getFileName());
        try {
            // Delete the leaderboard file if it exists
            if (file.exists()) {
                file.delete();
            }
            // Create an empty leaderboard file with an empty list
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(new ArrayList<ScoreEntry>());
            }
        } catch (IOException e) {
            System.err.println("Failed to reset leaderboard for " + mode + ": " + e.getMessage());
        }
    }
    
    /**
     * Resets all high score/leaderboards for all game modes.
     * This method resets CLASSIC, MIRROR, and POWERUPS mode leaderboards.
     * Also maintains backward compatibility with the old highscore.dat file.
     */
    public static void resetHighScore() {
        // Reset all game mode leaderboards
        resetLeaderboard(GameMode.CLASSIC);
        resetLeaderboard(GameMode.MIRROR);
        resetLeaderboard(GameMode.POWERUPS);
        
        // Also reset the old highscore.dat file for backward compatibility
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGHSCORE_FILE))) {
            writer.write("0");
        } catch (IOException e) {
            System.err.println("Failed to reset high score file: " + e.getMessage());
        }
    }
}
