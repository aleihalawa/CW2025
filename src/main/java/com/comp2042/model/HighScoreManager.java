package com.comp2042.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Manages high score persistence to a local file.
 */
public class HighScoreManager {

    private static final String HIGHSCORE_FILE = "highscore.dat";

    /**
     * Loads the high score from the file.
     * @return The saved high score, or 0 if the file doesn't exist or an error occurs
     */
    public int loadHighScore() {
        File file = new File(HIGHSCORE_FILE);
        if (!file.exists()) {
            return 0;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                return Integer.parseInt(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            // Return 0 if file read fails or content is invalid
            return 0;
        }

        return 0;
    }

    /**
     * Saves a new high score to the file.
     * @param newScore The score to save
     */
    public void saveHighScore(int newScore) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGHSCORE_FILE))) {
            writer.write(String.valueOf(newScore));
        } catch (IOException e) {
            // Log error but don't throw - high score saving failure shouldn't crash the game
            System.err.println("Failed to save high score: " + e.getMessage());
        }
    }

    /**
     * Checks if the current score is a new high score.
     * @param currentScore The current score to check
     * @return true if currentScore is greater than the saved high score, false otherwise
     */
    public boolean isNewHighScore(int currentScore) {
        int savedScore = loadHighScore();
        return currentScore > savedScore;
    }
}

