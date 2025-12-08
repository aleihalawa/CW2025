package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Score class.
 * Tests score calculation, level progression, and line counting.
 */
class ScoreTest {

    private Score score;

    @BeforeEach
    void setUp() {
        score = new Score();
    }

    @Test
    void testInitialState() {
        assertEquals(0, score.scoreProperty().get());
        assertEquals(0, score.linesProperty().get());
        assertEquals(1, score.levelProperty().get());
    }

    @Test
    void testAddScore() {
        score.add(100);
        assertEquals(100, score.scoreProperty().get());
        
        score.add(50);
        assertEquals(150, score.scoreProperty().get());
    }

    @Test
    void testAddLines() {
        score.addLines(1);
        assertEquals(1, score.linesProperty().get());
        
        score.addLines(4);
        assertEquals(5, score.linesProperty().get());
    }

    @Test
    void testLevelProgression() {
        // Level should increase every 10 lines
        score.addLines(10);
        assertEquals(2, score.levelProperty().get());
        
        score.addLines(10);
        assertEquals(3, score.levelProperty().get());
    }

    @Test
    void testReset() {
        score.add(1000);
        score.addLines(15);
        
        score.reset();
        
        assertEquals(0, score.scoreProperty().get());
        assertEquals(0, score.linesProperty().get());
        assertEquals(1, score.levelProperty().get());
    }

    @Test
    void testScoreProperty() {
        assertNotNull(score.scoreProperty());
        assertTrue(score.scoreProperty().get() >= 0);
    }

    @Test
    void testLevelProperty() {
        assertNotNull(score.levelProperty());
        assertTrue(score.levelProperty().get() >= 1);
    }

    @Test
    void testLinesProperty() {
        assertNotNull(score.linesProperty());
        assertTrue(score.linesProperty().get() >= 0);
    }
}

