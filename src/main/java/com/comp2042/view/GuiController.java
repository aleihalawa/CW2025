package com.comp2042.view;

import com.comp2042.controller.InputEventListener;
import com.comp2042.events.EventSource;
import com.comp2042.events.EventType;
import com.comp2042.events.MoveEvent;
import com.comp2042.model.DownData;
import com.comp2042.model.GameMode;
import com.comp2042.model.GameSettings;
import com.comp2042.model.Score;
import com.comp2042.model.ViewData;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Reflection;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;

    @FXML
    private ImageView gameBackgroundImage;

    @FXML
    private GridPane gamePanel;

    @FXML
    private BorderPane gameBoard;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane ghostPanel;

    @FXML
    private GridPane brickPanel;

    @FXML
    private VBox nextBrickPanel;
    
    @FXML
    private VBox inventoryPanel;
    
    @FXML
    private VBox inventoryContainer; // The parent VBox containing the "INVENTORY" label and inventoryPanel
    
    @FXML
    private Pane frostOverlay;
    
    @FXML
    private Rectangle frostVignette;

    @FXML
    private GameOverPanel gameOverPanel;
    
    @FXML
    private com.comp2042.view.LeaderboardPanel leaderboardPanel;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label levelLabel;

    @FXML
    private Label linesLabel;
    
    @FXML
    private Label corruptionTimerLabel;
    
    @FXML
    private VBox corruptionTimerContainer;
    
    /**
     * Gets the corruption timer container for visibility control.
     * 
     * @return The corruption timer container VBox
     */
    public VBox getCorruptionTimerContainer() {
        return corruptionTimerContainer;
    }

    @FXML
    private Button pauseButton;

    @FXML
    private Group pauseMenuGroup;

    @FXML
    private Rectangle dimOverlay;

    @FXML
    private ImageView pauseMenuImage;

    @FXML
    private VBox pauseMenuButtons;

    @FXML
    private Button resumeButton;

    @FXML
    private Button settingsPauseButton;

    @FXML
    private Button quitPauseButton;

    private Rectangle[][] displayMatrix;

    private InputEventListener eventListener;
    private com.comp2042.controller.GameController gameController;

    private Rectangle[][] rectangles;
    private Rectangle[][] ghostRectangles;
    
    private final List<Rectangle[][]> nextBrickGrids = new ArrayList<>();
    
    // Store last ViewData to update colors without changing brick shape
    private ViewData lastViewData;

    private Timeline timeLine;
    
    // Cache coordinate calculations to avoid expensive localToScene calls every frame
    private double cachedBoardOffsetX = 0;
    private double cachedBoardOffsetY = 0;
    private double cachedCellWidth = BRICK_SIZE + 1;
    private double cachedCellHeight = BRICK_SIZE + 1;
    private boolean coordinatesCached = false;
    
    private IntegerProperty scoreProperty;

    private final BooleanProperty isPause = new SimpleBooleanProperty();

    private final BooleanProperty isGameOver = new SimpleBooleanProperty();
    
    private boolean isAnimating = false;
    
    private boolean isShowingHighScoreNotification = false;
    
    // Track mirror mode for positioning adjustments
    private boolean isMirrorMode = false;
    
    // Board dimensions for flip calculations
    private static final int BOARD_WIDTH_PX = 10 * 21;  // 210px
    private static final int BOARD_HEIGHT_PX = 23 * 21; // 483px (23 visible rows)
    
    // Particle system for Mirror Mode
    private Pane particleContainer;
    private Timeline particleTimeline;
    private final List<Rectangle> activeParticles = new ArrayList<>();
    
    // Freeze effect system
    private Timeline freezeParticleTimeline;
    private boolean isFrozen = false;
    private AnimationTimer snowStormTimer;
    private final java.util.List<Circle> snowParticles = new java.util.ArrayList<>();
    private Scene gameScene;
    
    // Drill texture image
    private Image drillTexture;
    
    // Drill visual effects
    private double drillRotation = 0;
    private final java.util.List<Rectangle> drillParticles = new java.util.ArrayList<>();
    private AnimationTimer drillParticleTimer;
    private boolean isDrillActive = false;
    private int[][] previousBoardMatrix; // Track previous board state to detect destroyed blocks

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load background image
        try {
            URL imageUrl = getClass().getClassLoader().getResource("background_img.jpeg");
            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                gameBackgroundImage.setImage(image);
            } else {
                System.err.println("Could not find background_img.jpeg in resources");
            }
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Critical: Disable layout management for ghostPanel and brickPanel so manual positioning works
        ghostPanel.setManaged(false);
        brickPanel.setManaged(false);
        // Ensure proper z-order: gamePanel at back, ghostPanel in middle, brickPanel at front
        // Since they're in a VBox, we need to manage z-order explicitly
        ghostPanel.toFront(); // Bring ghost in front of gamePanel
        brickPanel.toFront(); // Bring brick in front of ghost
        
        // Disable layout management for overlays to prevent layout shifts
        groupNotification.setManaged(false);
        groupNotification.toFront();
        gameOverPanel.setManaged(false);
        gameOverPanel.toFront();
        
        // Setup game over panel size and position
        gameOverPanel.setPrefSize(650, 600);
        gameOverPanel.setLayoutX(0);
        gameOverPanel.setLayoutY(0);
        
        // Setup leaderboard panel
        if (leaderboardPanel != null) {
            leaderboardPanel.setManaged(false);
            leaderboardPanel.setPrefSize(650, 600);
            leaderboardPanel.setLayoutX(0);
            leaderboardPanel.setLayoutY(0);
            leaderboardPanel.setVisible(false);
            leaderboardPanel.toFront();
        }
        
        // Setup pause menu
        if (pauseMenuGroup != null) {
            pauseMenuGroup.setManaged(false);
            pauseMenuGroup.setVisible(false);
            pauseMenuGroup.toFront();
        }
        
        // Load pause menu image
        try {
            URL pauseMenuUrl = getClass().getClassLoader().getResource("Pause_menu.png");
            if (pauseMenuUrl != null && pauseMenuImage != null) {
                Image pauseImage = new Image(pauseMenuUrl.toExternalForm());
                pauseMenuImage.setImage(pauseImage);
            } else {
                System.err.println("Could not find Pause_menu.png in resources");
            }
        } catch (Exception e) {
            System.err.println("Error loading pause menu image: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Load drill texture image
        try {
            URL drillUrl = getClass().getClassLoader().getResource("drill logo.png");
            if (drillUrl != null) {
                drillTexture = new Image(drillUrl.toExternalForm());
            } else {
                System.err.println("Could not find drill logo.png in resources");
            }
        } catch (Exception e) {
            System.err.println("Error loading drill texture: " + e.getMessage());
            e.printStackTrace();
        }
        
        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
        
        // Set gameBoard size to match gameboard panel (10 columns × 23 visible rows)
        // Each cell is 21px (20px brick + 1px gap)
        int boardWidth = 10 * 21;  // 210px
        int boardHeight = 23 * 21; // 483px
        gameBoard.setPrefSize(boardWidth, boardHeight);
        gameBoard.setMinSize(boardWidth, boardHeight);
        gameBoard.setMaxSize(boardWidth, boardHeight);
        
        // Initialize particle container for Mirror Mode effects
        particleContainer = new Pane();
        particleContainer.setManaged(false);
        particleContainer.setMouseTransparent(true); // Don't interfere with mouse events
        particleContainer.setVisible(false);
        particleContainer.setPrefSize(boardWidth, boardHeight);
        // Add particle container to gameBoard as an overlay
        gameBoard.getChildren().add(particleContainer);
        
        // Initialize frost overlay to cover entire screen
        if (frostOverlay != null) {
            frostOverlay.setManaged(false);
            frostOverlay.setMouseTransparent(true);
            // Bind to root StackPane size (650x600)
            frostOverlay.setPrefSize(650, 600);
            frostOverlay.setMaxSize(650, 600);
            
            // Initialize frost vignette
            if (frostVignette != null) {
                frostVignette.widthProperty().bind(frostOverlay.widthProperty());
                frostVignette.heightProperty().bind(frostOverlay.heightProperty());
                frostVignette.setFill(Color.TRANSPARENT);
            }
        }
        
        // Store scene reference for snow storm (will be set when scene is available)
        // We'll also set it in setFreezeEffect if needed
        
        // Create grid pattern matching brick size (21px cells)
        createGridPattern(boardWidth, boardHeight);
        
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.SPACE) {
                        DownData downData = eventListener.onSpaceEvent(new MoveEvent(EventType.DOWN, EventSource.USER));
                        if (downData.getClearRow() != null && downData.getClearRow().getLinesRemovedCount() > 0) {
                            NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                            groupNotification.getChildren().add(notificationPanel);
                            notificationPanel.showScore(groupNotification.getChildren());
                        }
                        refreshBrick(downData.getViewData());
                        keyEvent.consume();
                    }
                    // Power-up key bindings - ONLY work in POWERUPS mode
                    if (GameSettings.getSelectedGameMode() == GameMode.POWERUPS) {
                        if (keyEvent.getCode() == KeyCode.DIGIT1 || keyEvent.getCode() == KeyCode.NUMPAD1) {
                            eventListener.onPowerUpEvent(0);
                            keyEvent.consume();
                        }
                        if (keyEvent.getCode() == KeyCode.DIGIT2 || keyEvent.getCode() == KeyCode.NUMPAD2) {
                            eventListener.onPowerUpEvent(1);
                            keyEvent.consume();
                        }
                        if (keyEvent.getCode() == KeyCode.DIGIT3 || keyEvent.getCode() == KeyCode.NUMPAD3) {
                            eventListener.onPowerUpEvent(2);
                            keyEvent.consume();
                        }
                    }
                }
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }
            }
        });
        gameOverPanel.setVisible(false);

        final Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);
        
        // Initialize inventory container visibility based on game mode
        // CRITICAL: Only show inventory in POWERUPS mode - hide entire container in other modes
        if (inventoryContainer != null) {
            inventoryContainer.setVisible(GameSettings.getSelectedGameMode() == GameMode.POWERUPS);
        }
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        // Initialize previous board matrix for destruction detection
        if (previousBoardMatrix == null) {
            previousBoardMatrix = new int[boardMatrix.length][];
            for (int i = 0; i < boardMatrix.length; i++) {
                previousBoardMatrix[i] = new int[boardMatrix[i].length];
                System.arraycopy(boardMatrix[i], 0, previousBoardMatrix[i], 0, boardMatrix[i].length);
            }
        }
        // Store board reference if available from eventListener
        if (eventListener instanceof com.comp2042.controller.GameController) {
            com.comp2042.controller.GameController gc = (com.comp2042.controller.GameController) eventListener;
            // We'll need to get board from GameController - for now, we'll pass it differently
        }
        // Invalidate coordinate cache when initializing new game view
        coordinatesCached = false;
        
        // Initialize next brick preview first
        initNextBrickView();
        
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        // Cache brick data to avoid multiple getBrickData() calls
        int[][] brickData = brick.getBrickData();
        
        rectangles = new Rectangle[brickData.length][brickData[0].length];
        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                int val = brickData[i][j];
                // Check if this is a drill (ID 11) and use texture
                if (val == 11 && drillTexture != null) {
                    // Scale image to match brick size (20x20)
                    rectangle.setFill(new ImagePattern(drillTexture, 0, 0, BRICK_SIZE, BRICK_SIZE, false));
                } else {
                    rectangle.setFill(getFillColor(val));
                }
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }

        // Initialize ghost panel with rectangles (outline only, no fill)
        ghostRectangles = new Rectangle[brickData.length][brickData[0].length];
        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                // Ghost piece: no fill, only thin outline in brick color with neon glow
                rectangle.setFill(Color.TRANSPARENT);
                // Match brick color (frozen or normal)
                Paint brickColor;
                if (isFrozen) {
                    brickColor = getGlacierColor(brickData[i][j]);
                } else {
                    brickColor = getFillColor(brickData[i][j]);
                }
                if (brickColor instanceof Color) {
                    rectangle.setStroke((Color) brickColor);
                    rectangle.setStrokeWidth(1.0); // Thin outline
                    rectangle.setStrokeType(StrokeType.INSIDE); // Stroke inside bounds for perfect alignment
                    // Add slight neon glow effect
                    javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.6);
                    rectangle.setEffect(glow);
                }
                rectangle.setArcHeight(9);
                rectangle.setArcWidth(9);
                ghostRectangles[i][j] = rectangle;
                ghostPanel.add(rectangle, j, i);
            }
        }
        // Use localToScene to dynamically align with gamePanel grid
        // 1. Get the exact screen position of the Board Grid (gamePanel)
        if (gamePanel.getScene() != null && brickPanel.getParent() != null) {
            javafx.geometry.Point2D boardPos = gamePanel.localToScene(0, 0);
            
            // 2. Get the screen position of brickPanel's parent container
            javafx.geometry.Point2D parentPos = brickPanel.getParent().localToScene(0, 0);
            
            // 3. Convert board position to brickPanel's parent coordinate system
            double boardOffsetX = boardPos.getX() - parentPos.getX();
            double boardOffsetY = boardPos.getY() - parentPos.getY();
            
            // 4. Calculate cell dimensions
            double cellWidth = BRICK_SIZE + 1; // 20px + 1px gap
            double cellHeight = BRICK_SIZE + 1;
            
            // 5. Position brickPanel to align with gamePanel grid
            double brickX = boardOffsetX + (brick.getxPosition() * cellWidth);
            double brickY = boardOffsetY + ((brick.getyPosition() - 2) * cellHeight);
            
            // Adjust Y positioning for mirror mode (anti-gravity: bricks fall from bottom to top)
            if (GameSettings.getSelectedGameMode() == GameMode.MIRROR) {
                // We need to shift everything UP significantly.
                // This formula calculates the distance from the bottom row.
                // (22 - y) means as Y gets bigger (logic falls down), the result gets smaller (visual moves up).
                double mirrorY = (22 - brick.getyPosition()) * cellHeight;
                
                // Manual Calibration: 
                // If it is still too low, make this number SMALLER (e.g. -600). 
                // If it is too high, make it LARGER (e.g. -50).
                double yCorrection = -420.0; // Fine-tuned: shifted down slightly (about 1.5 blocks)
                
                brickY = boardOffsetY + mirrorY + yCorrection;
            }
            
            brickPanel.setLayoutX(brickX);
            brickPanel.setLayoutY(brickY);
            
            // 6. Position ghostPanel at the ghost Y position (same X, different Y)
            // Use the exact same parent and calculation as brickPanel since they're siblings
            if (ghostPanel != null) {
                double ghostX = boardOffsetX + (brick.getxPosition() * cellWidth);
                double ghostY = boardOffsetY + ((brick.getGhostY() - 2) * cellHeight);
                
                // Adjust Y positioning for mirror mode (anti-gravity)
                if (GameSettings.getSelectedGameMode() == GameMode.MIRROR) {
                    // Apply exactly the same logic to the ghost
                    double ghostMirrorY = (22 - brick.getGhostY()) * cellHeight;
                    double yCorrection = -420.0; // Same correction factor - fine-tuned down slightly
                    
                    ghostY = boardOffsetY + ghostMirrorY + yCorrection;
                }
                
                ghostPanel.setLayoutX(ghostX);
                ghostPanel.setLayoutY(ghostY);
            }
        }
        // If scene not attached yet, refreshBrick will handle positioning on first update


        timeLine = new Timeline(new KeyFrame(
                Duration.millis(400),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
        
        // Refresh next brick preview with initial data
        refreshNextBrick(brick.getNextBricks());
        
        // Refresh inventory with initial data - ONLY in POWERUPS mode
        if (GameSettings.getSelectedGameMode() == GameMode.POWERUPS) {
            refreshInventory(brick.getInventory());
            // Show corruption timer in POWERUPS mode
            if (corruptionTimerContainer != null) {
                corruptionTimerContainer.setVisible(true);
            }
        } else {
            // Hide inventory container completely in non-POWERUPS modes
            if (inventoryContainer != null) {
                inventoryContainer.setVisible(false);
            }
            // Hide corruption timer in non-POWERUPS modes
            if (corruptionTimerContainer != null) {
                corruptionTimerContainer.setVisible(false);
            }
        }
        
        // Store initial ViewData for color updates during freeze
        lastViewData = brick;
        
        // Set up mouse click handler for bomb targeting (if gameController is available)
        if (gameController != null) {
            // Attach mouse click listener to gameBoard for bomb targeting
            gameBoard.setOnMouseClicked(gameController::handleMouseClick);
        }
    }

    /**
     * Updates the game speed by changing the timeline delay.
     * @param delayMillis The new delay in milliseconds between automatic drops
     */
    public void updateGameSpeed(double delayMillis) {
        if (timeLine != null) {
            // Stop the existing timeline
            timeLine.stop();
            
            // Clear old keyframes
            timeLine.getKeyFrames().clear();
            
            // Create a new KeyFrame with the new duration and the same event handler
            timeLine.getKeyFrames().add(new KeyFrame(
                Duration.millis(delayMillis),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
            ));
            
            // Restart the timeline if game is not paused or over
            if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                timeLine.play();
            }
        }
    }
    
    /**
     * Pauses the automatic falling timeline.
     */
    public void pauseTimeline() {
        if (timeLine != null && timeLine.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            timeLine.pause();
        }
    }
    
    /**
     * Resumes the automatic falling timeline.
     */
    public void resumeTimeline() {
        if (timeLine != null && 
            isPause.getValue() == Boolean.FALSE && 
            isGameOver.getValue() == Boolean.FALSE &&
            timeLine.getStatus() == javafx.animation.Animation.Status.PAUSED) {
            timeLine.play();
        }
    }
    
    /**
     * Sets the cursor for the game board.
     * 
     * @param cursor The cursor to set (e.g., Cursor.CROSSHAIR, Cursor.DEFAULT)
     */
    public void setGameCursor(Cursor cursor) {
        if (gameBoard != null) {
            gameBoard.setCursor(cursor);
        }
    }
    
    /**
     * Updates the corruption timer display.
     * 
     * @param seconds The number of seconds remaining until next corruption
     */
    public void updateCorruptionTimer(int seconds) {
        if (corruptionTimerLabel != null) {
            corruptionTimerLabel.setText(String.valueOf(seconds));
            // Change text color to red if 3 seconds or less, else purple
            if (seconds <= 3) {
                corruptionTimerLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', sans-serif;");
            } else {
                corruptionTimerLabel.setStyle("-fx-text-fill: #BA55D3; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', sans-serif;");
            }
        }
    }
    
    /**
     * Shows a subtle notification when a power-up is earned (added to inventory).
     * Positioned near the inventory panel, not distracting.
     * 
     * @param powerUpType The type of power-up that was earned
     */
    public void showPowerUpEarned(com.comp2042.model.PowerUp powerUpType) {
        if (groupNotification == null || inventoryContainer == null) {
            return;
        }
        
        String message;
        Color textColor;
        
        switch (powerUpType) {
            case FREEZE:
                message = "Freeze Earned!";
                textColor = Color.CYAN;
                break;
            case BOMB:
                message = "Bomb Earned!";
                textColor = Color.RED;
                break;
            case DRILL:
                message = "Drill Earned!";
                textColor = Color.GREY;
                break;
            default:
                return; // Don't show notification for NONE
        }
        
        // Create a smaller, more subtle notification panel
        NotificationPanel notificationPanel = new NotificationPanel(message);
        
        // Make it smaller and more subtle
        notificationPanel.setMinWidth(150);
        notificationPanel.setMinHeight(60);
        
        // Customize the label color and make it smaller
        javafx.scene.Node centerNode = notificationPanel.getCenter();
        if (centerNode instanceof Label) {
            Label label = (Label) centerNode;
            label.setTextFill(textColor);
            // Smaller, more subtle font
            label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        }
        
        // Position it near the inventory panel (top-right area), using scene coordinates to be robust
        // Use final array to hold position values for lambda
        final double[] position = {450, 50}; // [x, y] - default fallback position

        // Compute position relative to gameBoard and translate to groupNotification's parent
        Platform.runLater(() -> {
            try {
                javafx.geometry.Point2D scenePt = gameBoard.localToScene(gameBoard.getWidth(), 0);
                javafx.scene.Parent parent = groupNotification.getParent();
                if (scenePt != null && parent != null) {
                    javafx.geometry.Point2D localPt = parent.sceneToLocal(scenePt);
                    position[0] = localPt.getX() - 170; // offset left
                    position[1] = localPt.getY() + 20;  // small top offset
                }
            } catch (Exception ignored) { }

            notificationPanel.setLayoutX(position[0]);
            notificationPanel.setLayoutY(position[1]);

            // Add to notification group and bring to front
            groupNotification.getChildren().add(notificationPanel);
            groupNotification.toFront();

            // Show with shorter, more subtle animation
            FadeTransition ft = new FadeTransition(Duration.millis(1500), notificationPanel);
            TranslateTransition tt = new TranslateTransition(Duration.millis(2000), notificationPanel);
            tt.setToY(notificationPanel.getLayoutY() - 20); // Move up slightly
            ft.setFromValue(1);
            ft.setToValue(0);
            ParallelTransition transition = new ParallelTransition(tt, ft);
            transition.setOnFinished(e -> groupNotification.getChildren().remove(notificationPanel));
            transition.play();
        });
    }
    
    /**
     * Shows a power-up activation notification popup.
     * 
     * @param powerUpType The type of power-up that was activated
     */
    public void showPowerUpActivation(com.comp2042.model.PowerUp powerUpType) {
        if (groupNotification == null) {
            return;
        }
        
        String message;
        Color textColor;
        
        switch (powerUpType) {
            case FREEZE:
                message = "FREEZE ACTIVATED!";
                textColor = Color.CYAN;
                break;
            case BOMB:
                message = "BOMB ACTIVATED!";
                textColor = Color.RED;
                break;
            case DRILL:
                message = "DRILL ACTIVATED!";
                textColor = Color.GREY;
                break;
            default:
                return; // Don't show notification for NONE
        }
        
        // Create notification panel with custom styling
        NotificationPanel notificationPanel = new NotificationPanel(message);
        
        // Customize the label color
        javafx.scene.Node centerNode = notificationPanel.getCenter();
        if (centerNode instanceof Label) {
            Label label = (Label) centerNode;
            label.setTextFill(textColor);
            // Make it more prominent
            label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        }
        
        // Position it in the center of the screen (relative to gameBoard)
        double centerX = (gameBoard.getWidth() - notificationPanel.getMinWidth()) / 2;
        double centerY = (gameBoard.getHeight() - notificationPanel.getMinHeight()) / 2;
        notificationPanel.setLayoutX(centerX);
        notificationPanel.setLayoutY(centerY);
        
        // Add to notification group
        groupNotification.getChildren().add(notificationPanel);
        
        // Show with animation (fades out and moves up)
        notificationPanel.showScore(groupNotification.getChildren());
    }
    
    /**
     * Shows a notification when bedrock corruption occurs (a row turns to bedrock).
     */
    public void showBedrockCorruptionNotification() {
        if (groupNotification == null || gameBoard == null) {
            return;
        }
        
        String message = "BEDROCK RISING!";
        Color textColor = Color.DARKGRAY;
        
        // Create notification panel with custom styling
        NotificationPanel notificationPanel = new NotificationPanel(message);
        
        // Customize the label color
        javafx.scene.Node centerNode = notificationPanel.getCenter();
        if (centerNode instanceof Label) {
            Label label = (Label) centerNode;
            label.setTextFill(textColor);
            // Make it prominent with warning style
            label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        }
        
        // Position it in the center of the screen (relative to gameBoard)
        double centerX = (gameBoard.getWidth() - notificationPanel.getMinWidth()) / 2;
        double centerY = (gameBoard.getHeight() - notificationPanel.getMinHeight()) / 2;
        notificationPanel.setLayoutX(centerX);
        notificationPanel.setLayoutY(centerY);
        
        // Add to notification group
        groupNotification.getChildren().add(notificationPanel);
        
        // Show with animation (fades out and moves up)
        notificationPanel.showScore(groupNotification.getChildren());
    }
    
    /**
     * Plays an explosion animation at the specified grid position.
     * Includes flash, debris particles, and screen shake effects.
     * 
     * @param row The grid row (0-24, where 2-24 are visible)
     * @param col The grid column (0-9)
     */
    public void playExplosionAnimation(int row, int col) {
        if (gameBoard == null || gamePanel == null) {
            return;
        }
        
        // Convert grid coordinates to pixel coordinates (center of the 3x3 explosion area)
        // Block size is 21px (BRICK_SIZE + 1)
        final int BLOCK_SIZE = 21;
        // Calculate center of the target cell relative to gamePanel
        double centerX = col * BLOCK_SIZE + (BLOCK_SIZE / 2.0);
        double centerY = (row - 2) * BLOCK_SIZE + (BLOCK_SIZE / 2.0); // Account for 2 hidden rows
        
        // Use scene root to add particles (persists during board refreshes and is always visible)
        // Get the scene root (StackPane) which is always visible
        javafx.scene.Parent root = gamePanel.getScene() != null ? 
            gamePanel.getScene().getRoot() : null;
        if (root == null) {
            return;
        }
        
        final Pane particleContainer;
        // Prefer StackPane root, fallback to frostOverlay if root is not a Pane
        if (root instanceof StackPane) {
            particleContainer = (StackPane) root;
        } else if (root instanceof Pane) {
            particleContainer = (Pane) root;
        } else if (frostOverlay != null) {
            // Fallback to frostOverlay, but make it visible
            frostOverlay.setVisible(true);
            particleContainer = frostOverlay;
        } else {
            return; // Cannot add particles
        }
        
        // Convert gamePanel coordinates to scene root coordinates
        // Scene root (StackPane) covers the entire screen (650x600), so we need scene coordinates
        javafx.geometry.Point2D scenePoint = gamePanel.localToScene(centerX, centerY);
        javafx.geometry.Point2D rootPoint = particleContainer.sceneToLocal(scenePoint);
        final double x;
        final double y;
        
        // If conversion fails (NaN), use direct scene coordinates as fallback
        if (Double.isNaN(rootPoint.getX()) || Double.isNaN(rootPoint.getY())) {
            x = scenePoint.getX();
            y = scenePoint.getY();
        } else {
            x = rootPoint.getX();
            y = rootPoint.getY();
        }
        
        // LAYER 1: The Flash
        Circle flash = new Circle(50, Color.WHITE);
        flash.setOpacity(0.8);
        flash.setLayoutX(x);
        flash.setLayoutY(y);
        flash.setManaged(false);
        
        // Add flash to particle container (frostOverlay)
        particleContainer.getChildren().add(flash);
        flash.toFront();
        
        // Flash animation: scale from 0 to 3.0 and fade from 0.8 to 0
        ScaleTransition scaleFlash = new ScaleTransition(Duration.millis(300), flash);
        scaleFlash.setFromX(0);
        scaleFlash.setFromY(0);
        scaleFlash.setToX(3.0);
        scaleFlash.setToY(3.0);
        scaleFlash.setByX(1.5);
        scaleFlash.setByY(1.5);
        
        FadeTransition fadeFlash = new FadeTransition(Duration.millis(300), flash);
        fadeFlash.setFromValue(0.8);
        fadeFlash.setToValue(0.0);
        
        ParallelTransition flashAnimation = new ParallelTransition(scaleFlash, fadeFlash);
            flashAnimation.setOnFinished(e -> {
                if (flash.getParent() != null && flash.getParent() == particleContainer) {
                    particleContainer.getChildren().remove(flash);
                }
            });
        flashAnimation.play();
        
        // LAYER 2: The Debris (20 particles)
        Color[] debrisColors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.DARKRED};
        List<Rectangle> debrisParticles = new ArrayList<>();
        
        for (int i = 0; i < 20; i++) {
            // Create debris particle (5x5)
            Rectangle particle = new Rectangle(5, 5);
            
            // Random color from debrisColors
            Color particleColor = debrisColors[(int)(Math.random() * debrisColors.length)];
            particle.setFill(particleColor);
            
            // Set initial position at explosion center with small random offset
            double offsetX = (Math.random() - 0.5) * 10; // -5 to +5
            double offsetY = (Math.random() - 0.5) * 10;
            particle.setLayoutX(x + offsetX);
            particle.setLayoutY(y + offsetY);
            particle.setManaged(false);
            
            // Random velocity vector (flying outward from center)
            double angle = Math.random() * Math.PI * 2; // Random direction (0 to 2π)
            double speed = 2 + Math.random() * 4; // 2-6 pixels per frame
            double deltaX = Math.cos(angle) * speed;
            double deltaY = Math.sin(angle) * speed;
            
            // Add to particle container (frostOverlay)
            particleContainer.getChildren().add(particle);
            particle.toFront();
            debrisParticles.add(particle);
            
            // Create translate transition for movement
            TranslateTransition moveParticle = new TranslateTransition(Duration.millis(600), particle);
            moveParticle.setByX(deltaX * 10); // Multiply by frame count approximation
            moveParticle.setByY(deltaY * 10);
            
            // Fade out over 600ms
            FadeTransition fadeParticle = new FadeTransition(Duration.millis(600), particle);
            fadeParticle.setFromValue(1.0);
            fadeParticle.setToValue(0.0);
            
            // Play both animations in parallel
            ParallelTransition particleAnimation = new ParallelTransition(moveParticle, fadeParticle);
            particleAnimation.setOnFinished(e -> {
                debrisParticles.remove(particle);
                if (particle.getParent() != null && particle.getParent() == particleContainer) {
                    particleContainer.getChildren().remove(particle);
                }
            });
            particleAnimation.play();
        }
        
        // LAYER 3: The Screen Shake
        Timeline shakeTimeline = new Timeline();
        shakeTimeline.setCycleCount(4); // 4 keyframes over 200ms (50ms each)
        
        // Create keyframes that shake the board
        for (int i = 0; i < 4; i++) {
            KeyFrame shakeFrame = new KeyFrame(
                Duration.millis(i * 50), // 0, 50, 100, 150ms
                e -> {
                    if (gameBoard != null) {
                        gameBoard.setTranslateX((Math.random() - 0.5) * 10);
                        gameBoard.setTranslateY((Math.random() - 0.5) * 10);
                    }
                }
            );
            shakeTimeline.getKeyFrames().add(shakeFrame);
        }
        
        // Reset position at the end
        shakeTimeline.setOnFinished(e -> {
            if (gameBoard != null) {
                gameBoard.setTranslateX(0);
                gameBoard.setTranslateY(0);
            }
        });
        shakeTimeline.play();
    }

    private Paint getFillColor(int i) {
        Paint returnPaint;
        switch (i) {
            case 0:
                returnPaint = Color.TRANSPARENT;
                break;
            case 1:
                returnPaint = Color.AQUA;
                break;
            case 2:
                returnPaint = Color.BLUEVIOLET;
                break;
            case 3:
                returnPaint = Color.DARKGREEN;
                break;
            case 4:
                returnPaint = Color.YELLOW;
                break;
            case 5:
                returnPaint = Color.RED;
                break;
            case 6:
                returnPaint = Color.BEIGE;
                break;
            case 7:
                returnPaint = Color.BURLYWOOD;
                break;
            case 9:
                returnPaint = Color.DARKGRAY; // Bedrock
                break;
            default:
                returnPaint = Color.WHITE;
                break;
        }
        return returnPaint;
    }
    
    /**
     * Gets the color for a power up type.
     * 
     * @param type The power up type
     * @return The color for the power up
     */
    private javafx.scene.paint.Paint getPowerUpColor(com.comp2042.model.PowerUp type) {
        switch (type) {
            case BOMB:
                return Color.RED;
            case DRILL:
                return Color.GREY;
            case FREEZE:
                return Color.CYAN;
            case NONE:
            default:
                return Color.TRANSPARENT;
        }
    }
    
    /**
     * Gets glacier color (white and cyan only) for blocks when freeze effect is active.
     * Returns translucent white and cyan colors.
     * 
     * @param brickID The block color ID
     * @return A translucent white or cyan color variant
     */
    private Color getGlacierColor(int brickID) {
        if (brickID == 0) {
            return Color.TRANSPARENT;
        }
        // Map different block types to white or cyan (translucent)
        // Alternate between white and cyan for variety
        switch (brickID) {
            case 1:
                return Color.rgb(0, 255, 255, 0.6); // Translucent Cyan
            case 2:
                return Color.rgb(255, 255, 255, 0.6); // Translucent White
            case 3:
                return Color.rgb(0, 255, 255, 0.6); // Translucent Cyan
            case 4:
                return Color.rgb(255, 255, 255, 0.6); // Translucent White
            case 5:
                return Color.rgb(0, 255, 255, 0.6); // Translucent Cyan
            case 6:
                return Color.rgb(255, 255, 255, 0.6); // Translucent White
            case 7:
                return Color.rgb(0, 255, 255, 0.6); // Translucent Cyan
            default:
                return Color.rgb(255, 255, 255, 0.6); // Translucent White
        }
    }
    
    /**
     * Gets frozen color (shades of Cyan/Blue/White) for blocks when freeze effect is active.
     * 
     * @param i The block color ID
     * @return A frozen color variant
     */
    private Paint getFrozenColor(int i) {
        return getGlacierColor(i);
    }
    
    private void createGridPattern(int width, int height) {
        // Create a Pane to hold grid lines
        Pane gridPane = new Pane();
        gridPane.setPrefSize(width, height);
        gridPane.setMouseTransparent(true);
        
        // Grid cell size: 21px (20px brick + 1px gap)
        int cellSize = BRICK_SIZE + 1;
        Color gridColor = Color.rgb(50, 50, 60, 0.5);
        
        // Draw vertical lines (11 lines for 10 columns: 0, 21, 42, ..., 210)
        for (int col = 0; col <= 10; col++) {
            int x = col * cellSize;
            Line line = new Line(x, 0, x, height);
            line.setStroke(gridColor);
            line.setStrokeWidth(1);
            gridPane.getChildren().add(line);
        }
        
        // Draw horizontal lines (24 lines for 23 rows: 0, 21, 42, ..., 483)
        for (int row = 0; row <= 23; row++) {
            int y = row * cellSize;
            Line line = new Line(0, y, width, y);
            line.setStroke(gridColor);
            line.setStrokeWidth(1);
            gridPane.getChildren().add(line);
        }
        
        // Get the current center node (gamePanel) and wrap it with grid in a StackPane
        javafx.scene.Node currentCenter = gameBoard.getCenter();
        if (currentCenter != null) {
            javafx.scene.layout.StackPane centerPane = new javafx.scene.layout.StackPane();
            centerPane.getChildren().addAll(gridPane, currentCenter);
            gridPane.toBack();
            gameBoard.setCenter(centerPane);
        } else {
            // Fallback: just add grid to center
            gameBoard.setCenter(gridPane);
        }
    }

    public void refreshBrick(ViewData brick) {
        // Store the current ViewData for color updates during freeze
        lastViewData = brick;
        
        if (isPause.getValue() == Boolean.FALSE) {
            // Cache expensive coordinate calculations - only recalculate if scene changes
            if (!coordinatesCached || gamePanel.getScene() == null || brickPanel.getParent() == null) {
                if (gamePanel.getScene() != null && brickPanel.getParent() != null) {
                    javafx.geometry.Point2D boardPos = gamePanel.localToScene(0, 0);
                    javafx.geometry.Point2D parentPos = brickPanel.getParent().localToScene(0, 0);
                    cachedBoardOffsetX = boardPos.getX() - parentPos.getX();
                    cachedBoardOffsetY = boardPos.getY() - parentPos.getY();
                    cachedCellWidth = BRICK_SIZE + 1;
                    cachedCellHeight = BRICK_SIZE + 1;
                    coordinatesCached = true;
                }
            }
            
            // Position brickPanel using cached values
            if (coordinatesCached) {
                double brickX = cachedBoardOffsetX + (brick.getxPosition() * cachedCellWidth);
                double brickY = cachedBoardOffsetY + ((brick.getyPosition() - 2) * cachedCellHeight);
                
                // Adjust Y positioning for mirror mode (anti-gravity: bricks fall from bottom to top)
                if (GameSettings.getSelectedGameMode() == GameMode.MIRROR) {
                    // We need to shift everything UP significantly.
                    // This formula calculates the distance from the bottom row.
                    // (22 - y) means as Y gets bigger (logic falls down), the result gets smaller (visual moves up).
                    double mirrorY = (22 - brick.getyPosition()) * cachedCellHeight;
                    
                    // Manual Calibration: 
                    // If it is still too low, make this number SMALLER (e.g. -600). 
                    // If it is too high, make it LARGER (e.g. -50).
                    double yCorrection = -420.0; // Fine-tuned: shifted down slightly (about 1.5 blocks)
                    
                    brickY = cachedBoardOffsetY + mirrorY + yCorrection;
                }
                
                brickPanel.setLayoutX(brickX);
                brickPanel.setLayoutY(brickY);
                
                // Position ghostPanel at the ghost Y position
                if (ghostPanel != null && ghostRectangles != null) {
                    double ghostX = cachedBoardOffsetX + (brick.getxPosition() * cachedCellWidth);
                    double ghostY = cachedBoardOffsetY + ((brick.getGhostY() - 2) * cachedCellHeight);
                    
                    // Adjust Y positioning for mirror mode (anti-gravity)
                    if (GameSettings.getSelectedGameMode() == GameMode.MIRROR) {
                        // Apply exactly the same logic to the ghost
                        double ghostMirrorY = (22 - brick.getGhostY()) * cachedCellHeight;
                        double yCorrection = -420.0; // Same correction factor - fine-tuned down slightly
                        
                        ghostY = cachedBoardOffsetY + ghostMirrorY + yCorrection;
                    }
                    
                    ghostPanel.setLayoutX(ghostX);
                    ghostPanel.setLayoutY(ghostY);
                }
            }
            
            // Update dimming indicator based on locking state
            if (brick.isLocking()) {
                brickPanel.setOpacity(0.4); // Dim the block to show it's waiting
            } else {
                brickPanel.setOpacity(1.0); // Normal brightness
            }
            
            // Cache brick data to avoid multiple getBrickData() calls
            int[][] brickData = brick.getBrickData();
            
            // CRITICAL: Check if brick size changed (e.g., switching to/from drill)
            // If the brick size changed, we need to recreate the rectangles array
            if (rectangles == null || 
                rectangles.length != brickData.length || 
                (brickData.length > 0 && rectangles[0].length != brickData[0].length)) {
                // Brick size changed - recreate rectangles array
                // First, remove old rectangles from brickPanel
                if (rectangles != null) {
                    for (Rectangle[] row : rectangles) {
                        for (Rectangle rect : row) {
                            if (rect != null) {
                                brickPanel.getChildren().remove(rect);
                            }
                        }
                    }
                }
                
                // Create new rectangles array matching the new brick size
                rectangles = new Rectangle[brickData.length][brickData[0].length];
                for (int i = 0; i < brickData.length; i++) {
                    for (int j = 0; j < brickData[i].length; j++) {
                        Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                        rectangles[i][j] = rectangle;
                        brickPanel.add(rectangle, j, i);
                    }
                }
            }
            
            // Check if drill is active (any cell with ID 11)
            boolean drillFound = false;
            double drillScreenX = 0;
            double drillScreenY = 0;
            Rectangle drillRect = null;
            for (int i = 0; i < brickData.length && !drillFound; i++) {
                for (int j = 0; j < brickData[i].length && !drillFound; j++) {
                    if (brickData[i][j] == 11 && i < rectangles.length && j < rectangles[i].length && rectangles[i][j] != null) {
                        drillFound = true;
                        drillRect = rectangles[i][j];
                        // Calculate drill screen position for particles
                        if (coordinatesCached) {
                            drillScreenX = cachedBoardOffsetX + (brick.getxPosition() * cachedCellWidth) + (j * cachedCellWidth) + (BRICK_SIZE / 2.0);
                            drillScreenY = cachedBoardOffsetY + ((brick.getyPosition() - 2) * cachedCellHeight) + (i * cachedCellHeight) + (BRICK_SIZE / 2.0);
                            // Adjust for mirror mode if needed
                            if (GameSettings.getSelectedGameMode() == GameMode.MIRROR) {
                                double mirrorY = (22 - brick.getyPosition()) * cachedCellHeight;
                                double yCorrection = -420.0;
                                drillScreenY = cachedBoardOffsetY + mirrorY + yCorrection + (i * cachedCellHeight) + (BRICK_SIZE / 2.0);
                            }
                        }
                        break;
                    }
                }
            }
            
            // Update drill active state and effects
            if (drillFound != isDrillActive) {
                isDrillActive = drillFound;
                if (isDrillActive) {
                    // Start drill effects
                    startDrillEffects();
                    // Hide ghost piece - drill doesn't need it (it just drills through)
                    if (ghostPanel != null) {
                        ghostPanel.setVisible(false);
                    }
                } else {
                    // Stop drill effects
                    stopDrillEffects();
                    // Show ghost piece again for normal bricks
                    if (ghostPanel != null && GameSettings.isGhostModeEnabled()) {
                        ghostPanel.setVisible(true);
                    }
                }
            }
            
            // Also hide ghost during drill (in case it was already visible)
            if (isDrillActive && ghostPanel != null) {
                ghostPanel.setVisible(false);
            }
            
            // Update brick rectangles - hide unused ones, show and update used ones
            for (int i = 0; i < rectangles.length; i++) {
                for (int j = 0; j < rectangles[i].length; j++) {
                    if (rectangles[i][j] != null) {
                        if (i < brickData.length && j < brickData[i].length) {
                            // This rectangle is part of the current brick
                            int val = brickData[i][j];
                            rectangles[i][j].setVisible(val != 0); // Hide if empty, show if filled
                            
                            if (val != 0) {
                                // Check if this is a drill (ID 11) and use texture
                                if (val == 11 && drillTexture != null) {
                                    // Scale image to match brick size (20x20)
                                    rectangles[i][j].setFill(new ImagePattern(drillTexture, 0, 0, BRICK_SIZE, BRICK_SIZE, false));
                                    
                                    // SPINNING ANIMATION: Rotate the drill
                                    drillRotation += 30; // Increment rotation angle
                                    if (drillRotation >= 360) {
                                        drillRotation -= 360; // Keep in 0-360 range
                                    }
                                    // Center pivot point for rotation
                                    rectangles[i][j].getTransforms().clear();
                                    rectangles[i][j].getTransforms().add(new javafx.scene.transform.Rotate(drillRotation, BRICK_SIZE / 2.0, BRICK_SIZE / 2.0));
                                    
                                    // Spawn continuous drill particles (sparks from drill tip)
                                    if (coordinatesCached && drillScreenX > 0 && drillScreenY > 0) {
                                        spawnDrillSparks(drillScreenX, drillScreenY);
                                    }
                                } else {
                                    // Normal brick - reset rotation and transforms
                                    rectangles[i][j].getTransforms().clear();
                                    
                                    // Check if frozen and use glacier colors
                                    if (isFrozen) {
                                        rectangles[i][j].setFill(getGlacierColor(val));
                                    } else {
                                        rectangles[i][j].setFill(getFillColor(val));
                                    }
                                }
                                rectangles[i][j].setArcHeight(9);
                                rectangles[i][j].setArcWidth(9);
                            } else {
                                // Empty cell - make transparent
                                rectangles[i][j].setFill(Color.TRANSPARENT);
                            }
                        } else {
                            // This rectangle is beyond the current brick size - hide it
                            rectangles[i][j].setVisible(false);
                        }
                    }
                }
            }
            
            // Update ghost rectangles to match brick shape with outline only
            // CRITICAL: Hide ghost for drill - it's distracting and doesn't make sense
            if (isDrillActive && ghostPanel != null) {
                ghostPanel.setVisible(false);
            } else if (GameSettings.isGhostModeEnabled()) {
                // Check if ghost rectangles need to be recreated
                if (ghostRectangles == null || 
                    ghostRectangles.length != brickData.length || 
                    (brickData.length > 0 && ghostRectangles[0].length != brickData[0].length)) {
                    // Ghost size changed - recreate ghost rectangles array
                    // CRITICAL: Clear all children and constraints to prevent layout issues
                    if (ghostRectangles != null) {
                        for (Rectangle[] row : ghostRectangles) {
                            for (Rectangle rect : row) {
                                if (rect != null) {
                                    ghostPanel.getChildren().remove(rect);
                                }
                            }
                        }
                    }
                    // Clear all GridPane constraints to ensure clean state
                    ghostPanel.getChildren().clear();
                    // Also clear column/row constraints if they exist
                    ghostPanel.getColumnConstraints().clear();
                    ghostPanel.getRowConstraints().clear();
                    
                    // Create new ghost rectangles array
                    // CRITICAL: Must set all the same properties as initial creation to avoid shape/visual issues
                    ghostRectangles = new Rectangle[brickData.length][brickData[0].length];
                    for (int i = 0; i < brickData.length; i++) {
                        for (int j = 0; j < brickData[i].length; j++) {
                            Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                            // Ghost piece: no fill, only thin outline in brick color with neon glow
                            rectangle.setFill(Color.TRANSPARENT);
                            // Set arc properties to match initial creation
                            rectangle.setArcHeight(9);
                            rectangle.setArcWidth(9);
                            // Set stroke properties (will be updated based on brick data below)
                            rectangle.setStrokeWidth(1.0);
                            rectangle.setStrokeType(StrokeType.INSIDE);
                            // Add glow effect to match initial creation
                            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.6);
                            rectangle.setEffect(glow);
                            ghostRectangles[i][j] = rectangle;
                            ghostPanel.add(rectangle, j, i);
                        }
                    }
                }
                
                // Update ghost rectangles
                if (ghostRectangles != null) {
                    for (int i = 0; i < ghostRectangles.length; i++) {
                        for (int j = 0; j < ghostRectangles[i].length; j++) {
                            if (ghostRectangles[i][j] != null) {
                                if (i < brickData.length && j < brickData[i].length) {
                                    int brickValue = brickData[i][j];
                                    if (brickValue != 0) {
                                        // Show ghost piece where brick has blocks, with thin outline in brick color
                                        ghostRectangles[i][j].setVisible(true);
                                        ghostRectangles[i][j].setFill(Color.TRANSPARENT);
                                        // Match brick color (frozen or normal)
                                        Paint brickColor;
                                        if (isFrozen) {
                                            brickColor = getGlacierColor(brickValue);
                                        } else {
                                            brickColor = getFillColor(brickValue);
                                        }
                                        if (brickColor instanceof Color) {
                                            ghostRectangles[i][j].setStroke((Color) brickColor);
                                            ghostRectangles[i][j].setStrokeWidth(1.0); // Thin outline
                                            ghostRectangles[i][j].setStrokeType(StrokeType.INSIDE); // Stroke inside bounds for perfect alignment
                                            // Add slight neon glow effect
                                            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.6);
                                            ghostRectangles[i][j].setEffect(glow);
                                        }
                                    } else {
                                        // Hide ghost piece where brick has no blocks
                                        ghostRectangles[i][j].setVisible(false);
                                    }
                                } else {
                                    // Beyond brick size - hide
                                    ghostRectangles[i][j].setVisible(false);
                                }
                            }
                        }
                    }
                }
            } else {
                // If Ghost Mode is OFF, ensure all ghost rectangles are hidden
                if (ghostRectangles != null) {
                    for (Rectangle[] row : ghostRectangles) {
                        for (Rectangle rect : row) {
                            if (rect != null) {
                                rect.setVisible(false);
                            }
                        }
                    }
                }
            }
            
            // Update next brick preview
            refreshNextBrick(brick.getNextBricks());
            
            // Update inventory - ONLY in POWERUPS mode
            if (GameSettings.getSelectedGameMode() == GameMode.POWERUPS) {
                refreshInventory(brick.getInventory());
            } else {
                // Hide inventory container completely in non-POWERUPS modes
                if (inventoryContainer != null) {
                    inventoryContainer.setVisible(false);
                }
            }
        }
    }

    /**
     * Initializes the next brick preview view with 3 mini-grids.
     */
    private void initNextBrickView() {
        // Clear existing grids
        nextBrickPanel.getChildren().clear();
        nextBrickGrids.clear();
        
        // Create 3 mini-grids for 3 preview slots
        for (int slot = 0; slot < 3; slot++) {
            GridPane miniGrid = new GridPane();
            miniGrid.setAlignment(javafx.geometry.Pos.CENTER);
            miniGrid.setHgap(1);
            miniGrid.setVgap(1);
            
            // Create a 4x4 matrix of Rectangle objects
            Rectangle[][] grid = new Rectangle[4][4];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    rectangle.setFill(Color.TRANSPARENT);
                    grid[i][j] = rectangle;
                    miniGrid.add(rectangle, j, i);
                }
            }
            
            // Store the grid and add to panel
            nextBrickGrids.add(grid);
            nextBrickPanel.getChildren().add(miniGrid);
        }
    }

    /**
     * Refreshes the inventory display with current power ups.
     * 
     * @param inventory List of power ups in the inventory
     */
    public void refreshInventory(java.util.List<com.comp2042.model.PowerUp> inventory) {
        if (inventoryPanel == null) {
            return;
        }
        
        // CRITICAL: Only show inventory in POWERUPS mode
        if (GameSettings.getSelectedGameMode() != GameMode.POWERUPS) {
            // Hide entire inventory container in Classic and Mirror modes
            if (inventoryContainer != null) {
                inventoryContainer.setVisible(false);
            }
            return;
        }
        
        // Show inventory container in POWERUPS mode
        if (inventoryContainer != null) {
            inventoryContainer.setVisible(true);
        }
        
        // Clear existing items
        inventoryPanel.getChildren().clear();
        
        // Loop through the inventory list
        for (com.comp2042.model.PowerUp item : inventory) {
            ImageView powerUpImage = null;
            String imageFileName = null;
            
            // Determine which image to use based on power-up type
            if (item == com.comp2042.model.PowerUp.FREEZE) {
                imageFileName = "snowflake logo.png";
            } else if (item == com.comp2042.model.PowerUp.BOMB) {
                imageFileName = "bomb logo.png";
            } else if (item == com.comp2042.model.PowerUp.DRILL) {
                imageFileName = "drill logo.png";
            }
            
            // Try to load image if we have a filename
            if (imageFileName != null) {
                try {
                    URL imageUrl = getClass().getClassLoader().getResource(imageFileName);
                    if (imageUrl != null) {
                        powerUpImage = new ImageView(new Image(imageUrl.toExternalForm()));
                        powerUpImage.setFitWidth(20);
                        powerUpImage.setFitHeight(20);
                        powerUpImage.setPreserveRatio(true);
                        powerUpImage.setSmooth(true);
                        inventoryPanel.getChildren().add(powerUpImage);
                    } else {
                        // Fallback to rectangle if image not found
                        Rectangle rect = new Rectangle(20, 20);
                        rect.setFill(getPowerUpColor(item));
                        rect.setArcWidth(5);
                        rect.setArcHeight(5);
                        inventoryPanel.getChildren().add(rect);
                    }
                } catch (Exception e) {
                    // Fallback to rectangle if image loading fails
                    Rectangle rect = new Rectangle(20, 20);
                    rect.setFill(getPowerUpColor(item));
                    rect.setArcWidth(5);
                    rect.setArcHeight(5);
                    inventoryPanel.getChildren().add(rect);
                }
            } else {
                // Create a Rectangle (width 20, height 20) for other power-ups
                Rectangle rect = new Rectangle(20, 20);
                // Set Fill to getPowerUpColor(item)
                rect.setFill(getPowerUpColor(item));
                // Set ArcWidth/ArcHeight to 5
                rect.setArcWidth(5);
                rect.setArcHeight(5);
                // Add it to inventoryPanel
                inventoryPanel.getChildren().add(rect);
            }
        }
        
        // Empty Slots: If the inventory has fewer than 3 items, fill the remaining slots
        while (inventoryPanel.getChildren().size() < 3) {
            Rectangle emptyRect = new Rectangle(20, 20);
            emptyRect.setFill(Color.TRANSPARENT);
            emptyRect.setStroke(Color.WHITE);
            emptyRect.setStrokeWidth(1);
            emptyRect.setArcWidth(5);
            emptyRect.setArcHeight(5);
            inventoryPanel.getChildren().add(emptyRect);
        }
    }
    
    /**
     * Refreshes the next brick preview with new brick data.
     * @param nextBricks List of brick matrices to display (up to 3)
     */
    private void refreshNextBrick(List<int[][]> nextBricks) {
        if (nextBricks == null || nextBrickGrids.isEmpty()) {
            return;
        }
        
        // Loop through the input list (up to 3 bricks)
        for (int i = 0; i < Math.min(nextBricks.size(), 3); i++) {
            int[][] brickData = nextBricks.get(i);
            Rectangle[][] grid = nextBrickGrids.get(i);
            
            // Clear the grid (set all fills to TRANSPARENT)
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    grid[row][col].setFill(Color.TRANSPARENT);
                }
            }
            
            // Paint the new shape: If a cell in the matrix is > 0, set the rectangle fill
            if (brickData != null) {
                for (int row = 0; row < brickData.length && row < 4; row++) {
                    for (int col = 0; col < brickData[row].length && col < 4; col++) {
                        int val = brickData[row][col];
                        if (val > 0) {
                            // Check if this is a drill (ID 11) and use texture
                            if (val == 11 && drillTexture != null) {
                                // Scale image to match brick size (20x20)
                                grid[row][col].setFill(new ImagePattern(drillTexture, 0, 0, BRICK_SIZE, BRICK_SIZE, false));
                            } else {
                                // Check if frozen and use glacier colors
                                if (isFrozen) {
                                    grid[row][col].setFill(getGlacierColor(val));
                                } else {
                                    grid[row][col].setFill(getFillColor(val));
                                }
                            }
                            grid[row][col].setArcHeight(9);
                            grid[row][col].setArcWidth(9);
                        }
                    }
                }
            }
        }
    }

    public void refreshGameBackground(int[][] board) {
        // DETECT DESTROYED BLOCKS: Compare with previous board state (only when drill is active)
        // Optimized: Only check when drill is active and limit comparison scope
        if (previousBoardMatrix != null && isDrillActive) {
            // Only check visible rows (2 to 24) to reduce comparison overhead
            int maxRows = Math.min(board.length, previousBoardMatrix.length);
            for (int i = 2; i < maxRows; i++) {
                int maxCols = Math.min(board[i].length, previousBoardMatrix[i].length);
                for (int j = 0; j < maxCols; j++) {
                    // Block was there before but is now gone - it was destroyed!
                    if (previousBoardMatrix[i][j] != 0 && board[i][j] == 0) {
                        // Animate block destruction with debris (only if not too many animations running)
                        if (drillParticles.size() < 30) { // Reduced limit for better performance
                            animateBlockDestruction(i, j, previousBoardMatrix[i][j]);
                        }
                    }
                }
            }
        }
        
        // Store current board state for next comparison (only when drill is active)
        if (isDrillActive) {
            if (previousBoardMatrix == null || previousBoardMatrix.length != board.length || 
                (board.length > 0 && previousBoardMatrix[0].length != board[0].length)) {
                previousBoardMatrix = new int[board.length][];
                for (int i = 0; i < board.length; i++) {
                    previousBoardMatrix[i] = new int[board[i].length];
                }
            }
            // Only copy visible rows to reduce overhead
            for (int i = 2; i < board.length; i++) {
                System.arraycopy(board[i], 0, previousBoardMatrix[i], 0, board[i].length);
            }
        }
        
        // Only update visible rows (2 to 24, skipping top 2 hidden rows)
        // This is already optimized - only called when board state changes, not every frame
        for (int i = 2; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Rectangle rectangle = displayMatrix[i][j];
                // Reset visual properties modified by animations
                rectangle.setOpacity(1.0);
                rectangle.setTranslateY(0);
                // Set the color
                setRectangleData(board[i][j], rectangle);
            }
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        // Check if this is bedrock (ID 9) - special handling
        if (color == 9) {
            rectangle.setFill(Color.DARKGRAY);
            rectangle.setStroke(Color.BLACK);
            rectangle.setStrokeWidth(2.0);
            rectangle.setStrokeType(StrokeType.INSIDE);
        } else if (color == 11 && drillTexture != null) {
            // Check if this is a drill (ID 11) and use texture
            // Scale image to match brick size (20x20)
            rectangle.setFill(new ImagePattern(drillTexture, 0, 0, BRICK_SIZE, BRICK_SIZE, false));
            rectangle.setStroke(null); // No stroke for drill
        } else {
            // Check if frozen and use glacier colors
            if (isFrozen) {
                rectangle.setFill(getGlacierColor(color));
            } else {
                rectangle.setFill(getFillColor(color));
            }
            rectangle.setStroke(null); // No stroke for normal blocks
        }
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }
    
    /**
     * Updates only the colors of existing brick rectangles without changing their shape or position.
     * This is used when freeze effect activates/deactivates to avoid changing the brick shape.
     */
    private void updateBrickColors() {
        if (rectangles == null) {
            return;
        }
        
        // Use the last ViewData that was displayed to avoid changing brick shape/rotation
        if (lastViewData == null) {
            return;
        }
        
        int[][] brickData = lastViewData.getBrickData();
        if (brickData == null || rectangles.length == 0) {
            return;
        }
        
        // Update only the colors of existing rectangles (falling brick)
        for (int i = 0; i < brickData.length && i < rectangles.length; i++) {
            for (int j = 0; j < brickData[i].length && j < rectangles[i].length; j++) {
                if (rectangles[i][j] != null) {
                    int val = brickData[i][j];
                    // Check if this is a drill (ID 11) and use texture
                    if (val == 11 && drillTexture != null) {
                        // Scale image to match brick size (20x20)
                        rectangles[i][j].setFill(new ImagePattern(drillTexture, 0, 0, BRICK_SIZE, BRICK_SIZE, false));
                    } else {
                        // Check if frozen and use glacier colors
                        if (isFrozen) {
                            rectangles[i][j].setFill(getGlacierColor(val));
                        } else {
                            rectangles[i][j].setFill(getFillColor(val));
                        }
                    }
                    // Don't change arcHeight/arcWidth as they're already set
                }
            }
        }
        
        // Also update next brick preview colors
        if (lastViewData.getNextBricks() != null && !nextBrickGrids.isEmpty()) {
            List<int[][]> nextBricks = lastViewData.getNextBricks();
            for (int i = 0; i < Math.min(nextBricks.size(), 3) && i < nextBrickGrids.size(); i++) {
                int[][] nextBrickData = nextBricks.get(i);
                Rectangle[][] grid = nextBrickGrids.get(i);
                
                if (nextBrickData != null && grid != null) {
                    for (int row = 0; row < nextBrickData.length && row < 4; row++) {
                        for (int col = 0; col < nextBrickData[row].length && col < 4; col++) {
                            if (grid[row][col] != null) {
                                int val = nextBrickData[row][col];
                                if (val > 0) {
                                    // Check if this is a drill (ID 11) and use texture
                                    if (val == 11 && drillTexture != null) {
                                        // Scale image to match brick size (20x20)
                                        grid[row][col].setFill(new ImagePattern(drillTexture, 0, 0, BRICK_SIZE, BRICK_SIZE, false));
                                    } else {
                                        // Check if frozen and use glacier colors
                                        if (isFrozen) {
                                            grid[row][col].setFill(getGlacierColor(val));
                                        } else {
                                            grid[row][col].setFill(getFillColor(val));
                                        }
                                    }
                                } else {
                                    grid[row][col].setFill(Color.TRANSPARENT);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Also update ghost piece colors to match brick (reuse brickData from above)
        if (ghostRectangles != null && brickData != null) {
            for (int i = 0; i < brickData.length && i < ghostRectangles.length; i++) {
                for (int j = 0; j < brickData[i].length && j < ghostRectangles[i].length; j++) {
                    if (ghostRectangles[i][j] != null) {
                        int brickValue = brickData[i][j];
                        if (brickValue != 0) {
                            // Match brick color (frozen or normal)
                            Paint brickColor;
                            if (isFrozen) {
                                brickColor = getGlacierColor(brickValue);
                            } else {
                                brickColor = getFillColor(brickValue);
                            }
                            if (brickColor instanceof Color) {
                                ghostRectangles[i][j].setStroke((Color) brickColor);
                            }
                        }
                    }
                }
            }
        }
    }

    private void moveDown(MoveEvent event) {
        if (isPause.getValue() == Boolean.FALSE) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemovedCount() > 0) {
                NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
        // Store GameController reference if it's a GameController
        if (eventListener instanceof com.comp2042.controller.GameController) {
            this.gameController = (com.comp2042.controller.GameController) eventListener;
            // Set up mouse click handler for bomb targeting
            if (gameBoard != null && gameController != null) {
                gameBoard.setOnMouseClicked(gameController::handleMouseClick);
            }
        }
    }

    public void bindScore(Score score) {
        if (score == null) {
            return;
        }
        
        // Bind score label
        if (scoreLabel != null) {
            scoreLabel.textProperty().bind(score.scoreProperty().asString());
            scoreProperty = score.scoreProperty();
        }
        
        // Bind level label
        if (levelLabel != null) {
            levelLabel.textProperty().bind(score.levelProperty().asString());
        }
        
        // Bind lines label
        if (linesLabel != null) {
            linesLabel.textProperty().bind(score.linesProperty().asString());
        }
    }
    
    /**
     * Shows the "NEW HIGH SCORE!" notification mid-game.
     * Pauses the game, shows the notification, then resumes after animation.
     */
    public void showHighScoreNotification() {
        if (isShowingHighScoreNotification) {
            return;
        }
        
        isShowingHighScoreNotification = true;
        
        // Pause the game timeline
        final boolean[] wasPlaying = {false};
        if (timeLine != null && timeLine.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            wasPlaying[0] = true;
            timeLine.pause();
        }
        
        // Get the root StackPane from the scene
        final StackPane[] gameRootRef = {null};
        if (gamePanel != null && gamePanel.getScene() != null) {
            javafx.scene.Node root = gamePanel.getScene().getRoot();
            if (root instanceof StackPane) {
                gameRootRef[0] = (StackPane) root;
            }
        }
        
        if (gameRootRef[0] == null) {
            // Fallback: can't find root, just resume
            isShowingHighScoreNotification = false;
            if (wasPlaying[0] && timeLine != null && 
                isPause.getValue() == Boolean.FALSE && 
                isGameOver.getValue() == Boolean.FALSE) {
                timeLine.play();
            }
            return;
        }
        
        final StackPane gameRoot = gameRootRef[0];
        
        // Create dimmer - separate from label
        Rectangle dimmer = new Rectangle();
        dimmer.setFill(Color.BLACK);
        dimmer.setOpacity(0.0);
        // Bind width and height to gameRoot
        dimmer.widthProperty().bind(gameRoot.widthProperty());
        dimmer.heightProperty().bind(gameRoot.heightProperty());
        
        // Create label - separate from dimmer
        Label label = new Label("NEW HIGH SCORE!");
        label.setTextFill(Color.web("#FFD700")); // Gold text
        label.setStyle("-fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; " +
                      "-fx-font-size: 36px; " +
                      "-fx-font-weight: bold;");
        // Add glow effect
        javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.8);
        label.setEffect(glow);
        // Center the label
        StackPane.setAlignment(label, javafx.geometry.Pos.CENTER);
        // Start with scale 0
        label.setScaleX(0.0);
        label.setScaleY(0.0);
        
        // Add both to root individually (NOT in a shared Group)
        gameRoot.getChildren().addAll(dimmer, label);
        // Bring to front
        dimmer.toFront();
        label.toFront();
        
        // Appear Phase (Parallel) - faster for better responsiveness
        FadeTransition dimmerFadeIn = new FadeTransition(Duration.millis(150), dimmer);
        dimmerFadeIn.setFromValue(0.0);
        dimmerFadeIn.setToValue(0.7);
        
        ScaleTransition labelScaleIn = new ScaleTransition(Duration.millis(250), label);
        labelScaleIn.setFromX(0.0);
        labelScaleIn.setFromY(0.0);
        labelScaleIn.setToX(1.0);
        labelScaleIn.setToY(1.0);
        labelScaleIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        ParallelTransition appearTransition = new ParallelTransition(dimmerFadeIn, labelScaleIn);
        
        // Hold Phase - shorter for faster flow
        javafx.animation.PauseTransition hold = new javafx.animation.PauseTransition(Duration.millis(1000));
        
        // Disappear Phase (Parallel) - faster
        FadeTransition dimmerFadeOut = new FadeTransition(Duration.millis(150), dimmer);
        dimmerFadeOut.setFromValue(0.7);
        dimmerFadeOut.setToValue(0.0);
        
        FadeTransition labelFadeOut = new FadeTransition(Duration.millis(150), label);
        labelFadeOut.setFromValue(1.0);
        labelFadeOut.setToValue(0.0);
        
        ParallelTransition disappearTransition = new ParallelTransition(dimmerFadeOut, labelFadeOut);
        
        // Sequence: Appear -> Hold -> Disappear
        SequentialTransition sequence = new SequentialTransition(appearTransition, hold, disappearTransition);
        sequence.setOnFinished(e -> {
            // Cleanup: Remove dimmer and label from gameRoot
            gameRoot.getChildren().removeAll(dimmer, label);
            
            // Resume timeline
            if (wasPlaying[0] && timeLine != null && 
                isPause.getValue() == Boolean.FALSE && 
                isGameOver.getValue() == Boolean.FALSE) {
                timeLine.play();
            }
            
            // Reset flag and run callback
            isShowingHighScoreNotification = false;
        });
        
        sequence.play();
    }

    public void gameOver() {
        timeLine.stop();
        
        // Get current score from the property
        int finalScore = 0;
        if (scoreProperty != null) {
            finalScore = scoreProperty.get();
        } else if (scoreLabel != null && scoreLabel.getText() != null) {
            try {
                finalScore = Integer.parseInt(scoreLabel.getText());
            } catch (NumberFormatException e) {
                finalScore = 0;
            }
        }
        
        // Get high score for current game mode
        int highScore = 0;
        try {
            com.comp2042.model.GameMode currentMode = com.comp2042.model.GameSettings.getSelectedGameMode();
            java.util.List<com.comp2042.model.ScoreEntry> topScores = com.comp2042.model.HighScoreManager.loadLeaderboard(currentMode);
            if (!topScores.isEmpty()) {
                highScore = topScores.get(0).getScore();
            }
        } catch (Exception e) {
            System.err.println("Error loading high score: " + e.getMessage());
        }
        
        // Set scores on game over panel
        gameOverPanel.setScores(finalScore, highScore);
        
        // Setup button handlers
        gameOverPanel.setOnRestart(e -> {
            newGame(e);
        });
        
        gameOverPanel.setOnLeaderboard(e -> {
            if (leaderboardPanel != null) {
                leaderboardPanel.setOnClose(ev -> {
                    leaderboardPanel.hideWithAnimation();
                });
                leaderboardPanel.toFront();
                leaderboardPanel.showWithAnimation();
            }
        });
        
        gameOverPanel.setOnMainMenu(e -> {
            navigateToMainMenu(e);
        });
        
        // Show with animation
        gameOverPanel.showWithAnimation();
        
        // Disable pause button when game is over
        if (pauseButton != null) {
            pauseButton.setDisable(true);
            pauseButton.setOpacity(0.5);
        }
        
        isGameOver.setValue(Boolean.TRUE);
        // Hide the falling brick and ghost when game is over to prevent glitch
        brickPanel.setVisible(false);
        if (ghostPanel != null) {
            ghostPanel.setVisible(false);
        }
    }
    
    private void navigateToMainMenu(ActionEvent event) {
        try {
            // Stop the game timeline
            if (timeLine != null) {
                timeLine.stop();
            }
            
            // Stop background music
            if (gameController != null) {
                gameController.stopBackgroundMusic();
            }
            
            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Load MainMenu.fxml
            URL location = getClass().getClassLoader().getResource("com/comp2042/view/MainMenu.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location);
            Parent newRoot = fxmlLoader.load();
            
            // Simple direct switch - no transition when going back to main menu
            Scene scene = new Scene(newRoot, 650, 600);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void newGame(ActionEvent actionEvent) {
        timeLine.stop();
        gameOverPanel.setVisible(false);
        brickPanel.setVisible(true); // Make brick panel visible again for new game
        if (ghostPanel != null) {
            ghostPanel.setVisible(true); // Make ghost panel visible again for new game
        }
        
        // Update inventory container visibility based on current game mode
        // CRITICAL: Only show inventory in POWERUPS mode - hide entire container in other modes
        if (inventoryContainer != null) {
            inventoryContainer.setVisible(GameSettings.getSelectedGameMode() == GameMode.POWERUPS);
        }
        
        // Update corruption timer visibility based on current game mode
        if (corruptionTimerContainer != null) {
            corruptionTimerContainer.setVisible(GameSettings.getSelectedGameMode() == GameMode.POWERUPS);
        }
        
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
        
        // Re-enable pause button for new game
        if (pauseButton != null) {
            pauseButton.setDisable(false);
            pauseButton.setOpacity(1.0);
            pauseButton.setText("PAUSE");
        }
    }

    public void pauseGame(ActionEvent actionEvent) {
        gamePanel.requestFocus();
    }

    @FXML
    private void onPause(ActionEvent event) {
        // Toggle pause state
        if (isPause.getValue() == Boolean.FALSE) {
            isPause.setValue(Boolean.TRUE);
            if (timeLine != null) {
                timeLine.pause();
            }
            // Pause corruption loop if game controller is available
            if (gameController != null) {
                gameController.pauseCorruptionLoop();
            }
            pauseButton.setText("RESUME");
            
            // Show pause menu
            if (pauseMenuGroup != null && pauseMenuImage != null) {
                pauseMenuGroup.setVisible(true);
                pauseMenuGroup.toFront();
                
                // Center the pause menu image
                double windowWidth = 650;
                double windowHeight = 600;
                double imageWidth = pauseMenuImage.getFitWidth();
                double imageHeight = pauseMenuImage.getImage() != null ? 
                    (pauseMenuImage.getImage().getHeight() * imageWidth / pauseMenuImage.getImage().getWidth()) : 300;
                
                double imageX = (windowWidth - imageWidth) / 2;
                double imageY = (windowHeight - imageHeight) / 2;
                pauseMenuImage.setLayoutX(imageX);
                pauseMenuImage.setLayoutY(imageY);
                
                // Position buttons over the pause menu image (shifted up and left)
                if (pauseMenuButtons != null) {
                    pauseMenuButtons.setManaged(false);
                    // Position buttons shifted right and down to match the panel
                    pauseMenuButtons.setLayoutX((windowWidth - 180) / 2 - 20); // Shift right (was -50, now -20)
                    pauseMenuButtons.setLayoutY(imageY + imageHeight * 0.35); // Shift down more (was 0.25, now 0.35)
                }
            }
            
            // Game paused
        } else {
            isPause.setValue(Boolean.FALSE);
            if (timeLine != null) {
                timeLine.play();
            }
            // Resume corruption loop if game controller is available
            if (gameController != null) {
                gameController.resumeCorruptionLoop();
            }
            pauseButton.setText("PAUSE");
            
            // Hide pause menu
            if (pauseMenuGroup != null) {
                pauseMenuGroup.setVisible(false);
            }
            
            // Game resumed
        }
        gamePanel.requestFocus();
    }

    @FXML
    private void onResume(ActionEvent event) {
        // Resume the game (same as clicking pause button again)
        if (isPause.getValue() == Boolean.TRUE) {
            isPause.setValue(Boolean.FALSE);
            if (timeLine != null) {
                timeLine.play();
            }
            pauseButton.setText("PAUSE");
            if (pauseMenuGroup != null) {
                pauseMenuGroup.setVisible(false);
            }
        }
        gamePanel.requestFocus();
    }

    @FXML
    private void onPauseSettings(ActionEvent event) {
        // Don't stop music - just navigate to Settings
        // Music will continue playing in the background
        try {
            // Get the current game scene before navigating away
            Node source = (Node) event.getSource();
            Scene gameScene = source.getScene();
            
            // Load Settings.fxml
            URL location = getClass().getClassLoader().getResource("com/comp2042/view/Settings.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location);
            Parent root = fxmlLoader.load();
            
            // Get the current stage from the scene
            Stage stage = (Stage) gameScene.getWindow();
            
            // Create new scene and set it on the stage
            Scene scene = new Scene(root, 650, 600);
            
            // Store reference to game scene so Settings knows to return here
            scene.getProperties().put("returnToGame", true);
            scene.getProperties().put("gameScene", gameScene);
            
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onPauseQuit(ActionEvent event) {
        try {
            // Stop the game timeline
            if (timeLine != null) {
                timeLine.stop();
            }
            
            // Stop background music
            if (gameController != null) {
                gameController.stopBackgroundMusic();
            }
            
            // Load MainMenu.fxml
            URL location = getClass().getClassLoader().getResource("com/comp2042/view/MainMenu.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location);
            Parent root = fxmlLoader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create new scene and set it on the stage
            Scene scene = new Scene(root, 650, 600);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays a board shake animation when a piece lands to create a physical impact effect.
     */
    public void playLandAnimation() {
        if (gameBoard != null) {
            TranslateTransition shake = new TranslateTransition(Duration.millis(50), gameBoard);
            shake.setByY(3); // Move down by 3 pixels
            shake.setAutoReverse(true); // Move back up
            shake.setCycleCount(2); // Down then Up (2 cycles)
            shake.play();
        }
    }

    /**
     * Hides the falling brick and ghost panels (used before line clear animation).
     */
    public void hideFallingPieces() {
        if (brickPanel != null) {
            brickPanel.setVisible(false);
        }
        if (ghostPanel != null) {
            ghostPanel.setVisible(false);
        }
    }

    /**
     * Shows the falling brick and ghost panels (used after line clear animation).
     */
    public void showFallingPieces() {
        if (brickPanel != null) {
            brickPanel.setVisible(true);
        }
        if (ghostPanel != null) {
            ghostPanel.setVisible(true);
        }
    }

    /**
     * Animates the line clear sequence: fade out cleared rows, then slide down blocks above.
     * 
     * @param clearedRows List of row indices that were cleared (board matrix indices)
     * @param onFinished Callback to execute when animation completes
     */
    public void animateLineClear(List<Integer> clearedRows, Runnable onFinished) {
        // Prevent multiple simultaneous animations
        if (isAnimating) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }
        
        // Pause the automatic timeline during animation to prevent interference
        if (timeLine != null) {
            timeLine.pause();
        }
        
        if (clearedRows == null || clearedRows.isEmpty()) {
            // Resume timeline if no animation needed
            if (timeLine != null && isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                timeLine.play();
            }
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }
        
        isAnimating = true;

        // Get board dimensions
        int boardHeight = displayMatrix.length;
        int boardWidth = displayMatrix[0].length;
        
        // Find the lowest cleared row index
        int lowestClearedRow = clearedRows.stream().mapToInt(Integer::intValue).max().orElse(-1);
        if (lowestClearedRow < 0) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        // Step 1: Vanish - Fade out all blocks in cleared rows
        ParallelTransition vanishTransition = new ParallelTransition();
        for (Integer rowIndex : clearedRows) {
            if (rowIndex >= 2 && rowIndex < boardHeight) { // Only animate visible rows (starting from index 2)
                for (int col = 0; col < boardWidth; col++) {
                    Rectangle rectangle = displayMatrix[rowIndex][col];
                    if (rectangle != null) {
                        FadeTransition fade = new FadeTransition(Duration.millis(300), rectangle);
                        fade.setFromValue(1.0);
                        fade.setToValue(0.0);
                        vanishTransition.getChildren().add(fade);
                    }
                }
            }
        }

        // Step 2: Gravity - Slide down blocks above cleared rows
        ParallelTransition gravityTransition = new ParallelTransition();
        for (int i = 2; i < boardHeight; i++) {
            // Only animate rows that are above the lowest cleared row and not themselves cleared
            if (i < lowestClearedRow && !clearedRows.contains(i)) {
                // Calculate how many cleared rows are below this row
                int shiftCount = 0;
                for (Integer clearedRow : clearedRows) {
                    if (clearedRow > i) {
                        shiftCount++;
                    }
                }

                if (shiftCount > 0) {
                    // Animate all blocks in this row down
                    for (int col = 0; col < boardWidth; col++) {
                        Rectangle rectangle = displayMatrix[i][col];
                        if (rectangle != null) {
                            TranslateTransition translate = new TranslateTransition(Duration.millis(300), rectangle);
                            translate.setByY(shiftCount * (BRICK_SIZE + 1)); // +1 for grid gap
                            gravityTransition.getChildren().add(translate);
                        }
                    }
                }
            }
        }

        // Create sequential transition: vanish first, then gravity
        SequentialTransition sequence = new SequentialTransition();
        sequence.getChildren().addAll(vanishTransition, gravityTransition);
        
        // Execute callback when animation completes (on JavaFX application thread)
        sequence.setOnFinished(e -> {
            isAnimating = false;
            if (onFinished != null) {
                javafx.application.Platform.runLater(() -> {
                    onFinished.run();
                    // Resume the automatic timeline after animation completes
                    if (timeLine != null && isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                        timeLine.play();
                    }
                });
            } else {
                // Resume timeline even if no callback
                if (timeLine != null && isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                    timeLine.play();
                }
            }
        });

        // Play the animation
        sequence.play();
    }
    
    /**
     * Shows the leaderboard overlay with top 5 scores.
     */
    public void showLeaderboard() {
        if (leaderboardPanel != null) {
            leaderboardPanel.showWithAnimation();
        }
    }
    
    /**
     * Sets mirror mode on or off. When active, flips the gameboard vertically
     * so bricks appear to fall from bottom to top (anti-gravity).
     * Also enables neon particle trails rising upward along the board edges.
     * 
     * @param active true to enable mirror mode, false to disable
     */
    public void setMirrorMode(boolean active) {
        isMirrorMode = active;
        if (active) {
            // Flip gameboard vertically only (Y-axis flip for anti-gravity)
            if (gamePanel != null) {
                gamePanel.setScaleX(1.0);  // Keep X normal
                gamePanel.setScaleY(-1.0); // Flip Y vertically
            }
            if (ghostPanel != null) {
                ghostPanel.setScaleX(1.0);
                ghostPanel.setScaleY(-1.0);
            }
            if (brickPanel != null) {
                brickPanel.setScaleX(1.0);
                brickPanel.setScaleY(-1.0);
            }
            
            // Enable particle trails
            startParticleTrails();
        } else {
            // Reset to normal
            if (gamePanel != null) {
                gamePanel.setScaleX(1.0);
                gamePanel.setScaleY(1.0);
            }
            if (ghostPanel != null) {
                ghostPanel.setScaleX(1.0);
                ghostPanel.setScaleY(1.0);
            }
            if (brickPanel != null) {
                brickPanel.setScaleX(1.0);
                brickPanel.setScaleY(1.0);
            }
            
            // Disable particle trails
            stopParticleTrails();
        }
    }
    
    /**
     * Starts the neon particle trail animation for Mirror Mode.
     * Particles rise upward along the left and right edges of the board.
     */
    private void startParticleTrails() {
        if (particleContainer == null || gameBoard == null) {
            return;
        }
        
        // Make particle container visible and position it
        particleContainer.setVisible(true);
        particleContainer.setLayoutX(0);
        particleContainer.setLayoutY(0);
        particleContainer.setPrefSize(BOARD_WIDTH_PX, BOARD_HEIGHT_PX);
        
        // Ensure particle container is on top
        particleContainer.toFront();
        
        // Create timeline to spawn particles continuously
        particleTimeline = new Timeline(new KeyFrame(
            Duration.millis(50), // Spawn a particle every 50ms (much more frequent)
            e -> spawnParticle()
        ));
        particleTimeline.setCycleCount(Timeline.INDEFINITE);
        particleTimeline.play();
    }
    
    /**
     * Stops the particle trail animation and clears all particles.
     */
    private void stopParticleTrails() {
        if (particleTimeline != null) {
            particleTimeline.stop();
            particleTimeline = null;
        }
        
        if (particleContainer != null) {
            particleContainer.getChildren().clear();
            particleContainer.setVisible(false);
        }
        
        activeParticles.clear();
    }
    
    /**
     * Spawns a single neon particle at the bottom edge (left or right side).
     * The particle will rise upward and fade out.
     */
    private void spawnParticle() {
        if (particleContainer == null || gameBoard == null) {
            return;
        }
        
        // Spawn particles on both edges more frequently
        // Randomly choose left or right edge, with chance to spawn on both
        boolean spawnLeft = Math.random() < 0.6; // 60% chance for left
        boolean spawnRight = Math.random() < 0.6; // 60% chance for right (can spawn both)
        
        // Particle size (small neon dots)
        double particleSize = 2 + Math.random() * 3; // 2-5 pixels (slightly larger)
        
        // Random neon color (cyan, blue, or purple)
        Color[] neonColors = {
            Color.CYAN,
            Color.web("#00ffff"), // Bright cyan
            Color.web("#0080ff"), // Bright blue
            Color.web("#8000ff"), // Bright purple
            Color.web("#ff00ff")  // Magenta
        };
        Color particleColor = neonColors[(int)(Math.random() * neonColors.length)];
        
        // Spawn on left edge if selected
        if (spawnLeft) {
            spawnParticleAtPosition(0 + Math.random() * 8, particleSize, particleColor);
        }
        
        // Spawn on right edge if selected
        if (spawnRight) {
            spawnParticleAtPosition(BOARD_WIDTH_PX - 8 - Math.random() * 8, particleSize, particleColor);
        }
    }
    
    /**
     * Helper method to spawn a particle at a specific X position.
     */
    private void spawnParticleAtPosition(double xPos, double particleSize, Color particleColor) {
        // Create particle rectangle
        Rectangle particle = new Rectangle(particleSize, particleSize);
        particle.setFill(particleColor);
        
        // Add glow effect
        Glow glow = new Glow(0.8);
        particle.setEffect(glow);
        
        // Position at bottom of board
        // In Mirror Mode, the board is flipped, so visual bottom is at y=0
        double yPos = 0; // Start at visual bottom (which is top after flip)
        
        particle.setLayoutX(xPos);
        particle.setLayoutY(yPos);
        
        // Add to container
        particleContainer.getChildren().add(particle);
        activeParticles.add(particle);
        
        // Animate particle rising upward (in Mirror Mode, up means negative Y)
        TranslateTransition translate = new TranslateTransition(Duration.millis(2000 + Math.random() * 1000), particle);
        translate.setByY(-BOARD_HEIGHT_PX - 50); // Move upward beyond the board
        translate.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        FadeTransition fade = new FadeTransition(Duration.millis(2000 + Math.random() * 1000), particle);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        ParallelTransition animation = new ParallelTransition(translate, fade);
        animation.setOnFinished(e -> {
            particleContainer.getChildren().remove(particle);
            activeParticles.remove(particle);
        });
        animation.play();
    }
    
    /**
     * Sets the freeze visual effect on or off with immersive winter storm atmosphere.
     * 
     * @param active true to enable freeze effects, false to disable
     */
    public void setFreezeEffect(boolean active) {
        isFrozen = active;
        
        // Ensure we have scene reference
        if (gameScene == null && gamePanel != null && gamePanel.getScene() != null) {
            gameScene = gamePanel.getScene();
        }
        
        if (active) {
            // Activate freeze effects
            if (frostOverlay != null) {
                frostOverlay.setVisible(true);
                
                // Set frost vignette with RadialGradient (transparent center, icy edges)
                if (frostVignette != null) {
                    RadialGradient vignetteGradient = new RadialGradient(
                        0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.TRANSPARENT),
                        new Stop(0.5, Color.rgb(200, 240, 255, 0.2)),
                        new Stop(1.0, Color.rgb(255, 255, 255, 0.4))
                    );
                    frostVignette.setFill(vignetteGradient);
                }
                
                // Smooth fade-in transition
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), frostOverlay);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
            
            // Start snow storm AnimationTimer
            startSnowStorm();
            
            // Apply CSS class to gameBoard
            if (gameBoard != null) {
                gameBoard.getStyleClass().add("frozen-theme");
            }
            
            // Update colors of existing rectangles without changing shape
            updateBrickColors();
            if (displayMatrix != null && gameController != null) {
                com.comp2042.model.Board currentBoard = ((com.comp2042.controller.GameController) gameController).getBoard();
                if (currentBoard != null) {
                    refreshGameBackground(currentBoard.getBoardMatrix());
                }
            }
        } else {
            // Deactivate freeze effects
            if (frostOverlay != null) {
                // Smooth fade-out transition
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), frostOverlay);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    frostOverlay.setVisible(false);
                    // Clear all particles after fade
                    if (frostOverlay != null) {
                        frostOverlay.getChildren().clear();
                    }
                });
                fadeOut.play();
            }
            
            // Stop snow storm
            stopSnowStorm();
            
            // Remove CSS class
            if (gameBoard != null) {
                gameBoard.getStyleClass().remove("frozen-theme");
            }
            
            // Update colors of existing rectangles without changing shape (uses lastViewData which includes any rotations during freeze)
            updateBrickColors();
            if (displayMatrix != null && gameController != null) {
                com.comp2042.model.Board currentBoard = ((com.comp2042.controller.GameController) gameController).getBoard();
                if (currentBoard != null) {
                    refreshGameBackground(currentBoard.getBoardMatrix());
                }
            }
        }
    }
    
    /**
     * Starts the snow storm AnimationTimer for immersive particle effects.
     */
    private void startSnowStorm() {
        if (snowStormTimer != null) {
            stopSnowStorm();
        }
        
        snowStormTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Spawn 1-2 new particles per frame
                int spawnCount = (Math.random() < 0.5) ? 1 : 2;
                for (int i = 0; i < spawnCount; i++) {
                    spawnSnowParticle();
                }
                
                // Update existing particles
                updateSnowParticles();
            }
        };
        snowStormTimer.start();
    }
    
    /**
     * Stops the snow storm AnimationTimer and clears particles.
     */
    private void stopSnowStorm() {
        if (snowStormTimer != null) {
            snowStormTimer.stop();
            snowStormTimer = null;
        }
        
        // Clear all snow particles
        if (frostOverlay != null) {
            for (Circle particle : snowParticles) {
                frostOverlay.getChildren().remove(particle);
            }
        }
        snowParticles.clear();
    }
    
    /**
     * Spawns a single snow particle at the top of the screen.
     */
    private void spawnSnowParticle() {
        if (frostOverlay == null || gameScene == null) {
            return;
        }
        
        // Get screen dimensions
        double screenWidth = gameScene.getWidth();
        double screenHeight = gameScene.getHeight();
        
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        
        // Create snow particle (Circle)
        double radius = 1.0 + Math.random() * 2.0; // 1-3px
        Circle particle = new Circle(radius);
        
        // Random opacity between 0.4 and 0.9
        double opacity = 0.4 + Math.random() * 0.5;
        particle.setFill(Color.rgb(255, 255, 255, opacity));
        
        // Position at top of screen (Y = -10) and random X
        particle.setLayoutX(Math.random() * screenWidth);
        particle.setLayoutY(-10);
        
        // Store initial properties for animation
        particle.setUserData(new double[]{2.0 + Math.random() * 3.0, Math.random() * 0.1 - 0.05}); // [speed, wind]
        
        // Add to overlay and tracking list
        frostOverlay.getChildren().add(particle);
        snowParticles.add(particle);
    }
    
    /**
     * Updates all snow particles: moves them down and applies wind/sway.
     */
    private void updateSnowParticles() {
        if (gameScene == null || frostOverlay == null) {
            return;
        }
        
        double screenHeight = gameScene.getHeight();
        java.util.Iterator<Circle> iterator = snowParticles.iterator();
        
        while (iterator.hasNext()) {
            Circle particle = iterator.next();
            
            // Get particle data
            double[] data = (double[]) particle.getUserData();
            if (data == null || data.length < 2) {
                iterator.remove();
                frostOverlay.getChildren().remove(particle);
                continue;
            }
            
            double speed = data[0];
            double wind = data[1];
            
            // Move particle down
            double currentY = particle.getLayoutY();
            double newY = currentY + speed;
            
            // Apply wind/sway using sine wave
            double currentX = particle.getLayoutX();
            double time = System.currentTimeMillis() / 1000.0; // Time in seconds
            double sway = Math.sin(time * 2.0 + currentX * 0.01) * wind;
            double newX = currentX + sway;
            
            particle.setLayoutX(newX);
            particle.setLayoutY(newY);
            
            // Remove if particle is below screen
            if (newY > screenHeight) {
                iterator.remove();
                frostOverlay.getChildren().remove(particle);
            }
        }
    }
    
    /**
     * Starts drill visual effects (screen shake, particle timer).
     */
    private void startDrillEffects() {
        // Start particle update timer if not already running
        if (drillParticleTimer == null) {
            drillParticleTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    updateDrillParticles();
                }
            };
            drillParticleTimer.start();
        }
    }
    
    /**
     * Stops drill visual effects (screen shake, particle timer).
     */
    private void stopDrillEffects() {
        // Stop particle timer
        if (drillParticleTimer != null) {
            drillParticleTimer.stop();
            drillParticleTimer = null;
        }
        
        // Reset screen shake
        if (gameBoard != null) {
            gameBoard.setTranslateX(0);
            gameBoard.setTranslateY(0);
        }
        
        // Clear all particles
        if (gamePanel != null && gamePanel.getParent() != null) {
            for (Rectangle particle : new ArrayList<>(drillParticles)) {
                if (particle.getParent() != null) {
                    javafx.scene.Parent parent = (javafx.scene.Parent) particle.getParent();
                    if (parent instanceof javafx.scene.layout.Pane) {
                        ((javafx.scene.layout.Pane) parent).getChildren().remove(particle);
                    } else if (parent instanceof javafx.scene.Group) {
                        ((javafx.scene.Group) parent).getChildren().remove(particle);
                    }
                }
            }
        }
        drillParticles.clear();
    }
    
    /**
     * Animates block destruction with debris particles flying off.
     * @param row Board row index (0-24)
     * @param col Board column index (0-9)
     * @param blockColor The color ID of the destroyed block
     */
    private void animateBlockDestruction(int row, int col, int blockColor) {
        // Calculate screen position of the destroyed block using cached coordinates
        if (!coordinatesCached || gamePanel == null || gameBoard == null) {
            return;
        }
        
        // Calculate block center position in screen coordinates
        // Row 2 is the first visible row, so row-2 gives us the visual row index
        double blockCenterX = cachedBoardOffsetX + (col * cachedCellWidth) + (BRICK_SIZE / 2.0);
        double blockCenterY = cachedBoardOffsetY + ((row - 2) * cachedCellHeight) + (BRICK_SIZE / 2.0);
        
        // Get block color for debris
        Paint blockPaint = getFillColor(blockColor);
        Color debrisColor = blockPaint instanceof Color ? (Color) blockPaint : Color.GREY;
        
        // Reduced particle count for better performance
        int particleCount = 6 + (int)(Math.random() * 3); // 6-8 particles instead of 12-16
        
        // Use gameBoard as the container (it's visible and properly positioned)
        for (int i = 0; i < particleCount; i++) {
            // Create debris particle (larger and more visible)
            double particleSize = 5 + Math.random() * 4; // 5-9px (much larger)
            Rectangle particle = new Rectangle(particleSize, particleSize);
            
            // Use block color with some variation
            double brightness = 0.8 + Math.random() * 0.2; // 0.8-1.0 (brighter)
            Color particleColor = Color.color(
                Math.min(1.0, debrisColor.getRed() * brightness),
                Math.min(1.0, debrisColor.getGreen() * brightness),
                Math.min(1.0, debrisColor.getBlue() * brightness),
                1.0
            );
            particle.setFill(particleColor);
            particle.setStroke(Color.WHITE);
            particle.setStrokeWidth(0.5);
            
            // Set initial position at block center (relative to gameBoard)
            // Convert to gameBoard's coordinate system
            javafx.geometry.Point2D gameBoardScenePos = gameBoard.localToScene(0, 0);
            javafx.geometry.Point2D blockScenePos = gamePanel.localToScene(blockCenterX - cachedBoardOffsetX, blockCenterY - cachedBoardOffsetY);
            
            particle.setLayoutX(blockScenePos.getX() - gameBoardScenePos.getX());
            particle.setLayoutY(blockScenePos.getY() - gameBoardScenePos.getY());
            
            // INTENSE DEBRIS: Strong outward velocity
            double angle = Math.random() * Math.PI * 2; // Random direction
            double speed = 4 + Math.random() * 5; // 4-9 pixels per frame (faster)
            double deltaX = Math.cos(angle) * speed;
            double deltaY = Math.sin(angle) * speed - 3; // Stronger upward bias
            
            particle.setUserData(new double[]{deltaX, deltaY, 0.15}); // velocity + stronger gravity
            
            // Add to gameBoard (it's a BorderPane, so we need to add to its center or create an overlay)
            // Actually, let's add to the scene root but use absolute positioning
            javafx.scene.Parent root = gamePanel.getScene() != null ? gamePanel.getScene().getRoot() : null;
            if (root != null) {
                // Use absolute scene coordinates
                particle.setLayoutX(blockScenePos.getX());
                particle.setLayoutY(blockScenePos.getY());
                particle.setManaged(false); // Don't let layout manager interfere
                
                if (root instanceof javafx.scene.layout.Pane) {
                    ((javafx.scene.layout.Pane) root).getChildren().add(particle);
                } else if (root instanceof javafx.scene.Group) {
                    ((javafx.scene.Group) root).getChildren().add(particle);
                } else if (root instanceof javafx.scene.layout.StackPane) {
                    ((javafx.scene.layout.StackPane) root).getChildren().add(particle);
                }
                
                // Bring particle to front
                particle.toFront();
            }
            
            drillParticles.add(particle);
            
            // Fade out and remove after 1.5 seconds (longer visibility)
            FadeTransition fade = new FadeTransition(Duration.millis(1500), particle);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                drillParticles.remove(particle);
                if (particle.getParent() != null) {
                    javafx.scene.Parent parent = (javafx.scene.Parent) particle.getParent();
                    if (parent instanceof javafx.scene.layout.Pane) {
                        ((javafx.scene.layout.Pane) parent).getChildren().remove(particle);
                    } else if (parent instanceof javafx.scene.Group) {
                        ((javafx.scene.Group) parent).getChildren().remove(particle);
                    } else if (parent instanceof javafx.scene.layout.StackPane) {
                        ((javafx.scene.layout.StackPane) parent).getChildren().remove(particle);
                    }
                }
            });
            fade.play();
        }
        
        // INTENSE SCREEN SHAKE on block destruction
        if (gameBoard != null) {
            double shakeX = (Math.random() - 0.5) * 8; // -4 to +4 (stronger shake)
            double shakeY = (Math.random() - 0.5) * 6; // -3 to +3
            gameBoard.setTranslateX(shakeX);
            gameBoard.setTranslateY(shakeY);
            
            // Reset shake after a short delay
            Timeline shakeReset = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                if (gameBoard != null) {
                    gameBoard.setTranslateX(0);
                    gameBoard.setTranslateY(0);
                }
            }));
            shakeReset.setCycleCount(1);
            shakeReset.play();
        }
    }
    
    /**
     * Spawns continuous sparks from the drill tip (smaller, less intense than destruction debris).
     * @param x Screen X coordinate of the drill center
     * @param y Screen Y coordinate of the drill center
     */
    private void spawnDrillSparks(double x, double y) {
        // Spawn 2-3 small sparks per call (more visible)
        int particleCount = 2 + (int)(Math.random() * 2); // 2 or 3 particles
        
        javafx.scene.Parent root = gamePanel != null && gamePanel.getScene() != null ? 
            gamePanel.getScene().getRoot() : null;
        if (root == null) {
            return;
        }
        
        for (int i = 0; i < particleCount; i++) {
            // Create spark particle (larger for visibility)
            double sparkSize = 3 + Math.random() * 2; // 3-5px
            Rectangle particle = new Rectangle(sparkSize, sparkSize);
            
            // Random color: DARKGREY, GREY, or ORANGE (sparks)
            double colorRand = Math.random();
            if (colorRand < 0.3) {
                particle.setFill(Color.DARKGREY);
            } else if (colorRand < 0.7) {
                particle.setFill(Color.GREY);
            } else {
                particle.setFill(Color.ORANGE); // Bright sparks
            }
            
            // Set initial position at drill center with small random offset
            double offsetX = (Math.random() - 0.5) * 6; // -3 to +3
            double offsetY = (Math.random() - 0.5) * 6;
            particle.setLayoutX(x + offsetX);
            particle.setLayoutY(y + offsetY);
            particle.setManaged(false); // Don't let layout manager interfere
            
            // Store velocity in userData for update
            double deltaX = (Math.random() - 0.5) * 4; // -2 to +2
            double deltaY = -2 - (Math.random() * 3); // -2 to -5 (upwards)
            particle.setUserData(new double[]{deltaX, deltaY, 0.08}); // velocity + gravity
            
            // Add to scene root
            if (root instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane) root).getChildren().add(particle);
            } else if (root instanceof javafx.scene.Group) {
                ((javafx.scene.Group) root).getChildren().add(particle);
            } else if (root instanceof javafx.scene.layout.StackPane) {
                ((javafx.scene.layout.StackPane) root).getChildren().add(particle);
            }
            
            // Bring to front
            particle.toFront();
            
            drillParticles.add(particle);
            
            // Fade out and remove after 0.8 seconds
            FadeTransition fade = new FadeTransition(Duration.millis(800), particle);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                drillParticles.remove(particle);
                if (particle.getParent() != null) {
                    javafx.scene.Parent parent = (javafx.scene.Parent) particle.getParent();
                    if (parent instanceof javafx.scene.layout.Pane) {
                        ((javafx.scene.layout.Pane) parent).getChildren().remove(particle);
                    } else if (parent instanceof javafx.scene.Group) {
                        ((javafx.scene.Group) parent).getChildren().remove(particle);
                    } else if (parent instanceof javafx.scene.layout.StackPane) {
                        ((javafx.scene.layout.StackPane) parent).getChildren().remove(particle);
                    }
                }
            });
            fade.play();
        }
        
        // CONTINUOUS SCREEN SHAKE: Subtle vibration while drilling
        if (gameBoard != null && Math.random() < 0.6) { // 60% chance per frame
            double shakeX = (Math.random() - 0.5) * 2; // -1 to +1 (subtle continuous shake)
            gameBoard.setTranslateX(shakeX);
        }
    }
    
    /**
     * Updates drill particle positions.
     */
    private void updateDrillParticles() {
        java.util.Iterator<Rectangle> iterator = drillParticles.iterator();
        while (iterator.hasNext()) {
            Rectangle particle = iterator.next();
            if (particle.getUserData() instanceof double[]) {
                double[] velocity = (double[]) particle.getUserData();
                double currentX = particle.getLayoutX();
                double currentY = particle.getLayoutY();
                
                // Update position
                particle.setLayoutX(currentX + velocity[0]);
                particle.setLayoutY(currentY + velocity[1]);
                
                // Apply gravity (if present in userData)
                if (velocity.length > 2) {
                    velocity[1] += velocity[2]; // Use stored gravity value
                } else {
                    velocity[1] += 0.1; // Default gravity for old particles
                }
                
                // Remove if off screen (wider bounds)
                if (currentY > 1200 || currentY < -200 || currentX < -200 || currentX > 1200) {
                    iterator.remove();
                    if (particle.getParent() != null) {
                        javafx.scene.Parent parent = (javafx.scene.Parent) particle.getParent();
                        if (parent instanceof javafx.scene.layout.Pane) {
                            ((javafx.scene.layout.Pane) parent).getChildren().remove(particle);
                        } else if (parent instanceof javafx.scene.Group) {
                            ((javafx.scene.Group) parent).getChildren().remove(particle);
                        } else if (parent instanceof javafx.scene.layout.StackPane) {
                            ((javafx.scene.layout.StackPane) parent).getChildren().remove(particle);
                        }
                    }
                }
            }
        }
    }
}
