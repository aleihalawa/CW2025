package com.comp2042.model.logic;

import com.comp2042.model.ClearRow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LineClearServiceTest {

    private final LineClearService lineClearService = new LineClearService();

    @Test
    void clearFullLines_doesNothing_whenBoardIsEmpty() {
        int[][] matrix = new int[5][5];
        ClearRow result = lineClearService.clearFullLines(matrix);
        assertEquals(0, result.getLinesRemovedCount());
        assertEquals(0, result.getScoreBonus());
    }

    @Test
    void clearFullLines_removesOneRow_whenRowIsFull() {
        int[][] matrix = new int[5][5];
        for (int x = 0; x < 5; x++) matrix[4][x] = 1; // Fill bottom row
        ClearRow result = lineClearService.clearFullLines(matrix);
        
        assertEquals(1, result.getLinesRemovedCount());
        assertEquals(50, result.getScoreBonus());
        assertEquals(0, result.getNewMatrix()[4][0], "Bottom row should be cleared");
    }

    @Test
    void clearFullLines_calculatesScore_forMultipleRows() {
        int[][] matrix = new int[5][5];
        for (int x = 0; x < 5; x++) {
            matrix[4][x] = 1;
            matrix[3][x] = 1;
        }
        ClearRow result = lineClearService.clearFullLines(matrix);
        
        assertEquals(2, result.getLinesRemovedCount());
        assertEquals(200, result.getScoreBonus());
    }
}

