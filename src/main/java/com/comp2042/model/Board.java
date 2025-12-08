package com.comp2042.model;

import java.util.List;

/**
 * Defines the public API for the game Board.
 * Implementations hold the game state and logic.
 * (Refactor-only change: no behaviour change).
 */
public interface Board {
    /** Moves the current brick down one line, returns false if conflict. */
    boolean moveBrickDown();
    /** Moves the current brick left, returns false if conflict. */
    boolean moveBrickLeft();
    /** Moves the current brick right, returns false if conflict. */
    boolean moveBrickRight();
    /** Rotates the current brick, returns false if conflict. */
    boolean rotateLeftBrick();
    /** Rotates the current brick in the opposite direction, returns false if conflict. */
    boolean rotateRightBrick();
    /** Creates a new brick at the spawn point, returns true if it conflicts immediately (game over). */
    boolean createNewBrick();
    /** Returns the raw game matrix for rendering. */
    int[][] getBoardMatrix();
    /** Returns a snapshot of data needed by the view to render the current piece. */
    ViewData getViewData();
    /** "Stamps" the current brick's shape onto the game board matrix. */
    void mergeBrickToBackground();
    /** Checks for and clears any full lines, returning data on what was cleared. */
    ClearRow clearRows();
    /** Returns the Score object for displaying score, lines, and level. */
    Score getScore();
    /** Resets the board and score for a new game. */
    void newGame();
    /** Adds a power up to the inventory. */
    void addPowerUp(PowerUp type);
    /** Removes and returns the power up at the specified index. */
    PowerUp usePowerUp(int index);
    /** Returns a copy of the current inventory. */
    List<PowerUp> getInventory();
    /** Explodes blocks at the specified grid position, destroying blocks in a radius. */
    void explodeAt(int row, int col);
}
