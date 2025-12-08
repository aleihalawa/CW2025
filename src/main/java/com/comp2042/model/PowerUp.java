package com.comp2042.model;

/**
 * Represents different types of power ups available in Power Ups game mode.
 */
public enum PowerUp {
    NONE,
    BOMB,
    DRILL,
    FREEZE;
    
    /**
     * Gets the display name for this power up.
     * 
     * @return The display name (e.g., "BOMB", "DRILL")
     */
    public String getDisplayName() {
        if (this == NONE) {
            return "";
        }
        return this.name();
    }
}

