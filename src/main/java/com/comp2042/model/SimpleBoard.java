package com.comp2042.model;

import com.comp2042.model.logic.BrickRotator;
import com.comp2042.model.logic.bricks.Brick;
import com.comp2042.model.logic.bricks.BrickGenerator;
import com.comp2042.model.logic.bricks.RandomBrickGenerator;
import com.comp2042.model.logic.CollisionService;
import com.comp2042.model.logic.LineClearService;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
    private final List<PowerUp> inventory = new ArrayList<>();

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
        // COMPLETE REDESIGN: Drill is a special projectile with independent logic
        Brick currentBrick = brickRotator.getBrick();
        if (currentBrick instanceof com.comp2042.model.logic.bricks.DrillBrick) {
            return moveDrillDown();
        } else {
            // Standard Logic: Normal brick movement with collision detection
            Point p = new Point(currentOffset);
            p.translate(0, 1);
            boolean conflict = collisionService.intersect(
                    currentGameMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
            if (conflict) {
                return false;
            } else {
                currentOffset = p;
                return true;
            }
        }
    }
    
    /**
     * Independent drill movement logic - completely separate from normal brick physics.
     * The drill is a projectile that:
     * 1. Moves down one cell per frame
     * 2. Destroys any block it passes through
     * 3. Stops only when it reaches the floor
     * 4. Never locks into the grid
     */
    private boolean moveDrillDown() {
        int currentY = (int) currentOffset.getY();
        int currentX = (int) currentOffset.getX();
        int nextY = currentY + 1;
        
        // FLOOR CHECK: Stop when drill reaches the bottom of the board
        // Board dimensions: width=25 (rows), height=10 (columns)
        // Matrix is [25][10] = [rows][columns]
        // The drill moves in Y direction (rows), so it should stop when nextY >= width (25)
        // Valid rows are 0-24, so nextY = 25 means we've hit the floor
        if (nextY >= width) {
            // Drill has reached the floor - it vanishes and we spawn a new normal brick
            // IMPORTANT: Do NOT merge the drill into the grid - it just disappears
            createNewBrick();
            return false; // Stop movement - but this is NOT a collision, it's completion
        }
        
        // DESTRUCTION: Destroy any block at the target position before moving
        // Matrix structure: currentGameMatrix is [width][height] = [25][10]
        // Matrix is accessed as matrix[row][column] = matrix[y][x]
        // To access row nextY (0-24) and column currentX (0-9): matrix[nextY][currentX]
        // Bounds check: nextY must be < width (25), currentX must be < height (10)
        if (nextY >= 0 && nextY < width && currentX >= 0 && currentX < height) {
            // Access as [row][column] = [nextY][currentX]
            if (currentGameMatrix[nextY][currentX] != 0) {
                // Destroy the block
                currentGameMatrix[nextY][currentX] = 0;
            }
        }
        
        // MOVE: Update position and continue falling
        currentOffset = new Point(currentX, nextY);
        return true; // Continue falling
    }
    
    /**
     * Checks if the current brick is a drill.
     * Used by controller to prevent locking logic for drills.
     */
    public boolean isDrillActive() {
        return brickRotator.getBrick() instanceof com.comp2042.model.logic.bricks.DrillBrick;
    }


    @Override
    public boolean moveBrickLeft() {
        // Check if the current brick is a Drill
        Brick currentBrick = brickRotator.getBrick();
        if (currentBrick instanceof com.comp2042.model.logic.bricks.DrillBrick) {
            // Drill can move freely left/right - only check screen bounds
            int currentX = (int) currentOffset.getX();
            int newX = currentX - 1;
            
            // Check bounds: don't wrap around the screen
            if (newX < 0) {
                return false;
            }
            
            // Move the drill left
            currentOffset = new Point(newX, (int) currentOffset.getY());
            return true;
        } else {
            // Standard Logic: Use collision check for normal bricks
            Point p = new Point(currentOffset);
            p.translate(-1, 0);
            boolean conflict = collisionService.intersect(
                    currentGameMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
            if (conflict) {
                return false;
            } else {
                currentOffset = p;
                return true;
            }
        }
    }

    @Override
    public boolean moveBrickRight() {
        // Check if the current brick is a Drill
        Brick currentBrick = brickRotator.getBrick();
        if (currentBrick instanceof com.comp2042.model.logic.bricks.DrillBrick) {
            // Drill can move freely left/right - only check screen bounds
            int currentX = (int) currentOffset.getX();
            int newX = currentX + 1;
            
            // Check bounds: don't wrap around the screen
            // Drill is 1x1, so check if newX exceeds width
            if (newX >= width) {
                return false;
            }
            
            // Move the drill right
            currentOffset = new Point(newX, (int) currentOffset.getY());
            return true;
        } else {
            // Standard Logic: Use collision check for normal bricks
            Point p = new Point(currentOffset);
            p.translate(1, 0);
            boolean conflict = collisionService.intersect(
                    currentGameMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
            if (conflict) {
                return false;
            } else {
                currentOffset = p;
                return true;
            }
        }
    }

    @Override
    public boolean rotateLeftBrick() {
        NextShapeInfo nextShape = brickRotator.getNextShape();
        boolean conflict = collisionService.intersect(
                currentGameMatrix, nextShape.getShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
        if (conflict) {
            return false;
        } else {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
    }

    @Override
    public boolean rotateRightBrick() {
        NextShapeInfo previousShape = brickRotator.getPreviousShape();
        boolean conflict = collisionService.intersect(
                currentGameMatrix, previousShape.getShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
        if (conflict) {
            return false;
        } else {
            brickRotator.setCurrentShape(previousShape.getPosition());
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
    
    /**
     * COMPLETE REDESIGN: Spawns a drill projectile, completely replacing the current brick.
     * This is a clean state transition - the previous brick is completely discarded.
     * The drill spawns at the top of the board at the player's current X position.
     */
    public void spawnDrill() {
        // CRITICAL: Completely reset all state to ensure clean transition
        isLocking = false;
        
        // Get the current X position for aiming (before replacing the brick)
        int aimX = (int) currentOffset.getX();
        
        // Clamp X to valid bounds [0, width-1]
        if (aimX < 0) {
            aimX = 0;
        } else if (aimX >= width) {
            aimX = width - 1;
        }
        
        // Replace the brick type FIRST - this completely changes what's falling
        Brick drillBrick = new com.comp2042.model.logic.bricks.DrillBrick();
        brickRotator.setBrick(drillBrick); // This resets shape to drill's 1x1 matrix and resets currentShape to 0
        
        // Spawn at top row (Y=0) - clean start, no overlap with existing bricks
        // Use the saved aimX for positioning
        currentOffset = new Point(aimX, SPAWN_Y); // Y = 0, X = player's aim position
        
        // The drill is now active and will fall automatically via moveBrickDown()
    }

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    private int calculateGhostY() {
        int currentY = (int) currentOffset.getY();
        int[][] currentShape = brickRotator.getCurrentShape();
        int currentX = (int) currentOffset.getX();
        
        // Start at current Y and simulate moving down until collision
        int testY = currentY;
        while (true) {
            testY++;
            boolean conflict = collisionService.intersect(
                currentGameMatrix, currentShape, currentX, testY);
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
        // Get next 3 bricks
        List<Brick> nextBricks = brickGenerator.getNextBricks(3);
        // Convert List<Brick> to List<int[][]> (taking the first rotation of each brick)
        List<int[][]> nextBrickShapes = new ArrayList<>();
        for (Brick brick : nextBricks) {
            nextBrickShapes.add(brick.getShapeMatrix().get(0));
        }
        return new ViewData(brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY(), ghostY, nextBrickShapes, this.isLocking, getInventory());
    }

    @Override
    public void mergeBrickToBackground() {
        // CRITICAL: Never merge a drill into the background - it's a projectile that vanishes
        if (brickRotator.getBrick() instanceof com.comp2042.model.logic.bricks.DrillBrick) {
            // Drill should never be merged - it just disappears when it hits the floor
            return;
        }
        // Normal brick: merge into background
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
        inventory.clear();
        createNewBrick();
    }
    
    @Override
    public void addPowerUp(PowerUp type) {
        inventory.add(type);
        // If the list size exceeds 3, remove the oldest item (index 0)
        if (inventory.size() > 3) {
            inventory.remove(0);
        }
    }
    
    @Override
    public PowerUp usePowerUp(int index) {
        // Check if the index is valid (bounds check)
        if (index < 0 || index >= inventory.size()) {
            return PowerUp.NONE;
        }
        // Remove the item and return it
        return inventory.remove(index);
    }
    
    @Override
    public List<PowerUp> getInventory() {
        // Return defensive copy
        return new ArrayList<>(inventory);
    }
}
