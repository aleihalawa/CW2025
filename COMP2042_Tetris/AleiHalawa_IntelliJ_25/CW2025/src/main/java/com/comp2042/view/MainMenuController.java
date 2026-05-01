package com.comp2042.view;

import com.comp2042.controller.GameController;
import com.comp2042.model.GameSettings;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {

    @FXML
    private MediaView backgroundVideo;
    
    @FXML
    private javafx.scene.layout.StackPane rootPane;
    
    @FXML
    private Button startButton;
    
    @FXML
    private Button settingsButton;
    
    @FXML
    private Button quitButton;
    
    @FXML
    private Label highScoreLabel;
    
    @FXML
    private NameEntryPanel nameEntryPanel;
    
    @FXML
    private com.comp2042.view.GameModeSelectionPanel gameModeSelectionPanel;
    
    @FXML
    private Button gameModeButton;
    
    private MediaPlayer mediaPlayer;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private int retryAttempts = 0;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Dispose any existing media player first (in case of scene reload)
        disposeMediaPlayer();
        
        // Ensure video is visible
        if (backgroundVideo != null) {
            backgroundVideo.setVisible(true);
            backgroundVideo.setMediaPlayer(null); // Clear any previous media player
        }
        
        // Load video - will retry on failure
        loadVideo();
        
        // Load and display high score
        loadHighScore();
        
        // Initialize name entry panel
        if (nameEntryPanel != null) {
            nameEntryPanel.setManaged(false);
            nameEntryPanel.setPrefSize(650, 600);
            nameEntryPanel.setLayoutX(0);
            nameEntryPanel.setLayoutY(0);
            nameEntryPanel.setVisible(false);
            nameEntryPanel.toFront();
            
            // Set up submit handler
            nameEntryPanel.setOnSubmit(e -> {
                String name = nameEntryPanel.getNameField().getText().trim();
                if (name.isEmpty()) {
                    name = "Player";
                }
                GameSettings.setPlayerName(name);
                startGameWithName(name);
            });
            
            // Set up close handler
            nameEntryPanel.setOnClose(e -> {
                nameEntryPanel.hideWithAnimation();
            });
            
            // Pre-fill with saved name if available
            String savedName = GameSettings.getPlayerName();
            if (savedName != null && !savedName.equals("Player")) {
                nameEntryPanel.getNameField().setText(savedName);
            }
        }
        
        // Initialize game mode selection panel
        if (gameModeSelectionPanel != null) {
            gameModeSelectionPanel.setManaged(false);
            gameModeSelectionPanel.setPrefSize(650, 600);
            gameModeSelectionPanel.setLayoutX(0);
            gameModeSelectionPanel.setLayoutY(0);
            gameModeSelectionPanel.setVisible(false);
            gameModeSelectionPanel.toFront();
            
            // Set up mode selection handler
            gameModeSelectionPanel.setOnModeSelected(selectedMode -> {
                if (selectedMode != null) {
                    // All game modes are now fully implemented
                    GameSettings.setSelectedGameMode(selectedMode);
                    // Update high score display for the selected game mode
                    loadHighScore();
                }
            });
            
            // Set up close handler
            gameModeSelectionPanel.setOnClose(e -> {
                gameModeSelectionPanel.hideWithAnimation();
            });
        }
        
        // Add hover effects to all buttons
        setupButtonHoverEffect(startButton);
        setupButtonHoverEffect(gameModeButton);
        setupButtonHoverEffect(settingsButton);
        setupButtonHoverEffect(quitButton);
    }
    
    /**
     * Loads the high score from the leaderboard and updates the label.
     */
    private void loadHighScore() {
        if (highScoreLabel != null) {
            // Load high score for the currently selected game mode
            com.comp2042.model.GameMode currentMode = com.comp2042.model.GameSettings.getSelectedGameMode();
            java.util.List<com.comp2042.model.ScoreEntry> topScores = com.comp2042.model.HighScoreManager.loadLeaderboard(currentMode);
            if (topScores.isEmpty()) {
                highScoreLabel.setText("HIGH SCORE: 0");
            } else {
                int topScore = topScores.get(0).getScore();
                highScoreLabel.setText("HIGH SCORE: " + topScore);
            }
        }
    }
    
    private void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing media player: " + e.getMessage());
            }
            mediaPlayer = null;
        }
    }
    
    private void loadVideo() {
        retryAttempts = 0;
        loadVideoWithRetry();
    }
    
    private void loadVideoWithRetry() {
        try {
            URL videoUrl = getClass().getClassLoader().getResource("Retro_Arcade_Tetris_Video_Generation.mp4");
            if (videoUrl == null) {
                System.err.println("Could not find Retro_Arcade_Tetris_Video_Generation.mp4 in resources");
                retryVideoLoad();
                return;
            }
            
            String videoPath = videoUrl.toExternalForm();
            
            Media media = new Media(videoPath);
            
            // Check for media errors during construction
            media.errorProperty().addListener((obs, oldError, newError) -> {
                if (newError != null) {
                    System.err.println("Media error during construction: " + newError.getMessage());
                    retryVideoLoad();
                }
            });
            
            mediaPlayer = new MediaPlayer(media);
            
            // Set video to loop indefinitely
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            
            // Set video to auto-play
            mediaPlayer.setAutoPlay(true);
            
            // Set mute
            mediaPlayer.setMute(true);
            
            // Set the MediaView to use the MediaPlayer
            if (backgroundVideo != null) {
                backgroundVideo.setMediaPlayer(mediaPlayer);
                
                // Center the video and preserve aspect ratio (will crop sides if needed)
                backgroundVideo.setPreserveRatio(true);
                
                // Set fit height to window height (600px), width will adjust automatically
                backgroundVideo.setFitHeight(600);
                
                // Bind to StackPane height to maintain fit
                if (rootPane != null) {
                    backgroundVideo.fitHeightProperty().bind(rootPane.heightProperty());
                }
            }
            
            // Configure when media is ready
            mediaPlayer.setOnReady(() -> {
                if (backgroundVideo != null) {
                    backgroundVideo.setPreserveRatio(true);
                    if (rootPane != null) {
                        backgroundVideo.setFitHeight(rootPane.getHeight());
                    } else {
                        backgroundVideo.setFitHeight(600);
                    }
                    backgroundVideo.setVisible(true);
                }
                retryAttempts = 0; // Reset retry counter on success
            });
            
            // Handle errors - retry instead of falling back
            mediaPlayer.setOnError(() -> {
                javafx.scene.media.MediaException error = mediaPlayer.getError();
                if (error != null) {
                    System.err.println("Error playing video: " + error.getMessage());
                    System.err.println("Error type: " + (error.getType() != null ? error.getType() : "UNKNOWN"));
                    retryVideoLoad();
                }
            });
            
            // Monitor status - only log, don't retry on status changes
            mediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                // Only retry if we get HALTED with an actual error
                if (newStatus == javafx.scene.media.MediaPlayer.Status.HALTED && 
                    mediaPlayer.getError() != null) {
                    System.err.println("MediaPlayer halted with error - retrying");
                    retryVideoLoad();
                }
            });
            
            // Start playing - use Platform.runLater to ensure scene is fully initialized
            javafx.application.Platform.runLater(() -> {
                try {
                    if (mediaPlayer != null) {
                        mediaPlayer.play();
                    }
                } catch (Exception e) {
                    System.err.println("Error starting video playback: " + e.getMessage());
                    retryVideoLoad();
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error loading background video: " + e.getMessage());
            e.printStackTrace();
            retryVideoLoad();
        }
    }
    
    private void retryVideoLoad() {
        if (retryAttempts < MAX_RETRY_ATTEMPTS) {
            retryAttempts++;
            
            // Dispose current player
            disposeMediaPlayer();
            
            // Retry after a short delay
            javafx.application.Platform.runLater(() -> {
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.millis(500));
                delay.setOnFinished(e -> loadVideoWithRetry());
                delay.play();
            });
        } else {
            System.err.println("Failed to load video after " + MAX_RETRY_ATTEMPTS + " attempts. Video will not be displayed.");
            // Keep video visible but it won't play - better than showing nothing
            if (backgroundVideo != null) {
                backgroundVideo.setVisible(true);
            }
        }
    }
    
    private void setupButtonHoverEffect(Button button) {
        Glow glow = new Glow(0.0);
        button.setEffect(glow);
        
        // Store original style
        String originalStyle = button.getStyle();
        
        // Hover in effect
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), button);
            scaleIn.setToX(1.05);
            scaleIn.setToY(1.05);
            scaleIn.play();
            
            glow.setLevel(0.8);
            button.setStyle("-fx-background-color: rgba(0, 255, 255, 0.3); " +
                          "-fx-text-fill: #ffffff; " +
                          "-fx-border-color: #ffffff; " +
                          "-fx-border-width: 3px; " +
                          "-fx-border-radius: 5px; " +
                          "-fx-background-radius: 5px;");
        });
        
        // Hover out effect
        button.setOnMouseExited(e -> {
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), button);
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);
            scaleOut.play();
            
            glow.setLevel(0.0);
            button.setStyle(originalStyle);
        });
        
        // Press effect
        button.setOnMousePressed(e -> {
            ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), button);
            scalePress.setToX(0.98);
            scalePress.setToY(0.98);
            scalePress.play();
        });
        
        button.setOnMouseReleased(e -> {
            if (button.isHover()) {
                ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), button);
                scaleRelease.setToX(1.05);
                scaleRelease.setToY(1.05);
                scaleRelease.play();
            }
        });
    }

    @FXML
    private void onStartGame(ActionEvent event) {
        // Show name entry panel
        if (nameEntryPanel != null) {
            nameEntryPanel.showWithAnimation();
        }
    }
    
    @FXML
    private void onSelectGameMode(ActionEvent event) {
        // Show game mode selection panel
        if (gameModeSelectionPanel != null) {
            gameModeSelectionPanel.showWithAnimation();
        }
    }
    
    private void startGameWithName(String name) {
        // Stop and dispose video player when switching to game
        disposeMediaPlayer();
        
        try {
            // Load gameLayout.fxml
            URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location);
            Parent root = fxmlLoader.load();
            GuiController controller = fxmlLoader.getController();

            // Get the current stage
            Stage stage = (Stage) rootPane.getScene().getWindow();

            // Create new scene and set it on the stage
            Scene scene = new Scene(root, 650, 600);
            stage.setScene(scene);

            // Initialize the game controller and set player name and game mode
            GameController gameController = new GameController(controller);
            gameController.setPlayerName(name);
            gameController.setGameMode(GameSettings.getSelectedGameMode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSettings(ActionEvent event) {
        try {
            // Load Settings.fxml
            URL location = getClass().getClassLoader().getResource("com/comp2042/view/Settings.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location);
            Parent root = fxmlLoader.load();
            
            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Create new scene and set it on the stage
            Scene scene = new Scene(root, 650, 600);
            
            // Mark that we came from main menu (not game)
            scene.getProperties().put("returnToGame", false);
            
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onQuit(ActionEvent event) {
        disposeMediaPlayer();
        System.exit(0);
    }
}
