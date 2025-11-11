package com.comp2042.model.logic;

import com.comp2042.model.MatrixOperations;


public class CollisionService {

    public boolean intersect(int[][] board, int[][] shape, int offX, int offY) {
        return MatrixOperations.intersect(board, shape, offX, offY);
    }
}
