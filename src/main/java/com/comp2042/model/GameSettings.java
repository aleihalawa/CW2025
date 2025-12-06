package com.comp2042.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Manages global game settings and preferences.
 * Uses static fields to maintain settings across the application.
 * Settings are persisted to a file and loaded on startup.
 */
public class GameSettings {
    
    private static final String SETTINGS_FILE = "settings.dat";
    
    private static boolean ghostModeEnabled = true;
    private static double musicVolume = 0.5;
    private static double sfxVolume = 0.5;
    private static String playerName = "Player";
    private static com.comp2042.model.GameMode selectedMode = com.comp2042.model.GameMode.CLASSIC;
    
    // Load settings when class is first loaded
    static {
        load();
    }
    
    /**
     * Checks if ghost mode is enabled.
     * 
     * @return true if ghost mode is enabled, false otherwise
     */
    public static boolean isGhostModeEnabled() {
        return ghostModeEnabled;
    }
    
    /**
     * Sets the ghost mode enabled state and saves to file.
     * 
     * @param enabled true to enable ghost mode, false to disable
     */
    public static void setGhostModeEnabled(boolean enabled) {
        ghostModeEnabled = enabled;
        save();
    }
    
    /**
     * Gets the music volume.
     * 
     * @return Music volume (0.0 to 1.0)
     */
    public static double getMusicVolume() {
        return musicVolume;
    }
    
    /**
     * Sets the music volume and saves to file.
     * 
     * @param volume Music volume (0.0 to 1.0)
     */
    public static void setMusicVolume(double volume) {
        musicVolume = Math.max(0.0, Math.min(1.0, volume)); // Clamp to 0.0-1.0
        save();
    }
    
    /**
     * Gets the SFX volume.
     * 
     * @return SFX volume (0.0 to 1.0)
     */
    public static double getSfxVolume() {
        return sfxVolume;
    }
    
    /**
     * Sets the SFX volume and saves to file.
     * 
     * @param volume SFX volume (0.0 to 1.0)
     */
    public static void setSfxVolume(double volume) {
        sfxVolume = Math.max(0.0, Math.min(1.0, volume)); // Clamp to 0.0-1.0
        save();
    }
    
    /**
     * Gets the player name.
     * 
     * @return The player's name
     */
    public static String getPlayerName() {
        return playerName;
    }
    
    /**
     * Sets the player name and saves to file.
     * 
     * @param name The player's name
     */
    public static void setPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            playerName = "Player";
        } else {
            playerName = name.trim();
        }
        save();
    }
    
    /**
     * Gets the selected game mode.
     * 
     * @return The selected game mode
     */
    public static com.comp2042.model.GameMode getSelectedGameMode() {
        return selectedMode;
    }
    
    /**
     * Sets the selected game mode.
     * 
     * @param mode The game mode to select
     */
    public static void setSelectedGameMode(com.comp2042.model.GameMode mode) {
        if (mode != null) {
            selectedMode = mode;
        }
    }
    
    /**
     * Saves all settings to the settings file.
     */
    private static void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SETTINGS_FILE))) {
            writer.write("ghostMode=" + ghostModeEnabled);
            writer.newLine();
            writer.write("musicVolume=" + musicVolume);
            writer.newLine();
            writer.write("sfxVolume=" + sfxVolume);
            writer.newLine();
            writer.write("playerName=" + playerName);
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
    
    /**
     * Loads settings from the settings file.
     * If the file doesn't exist or an error occurs, uses default values.
     */
    private static void load() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            // Use defaults if file doesn't exist
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                String[] parts = line.split("=", 2);
                if (parts.length != 2) {
                    continue;
                }
                
                String key = parts[0].trim();
                String value = parts[1].trim();
                
                switch (key) {
                    case "ghostMode":
                        ghostModeEnabled = Boolean.parseBoolean(value);
                        break;
                    case "musicVolume":
                        try {
                            musicVolume = Double.parseDouble(value);
                            musicVolume = Math.max(0.0, Math.min(1.0, musicVolume)); // Clamp
                        } catch (NumberFormatException e) {
                            // Use default if invalid
                        }
                        break;
                    case "sfxVolume":
                        try {
                            sfxVolume = Double.parseDouble(value);
                            sfxVolume = Math.max(0.0, Math.min(1.0, sfxVolume)); // Clamp
                        } catch (NumberFormatException e) {
                            // Use default if invalid
                        }
                        break;
                    case "playerName":
                        playerName = value.isEmpty() ? "Player" : value;
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load settings: " + e.getMessage());
            // Use defaults on error
        }
    }
}

