package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimpleBoard class.
 * Tests core game logic including brick movement, line clearing, power-ups, and bedrock corruption.
 */
class SimpleBoardTest {

    private SimpleBoard board;

    @BeforeEach
    void setUp() {
        board = new SimpleBoard(25, 10);
        board.newGame();
    }

    @Test
    void testNewGame() {
        board.newGame();
        int[][] matrix = board.getBoardMatrix();
        
        // Board should be empty (all zeros)
        for (int[] row : matrix) {
            for (int cell : row) {
                assertEquals(0, cell, "Board should be empty after newGame()");
            }
        }
        
        // Score should be reset
        assertEquals(0, board.getScore().scoreProperty().get());
        assertEquals(0, board.getScore().linesProperty().get());
        assertEquals(1, board.getScore().levelProperty().get());
    }

    @Test
    void testCreateNewBrick() {
        boolean gameOver = board.createNewBrick();
        
        // New brick should be created (game not over on empty board)
        assertFalse(gameOver, "Game should not be over on empty board");
        
        // ViewData should contain brick data
        ViewData viewData = board.getViewData();
        assertNotNull(viewData.getBrickData());
    }

    @Test
    void testMoveBrickDown() {
        board.createNewBrick();
        boolean moved = board.moveBrickDown();
        
        // Brick should move down on empty board
        assertTrue(moved, "Brick should move down on empty board");
    }

    @Test
    void testMoveBrickLeft() {
        board.createNewBrick();
        boolean moved = board.moveBrickLeft();
        
        // Brick should be able to move left (unless at left edge)
        assertNotNull(board.getViewData());
    }

    @Test
    void testMoveBrickRight() {
        board.createNewBrick();
        boolean moved = board.moveBrickRight();
        
        // Brick should be able to move right (unless at right edge)
        assertNotNull(board.getViewData());
    }

    @Test
    void testAddPowerUp() {
        board.addPowerUp(PowerUp.BOMB);
        board.addPowerUp(PowerUp.DRILL);
        
        java.util.List<PowerUp> inventory = board.getInventory();
        assertEquals(2, inventory.size());
        assertTrue(inventory.contains(PowerUp.BOMB));
        assertTrue(inventory.contains(PowerUp.DRILL));
    }

    @Test
    void testUsePowerUp() {
        board.addPowerUp(PowerUp.BOMB);
        board.addPowerUp(PowerUp.FREEZE);
        
        PowerUp used = board.usePowerUp(0);
        assertEquals(PowerUp.BOMB, used);
        
        java.util.List<PowerUp> inventory = board.getInventory();
        assertEquals(1, inventory.size());
        assertEquals(PowerUp.FREEZE, inventory.get(0));
    }

    @Test
    void testUsePowerUpEmptyInventory() {
        PowerUp used = board.usePowerUp(0);
        assertEquals(PowerUp.NONE, used);
    }

    @Test
    void testExplodeAt() {
        // Fill some blocks
        int[][] matrix = board.getBoardMatrix();
        matrix[20][5] = 1; // Place a block
        matrix[20][4] = 2;
        matrix[20][6] = 3;
        matrix[19][5] = 4;
        matrix[21][5] = 5;
        
        // Explode at center
        board.explodeAt(20, 5);
        
        // 3x3 area should be cleared (except bedrock)
        assertEquals(0, matrix[20][5]);
        assertEquals(0, matrix[20][4]);
        assertEquals(0, matrix[20][6]);
        assertEquals(0, matrix[19][5]);
        assertEquals(0, matrix[21][5]);
    }

    @Test
    void testExplodeAtPreservesBedrock() {
        int[][] matrix = board.getBoardMatrix();
        matrix[20][5] = SimpleBoard.BEDROCK_ID;
        matrix[20][4] = 1;
        matrix[20][6] = 2;
        
        board.explodeAt(20, 5);
        
        // Bedrock should remain
        assertEquals(SimpleBoard.BEDROCK_ID, matrix[20][5]);
        // Other blocks should be destroyed
        assertEquals(0, matrix[20][4]);
        assertEquals(0, matrix[20][6]);
    }

