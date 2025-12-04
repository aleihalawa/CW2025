package com.comp2042.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable snapshot of the data the view needs to render
 * the current falling brick and the next brick preview.
 */
public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;
    private final int ghostY;
    private final List<int[][]> nextBricks;
    private final boolean isLocking;

    public ViewData(int[][] brickData, int xPosition, int yPosition, int ghostY, List<int[][]> nextBricks, boolean isLocking) {
        this.brickData = brickData;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.ghostY = ghostY;
        // Create defensive copy
        this.nextBricks = new ArrayList<>();
        for (int[][] brick : nextBricks) {
            this.nextBricks.add(MatrixOperations.copy(brick));
        }
        this.isLocking = isLocking;
    }

    public int[][] getBrickData() {
        // Return direct reference - brickData is already immutable (final field)
        // No need to copy since this is read-only data that won't be modified
        return brickData;
    }

    public int getxPosition() {
        return xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public int getGhostY() {
        return ghostY;
    }

    public List<int[][]> getNextBricks() {
        // Return defensive copy
        List<int[][]> result = new ArrayList<>();
        for (int[][] brick : nextBricks) {
            result.add(MatrixOperations.copy(brick));
        }
        return result;
    }

    public boolean isLocking() {
        return isLocking;
    }
}
