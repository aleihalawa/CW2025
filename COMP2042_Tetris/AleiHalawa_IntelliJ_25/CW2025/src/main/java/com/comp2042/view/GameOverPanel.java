package com.comp2042.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;

public class GameOverPanel extends Pane {
    
    private ImageView menuImage;
    private Button restartButton;
    private Button leaderboardButton;
    private Button mainMenuButton;
    private VBox buttonContainer;
    private Rectangle dimOverlay;
    private Pane contentPane; // Container for image and buttons that will animate
    private Label scoreLabel;
    private Label highScoreLabel;
    private VBox scoreContainer;
    
    public GameOverPanel() {
        setStyle("-fx-background-color: transparent;");
        setPrefSize(650, 600);
        setMaxSize(650, 600);
        
        // Create dimming overlay like pause menu - appears instantly, no animation
        dimOverlay = new Rectangle(650, 600);
        dimOverlay.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.6));
        dimOverlay.setOpacity(0); // Start invisible, will be set to 1 instantly
        getChildren().add(dimOverlay);
        
        // Create content pane that will animate (image + buttons)
        contentPane = new Pane();
        contentPane.setPrefSize(650, 600);
        getChildren().add(contentPane);
        
        // Load and display the game over menu image
        try {
            URL imageUrl = getClass().getClassLoader().getResource("Game_over_menu.jpeg");
            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                menuImage = new ImageView(image);
                menuImage.setPreserveRatio(true);
                menuImage.setFitWidth(280);
                
                // Wait for image to load before positioning
                menuImage.imageProperty().addListener((obs, oldImage, newImage) -> {
                    if (newImage != null) {
                        updateButtonPosition();
                    }
                });
                
                contentPane.getChildren().add(menuImage);
                
                // Center the image
                centerImage();
            } else {
                System.err.println("Could not find Game_over_menu.jpeg in resources");
            }
        } catch (Exception e) {
            System.err.println("Error loading game over menu image: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Create score container
        scoreContainer = new VBox(8);
        scoreContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Score Label
        scoreLabel = new Label("SCORE: 0");
        scoreLabel.setStyle("-fx-text-fill: #BA55D3; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', sans-serif; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // High Score Label
        highScoreLabel = new Label("HIGH SCORE: 0");
        highScoreLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', sans-serif; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        scoreContainer.getChildren().addAll(scoreLabel, highScoreLabel);
        
        // Create button container
        buttonContainer = new VBox(20);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Restart Button - use same style as pause menu buttons
        restartButton = new Button("RESTART");
        restartButton.getStyleClass().add("pause-menu-button");
        
        // Leaderboard Button - use same style as pause menu buttons
        leaderboardButton = new Button("LEADERBOARD");
        leaderboardButton.getStyleClass().add("pause-menu-button");
        
        // Main Menu Button - use same style as pause menu buttons
        mainMenuButton = new Button("MAIN MENU");
        mainMenuButton.getStyleClass().add("pause-menu-button");
        
        buttonContainer.getChildren().addAll(restartButton, leaderboardButton, mainMenuButton);
        contentPane.getChildren().addAll(scoreContainer, buttonContainer);
        
        // Position buttons below the image
        updateButtonPosition();
        
        setVisible(false);
    }
    
    private void centerImage() {
        if (menuImage != null) {
            double imageWidth = menuImage.getFitWidth();
            double imageHeight = menuImage.getImage() != null ? 
                (menuImage.getImage().getHeight() * imageWidth / menuImage.getImage().getWidth()) : 300;
            
            menuImage.setLayoutX((650 - imageWidth) / 2.0);
            menuImage.setLayoutY((600 - imageHeight) / 2.0);
        }
    }
    
    private void updateButtonPosition() {
        if (menuImage != null && buttonContainer != null && menuImage.getImage() != null) {
            // Calculate image height
            double imageWidth = menuImage.getFitWidth();
            double imageHeight = menuImage.getImage().getHeight() * imageWidth / menuImage.getImage().getWidth();
            
            // Position score container above buttons, shifted right and up
            double imageCenterX = menuImage.getLayoutX() + imageWidth / 2.0;
            double scoreY = menuImage.getLayoutY() + imageHeight - 250; // Shift up more
            double buttonY = menuImage.getLayoutY() + imageHeight - 180; // Shift buttons up a bit
            
            if (scoreContainer != null) {
                scoreContainer.setLayoutX(imageCenterX - 95); // Shift left just a bit more
                scoreContainer.setLayoutY(scoreY);
            }
            
            buttonContainer.setLayoutX(imageCenterX - 90); // Half of button width (180/2 from CSS)
            buttonContainer.setLayoutY(buttonY);
        } else {
            // Fallback positioning - center of screen, shifted slightly left and up
            if (scoreContainer != null) {
                scoreContainer.setLayoutX((650 - 200) / 2.0 + 5); // Shift left just a bit more
                scoreContainer.setLayoutY(150); // Shift up
            }
            buttonContainer.setLayoutX((650 - 180) / 2.0);
            buttonContainer.setLayoutY(220); // Shift buttons up a bit
        }
    }
    
    /**
     * Sets the score and high score to display on the game over panel.
     * 
     * @param score The player's final score
     * @param highScore The current high score for this game mode
     */
    public void setScores(int score, int highScore) {
        if (scoreLabel != null) {
            scoreLabel.setText("SCORE: " + score);
        }
        if (highScoreLabel != null) {
            highScoreLabel.setText("HIGH SCORE: " + highScore);
        }
    }
    
    public void setOnRestart(EventHandler<ActionEvent> handler) {
        if (restartButton != null) {
            restartButton.setOnAction(handler);
        }
    }
    
    public void setOnLeaderboard(EventHandler<ActionEvent> handler) {
        if (leaderboardButton != null) {
            leaderboardButton.setOnAction(handler);
        }
    }
    
    public void setOnMainMenu(EventHandler<ActionEvent> handler) {
        if (mainMenuButton != null) {
            mainMenuButton.setOnAction(handler);
        }
    }
    
    public void showWithAnimation() {
        setVisible(true);
        toFront();
        
        // Ensure image and buttons are properly positioned
        centerImage();
        updateButtonPosition();
        
        // Dim overlay appears instantly (no animation)
        dimOverlay.setOpacity(1.0);
        
        // Content pane (image + buttons) starts invisible and scaled down
        contentPane.setOpacity(0);
        contentPane.setScaleX(0.85);
        contentPane.setScaleY(0.85);
        
        // Smooth fade in animation for content only - faster for better responsiveness
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), contentPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        // Smooth scale in animation - start slightly smaller and grow - faster
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), contentPane);
        scaleIn.setFromX(0.9);
        scaleIn.setFromY(0.9);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        // Play both animations together for a smooth, thorough transition
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn);
        parallelTransition.play();
    }
}
