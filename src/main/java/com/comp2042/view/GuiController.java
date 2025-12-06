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
import javafx.stage.Stage;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private Button howToPlayPauseButton;

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
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
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
                rectangle.setFill(getFillColor(brickData[i][j]));
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
        
        // Refresh inventory with initial data
        refreshInventory(brick.getInventory());
        
        // Store initial ViewData for color updates during freeze
        lastViewData = brick;
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
            
            // Update brick rectangles
            for (int i = 0; i < brickData.length; i++) {
                for (int j = 0; j < brickData[i].length; j++) {
                    // Check if frozen and use glacier colors
                    if (isFrozen) {
                        rectangles[i][j].setFill(getGlacierColor(brickData[i][j]));
                    } else {
                        rectangles[i][j].setFill(getFillColor(brickData[i][j]));
                    }
                    rectangles[i][j].setArcHeight(9);
                    rectangles[i][j].setArcWidth(9);
                }
            }
            
            // Update ghost rectangles to match brick shape with outline only
            if (GameSettings.isGhostModeEnabled()) {
                if (ghostRectangles != null) {
                    for (int i = 0; i < brickData.length; i++) {
                        for (int j = 0; j < brickData[i].length; j++) {
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
                        }
                    }
                }
            } else {
                // If Ghost Mode is OFF, ensure all ghost rectangles are hidden
                if (ghostRectangles != null) {
                    for (Rectangle[] row : ghostRectangles) {
                        for (Rectangle rect : row) {
                            rect.setVisible(false);
                        }
                    }
                }
            }
            
            // Update next brick preview
            refreshNextBrick(brick.getNextBricks());
            
            // Update inventory
            refreshInventory(brick.getInventory());
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
        
        // Clear existing items
        inventoryPanel.getChildren().clear();
        
        // Loop through the inventory list
        for (com.comp2042.model.PowerUp item : inventory) {
            if (item == com.comp2042.model.PowerUp.FREEZE) {
                // Use snowflake image for FREEZE power-up
                try {
                    URL snowflakeUrl = getClass().getClassLoader().getResource("snowflake logo.png");
                    if (snowflakeUrl != null) {
                        ImageView snowflakeImage = new ImageView(new Image(snowflakeUrl.toExternalForm()));
                        snowflakeImage.setFitWidth(20);
                        snowflakeImage.setFitHeight(20);
                        snowflakeImage.setPreserveRatio(true);
                        snowflakeImage.setSmooth(true);
                        inventoryPanel.getChildren().add(snowflakeImage);
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
                        if (brickData[row][col] > 0) {
                            // Check if frozen and use glacier colors
                            if (isFrozen) {
                                grid[row][col].setFill(getGlacierColor(brickData[row][col]));
                            } else {
                                grid[row][col].setFill(getFillColor(brickData[row][col]));
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
        // Check if frozen and use glacier colors
        if (isFrozen) {
            rectangle.setFill(getGlacierColor(color));
        } else {
            rectangle.setFill(getFillColor(color));
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
                    // Check if frozen and use glacier colors
                    if (isFrozen) {
                        rectangles[i][j].setFill(getGlacierColor(brickData[i][j]));
                    } else {
                        rectangles[i][j].setFill(getFillColor(brickData[i][j]));
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
                                if (nextBrickData[row][col] > 0) {
                                    // Check if frozen and use glacier colors
                                    if (isFrozen) {
                                        grid[row][col].setFill(getGlacierColor(nextBrickData[row][col]));
                                    } else {
                                        grid[row][col].setFill(getFillColor(nextBrickData[row][col]));
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
            
            System.out.println("Game paused");
        } else {
            isPause.setValue(Boolean.FALSE);
            if (timeLine != null) {
                timeLine.play();
            }
            pauseButton.setText("PAUSE");
            
            // Hide pause menu
            if (pauseMenuGroup != null) {
                pauseMenuGroup.setVisible(false);
            }
            
            System.out.println("Game resumed");
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
    private void onPauseHowToPlay(ActionEvent event) {
        System.out.println("How to Play feature coming soon");
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
}
