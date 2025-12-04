package com.comp2042.model;

/**
 * Manages global game settings and preferences.
 * Uses static fields to maintain settings across the application.
 */
public class GameSettings {
    
    private static boolean ghostModeEnabled = true;
    
    /**
     * Checks if ghost mode is enabled.
     * 
     * @return true if ghost mode is enabled, false otherwise
     */
    public static boolean isGhostModeEnabled() {
        return ghostModeEnabled;
    }
    
    /**
     * Sets the ghost mode enabled state.
     * 
     * @param enabled true to enable ghost mode, false to disable
     */
    public static void setGhostModeEnabled(boolean enabled) {
        ghostModeEnabled = enabled;
    }
}

