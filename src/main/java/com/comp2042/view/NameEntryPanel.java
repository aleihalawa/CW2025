package com.comp2042.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Overlay panel for entering player name before starting a game.
 */
public class NameEntryPanel extends Pane {
    
    private TextField nameField;
    private Button submitButton;
    private Button closeButton;
    private Rectangle dimOverlay;
    private VBox contentPane;
    private Pane contentContainer;
    
    public NameEntryPanel() {
        setStyle("-fx-background-color: transparent;");
        setPrefSize(650, 600);
        setMaxSize(650, 600);
        
        // Create dimming overlay
        dimOverlay = new Rectangle(650, 600);
        dimOverlay.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
        dimOverlay.setOpacity(0);
        getChildren().add(dimOverlay);
        
        // Create content container (Pane) to allow absolute positioning of X button
        contentContainer = new Pane();
        contentContainer.setPrefSize(400, 300);
        contentContainer.setLayoutX((650 - 400) / 2.0);
        contentContainer.setLayoutY((600 - 300) / 2.0);
        contentContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-border-color: #00ffff; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        
        // Create VBox for main content
        contentPane = new VBox(20);
        contentPane.setAlignment(javafx.geometry.Pos.CENTER);
        contentPane.setPrefSize(400, 300);
        contentPane.setLayoutX(0);
        contentPane.setLayoutY(0);
        contentPane.setStyle("-fx-padding: 40;");
        
        // Close button (X) - positioned at top right of content container
        closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-font-family: 'Arial', sans-serif; -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 5px;");
        closeButton.setLayoutX(400 - 35); // Position at top right with padding
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
        Label titleLabel = new Label("WELCOME!");
        titleLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; -fx-font-size: 24px; -fx-font-weight: bold; -fx-letter-spacing: 2px;");
        
        // Instruction label
        Label instructionLabel = new Label("ENTER YOUR NAME:");
        instructionLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Text field
        nameField = new TextField();
        nameField.setMaxWidth(200);
        nameField.setPrefWidth(200);
        nameField.setPromptText("Your Name");
        nameField.setStyle("-fx-font-family: 'Public Pixel', 'Impact', 'Arial', sans-serif; -fx-font-size: 14px; -fx-padding: 8px;");
        
        // Submit button
        submitButton = new Button("SUBMIT");
        submitButton.getStyleClass().add("menu-button");
        
        contentPane.getChildren().addAll(titleLabel, instructionLabel, nameField, submitButton);
        contentContainer.getChildren().addAll(contentPane, closeButton);
        getChildren().add(contentContainer);
        
        setVisible(false);
    }
    
    public TextField getNameField() {
        return nameField;
    }
    
    public void setOnSubmit(EventHandler<ActionEvent> handler) {
        if (submitButton != null) {
            submitButton.setOnAction(handler);
        }
    }
    
    public void setOnClose(EventHandler<ActionEvent> handler) {
        if (closeButton != null) {
            closeButton.setOnAction(handler);
        }
    }
    
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
        
        // Request focus on text field
        javafx.application.Platform.runLater(() -> nameField.requestFocus());
    }
    
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

