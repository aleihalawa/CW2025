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
    
    
    private MediaPlayer backgroundMusic; // Keep MediaPlayer for background music (needs looping)
    private final SoundManager soundManager = SoundManager.getInstance();
    
    // Track player's previous best score for personal high score notification
    private int playerPreviousBestScore = 0;
    private boolean hasShownPersonalHighScoreNotification = false;
    
    // Current game mode
    private GameMode currentMode = GameMode.CLASSIC;
    
    // Player name
    private String playerName;
    
    /**
     * Sets the game mode.
     * Also updates ghost piece mirror mode.
     * 
     * @param mode The game mode to set
     */
    public void setGameMode(GameMode mode) {
        this.currentMode = mode;
        // Update ghost piece mirror mode when mode changes
        if (viewGuiController != null) {
            viewGuiController.setMirrorMode(currentMode == GameMode.MIRROR);
        }
    }
    
    /**
     * Gets the current game mode.
     * 
     * @return The current game mode
     */
    public GameMode getGameMode() {
        return currentMode;
    }
    
    /**
     * Sets the player name.
     * 
     * @param name The player's name
     */
    public void setPlayerName(String name) {
        this.playerName = name;
    }
    
    /**
     * Gets the player name.
     * 
     * @return The player's name
     */
    public String getPlayerName() {
        return playerName;
    }

    public GameController(GuiController c) {
        viewGuiController = c;
        
        // Initialize game mode from GameSettings
        currentMode = GameSettings.getSelectedGameMode();
        if (currentMode == null) {
            currentMode = GameMode.CLASSIC; // Default fallback
        }
        
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore());
        
        // Set mirror mode for ghost piece inversion
        viewGuiController.setMirrorMode(currentMode == GameMode.MIRROR);
        
        // Initialize lock timer: 0.5s delay before locking
        lockTimer = new Timeline(new KeyFrame(Duration.millis(500), e -> lockPieceAndHandleNewBrick()));
        lockTimer.setCycleCount(1);
        
        // Initialize sound manager (pre-loads all sound effects into memory)
        soundManager.initialize();
        
        // Load player's previous best score
        loadPlayerPreviousBestScore();
        
        // Load and play background music
        loadBackgroundMusic();
    }
    
    /**
     * Plays the line clear sound effect.
     */
    private void playLineClearSound() {
        soundManager.play("tetris-line-clear-sound.mp3");
    }
    
    /**
     * Plays the rotation sound effect.
     */
    private void playRotateSound() {
        soundManager.play("tetris-gb-19-rotate-piece.mp3");
    }
    
    /**
     * Plays the game over sound effect and stops background music.
     */
    private void playGameOverSound() {
        // Stop background music when game is over
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
        
        // Play game over sound
        soundManager.play("game-over-arcade-6435.mp3");
    }
    
    /**
     * Plays the hard drop sound effect.
     */
    private void playHardDropSound() {
        soundManager.play("fx-hrddp.mp3");
    }
    
    /**
     * Plays the move sound effect.
     */
    private void playMoveSound() {
        soundManager.play("tetris.mp3");
    }
    
    /**
     * Plays the high score success sound effect.
     */
    private void playHighScoreSuccessSound() {
        soundManager.play("tetris-success.mp3");
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
                
                // Register with SoundManager for volume control
                soundManager.setBackgroundMusicPlayer(backgroundMusic);
                
                // Set initial volume from SoundManager
                backgroundMusic.setVolume(soundManager.getMusicVolume());
                
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
     * Unregisters from SoundManager to prevent multiple instances.
     * Note: Does NOT dispose SoundManager (it's a singleton shared across the app).
     */
    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            try {
                backgroundMusic.stop();
                // Unregister from SoundManager before disposing to prevent multiple instances
                soundManager.setBackgroundMusicPlayer(null);
                backgroundMusic.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing background music: " + e.getMessage());
            }
            backgroundMusic = null;
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
        boolean moved;
        if (currentMode == GameMode.MIRROR) {
            // Mirror mode: left key moves right
            moved = board.moveBrickRight();
        } else {
            // Classic mode: left key moves left
            moved = board.moveBrickLeft();
        }
        if (moved) {
            // Play move sound effect
            playMoveSound();
            
            // Move was successful - stop timer and reset locking state
            lockTimer.stop();
            ((SimpleBoard) board).setLocking(false);
        }
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        boolean moved;
        if (currentMode == GameMode.MIRROR) {
            // Mirror mode: right key moves left
            moved = board.moveBrickLeft();
        } else {
            // Classic mode: right key moves right
            moved = board.moveBrickRight();
        }
        if (moved) {
            // Play move sound effect
            playMoveSound();
            
            // Move was successful - stop timer and reset locking state
            lockTimer.stop();
            ((SimpleBoard) board).setLocking(false);
        }
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        boolean rotated;
        if (currentMode == GameMode.MIRROR) {
            // Mirror mode: reverse rotation (rotate right instead of left)
            rotated = board.rotateRightBrick();
        } else {
            // Classic mode: standard rotation
            rotated = board.rotateLeftBrick();
        }
        
        if (rotated) {
            // Play rotation sound effect
            playRotateSound();
            
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
            // Check if player beat their own high score after score update
            checkPersonalHighScore();
        }
        
        // Play hard drop landing sound effect
        playHardDropSound();
        
        // Lock the piece immediately on hard drop (no delay)
        ClearRow clearRow = lockPieceAndHandleNewBrick();
        
        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public void createNewGame() {
        // Reload background music if it was disposed (e.g., after going to Settings)
        if (backgroundMusic == null) {
            loadBackgroundMusic();
        } else {
            // Music still exists, just restart it
            backgroundMusic.play();
        }
        
        // Reset notification flag for new game
        hasShownPersonalHighScoreNotification = false;
        
        // Reload player's previous best score (in case leaderboard was updated)
        loadPlayerPreviousBestScore();
        
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
                    playGameOverSound();
                    handleGameOver();
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
                playGameOverSound();
                handleGameOver();
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
            // Check if player beat their own high score after score update
            checkPersonalHighScore();
        }
    }

    /**
     * Rewards the player for actively pressing down (soft drop).
     */
    private void rewardManualDrop(MoveEvent event) {
        if (event.getEventSource() == EventSource.USER) {
            board.getScore().add(1);
            // Check if player beat their own high score after score update
            checkPersonalHighScore();
        }
    }
    
    /**
     * Loads the player's previous best score from the leaderboard.
     */
    private void loadPlayerPreviousBestScore() {
        String playerName = com.comp2042.model.GameSettings.getPlayerName();
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }
        
        // Load leaderboard and find this player's best score for the current game mode
        java.util.List<com.comp2042.model.ScoreEntry> leaderboard = 
            com.comp2042.model.HighScoreManager.loadLeaderboard(currentMode);
        
        playerPreviousBestScore = 0;
        for (com.comp2042.model.ScoreEntry entry : leaderboard) {
            // Case-sensitive comparison
            if (entry.getName().equals(playerName)) {
                playerPreviousBestScore = entry.getScore();
                break;
            }
        }
    }
    
    /**
     * Checks if the current score beats the player's previous best score.
     * If so, shows the "NEW HIGH SCORE!" notification (only once per game).
     */
    private void checkPersonalHighScore() {
        // Only check if we haven't shown the notification yet this game
        if (hasShownPersonalHighScoreNotification) {
            return;
        }
        
        int currentScore = board.getScore().scoreProperty().get();
        
        // Check if current score beats previous best (or if it's their first score)
        if (currentScore > playerPreviousBestScore) {
            // Player beat their own high score!
            hasShownPersonalHighScoreNotification = true;
            
            // Play high score success sound
            playHighScoreSuccessSound();
            
            // Show the notification
            viewGuiController.showHighScoreNotification();
        }
    }
    
    /**
     * Handles game over logic: saves score if it qualifies for leaderboard.
     */
    private void handleGameOver() {
        int finalScore = board.getScore().scoreProperty().get();
        
        // Check if this score qualifies for the leaderboard
        // Always save/update the player's score entry
        // The saveEntry method will handle updating if the player already exists
        // and will only keep the entry if it qualifies for top 10
        String playerName = com.comp2042.model.GameSettings.getPlayerName();
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }
        com.comp2042.model.ScoreEntry entry = new com.comp2042.model.ScoreEntry(playerName, finalScore);
        com.comp2042.model.HighScoreManager.saveEntry(currentMode, entry);
        
        // Show game over panel
        viewGuiController.gameOver();
    }
}
