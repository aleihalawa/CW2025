package com.comp2042.controller;

import com.comp2042.events.EventSource;
import com.comp2042.view.GuiController;
import com.comp2042.events.MoveEvent;
import com.comp2042.model.*;

public class GameController implements InputEventListener {

    private final Board board = new SimpleBoard(25, 10);

    private final GuiController viewGuiController;

    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow;

        if (!canMove) {
            clearRow = lockPieceAndHandleNewBrick();
        } else {
            rewardManualDrop(event);
            clearRow = null;
        }

        return new DownData(clearRow, board.getViewData());
    }


    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
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
