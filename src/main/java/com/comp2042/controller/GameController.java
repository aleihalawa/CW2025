package com.comp2042.controller;

import com.comp2042.events.EventSource;
import com.comp2042.view.GuiController;
import com.comp2042.events.MoveEvent;
import com.comp2042.model.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
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
    
    // Power-up earning threshold
    private int nextPowerUpThreshold = 100;
    
    // Bomb targeting state
    private boolean isBombTargeting = false;
    
    // Bedrock Corruption timeline and countdown
    private Timeline corruptionLoop;
    private int corruptionCountdown = 15;
    
    // Gravity animation timeline (to prevent multiple from running)
    private Timeline gravityTimeline;
    
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
        
        // Initialize corruption loop for POWERUPS mode on game start
        if (currentMode == GameMode.POWERUPS) {
            // Start at higher speed/intensity: level 5 for faster pace
            board.getScore().levelProperty().set(5);
            initializeCorruptionLoop();
            if (corruptionLoop != null) {
                corruptionLoop.play();
            }
            // Show corruption timer UI
            if (viewGuiController != null && viewGuiController.getCorruptionTimerContainer() != null) {
                viewGuiController.getCorruptionTimerContainer().setVisible(true);
                // Initialize timer display to 15
                viewGuiController.updateCorruptionTimer(15);
            }
        }
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
        // CRITICAL: Check if drill is active BEFORE calling moveBrickDown
        // If drill just finished (returned false), it already spawned a new brick
        boolean wasDrill = ((SimpleBoard) board).isDrillActive();
        
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow;

        if (!canMove) {
            // Check if this was a drill finishing (not a collision)
            if (wasDrill) {
                // Drill finished - it already spawned a new brick in moveDrillDown()
                // Refresh background to show any blocks that were destroyed
                viewGuiController.refreshGameBackground(board.getBoardMatrix());
                // Apply gravity animation to make floating blocks fall down
                startGravityAnimation();
                // Do NOT start lock timer or merge - the drill just vanished
                clearRow = null;
            } else {
                // Normal brick hit bottom - start lock delay timer if not already running
                if (lockTimer.getStatus() != Animation.Status.RUNNING) {
                    lockTimer.play();
                    ((SimpleBoard) board).setLocking(true);
                }
                // Do NOT call lockPieceAndHandleNewBrick() immediately - wait for timer
                clearRow = null;
            }
        } else {
            // Piece moved successfully - stop timer and reset locking state
            lockTimer.stop();
            ((SimpleBoard) board).setLocking(false);
            rewardManualDrop(event);
            clearRow = null;
            
            // CRITICAL: If drill is still active, refresh background immediately
            // This ensures destroyed blocks disappear right away, not after the drill finishes
            if (((SimpleBoard) board).isDrillActive()) {
                viewGuiController.refreshGameBackground(board.getBoardMatrix());
            }
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
            
            // CRITICAL: If drill is active, refresh background immediately
            // This ensures destroyed blocks disappear right away when drill moves horizontally
            if (((SimpleBoard) board).isDrillActive()) {
                viewGuiController.refreshGameBackground(board.getBoardMatrix());
            }
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
            
            // CRITICAL: If drill is active, refresh background immediately
            // This ensures destroyed blocks disappear right away when drill moves horizontally
            if (((SimpleBoard) board).isDrillActive()) {
                viewGuiController.refreshGameBackground(board.getBoardMatrix());
            }
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
            // Check for power-up earning
            checkPowerUpEarning();
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
        
        // Reset power-up threshold for new game
        nextPowerUpThreshold = 100;
        
        // Reset corruption countdown
        corruptionCountdown = 15;
        
        // Stop existing corruption loop if running
        if (corruptionLoop != null) {
            corruptionLoop.stop();
        }
        
        // Stop existing gravity animation if running
        if (gravityTimeline != null && gravityTimeline.getStatus() == Animation.Status.RUNNING) {
            gravityTimeline.stop();
            gravityTimeline = null;
        }
        
        // Initialize corruption loop for POWERUPS mode
        if (GameSettings.getSelectedGameMode() == GameMode.POWERUPS) {
            initializeCorruptionLoop();
            corruptionLoop.play();
            // Show corruption timer UI
            if (viewGuiController != null && viewGuiController.getCorruptionTimerContainer() != null) {
                viewGuiController.getCorruptionTimerContainer().setVisible(true);
            }
        } else {
            // Hide corruption timer UI in other modes
            if (viewGuiController != null && viewGuiController.getCorruptionTimerContainer() != null) {
                viewGuiController.getCorruptionTimerContainer().setVisible(false);
            }
        }
        
        // Reload player's previous best score (in case leaderboard was updated)
        loadPlayerPreviousBestScore();
        
        board.newGame();
        // For POWERUPS mode, start at level 5 for faster pace
        if (GameSettings.getSelectedGameMode() == GameMode.POWERUPS) {
            board.getScore().levelProperty().set(5);
        }
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }
    
    /**
     * Pauses the corruption loop timeline.
     */
    public void pauseCorruptionLoop() {
        if (corruptionLoop != null && corruptionLoop.getStatus() == Animation.Status.RUNNING) {
            corruptionLoop.pause();
        }
    }
    
    /**
     * Resumes the corruption loop timeline.
     */
    public void resumeCorruptionLoop() {
        if (corruptionLoop != null && corruptionLoop.getStatus() == Animation.Status.PAUSED) {
            corruptionLoop.play();
        }
    }
    
    /**
     * Initializes the corruption loop timeline for Bedrock Corruption mechanic.
     * This runs every 15 seconds in POWERUPS mode, corrupting the lowest playable row.
     */
    private void initializeCorruptionLoop() {
        // Reset countdown to 15
        corruptionCountdown = 15;
        
        // Initialize timer display
        if (viewGuiController != null) {
            viewGuiController.updateCorruptionTimer(corruptionCountdown);
        }
        
        corruptionLoop = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            // Decrement countdown
            corruptionCountdown--;
            
            // Update UI
            if (viewGuiController != null) {
                viewGuiController.updateCorruptionTimer(corruptionCountdown);
            }
            
            // If countdown reaches 0, corrupt next row
            if (corruptionCountdown <= 0) {
                // Reset countdown
                corruptionCountdown = 15;
                
                // Corrupt next row
                boolean success = ((SimpleBoard) board).corruptNextRow();
                
                // Show bedrock corruption notification
                if (viewGuiController != null && success) {
                    viewGuiController.showBedrockCorruptionNotification();
                }
                
                // Refresh board view to show bedrock
                if (viewGuiController != null) {
                    viewGuiController.refreshGameBackground(board.getBoardMatrix());
                }
                
                // If corruption failed (game over), trigger game over
                if (!success) {
                    // Game over - corruption reached the top
                    // Find where game over is handled
                    if (viewGuiController != null) {
                        viewGuiController.gameOver();
                    }
                    // Stop corruption loop
                    if (corruptionLoop != null) {
                        corruptionLoop.stop();
                    }
                }
            }
        }));
        
        // Set to repeat indefinitely
        corruptionLoop.setCycleCount(Timeline.INDEFINITE);
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
            // Check for power-up earning
            checkPowerUpEarning();
        }
    }
    
    /**
     * Checks if the player has earned a power-up based on score threshold.
     * CRITICAL: Only works in POWERUPS mode.
     */
    private void checkPowerUpEarning() {
        // Only award power-ups in POWERUPS mode
        if (currentMode != GameMode.POWERUPS) {
            return;
        }
        
        int currentScore = board.getScore().scoreProperty().get();
        
        if (currentScore >= nextPowerUpThreshold) {
            // Pick a random PowerUp type (BOMB, DRILL, or FREEZE)
            PowerUp[] powerUps = {PowerUp.BOMB, PowerUp.DRILL, PowerUp.FREEZE};
            PowerUp randomPowerUp = powerUps[(int) (Math.random() * powerUps.length)];
            
            // Add to inventory
            board.addPowerUp(randomPowerUp);
            
            // Increase threshold
            nextPowerUpThreshold += 100;
            
            // Update Inventory UI
            viewGuiController.refreshInventory(board.getInventory());
            
            // Show subtle notification that power-up was earned
            viewGuiController.showPowerUpEarned(randomPowerUp);
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
        // Stop corruption loop if running
        if (corruptionLoop != null) {
            corruptionLoop.stop();
        }
        
        // Stop gravity animation if running
        if (gravityTimeline != null && gravityTimeline.getStatus() == Animation.Status.RUNNING) {
            gravityTimeline.stop();
            gravityTimeline = null;
        }
        
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
    
    @Override
    public void onPowerUpEvent(int slotIndex) {
        // CRITICAL: Only allow power-ups in POWERUPS mode
        if (currentMode != GameMode.POWERUPS) {
            return;
        }
        
        // Call board.usePowerUp(slotIndex)
        PowerUp item = board.usePowerUp(slotIndex);
        
        // If item is NONE, return
        if (item == PowerUp.NONE) {
            return;
        }
        
        // Update UI
        viewGuiController.refreshInventory(board.getInventory());
        
        // Switch Statement for effects
        switch (item) {
            case FREEZE:
                activateFreeze();
                break;
            case BOMB:
                enterBombTargetingMode();
                break;
            case DRILL:
                activateDrill(); // Placeholder
                break;
            case NONE:
            default:
                break;
        }
    }
    
    /**
     * Gets the board (for GUI access).
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * Activates the FREEZE power-up effect.
     * Pauses automatic falling for 8 seconds while allowing normal movement.
     */
    private void activateFreeze() {
        // Freeze Active!
        
        // Show activation notification
        viewGuiController.showPowerUpActivation(PowerUp.FREEZE);
        
        // Enable freeze visual effects
        viewGuiController.setFreezeEffect(true);
        
        // Pause the automatic falling timeline
        viewGuiController.pauseTimeline();
        
        // Create timer to resume timeline and disable effects after 8 seconds
        Timeline freezeTimer = new Timeline(new KeyFrame(Duration.seconds(8), e -> {
            viewGuiController.setFreezeEffect(false);
            viewGuiController.resumeTimeline();
        }));
        freezeTimer.setCycleCount(1);
        freezeTimer.play();
    }
    
    /**
     * Enters bomb targeting mode, allowing the player to click on the board to select a target.
     */
    private void enterBombTargetingMode() {
        // Show activation notification
        viewGuiController.showPowerUpActivation(PowerUp.BOMB);
        
        isBombTargeting = true;
        viewGuiController.setGameCursor(Cursor.CROSSHAIR);
        // Select Target
    }
    
    /**
     * Handles mouse click events when in bomb targeting mode.
     * Converts pixel coordinates to grid coordinates and explodes blocks at that position.
     * 
     * @param event The mouse event containing click coordinates
     */
    public void handleMouseClick(MouseEvent event) {
        if (!isBombTargeting) {
            return;
        }
        
        // Get click coordinates relative to gameBoard (BorderPane)
        // The event coordinates are relative to the source node (gameBoard)
        // gamePanel (GridPane) is in the center of gameBoard, so coordinates align directly
        
        // Block size is 20px (BRICK_SIZE) + 1px gap = 21px per cell
        final int BLOCK_SIZE = 21;
        
        // Get the click position relative to gameBoard
        // Since gamePanel fills the center of gameBoard and gameBoard is sized to match the board,
        // the coordinates are directly usable for grid conversion
        double clickX = event.getX();
        double clickY = event.getY();
        
        // Convert pixel coordinates to grid coordinates
        // Column (X): divide by block size
        int col = (int) (clickX / BLOCK_SIZE);
        
        // Row (Y): divide by block size, but account for the 2 hidden rows at the top
        // The visible board starts at row 2, so we add 2 to the calculated row
        int row = (int) (clickY / BLOCK_SIZE) + 2;
        
        // Bounds check: ensure coordinates are within valid range
        // Board dimensions: width=25 (rows), height=10 (columns)
        // Matrix is [row][column] = [25][10]
        // Valid rows: 2-24 (visible board), valid columns: 0-9
        if (row >= 2 && row < board.getBoardMatrix().length && col >= 0 && col < board.getBoardMatrix()[0].length) {
            // Explode at the target position (destroys blocks, but does NOT apply gravity)
            board.explodeAt(row, col);
            
            // Play explosion animation (flash, debris, screen shake)
            viewGuiController.playExplosionAnimation(row, col);
            
            // Optional: Play explosion sound effect
            // soundManager.play("explosion.wav");
            
            // Exit targeting mode
            isBombTargeting = false;
            
            // Reset cursor
            viewGuiController.setGameCursor(Cursor.DEFAULT);
            
            // Refresh view to show destroyed blocks (before gravity animation)
            viewGuiController.refreshGameBackground(board.getBoardMatrix());
            
            // Start gravity animation timeline (cascading "avalanche" effect)
            startGravityAnimation();
        } else {
            // Click was outside the board - exit targeting mode without exploding
            isBombTargeting = false;
            viewGuiController.setGameCursor(Cursor.DEFAULT);
        }
    }
    
    /**
     * Starts the gravity animation timeline that makes blocks fall row-by-row.
     * Creates a cascading "avalanche" effect where blocks fall one step at a time.
     */
    private void startGravityAnimation() {
        // Stop any existing gravity timeline to prevent stacking
        if (gravityTimeline != null && gravityTimeline.getStatus() == Animation.Status.RUNNING) {
            gravityTimeline.stop();
        }
        
        // Create a timeline that runs every 100ms (reduced frequency for better performance)
        gravityTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            // Apply one step of gravity (moves floating blocks down by one row)
            ((SimpleBoard) board).applyGravityStep();
            
            // Refresh view to show the movement
            viewGuiController.refreshGameBackground(board.getBoardMatrix());
            
            // Check if there are still floating blocks
            if (!((SimpleBoard) board).hasFloatingBlocks()) {
                // No more floating blocks - stop the timeline
                gravityTimeline.stop();
                gravityTimeline = null;
            }
        }));
        
        // Set the timeline to repeat indefinitely until stopped
        gravityTimeline.setCycleCount(Timeline.INDEFINITE);
        
        // Start the animation
        gravityTimeline.play();
    }
    
    /**
     * Activates the DRILL power-up effect.
     * Replaces the current falling brick with a drill projectile that destroys blocks as it falls.
     */
    private void activateDrill() {
        // Drill activated!
        
        // Show activation notification
        viewGuiController.showPowerUpActivation(PowerUp.DRILL);
        
        // Stop any lock timer
        lockTimer.stop();
        ((SimpleBoard) board).setLocking(false);
        
        // Spawn the drill - let the board handle the logic to ensure variables are synced
        // Do not create a new DrillBrick manually here
        ((SimpleBoard) board).spawnDrill();
        
        // Refresh the view to show the new drill brick
        viewGuiController.refreshBrick(board.getViewData());
        
        // Ensure the game loop is running (start timeline if it was paused)
        // The drill will fall automatically via the game loop
        // The timeline should already be running, but ensure it continues
    }
}
