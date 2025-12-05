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
     * The leaderboard is automatically sorted and limited to the top 10 entries.
     * 
     * @param mode The game mode to save the entry for
     * @param entry The score entry to save
     */
    public static void saveEntry(GameMode mode, ScoreEntry entry) {
        // Load the current leaderboard
        List<ScoreEntry> leaderboard = loadLeaderboard(mode);
        
        // Add the new entry
        leaderboard.add(entry);
        
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
     * Resets the high score to 0 by overwriting the highscore.dat file.
     * This method is kept for backward compatibility with the old system.
     * 
     * @deprecated Consider using leaderboard methods instead
     */
    @Deprecated
    public static void resetHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGHSCORE_FILE))) {
            writer.write("0");
        } catch (IOException e) {
            System.err.println("Failed to reset high score: " + e.getMessage());
        }
    }
}
