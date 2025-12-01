package com.comp2042.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class HighScoreNotificationPanel extends StackPane {
    
    private Label label;
    private Rectangle dimOverlay;
    
    public HighScoreNotificationPanel() {
        setPrefSize(650, 600);
        setMaxSize(650, 600);
        
        // Create dimming overlay
        dimOverlay = new Rectangle(650, 600);
        dimOverlay.setFill(Color.rgb(0, 0, 0, 0.7));
        dimOverlay.setOpacity(0);
        getChildren().add(dimOverlay);
        
        // Create label
        label = new Label("NEW HIGH SCORE!");
        label.setTextFill(Color.web("#FFD700")); // Gold text
        label.setStyle("-fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; " +
                      "-fx-font-size: 36px; " +
                      "-fx-font-weight: bold;");
        
        // Add glow effect
        Glow glow = new Glow(0.8);
        label.setEffect(glow);
        
        getChildren().add(label);
        
        // Center the label
        setAlignment(javafx.geometry.Pos.CENTER);
        
        // Start invisible
        setOpacity(0);
        setScaleX(0.5);
        setScaleY(0.5);
    }
    
    public void showWithAnimation(Runnable onFinished) {
        // Dim overlay appears INSTANTLY before anything else
        dimOverlay.setOpacity(1.0);
        
        toFront();
        
        // Scale and fade in label (dim is already visible)
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), this);
        scaleIn.setFromX(0.5);
        scaleIn.setFromY(0.5);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showTransition = new ParallelTransition(scaleIn, fadeIn);
        
        // Hold for 1 second
        javafx.animation.PauseTransition hold = new javafx.animation.PauseTransition(Duration.millis(1000));
        
        // Fade out label only
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        // Sequence: show -> hold -> hide
        SequentialTransition sequence = new SequentialTransition(showTransition, hold, fadeOut);
        sequence.setOnFinished(e -> {
            // Dim overlay disappears INSTANTLY after panel fades out
            dimOverlay.setOpacity(0.0);
            if (onFinished != null) {
                onFinished.run();
            }
        });
        
        sequence.play();
    }
}

