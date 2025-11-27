package com.comp2042.view;

import com.comp2042.controller.GameController;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private ImageView backgroundImage;
    
    @FXML
    private javafx.scene.layout.StackPane rootPane;
    
    @FXML
    private Button startButton;
    
    @FXML
    private Button settingsButton;
    
    @FXML
    private Button quitButton;
    
    private MediaPlayer mediaPlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Try to load background video first
        try {
            URL videoUrl = getClass().getClassLoader().getResource("Retro_Arcade_Tetris_Video_Generation.mp4");
            if (videoUrl != null) {
                Media media = new Media(videoUrl.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                
                // Set video to loop indefinitely
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                
                // Set video to auto-play
                mediaPlayer.setAutoPlay(true);
                
                // Set mute (optional - remove if you want sound)
                mediaPlayer.setMute(true);
                
                // Set the MediaView to use the MediaPlayer
                backgroundVideo.setMediaPlayer(mediaPlayer);
                
                // Center the video and preserve aspect ratio (will crop sides if needed)
                backgroundVideo.setPreserveRatio(true);
                
                // Set fit height to window height (600px), width will adjust automatically
                // This ensures video fills height and crops sides if video is wider
                backgroundVideo.setFitHeight(600);
                
                // Bind to StackPane height to maintain fit
                if (rootPane != null) {
                    backgroundVideo.fitHeightProperty().bind(rootPane.heightProperty());
                }
                
                // Also configure when media is ready to ensure proper centering
                mediaPlayer.setOnReady(() -> {
                    backgroundVideo.setPreserveRatio(true);
                    if (rootPane != null) {
                        backgroundVideo.setFitHeight(rootPane.getHeight());
                    } else {
                        backgroundVideo.setFitHeight(600);
                    }
                });
                
                // Handle errors - fallback to image if video fails
                mediaPlayer.setOnError(() -> {
                    System.err.println("Error playing video: " + mediaPlayer.getError());
                    fallbackToImage();
                });
                
                // Start playing
                mediaPlayer.play();
            } else {
                System.err.println("Could not find Retro_Tetris_Menu_Video_Generation.mp4 in resources");
                fallbackToImage();
            }
        } catch (Exception e) {
            System.err.println("Error loading background video: " + e.getMessage());
            e.printStackTrace();
            fallbackToImage();
        }
        
        // Add hover effects to all buttons
        setupButtonHoverEffect(startButton);
        setupButtonHoverEffect(settingsButton);
        setupButtonHoverEffect(quitButton);
    }
    
    private void fallbackToImage() {
        // Hide video and show image fallback
        if (backgroundVideo != null) {
            backgroundVideo.setVisible(false);
        }
        if (backgroundImage != null) {
            backgroundImage.setVisible(true);
            try {
                URL imageUrl = getClass().getClassLoader().getResource("MainMenu.jpeg");
                if (imageUrl != null) {
                    Image image = new Image(imageUrl.toExternalForm());
                    backgroundImage.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Error loading fallback image: " + e.getMessage());
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
        // Stop and dispose video player when switching to game
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        
        try {
            // Load gameLayout.fxml
            URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location);
            Parent root = fxmlLoader.load();
            GuiController controller = fxmlLoader.getController();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create new scene and set it on the stage
            Scene scene = new Scene(root, 650, 600);
            stage.setScene(scene);

            // Initialize the game controller
            new GameController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSettings(ActionEvent event) {
        System.out.println("Feature coming soon");
    }

    @FXML
    private void onQuit(ActionEvent event) {
        System.exit(0);
    }
}


