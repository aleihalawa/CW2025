package com.comp2042.controller;

import com.comp2042.events.EventSource;
import com.comp2042.view.GuiController;
import com.comp2042.events.MoveEvent;
import com.comp2042.model.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class GameController implements InputEventListener {

    private final Board board = new SimpleBoard(25, 10);

    private final GuiController viewGuiController;

    private final Timeline lockTimer;
    
    private boolean hasBeatenHighScore = false;
    private int currentHighScore = 0;
    private final com.comp2042.model.HighScoreManager highScoreManager = new com.comp2042.model.HighScoreManager();
    
    private MediaPlayer backgroundMusic;
    private MediaPlayer lineClearSound;

    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore());
        
        // Initialize high score
        currentHighScore = highScoreManager.loadHighScore();
        
        // Initialize lock timer: 0.5s delay before locking
        lockTimer = new Timeline(new KeyFrame(Duration.millis(500), e -> lockPieceAndHandleNewBrick()));
        lockTimer.setCycleCount(1);
        
        // Load and play background music
        loadBackgroundMusic();
        
        // Load line clear sound effect
        loadLineClearSound();
    }
    
    /**
     * Loads the line clear sound effect.
     */
    private void loadLineClearSound() {
        try {
            java.net.URL soundUrl = getClass().getClassLoader().getResource("tetris-line-clear-sound.mp3");
            if (soundUrl != null) {
                String soundPath = soundUrl.toExternalForm();
                Media media = new Media(soundPath);
                lineClearSound = new MediaPlayer(media);
                
                // Set volume higher than background music to be more noticeable
                lineClearSound.setVolume(0.7); // 70% volume
            } else {
                System.err.println("Could not find tetris-line-clear-sound.mp3 in resources");
            }
        } catch (Exception e) {
            System.err.println("Error loading line clear sound: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Plays the line clear sound effect.
     */
    private void playLineClearSound() {
        if (lineClearSound != null) {
            // Reset to beginning if already playing
            lineClearSound.stop();
            lineClearSound.play();
        }
    }
    
    /**
     * Loads and starts playing the background music.
     */
    private void loadBackgroundMusic() {
        try {
            java.net.URL musicUrl = getClass().getClassLoader().getResource("tetris-ringtone.mp3");
            if (musicUrl != null) {
                String musicPath = musicUrl.toExternalForm();
                Media media = new Media(musicPath);
                backgroundMusic = new MediaPlayer(media);
                
                // Set to loop indefinitely
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
                
                // Set volume (optional, adjust as needed)
                backgroundMusic.setVolume(0.3); // 30% volume
                
                // Start playing
                backgroundMusic.play();
            } else {
                System.err.println("Could not find tetris-ringtone.mp3 in resources");
            }
        } catch (Exception e) {
            System.err.println("Error loading background music: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Stops and disposes the background music.
     */
    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            try {
                backgroundMusic.stop();
                backgroundMusic.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing background music: " + e.getMessage());
            }
            backgroundMusic = null;
        }
        
        // Also dispose line clear sound
        if (lineClearSound != null) {
            try {
                lineClearSound.stop();
                lineClearSound.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing line clear sound: " + e.getMessage());
            }
            lineClearSound = null;
        }
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
            // Check for new high score after score update
            checkHighScore();
        }
        
        // Lock the piece immediately on hard drop (no delay)
        ClearRow clearRow = lockPieceAndHandleNewBrick();
        
        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public void createNewGame() {
        // Reset the flag so the next game can trigger the alert again
        hasBeatenHighScore = false;
        // Reload the high score to be safe
        currentHighScore = highScoreManager.loadHighScore();
        
        board.newGame();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }
    
    /**
     * Checks if the current score has beaten the high score.
     * Only triggers once per game session.
     */
    private void checkHighScore() {
        int currentScore = board.getScore().scoreProperty().get();
        // Crucial check: !hasBeatenHighScore ensures this block runs ONLY ONCE per game
        if (!hasBeatenHighScore && currentScore > currentHighScore) {
            hasBeatenHighScore = true; // Set the flag immediately so it doesn't fire again
            
            viewGuiController.showHighScoreNotification();
            
            // Save the new high score
            highScoreManager.saveHighScore(currentScore);
            currentHighScore = currentScore; // Update the threshold
            
            // Optional: Logging
            System.out.println("High Score notification finished");
        }
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
        // Play landing animation for physical impact effect
        viewGuiController.playLandAnimation();
        
        // Reset locking state before locking
        ((SimpleBoard) board).setLocking(false);
        
        board.mergeBrickToBackground();
        
        // Get board matrix BEFORE clearRows() so we can animate from the state with full lines
        int[][] boardBeforeClear = MatrixOperations.copy(board.getBoardMatrix());
        
        ClearRow clearRow = board.clearRows();

        // Check if lines were cleared
        if (clearRow.getLinesRemovedCount() > 0) {
            // Play line clear sound effect (overlaps with background music)
            playLineClearSound();
            
            // Hide falling brick and ghost panels before animation to prevent visual conflicts
            // The piece is now merged into background, so it should only appear in displayMatrix
            viewGuiController.hideFallingPieces();
            
            // Refresh background with the PRE-CLEARED state so animation can show the transition
            // This ensures all blocks (including the piece that just landed) are visible before animation
            viewGuiController.refreshGameBackground(boardBeforeClear);
            
            // Lines cleared - animate the clear, then update game state
            viewGuiController.animateLineClear(clearRow.getLinesRemoved(), () -> {
                // Callback executed after animation completes
                applyLineClearScore(clearRow);
                
                // Update lines count and level
                board.getScore().addLines(clearRow.getLinesRemovedCount());
                
                // Update game speed based on new level
                int currentLevel = board.getScore().levelProperty().get();
                double newSpeed = calculateSpeed(currentLevel);
                viewGuiController.updateGameSpeed(newSpeed);
                
                // Refresh background with the FINAL state (after clearRows) to match board state
                viewGuiController.refreshGameBackground(board.getBoardMatrix());
                
                // Then create new brick and check for game over
                boolean gameOver = board.createNewBrick();
                if (gameOver) {
                    viewGuiController.gameOver();
                } else {
                    // Show falling pieces again and refresh brick view to show the new brick
                    viewGuiController.showFallingPieces();
                    viewGuiController.refreshBrick(board.getViewData());
                }
            });
        } else {
            // No lines cleared - proceed synchronously
            applyLineClearScore(clearRow);
            
            viewGuiController.refreshGameBackground(board.getBoardMatrix());
            
            boolean gameOver = board.createNewBrick();
            if (gameOver) {
                viewGuiController.gameOver();
            } else {
                // Refresh brick view to show the new brick immediately
                viewGuiController.refreshBrick(board.getViewData());
            }
        }

        return clearRow;
    }

    /**
     * Calculates the game speed (delay in milliseconds) based on the current level.
     * Formula: Start with 400ms, subtract 35ms for every level above 1.
     * Clamped to minimum 75ms to keep it playable.
     * @param level The current game level
     * @return The delay in milliseconds for the next automatic drop
     */
    private double calculateSpeed(int level) {
        // Start with 400ms, subtract 35ms for every level above 1
        double speed = 400.0 - (35.0 * (level - 1));
        // Clamp to minimum 75ms
        return Math.max(75.0, speed);
    }

    /**
     * Applies score bonus based on how many lines have been removed.
     */
    private void applyLineClearScore(ClearRow clearRow) {
        if (clearRow != null && clearRow.getLinesRemovedCount() > 0) {
            board.getScore().add(clearRow.getScoreBonus());
            // Check for new high score after score update
            checkHighScore();
        }
    }

    /**
     * Rewards the player for actively pressing down (soft drop).
     */
    private void rewardManualDrop(MoveEvent event) {
        if (event.getEventSource() == EventSource.USER) {
            board.getScore().add(1);
            // Check for new high score after score update
            checkHighScore();
        }
    }
}
