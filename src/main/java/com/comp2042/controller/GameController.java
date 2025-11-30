package com.comp2042.controller;

import com.comp2042.events.EventSource;
import com.comp2042.view.GuiController;
import com.comp2042.events.MoveEvent;
import com.comp2042.model.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class GameController implements InputEventListener {

    private final Board board = new SimpleBoard(25, 10);

    private final GuiController viewGuiController;

    private final Timeline lockTimer;

    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        
        // Initialize lock timer: 0.5s delay before locking
        lockTimer = new Timeline(new KeyFrame(Duration.millis(500), e -> lockPieceAndHandleNewBrick()));
        lockTimer.setCycleCount(1);
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow;

        if (!canMove) {
            // Hit bottom - start lock delay timer if not already running
            if (lockTimer.getStatus() != Animation.Status.RUNNING) {
                lockTimer.play();
                ((SimpleBoard) board).setLocking(true);
            }
            // Do NOT call lockPieceAndHandleNewBrick() immediately - wait for timer
            clearRow = null;
        } else {
            // Piece moved successfully - stop timer and reset locking state
            lockTimer.stop();
            ((SimpleBoard) board).setLocking(false);
            rewardManualDrop(event);
            clearRow = null;
        }

        return new DownData(clearRow, board.getViewData());
    }


    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        boolean moved = board.moveBrickLeft();
        if (moved) {
            // Move was successful - stop timer and reset locking state
            lockTimer.stop();
            ((SimpleBoard) board).setLocking(false);
        }
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        boolean moved = board.moveBrickRight();
        if (moved) {
            // Move was successful - stop timer and reset locking state
            lockTimer.stop();
            ((SimpleBoard) board).setLocking(false);
        }
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        boolean rotated = board.rotateLeftBrick();
        if (rotated) {
            // Rotate was successful - stop timer and reset locking state
            lockTimer.stop();
            ((SimpleBoard) board).setLocking(false);
        }
        return board.getViewData();
    }

    @Override
    public DownData onSpaceEvent(MoveEvent event) {
        // Stop any existing lock timer since we're hard dropping
        lockTimer.stop();
        ((SimpleBoard) board).setLocking(false);
        
        // Hard drop: keep moving down until collision
        int dropCount = 0;
        while (board.moveBrickDown()) {
            dropCount++;
        }
        
        // Calculate and apply hard drop score (2 points per cell dropped)
        if (dropCount > 0 && event.getEventSource() == EventSource.USER) {
            int hardDropScore = dropCount * 2;
            board.getScore().add(hardDropScore);
        }
        
        // Lock the piece immediately on hard drop (no delay)
        ClearRow clearRow = lockPieceAndHandleNewBrick();
        
        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public void createNewGame() {
        board.newGame();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }
    /**
     * Handles the situation when a falling piece can no longer move down:
     * - merges it into the background
     * - clears any full rows
     * - applies score for cleared lines
     * - spawns a new brick or triggers game over
     * - refreshes the background view
     */
    private ClearRow lockPieceAndHandleNewBrick() {
        // Reset locking state before locking
        ((SimpleBoard) board).setLocking(false);
        
        board.mergeBrickToBackground();
        ClearRow clearRow = board.clearRows();
        applyLineClearScore(clearRow);

        if (board.createNewBrick()) {
            viewGuiController.gameOver();
        }

        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        return clearRow;
    }

    /**
     * Applies score bonus based on how many lines have been removed.
     */
    private void applyLineClearScore(ClearRow clearRow) {
        if (clearRow != null && clearRow.getLinesRemoved() > 0) {
            board.getScore().add(clearRow.getScoreBonus());
        }
    }

    /**
     * Rewards the player for actively pressing down (soft drop).
     */
    private void rewardManualDrop(MoveEvent event) {
        if (event.getEventSource() == EventSource.USER) {
            board.getScore().add(1);
        }
    }
}
