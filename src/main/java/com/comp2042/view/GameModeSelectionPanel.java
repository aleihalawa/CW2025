package com.comp2042.view;

import com.comp2042.model.GameMode;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.function.Consumer;

/**
 * Overlay panel for selecting game mode before starting a game.
 */
public class GameModeSelectionPanel extends Pane {
    
    private Button closeButton;
    private Rectangle dimOverlay;
    private VBox contentPane;
    private Pane contentContainer;
    private HBox gameModeContainer;
    private StackPane classicContainer;
    private StackPane mirrorContainer;
    private StackPane powerupsContainer;
    
    private Consumer<GameMode> onModeSelectedHandler;
    
    public GameModeSelectionPanel() {
        setStyle("-fx-background-color: transparent;");
        setPrefSize(650, 600);
        setMaxSize(650, 600);
        
        // Create dimming overlay
        dimOverlay = new Rectangle(650, 600);
        dimOverlay.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
        dimOverlay.setOpacity(0);
        getChildren().add(dimOverlay);
        
        // Create content container
        contentContainer = new Pane();
        contentContainer.setPrefSize(580, 400);
        contentContainer.setLayoutX((650 - 580) / 2.0);
        contentContainer.setLayoutY((600 - 400) / 2.0);
        contentContainer.setStyle("-fx-background-color: transparent; -fx-border-color: #00ffff; -fx-border-width: 3px; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        
        // Load and add background image
        URL backgroundUrl = getClass().getClassLoader().getResource("gamemode panel background.png");
        if (backgroundUrl != null) {
            ImageView backgroundImageView = new ImageView();
            Image backgroundImage = new Image(backgroundUrl.toExternalForm());
            backgroundImageView.setImage(backgroundImage);
            backgroundImageView.setFitWidth(580);
            backgroundImageView.setFitHeight(400);
            backgroundImageView.setPreserveRatio(false);
            backgroundImageView.setSmooth(true);
            backgroundImageView.setOpacity(1.0);
            // Add background as first child so it's behind everything
            contentContainer.getChildren().add(backgroundImageView);
        } else {
            System.err.println("Could not find gamemode panel background.png in resources");
            // Fallback to dark background if image not found
            contentContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9); -fx-border-color: #00ffff; -fx-border-width: 3px; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        }
        
        // Create VBox for main content
        contentPane = new VBox(30);
        contentPane.setAlignment(javafx.geometry.Pos.CENTER);
        contentPane.setPrefSize(580, 400);
        contentPane.setLayoutX(0);
        contentPane.setLayoutY(0);
        contentPane.setStyle("-fx-padding: 40;");
        
        // Close button (X) - positioned at top right of content container
        closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-font-family: 'Arial', sans-serif; -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 5px;");
        closeButton.setLayoutX(580 - 35);
        closeButton.setLayoutY(5);
        closeButton.setPrefSize(30, 30);
        
        // Hover effect for X button
        closeButton.setOnMouseEntered(e -> {
            closeButton.setStyle("-fx-background-color: rgba(255, 0, 0, 0.3); -fx-text-fill: #ff0000; -fx-font-family: 'Arial', sans-serif; -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-color: #ff0000; -fx-border-width: 1px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand; -fx-padding: 5px;");
        });
        closeButton.setOnMouseExited(e -> {
            closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-font-family: 'Arial', sans-serif; -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 5px;");
        });
        
        // Title
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("SELECT GAME MODE");
        titleLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; -fx-font-size: 28px; -fx-font-weight: bold; -fx-letter-spacing: 3px;");
        
        // Create HBox for game mode images
        gameModeContainer = new HBox(20);
        gameModeContainer.setAlignment(javafx.geometry.Pos.CENTER);
        gameModeContainer.setPrefHeight(250);
        
        // Load and create image views for each game mode with new image files
        classicContainer = createGameModeButton("classic gamemode.png", GameMode.CLASSIC);
        mirrorContainer = createGameModeButton("mirror_gamemode (2).png", GameMode.MIRROR);
        powerupsContainer = createGameModeButton("power ups gamemode.png", GameMode.POWERUPS);
        
        gameModeContainer.getChildren().addAll(classicContainer, mirrorContainer, powerupsContainer);
        
        contentPane.getChildren().addAll(titleLabel, gameModeContainer);
        // Add contentPane and closeButton after background (background is already added first)
        contentContainer.getChildren().addAll(contentPane, closeButton);
        getChildren().add(contentContainer);
        
        setVisible(false);
    }
    
    /**
     * Creates a clickable image view for a game mode.
     * Optimized for performance with minimal animations.
     */
    private StackPane createGameModeButton(String imagePath, GameMode mode) {
        ImageView imageView = new ImageView();
        
        // Determine size based on mode - Mirror needs to be slightly larger
        int imageWidth = 160;
        int imageHeight = 200;
        if (mode == GameMode.MIRROR) {
            imageWidth = 180;
            imageHeight = 225;
        }
        
        // Load image with caching enabled
        URL imageUrl = getClass().getClassLoader().getResource(imagePath);
        if (imageUrl != null) {
            // Load image with background loading and caching
            Image image = new Image(imageUrl.toExternalForm(), imageWidth, imageHeight, true, true, true);
            imageView.setImage(image);
        } else {
            System.err.println("Could not find " + imagePath + " in resources");
        }
        
        // Set size - reduced to fit 3 images horizontally
        imageView.setFitWidth(imageWidth);
        imageView.setFitHeight(imageHeight);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(false); // Disable smooth rendering for better performance
        imageView.setCache(true); // Enable caching
        imageView.setCacheHint(javafx.scene.CacheHint.SPEED); // Optimize for speed
        
        // Wrap in StackPane
        StackPane container = new StackPane();
        container.setPrefSize(imageWidth, imageHeight);
        container.setMaxSize(imageWidth, imageHeight);
        container.getChildren().add(imageView);
        container.setStyle("-fx-background-color: transparent;");
        container.setCache(true);
        container.setCacheHint(javafx.scene.CacheHint.SPEED);
        
        // Make it clickable
        container.setPickOnBounds(true);
        container.setCursor(javafx.scene.Cursor.HAND);
        
        // Use direct property changes instead of animations for better performance
        // Only use minimal animations for smooth feel
        container.setOnMouseEntered(e -> {
            container.setScaleX(1.08);
            container.setScaleY(1.08);
        });
        
        container.setOnMouseExited(e -> {
            container.setScaleX(1.0);
            container.setScaleY(1.0);
        });
        
        // Simple click effect - no animation needed
        container.setOnMousePressed(e -> {
            container.setScaleX(0.96);
            container.setScaleY(0.96);
        });
        
        container.setOnMouseReleased(e -> {
            if (container.isHover()) {
                container.setScaleX(1.08);
                container.setScaleY(1.08);
            } else {
                container.setScaleX(1.0);
                container.setScaleY(1.0);
            }
        });
        
        // Click handler - select game mode
        container.setOnMouseClicked(e -> {
            selectGameMode(mode);
        });
        
        return container;
    }
    
    /**
     * Handles game mode selection with visual feedback.
     */
    private void selectGameMode(GameMode mode) {
        // Find the selected container and show selection animation
        StackPane selectedContainer = null;
        if (mode == GameMode.CLASSIC) {
            selectedContainer = classicContainer;
        } else if (mode == GameMode.MIRROR) {
            selectedContainer = mirrorContainer;
        } else if (mode == GameMode.POWERUPS) {
            selectedContainer = powerupsContainer;
        }
        
        // Show selection animation
        if (selectedContainer != null) {
            // Quick pulse animation to show selection
            ScaleTransition pulse = new ScaleTransition(Duration.millis(150), selectedContainer);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.15);
            pulse.setToY(1.15);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);
            pulse.setOnFinished(e -> {
                // After animation, close panel and notify handler
                if (onModeSelectedHandler != null) {
                    onModeSelectedHandler.accept(mode);
                }
                hideWithAnimation();
            });
            pulse.play();
        } else {
            // Fallback if container not found
            if (onModeSelectedHandler != null) {
                onModeSelectedHandler.accept(mode);
            }
            hideWithAnimation();
        }
    }
    
    /**
     * Sets the handler for when a game mode is selected.
     */
    public void setOnModeSelected(Consumer<GameMode> handler) {
        this.onModeSelectedHandler = handler;
    }
    
    /**
     * Sets the handler for when the panel is closed.
     */
    public void setOnClose(EventHandler<ActionEvent> handler) {
        if (closeButton != null) {
            closeButton.setOnAction(handler);
        }
    }
    
    /**
     * Shows the panel with animation.
     */
    public void showWithAnimation() {
        setVisible(true);
        toFront();
        
        // Dim overlay appears instantly
        dimOverlay.setOpacity(1.0);
        
        // Content container starts invisible and scaled down
        if (contentContainer != null) {
            contentContainer.setOpacity(0);
            contentContainer.setScaleX(0.85);
            contentContainer.setScaleY(0.85);
            
            // Smooth fade in and scale in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), contentContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), contentContainer);
            scaleIn.setFromX(0.85);
            scaleIn.setFromY(0.85);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            
            ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn);
            parallelTransition.play();
        }
    }
    
    /**
     * Hides the panel with animation.
     */
    public void hideWithAnimation() {
        // Fade out animation
        if (contentContainer != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), contentContainer);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                dimOverlay.setOpacity(0);
                setVisible(false);
            });
            fadeOut.play();
        } else {
            // Fallback if container not found
            dimOverlay.setOpacity(0);
            setVisible(false);
        }
    }
}

