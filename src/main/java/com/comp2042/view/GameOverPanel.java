package com.comp2042.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
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
    private Button mainMenuButton;
    private VBox buttonContainer;
    private Rectangle dimOverlay;
    private Pane contentPane; // Container for image and buttons that will animate
    
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
        
        // Create button container
        buttonContainer = new VBox(20);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Restart Button - use same style as pause menu buttons
        restartButton = new Button("RESTART");
        restartButton.getStyleClass().add("pause-menu-button");
        
        // Main Menu Button - use same style as pause menu buttons
        mainMenuButton = new Button("MAIN MENU");
        mainMenuButton.getStyleClass().add("pause-menu-button");
        
        buttonContainer.getChildren().addAll(restartButton, mainMenuButton);
        contentPane.getChildren().add(buttonContainer);
        
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
            
            // Position buttons even higher (shift up more)
            double imageCenterX = menuImage.getLayoutX() + imageWidth / 2.0;
            double buttonY = menuImage.getLayoutY() + imageHeight - 150; // Shift up even more (was -80, now -150)
            
            buttonContainer.setLayoutX(imageCenterX - 90); // Half of button width (180/2 from CSS)
            buttonContainer.setLayoutY(buttonY);
        } else {
            // Fallback positioning - center of screen
            buttonContainer.setLayoutX((650 - 180) / 2.0);
            buttonContainer.setLayoutY(250);
        }
    }
    
    public void setOnRestart(EventHandler<ActionEvent> handler) {
        if (restartButton != null) {
            restartButton.setOnAction(handler);
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
