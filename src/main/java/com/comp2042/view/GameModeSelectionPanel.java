package com.comp2042.view;

import com.comp2042.model.GameMode;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    
    // Modal overlay for rules
    private VBox infoModal;
    private Label modalTitle;
    private Label modalContent;
    
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
        
        // Create modal overlay for rules
        createInfoModal();
        
        getChildren().addAll(contentContainer, infoModal);
        
        setVisible(false);
    }
    
    /**
     * Creates the info modal overlay for displaying game mode rules.
     */
    private void createInfoModal() {
        // Create modal overlay (covers entire screen)
        infoModal = new VBox();
        infoModal.setAlignment(javafx.geometry.Pos.CENTER);
        infoModal.setPrefSize(650, 600);
        infoModal.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        infoModal.setVisible(false);
        
        // Create content card
        VBox modalCard = new VBox(20);
        modalCard.setMaxWidth(500);
        modalCard.setMaxHeight(400);
        modalCard.setStyle("-fx-background-color: #1a1a1a; " +
                          "-fx-border-color: #00ffff; " +
                          "-fx-border-width: 2px; " +
                          "-fx-border-radius: 10px; " +
                          "-fx-background-radius: 10px; " +
                          "-fx-padding: 30px; " +
                          "-fx-effect: dropshadow(gaussian, #00ffff, 15, 0.5, 0, 0);");
        modalCard.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Title label
        modalTitle = new Label();
        modalTitle.setStyle("-fx-text-fill: #00ffff; " +
                           "-fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; " +
                           "-fx-font-size: 24px; " +
                           "-fx-font-weight: bold; " +
                           "-fx-alignment: CENTER;");
        
        // Content label
        modalContent = new Label();
        modalContent.setWrapText(true);
        modalContent.setStyle("-fx-text-fill: #ffffff; " +
                            "-fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; " +
                            "-fx-font-size: 14px; " +
                            "-fx-alignment: CENTER; " +
                            "-fx-line-spacing: 5px;");
        
        // Close button
        Button closeModalButton = new Button("CLOSE");
        closeModalButton.getStyleClass().add("menu-button");
        closeModalButton.setOnAction(e -> closeModal());
        
        modalCard.getChildren().addAll(modalTitle, modalContent, closeModalButton);
        infoModal.getChildren().add(modalCard);
    }
    
    /**
     * Creates a clickable image view for a game mode with a RULES button.
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
        
        // Wrap image in StackPane
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(imageWidth, imageHeight);
        imageContainer.setMaxSize(imageWidth, imageHeight);
        imageContainer.getChildren().add(imageView);
        imageContainer.setStyle("-fx-background-color: transparent;");
        imageContainer.setCache(true);
        imageContainer.setCacheHint(javafx.scene.CacheHint.SPEED);
        
        // Make it clickable
        imageContainer.setPickOnBounds(true);
        imageContainer.setCursor(javafx.scene.Cursor.HAND);
        
        // Use direct property changes instead of animations for better performance
        // Only use minimal animations for smooth feel
        imageContainer.setOnMouseEntered(e -> {
            imageContainer.setScaleX(1.08);
            imageContainer.setScaleY(1.08);
        });
        
        imageContainer.setOnMouseExited(e -> {
            imageContainer.setScaleX(1.0);
            imageContainer.setScaleY(1.0);
        });
        
        // Simple click effect - no animation needed
        imageContainer.setOnMousePressed(e -> {
            imageContainer.setScaleX(0.96);
            imageContainer.setScaleY(0.96);
        });
        
        imageContainer.setOnMouseReleased(e -> {
            if (imageContainer.isHover()) {
                imageContainer.setScaleX(1.08);
                imageContainer.setScaleY(1.08);
            } else {
                imageContainer.setScaleX(1.0);
                imageContainer.setScaleY(1.0);
            }
        });
        
        // Click handler - select game mode
        imageContainer.setOnMouseClicked(e -> {
            selectGameMode(mode);
        });
        
        // Create info image button
        ImageView infoImageView = new ImageView();
        URL infoImageUrl = getClass().getClassLoader().getResource("info logo.png");
        if (infoImageUrl != null) {
            Image infoImage = new Image(infoImageUrl.toExternalForm(), 30, 30, true, true);
            infoImageView.setImage(infoImage);
        }
        infoImageView.setFitWidth(30);
        infoImageView.setFitHeight(30);
        infoImageView.setPreserveRatio(true);
        infoImageView.setSmooth(true);
        infoImageView.setCache(true);
        infoImageView.setCacheHint(javafx.scene.CacheHint.SPEED);
        infoImageView.setCursor(javafx.scene.Cursor.HAND);
        infoImageView.setOpacity(0.8);
        
        // Make info image clickable
        infoImageView.setOnMouseClicked(e -> {
            e.consume(); // Prevent event from bubbling to image container
            showRulesForMode(mode);
        });
        
        // Create VBox to contain image and info button
        VBox modeCard = new VBox(10);
        modeCard.setAlignment(javafx.geometry.Pos.CENTER);
        modeCard.getChildren().addAll(imageContainer, infoImageView);
        
        // Wrap in StackPane for consistency with original structure
        StackPane container = new StackPane();
        container.getChildren().add(modeCard);
        container.setStyle("-fx-background-color: transparent;");
        
        return container;
    }
    
    /**
     * Shows the rules modal for the specified game mode.
     */
    private void showRulesForMode(GameMode mode) {
        String title;
        String content;
        
        switch (mode) {
            case CLASSIC:
                title = "CLASSIC MODE";
                content = "The standard Tetris experience.\n\nControls:\nArrows to Move/Rotate.\n\nGoal:\nClear lines to score points. Speed increases every level.";
                break;
            case MIRROR:
                title = "MIRROR MODE";
                content = "A twisted challenge for veterans.\n\nRules:\nALL controls are reversed!\nLeft is Right.\nRight is Left.\n\nGoal:\nSurvive as long as your brain can handle the confusion.";
                break;
            case POWERUPS:
                title = "ARCADE POWER-UP";
                content = "Chaos and destruction.\n\nRules:\n1. Hyper Speed (1.5x).\n2. Bedrock Corruption: Floor rises every 15s!\n\nPower-Ups:\n[1] Freeze: Stop time.\n[2] Drill: Smash columns.\n[3] Bomb: Area explosion.\n\nEarn items every 100 points!";
                break;
            default:
                return;
        }
        
        showModal(title, content);
    }
    
    /**
     * Shows the modal with the specified title and content.
     */
    private void showModal(String title, String content) {
        if (modalTitle != null && modalContent != null && infoModal != null) {
            modalTitle.setText(title);
            modalContent.setText(content);
            infoModal.setVisible(true);
            infoModal.toFront();
        }
    }
    
    /**
     * Closes the rules modal.
     */
    private void closeModal() {
        if (infoModal != null) {
            infoModal.setVisible(false);
        }
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