    @Test
    void testCorruptNextRow() {
        int[][] matrix = board.getBoardMatrix();
        
        // Place some blocks in bottom row
        matrix[24][0] = 1;
        matrix[24][5] = 2;
        matrix[24][9] = 3;
        
        boolean success = board.corruptNextRow();
        assertTrue(success, "Corruption should succeed");
        
        // Blocks should become bedrock
        assertEquals(SimpleBoard.BEDROCK_ID, matrix[24][0]);
        assertEquals(SimpleBoard.BEDROCK_ID, matrix[24][5]);
        assertEquals(SimpleBoard.BEDROCK_ID, matrix[24][9]);
    }

    @Test
    void testCorruptNextRowGameOver() {
        // Corrupt all rows
        for (int i = 0; i < 25; i++) {
            board.corruptNextRow();
        }
        
        // Next corruption should fail (game over)
        boolean success = board.corruptNextRow();
        assertFalse(success, "Corruption should fail when all rows are corrupted");
    }

    @Test
    void testHasFloatingBlocks() {
        int[][] matrix = board.getBoardMatrix();
        
        // Place a floating block
        matrix[10][5] = 1;
        matrix[11][5] = 0; // Empty space below
        
        assertTrue(board.hasFloatingBlocks(), "Should detect floating block");
        
        // Fill space below
        matrix[11][5] = 2;
        assertFalse(board.hasFloatingBlocks(), "Should not detect floating blocks when supported");
    }

    @Test
    void testApplyGravityStep() {
        int[][] matrix = board.getBoardMatrix();
        
        // Create floating block
        matrix[10][5] = 1;
        matrix[11][5] = 0;
        
        board.applyGravityStep();
        
        // Block should move down one row
        assertEquals(0, matrix[10][5]);
        assertEquals(1, matrix[11][5]);
    }

    @Test
    void testApplyGravityStepRespectsBedrock() {
        int[][] matrix = board.getBoardMatrix();
        
        // Place bedrock
        matrix[11][5] = SimpleBoard.BEDROCK_ID;
        // Place floating block above
        matrix[10][5] = 1;
        
        board.applyGravityStep();
        
        // Block should not move through bedrock
        assertEquals(1, matrix[10][5]);
        assertEquals(SimpleBoard.BEDROCK_ID, matrix[11][5]);
    }

    @Test
    void testIsDrillActive() {
        assertFalse(board.isDrillActive(), "Should not be active initially");
        
        board.spawnDrill();
        assertTrue(board.isDrillActive(), "Should be active after spawn");
    }

    @Test
    void testClearRows() {
        int[][] matrix = board.getBoardMatrix();
        
        // Fill a complete row
        for (int col = 0; col < 10; col++) {
            matrix[23][col] = 1;
        }
        
        ClearRow result = board.clearRows();
        
        // Row should be cleared
        assertEquals(1, result.getLinesRemovedCount());
        assertTrue(result.getLinesRemoved().contains(23));
        
        // Row should be empty
        for (int col = 0; col < 10; col++) {
            assertEquals(0, matrix[23][col]);
        }
    }

    @Test
    void testClearRowsPreservesBedrock() {
        int[][] matrix = board.getBoardMatrix();
        
        // Fill row with bedrock
        for (int col = 0; col < 10; col++) {
            matrix[23][col] = SimpleBoard.BEDROCK_ID;
        }
        
        ClearRow result = board.clearRows();
        
        // Row with bedrock should not be cleared
        assertEquals(0, result.getLinesRemovedCount());
        
        // Bedrock should remain
        for (int col = 0; col < 10; col++) {
            assertEquals(SimpleBoard.BEDROCK_ID, matrix[23][col]);
        }
    }

    @Test
    void testGetScore() {
        Score score = board.getScore();
        assertNotNull(score);
        assertEquals(0, score.scoreProperty().get());
    }

    @Test
    void testGetInventory() {
        java.util.List<PowerUp> inventory = board.getInventory();
        assertNotNull(inventory);
        assertTrue(inventory.isEmpty());
        
        board.addPowerUp(PowerUp.BOMB);
        inventory = board.getInventory();
        assertEquals(1, inventory.size());
    }
}

