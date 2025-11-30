package com.comp2042.model;

import com.comp2042.model.logic.BrickRotator;
import com.comp2042.model.logic.bricks.Brick;
import com.comp2042.model.logic.bricks.BrickGenerator;
import com.comp2042.model.logic.bricks.RandomBrickGenerator;
import com.comp2042.model.logic.CollisionService;
import com.comp2042.model.logic.LineClearService;


import java.awt.*;

public class SimpleBoard implements Board {

    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private int[][] currentGameMatrix;
    private Point currentOffset;
    private final Score score;
    private final CollisionService collisionService = new CollisionService();
    private final LineClearService lineClearService = new LineClearService();
    private static final int SPAWN_X = 4;
    private static final int SPAWN_Y = 0;
    private boolean isLocking = false;

    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        currentGameMatrix = new int[width][height];
        brickGenerator = new RandomBrickGenerator();
        brickRotator = new BrickRotator();
        score = new Score();
    }

    @Override
    public boolean moveBrickDown() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(0, 1);
        boolean conflict = collisionService.intersect(
                currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }


    @Override
    public boolean moveBrickLeft() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(-1, 0);
        boolean conflict = collisionService.intersect(
                currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean moveBrickRight() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(1, 0);
        boolean conflict = collisionService.intersect(
                currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean rotateLeftBrick() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();
        boolean conflict = collisionService.intersect(
                currentMatrix, nextShape.getShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
        if (conflict) {
            return false;
        } else {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
    }

    @Override
    public boolean createNewBrick() {
        isLocking = false;
        Brick currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);
        currentOffset = new Point(SPAWN_X, SPAWN_Y);
        return collisionService.intersect(
                currentGameMatrix, brickRotator.getCurrentShape(),
                (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    private int calculateGhostY() {
        int currentY = (int) currentOffset.getY();
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        int[][] currentShape = brickRotator.getCurrentShape();
        int currentX = (int) currentOffset.getX();
        
        // Start at current Y and simulate moving down until collision
        int testY = currentY;
        while (true) {
            testY++;
            boolean conflict = collisionService.intersect(
                currentMatrix, currentShape, currentX, testY);
            if (conflict) {
                // Return the last valid Y position (one above the collision)
                return testY - 1;
            }
        }
    }

    public void setLocking(boolean locking) {
        this.isLocking = locking;
    }

    @Override
    public ViewData getViewData() {
        int ghostY = calculateGhostY();
        return new ViewData(brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY(), ghostY, brickGenerator.getNextBrick().getShapeMatrix().get(0), this.isLocking);
    }

    @Override
    public void mergeBrickToBackground() {
        currentGameMatrix = MatrixOperations.merge(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = lineClearService.clearFullLines(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        return clearRow;
    }


    @Override
    public Score getScore() {
        return score;
    }


    @Override
    public void newGame() {
        isLocking = false;
        currentGameMatrix = new int[width][height];
        score.reset();
        createNewBrick();
    }
}
