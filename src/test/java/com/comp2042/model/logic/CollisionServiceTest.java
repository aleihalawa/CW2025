package com.comp2042.model.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CollisionServiceTest {

    private final CollisionService collisionService = new CollisionService();

    private final int[][] emptyBoard = new int[5][5];

    private final int[][] singleBlock = {{1}};

    @Test
    void intersect_returnsFalse_whenSpaceIsFree() {
        boolean result = collisionService.intersect(emptyBoard, singleBlock, 2, 2);
        assertFalse(result, "Should not collide with empty space");
    }

    @Test
    void intersect_returnsTrue_whenOutOfBounds_Left() {
        boolean result = collisionService.intersect(emptyBoard, singleBlock, -1, 2);
        assertTrue(result, "Should collide with left wall boundary");
    }

    @Test
    void intersect_returnsTrue_whenOutOfBounds_Right() {
        boolean result = collisionService.intersect(emptyBoard, singleBlock, 5, 2);
        assertTrue(result, "Should collide with right wall boundary");
    }

    @Test
    void intersect_returnsTrue_whenOutOfBounds_Bottom() {
        boolean result = collisionService.intersect(emptyBoard, singleBlock, 2, 5);
        assertTrue(result, "Should collide with floor");
    }

    @Test
    void intersect_returnsTrue_whenOverlappingExistingBlock() {
        int[][] filledBoard = new int[5][5];
        filledBoard[2][2] = 1;
        boolean result = collisionService.intersect(filledBoard, singleBlock, 2, 2);
        assertTrue(result, "Should collide with existing block");
    }
}

