package com.comp2042.model;

/**
 * Represents different game modes, each with its own leaderboard file.
 */
public enum GameMode {
    CLASSIC("leaderboard_classic.dat"),
    MIRROR("leaderboard_mirror.dat"),
    POWERUPS("leaderboard_powerup.dat");
    
    private final String fileName;
    
    /**
     * Constructor that maps each game mode to a leaderboard filename.
     * 
     * @param fileName The filename for the leaderboard file
     */
    GameMode(String fileName) {
        this.fileName = fileName;
    }
    
    /**
     * Gets the filename associated with this game mode.
     * 
     * @return The filename for the leaderboard file
     */
    public String getFileName() {
        return fileName;
    }
}

