package com.comp2042.view;

import com.comp2042.controller.InputEventListener;
import com.comp2042.events.EventSource;
import com.comp2042.events.EventType;
import com.comp2042.events.MoveEvent;
import com.comp2042.model.DownData;
import com.comp2042.model.ViewData;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
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
    private GridPane brickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

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

    private Rectangle[][] rectangles;

    private Timeline timeLine;
    
    private IntegerProperty scoreProperty;

    private final BooleanProperty isPause = new SimpleBooleanProperty();

    private final BooleanProperty isGameOver = new SimpleBooleanProperty();

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
        
        // Critical: Disable layout management for brickPanel so manual positioning works
        brickPanel.setManaged(false);
        brickPanel.toFront();
        
        // Disable layout management for overlays to prevent layout shifts
        groupNotification.setManaged(false);
        groupNotification.toFront();
        gameOverPanel.setManaged(false);
        gameOverPanel.toFront();
        
        // Setup game over panel size and position
        gameOverPanel.setPrefSize(650, 600);
        gameOverPanel.setLayoutX(0);
        gameOverPanel.setLayoutY(0);
        
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
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
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
            brickPanel.setLayoutX(boardOffsetX + (brick.getxPosition() * cellWidth));
            brickPanel.setLayoutY(boardOffsetY + ((brick.getyPosition() - 2) * cellHeight));
        }
        // If scene not attached yet, refreshBrick will handle positioning on first update


        timeLine = new Timeline(new KeyFrame(
                Duration.millis(400),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
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

    private void refreshBrick(ViewData brick) {
        if (isPause.getValue() == Boolean.FALSE) {
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
                brickPanel.setLayoutX(boardOffsetX + (brick.getxPosition() * cellWidth));
                brickPanel.setLayoutY(boardOffsetY + ((brick.getyPosition() - 2) * cellHeight));
            }
            
            for (int i = 0; i < brick.getBrickData().length; i++) {
                for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                    setRectangleData(brick.getBrickData()[i][j], rectangles[i][j]);
                }
            }
        }
    }

    public void refreshGameBackground(int[][] board) {
        for (int i = 2; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(getFillColor(color));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    private void moveDown(MoveEvent event) {
        if (isPause.getValue() == Boolean.FALSE) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
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
    }

    public void bindScore(IntegerProperty integerProperty) {
        if (scoreLabel != null && integerProperty != null) {
            scoreLabel.textProperty().bind(integerProperty.asString());
            scoreProperty = integerProperty;
        }
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
        // Hide the falling brick when game is over to prevent glitch
        brickPanel.setVisible(false);
    }
    
    private void navigateToMainMenu(ActionEvent event) {
        try {
            // Stop the game timeline
            if (timeLine != null) {
                timeLine.stop();
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
                
                // Position buttons over the pause menu image (centered, slightly below image center)
                if (pauseMenuButtons != null) {
                    pauseMenuButtons.setManaged(false);
                    // Position buttons in the center, slightly offset down from image center
                    pauseMenuButtons.setLayoutX((windowWidth - 180) / 2); // 180 is min-width of buttons
                    pauseMenuButtons.setLayoutY(imageY + imageHeight * 0.4); // Position buttons in lower part of image
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
        System.out.println("Settings feature coming soon");
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
}
