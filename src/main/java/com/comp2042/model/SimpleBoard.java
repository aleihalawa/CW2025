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
    
    // Bedrock Corruption constants
    public static final int BEDROCK_ID = 9;
    private int currentCorruptionRow = 24; // Initialize to bottom row (width - 1, since width=25, rows are 0-24)

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
        
        // BEDROCK CHECK: If target cell is bedrock, stop the drill
        if (nextY >= 0 && nextY < width && currentX >= 0 && currentX < height) {
            if (currentGameMatrix[nextY][currentX] == BEDROCK_ID) {
                // Drill cannot penetrate bedrock - stop and spawn new brick
                createNewBrick();
                return false;
            }
        }
        
        // DESTRUCTION: Destroy any block at the target position before moving
        // Matrix structure: currentGameMatrix is [width][height] = [25][10]
        // Matrix is accessed as matrix[row][column] = matrix[y][x]
        // To access row nextY (0-24) and column currentX (0-9): matrix[nextY][currentX]
        // Bounds check: nextY must be < width (25), currentX must be < height (10)
        if (nextY >= 0 && nextY < width && currentX >= 0 && currentX < height) {
            // Access as [row][column] = [nextY][currentX]
            if (currentGameMatrix[nextY][currentX] != 0) {
                // Destroy the block (but not bedrock, which we already checked above)
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
            int currentY = (int) currentOffset.getY();
            int newX = currentX - 1;
            
            // Check bounds: don't wrap around the screen
            if (newX < 0) {
                return false;
            }
            
            // BEDROCK CHECK: If target cell is bedrock, cannot move
            if (currentY >= 0 && currentY < width && newX >= 0 && newX < height) {
                if (currentGameMatrix[currentY][newX] == BEDROCK_ID) {
                    return false; // Cannot move through bedrock
                }
            }
            
            // DESTRUCTION: Destroy any block at the new position before moving
            // Matrix is [row][column] = [y][x]
            // To access row currentY (0-24) and column newX (0-9): matrix[currentY][newX]
            if (currentY >= 0 && currentY < width && newX >= 0 && newX < height) {
                // Access as [row][column] = [currentY][newX]
                if (currentGameMatrix[currentY][newX] != 0) {
                    // Destroy the block (but not bedrock, which we already checked above)
                    currentGameMatrix[currentY][newX] = 0;
                }
            }
            
            // Move the drill left
            currentOffset = new Point(newX, currentY);
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
            int currentY = (int) currentOffset.getY();
            int newX = currentX + 1;
            
            // Check bounds: don't wrap around the screen
            // Drill is 1x1, so check if newX exceeds height (columns)
            if (newX >= height) {
                return false;
            }
            
            // BEDROCK CHECK: If target cell is bedrock, cannot move
            if (currentY >= 0 && currentY < width && newX >= 0 && newX < height) {
                if (currentGameMatrix[currentY][newX] == BEDROCK_ID) {
                    return false; // Cannot move through bedrock
                }
            }
            
            // DESTRUCTION: Destroy any block at the new position before moving
            // Matrix is [row][column] = [y][x]
            // To access row currentY (0-24) and column newX (0-9): matrix[currentY][newX]
            if (currentY >= 0 && currentY < width && newX >= 0 && newX < height) {
                // Access as [row][column] = [currentY][newX]
                if (currentGameMatrix[currentY][newX] != 0) {
                    // Destroy the block (but not bedrock, which we already checked above)
                    currentGameMatrix[currentY][newX] = 0;
                }
            }
            
            // Move the drill right
            currentOffset = new Point(newX, currentY);
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
        // Standard Tetris line clearing: use the standard service, but skip rows with bedrock
        // Create a copy where rows with bedrock are marked as non-full (so they won't be cleared)
        int[][] matrixCopy = new int[width][height];
        for (int i = 0; i < width; i++) {
            boolean rowHasBedrock = false;
            // Check if this row contains bedrock
            for (int j = 0; j < height; j++) {
                if (currentGameMatrix[i][j] == BEDROCK_ID) {
                    rowHasBedrock = true;
                    break;
                }
            }
            
            // If row has bedrock, mark it as non-full by leaving one cell empty
            // Otherwise, copy the row normally
            for (int j = 0; j < height; j++) {
                if (rowHasBedrock) {
                    // Mark as non-full by leaving first cell empty (prevents clearing)
                    matrixCopy[i][j] = (j == 0) ? 0 : currentGameMatrix[i][j];
                } else {
                    matrixCopy[i][j] = currentGameMatrix[i][j];
                }
            }
        }
        
        // Use standard line clearing service (standard Tetris behavior - no gravity, just remove full lines)
        ClearRow clearRow = lineClearService.clearFullLines(matrixCopy);
        
        // Apply the cleared matrix, but preserve bedrock blocks exactly as they were
        int[][] newMatrix = clearRow.getNewMatrix();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // If the original had bedrock, keep it exactly as is
                if (currentGameMatrix[i][j] == BEDROCK_ID) {
                    // Keep bedrock - don't overwrite
                    continue;
                }
                // Otherwise, apply the cleared matrix (standard Tetris - no gravity, blocks stay in place)
                // But check if newMatrix has bedrock in this position (shouldn't happen, but be safe)
                if (newMatrix[i][j] == BEDROCK_ID) {
                    // This shouldn't happen since we marked bedrock rows as non-full
                    // But if it does, preserve the original bedrock
                    continue;
                }
                currentGameMatrix[i][j] = newMatrix[i][j];
            }
        }
        
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
        currentCorruptionRow = 24; // Reset to bottom row
        createNewBrick();
    }
    
    /**
     * Corrupts the next row by turning existing blocks into bedrock.
     * Starts from the bottom (row 24) and works upwards.
     * 
     * @return true if corruption was successful, false if game over (corruption row < 0)
     */
    public boolean corruptNextRow() {
        // Start from the bottom row (24) and work upwards
        // currentCorruptionRow starts at 24 (bottom), and we corrupt it, then decrement for next time
        
        // Game Over Check: If corruption row is below 0, game is over
        if (currentCorruptionRow < 0) {
            return false; // Game over
        }
        
        // Transformation: Loop through the CURRENT row and turn existing blocks to bedrock
        // Only turn existing blocks (non-zero and not already bedrock) to bedrock, leave empty cells as 0
        for (int col = 0; col < height; col++) {
            if (currentGameMatrix[currentCorruptionRow][col] != 0 && 
                currentGameMatrix[currentCorruptionRow][col] != BEDROCK_ID) {
                // Turn existing block to bedrock
                currentGameMatrix[currentCorruptionRow][col] = BEDROCK_ID;
            }
        }
        
        // Decrement corruption row (move up one row) for NEXT corruption
        currentCorruptionRow--;
        
        return true; // Corruption successful
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
    
    /**
     * Explodes blocks at the specified grid position, destroying blocks in a 3x3 radius.
     * After destruction, applies gravity to make blocks above fall down.
     * 
     * @param row The row (Y coordinate) in the game matrix
     * @param col The column (X coordinate) in the game matrix
     */
    @Override
    public void explodeAt(int row, int col) {
        // Explosion radius: 3x3 area (1 block in each direction from center)
        int radius = 1;
        
        // Iterate through all cells in the explosion radius
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int targetRow = row + dy;
                int targetCol = col + dx;
                
                // Bounds check: ensure we're within the board
                if (targetRow >= 0 && targetRow < width && targetCol >= 0 && targetCol < height) {
                    // BEDROCK CHECK: Do not destroy bedrock
                    if (currentGameMatrix[targetRow][targetCol] == BEDROCK_ID) {
                        continue; // Skip bedrock - it's indestructible
                    }
                    // Destroy the block (set to 0)
                    currentGameMatrix[targetRow][targetCol] = 0;
                }
            }
        }
        
        // Do NOT apply gravity here - let the controller handle it with animation
    }
    
    /**
     * Checks if there are any floating blocks (blocks with empty space below them).
     * 
     * @return true if any block has empty space (0) immediately beneath it and is not on the floor
     */
    public boolean hasFloatingBlocks() {
        // Scan the board from bottom to top
        // Matrix structure: currentGameMatrix[row][column] = currentGameMatrix[y][x]
        for (int y = width - 2; y >= 0; y--) { // Start from second-to-bottom row (width-2) down to 0
            for (int x = 0; x < height; x++) { // Check each column
                // If there's a block at [y][x] and empty space below it [y+1][x]
                if (currentGameMatrix[y][x] != 0 && currentGameMatrix[y + 1][x] == 0) {
                    return true; // Found a floating block
                }
            }
        }
        return false; // No floating blocks
    }
    
    /**
     * Applies one step of gravity, moving floating blocks down by one row.
     * This is called repeatedly to create a cascading "avalanche" effect.
     */
    public void applyGravityStep() {
        // Scan the board from bottom to top (height-1 down to 0)
        // Matrix structure: currentGameMatrix[row][column] = currentGameMatrix[y][x]
        for (int y = width - 2; y >= 0; y--) { // Start from second-to-bottom row down to top
            for (int x = 0; x < height; x++) { // Check each column
                // If a block at [y][x] has empty space below it [y+1][x]
                if (currentGameMatrix[y][x] != 0 && currentGameMatrix[y + 1][x] == 0) {
                    // Move the block down one step
                    currentGameMatrix[y + 1][x] = currentGameMatrix[y][x];
                    currentGameMatrix[y][x] = 0;
                }
            }
        }
    }
}
