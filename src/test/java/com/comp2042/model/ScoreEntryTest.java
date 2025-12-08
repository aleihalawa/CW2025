package com.comp2042.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScoreEntry class.
 * Tests score entry creation, comparison, and serialization.
 */
class ScoreEntryTest {

    @Test
    void testConstructor() {
        ScoreEntry entry = new ScoreEntry("Player1", 1000);
        
        assertEquals("Player1", entry.getPlayerName());
        assertEquals(1000, entry.getScore());
    }

    @Test
    void testCompareTo() {
        ScoreEntry entry1 = new ScoreEntry("Player1", 1000);
        ScoreEntry entry2 = new ScoreEntry("Player2", 500);
        ScoreEntry entry3 = new ScoreEntry("Player3", 1500);
        ScoreEntry entry4 = new ScoreEntry("Player4", 1000);
        
        // Higher score should be "less than" (for descending order)
        assertTrue(entry3.compareTo(entry1) < 0);
        assertTrue(entry1.compareTo(entry2) < 0);
        assertTrue(entry2.compareTo(entry3) > 0);
        
        // Equal scores
        assertEquals(0, entry1.compareTo(entry4));
    }

    @Test
    void testGetPlayerName() {
        ScoreEntry entry = new ScoreEntry("TestPlayer", 500);
        assertEquals("TestPlayer", entry.getPlayerName());
    }

    @Test
    void testGetScore() {
        ScoreEntry entry = new ScoreEntry("TestPlayer", 750);
        assertEquals(750, entry.getScore());
    }

    @Test
    void testToString() {
        ScoreEntry entry = new ScoreEntry("Player", 1000);
        String str = entry.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("Player"));
        assertTrue(str.contains("1000"));
    }
}

