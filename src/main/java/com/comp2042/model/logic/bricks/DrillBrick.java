package com.comp2042.model.logic.bricks;

import com.comp2042.model.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

public final class DrillBrick implements Brick {

    private final List<int[][]> brickMatrix = new ArrayList<>();

    public DrillBrick() {
        // 1x1 matrix with ID 11 representing the Drill
        brickMatrix.add(new int[][]{
                {11}
        });
    }

    @Override
    public List<int[][]> getShapeMatrix() {
        return MatrixOperations.deepCopyList(brickMatrix);
    }
}

