package com.comp2042.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MatrixOperations utility class.
 * Tests matrix copying and manipulation operations.
 */
class MatrixOperationsTest {

    @Test
    void testCopy() {
        int[][] original = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        
        int[][] copy = MatrixOperations.copy(original);
        
        // Should be equal
        assertArrayEquals(original, copy);
        
        // Should be different objects (deep copy)
        assertNotSame(original, copy);
        
        // Modifying copy should not affect original
        copy[0][0] = 99;
        assertEquals(1, original[0][0]);
        assertEquals(99, copy[0][0]);
    }

    @Test
    void testCopyEmptyMatrix() {
        int[][] original = new int[0][0];
        int[][] copy = MatrixOperations.copy(original);
        assertArrayEquals(original, copy);
    }

    @Test
    void testCopyNull() {
        int[][] copy = MatrixOperations.copy(null);
        assertNull(copy);
    }

    @Test
    void testDeepCopyList() {
        java.util.List<int[][]> original = new java.util.ArrayList<>();
        original.add(new int[][]{{1, 2}, {3, 4}});
        original.add(new int[][]{{5, 6}, {7, 8}});
        
        java.util.List<int[][]> copy = MatrixOperations.deepCopyList(original);
        
        assertEquals(original.size(), copy.size());
        
        // Should be different objects
        assertNotSame(original, copy);
        assertNotSame(original.get(0), copy.get(0));
        
        // Modifying copy should not affect original
        copy.get(0)[0][0] = 99;
        assertEquals(1, original.get(0)[0][0]);
        assertEquals(99, copy.get(0)[0][0]);
    }
}

