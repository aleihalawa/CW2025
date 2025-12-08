package com.comp2042.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PowerUp enum.
 * Tests enum values and basic functionality.
 */
class PowerUpTest {

    @Test
    void testPowerUpValues() {
        PowerUp[] values = PowerUp.values();
        
        assertTrue(values.length > 0);
        
        // Check that expected values exist
        boolean hasNone = false;
        boolean hasBomb = false;
        boolean hasDrill = false;
        boolean hasFreeze = false;
        
        for (PowerUp powerUp : values) {
            if (powerUp == PowerUp.NONE) hasNone = true;
            if (powerUp == PowerUp.BOMB) hasBomb = true;
            if (powerUp == PowerUp.DRILL) hasDrill = true;
            if (powerUp == PowerUp.FREEZE) hasFreeze = true;
        }
        
        assertTrue(hasNone, "PowerUp.NONE should exist");
        assertTrue(hasBomb, "PowerUp.BOMB should exist");
        assertTrue(hasDrill, "PowerUp.DRILL should exist");
        assertTrue(hasFreeze, "PowerUp.FREEZE should exist");
    }

    @Test
    void testValueOf() {
        assertEquals(PowerUp.NONE, PowerUp.valueOf("NONE"));
        assertEquals(PowerUp.BOMB, PowerUp.valueOf("BOMB"));
        assertEquals(PowerUp.DRILL, PowerUp.valueOf("DRILL"));
        assertEquals(PowerUp.FREEZE, PowerUp.valueOf("FREEZE"));
    }
}

