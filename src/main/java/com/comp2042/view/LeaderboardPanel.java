package com.comp2042.view;

import com.comp2042.model.GameMode;
import com.comp2042.model.HighScoreManager;
import com.comp2042.model.ScoreEntry;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

/**
 * Overlay panel for displaying the leaderboard.
 */
public class LeaderboardPanel extends Pane {
    
    private Button closeButton;
    private Rectangle dimOverlay;
    private VBox contentPane;
    private VBox scoresList;
    
    public LeaderboardPanel() {
        setStyle("-fx-background-color: transparent;");
        setPrefSize(650, 600);
        setMaxSize(650, 600);
        
        // Create dimming overlay
        dimOverlay = new Rectangle(650, 600);
        dimOverlay.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
        dimOverlay.setOpacity(0);
        getChildren().add(dimOverlay);
        
        // Create content pane - more compact
        contentPane = new VBox(15);
        contentPane.setAlignment(javafx.geometry.Pos.CENTER);
        contentPane.setPrefSize(450, 500);
        contentPane.setLayoutX((650 - 450) / 2.0);
        contentPane.setLayoutY((600 - 500) / 2.0);
        contentPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-border-color: #00ffff; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 25;");
        
        // Title - cleaner without emojis
        Label titleLabel = new Label("LEADERBOARD");
        titleLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; -fx-font-size: 22px; -fx-font-weight: bold; -fx-letter-spacing: 1px; -fx-padding: 0 0 10 0;");
        
        // Scores list container - more compact
        scoresList = new VBox(8);
        scoresList.setAlignment(javafx.geometry.Pos.CENTER);
        scoresList.setPrefWidth(380);
        scoresList.setPrefHeight(320);
        scoresList.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-border-color: #00ffff; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-padding: 15 20;");
        
        // Close button - use pause-menu-button style for consistency
        closeButton = new Button("CLOSE");
        closeButton.getStyleClass().add("pause-menu-button");
        
        contentPane.getChildren().addAll(titleLabel, scoresList, closeButton);
        getChildren().add(contentPane);
        
        setVisible(false);
    }
    
    public void setOnClose(EventHandler<ActionEvent> handler) {
        if (closeButton != null) {
            closeButton.setOnAction(handler);
        }
    }
    
    public void loadLeaderboard() {
        scoresList.getChildren().clear();
        
        List<ScoreEntry> topScores = HighScoreManager.loadLeaderboard(GameMode.CLASSIC);
        
        if (topScores.isEmpty()) {
            Label emptyLabel = new Label("No scores yet!");
            emptyLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.6); -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; -fx-font-size: 14px; -fx-alignment: CENTER;");
            scoresList.getChildren().add(emptyLabel);
        } else {
            // Show top 5 scores with better formatting
            int displayCount = Math.min(5, topScores.size());
            for (int i = 0; i < displayCount; i++) {
                ScoreEntry entry = topScores.get(i);
                
                // Create a container for each score entry
                HBox entryBox = new HBox(15);
                entryBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                entryBox.setPrefWidth(340);
                entryBox.setStyle("-fx-padding: 8px 10px;");
                
                // Rank label - smaller and left-aligned
                Label rankLabel = new Label(String.format("%d.", i + 1));
                rankLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; -fx-font-size: 16px; -fx-font-weight: bold; -fx-min-width: 30px;");
                
                // Name label - left-aligned
                Label nameLabel = new Label(entry.getName());
                nameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; -fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 200px;");
                
                // Score label - right-aligned
                Label scoreLabel = new Label(String.format("%d", entry.getScore()));
                scoreLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; -fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: CENTER_RIGHT;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                entryBox.getChildren().addAll(rankLabel, nameLabel, spacer, scoreLabel);
                scoresList.getChildren().add(entryBox);
            }
        }
    }
    
    public void showWithAnimation() {
        // Load leaderboard data
        loadLeaderboard();
        
        setVisible(true);
        toFront();
        
        // Dim overlay appears instantly
        dimOverlay.setOpacity(1.0);
        
        // Content pane starts invisible and scaled down
        contentPane.setOpacity(0);
        contentPane.setScaleX(0.85);
        contentPane.setScaleY(0.85);
        
        // Smooth fade in and scale in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), contentPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), contentPane);
        scaleIn.setFromX(0.85);
        scaleIn.setFromY(0.85);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn);
        parallelTransition.play();
    }
    
    public void hideWithAnimation() {
        // Fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), contentPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            dimOverlay.setOpacity(0);
            setVisible(false);
        });
        fadeOut.play();
    }
}

