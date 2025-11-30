package com.comp2042.model;
/**
 * Immutable snapshot of the data the view needs to render
 * the current falling brick and the next brick preview.
 */
public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;
    private final int ghostY;
    private final int[][] nextBrickData;
    private final boolean isLocking;

    public ViewData(int[][] brickData, int xPosition, int yPosition, int ghostY, int[][] nextBrickData, boolean isLocking) {
        this.brickData = brickData;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.ghostY = ghostY;
        this.nextBrickData = nextBrickData;
        this.isLocking = isLocking;
    }

    public int[][] getBrickData() {
        return MatrixOperations.copy(brickData);
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

    public int[][] getNextBrickData() {
        return MatrixOperations.copy(nextBrickData);
    }

    public boolean isLocking() {
        return isLocking;
    }
}
