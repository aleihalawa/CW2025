package com.comp2042.model.logic;

import com.comp2042.model.NextShapeInfo;
import com.comp2042.model.logic.bricks.Brick;

public class BrickRotator {

    private Brick brick;
    private int currentShape = 0;

    public NextShapeInfo getNextShape() {
        int nextShape = currentShape;
        nextShape = (++nextShape) % brick.getShapeMatrix().size();
        return new NextShapeInfo(brick.getShapeMatrix().get(nextShape), nextShape);
    }

    public NextShapeInfo getPreviousShape() {
        int nextShape = (currentShape - 1 + brick.getShapeMatrix().size()) % brick.getShapeMatrix().size();
        return new NextShapeInfo(brick.getShapeMatrix().get(nextShape), nextShape);
    }

    public int[][] getCurrentShape() {
        return brick.getShapeMatrix().get(currentShape);
    }

    public void setCurrentShape(int currentShape) {
        this.currentShape = currentShape;
    }

    public void setBrick(Brick brick) {
        this.brick = brick;
        currentShape = 0;
    }

    /**
     * Gets the current brick.
     * @return The current brick instance
     */
    public Brick getBrick() {
        return brick;
    }

}
