package com.comp2042.view;

import com.comp2042.model.GameMode;
import com.comp2042.model.HighScoreManager;
import com.comp2042.model.ScoreEntry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the leaderboard dialog that displays the top 5 scores.
 */
public class LeaderboardController implements Initializable {
    
    @FXML
    private ListView<Label> leaderboardList;
    
    @FXML
    private javafx.scene.control.Button closeButton;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadLeaderboard();
    }
    
    /**
     * Loads and displays the top 5 scores from the leaderboard.
     */
    private void loadLeaderboard() {
        List<ScoreEntry> topScores = HighScoreManager.loadLeaderboard(GameMode.CLASSIC);
        
        leaderboardList.getItems().clear();
        
        if (topScores.isEmpty()) {
            Label emptyLabel = new Label("No scores yet!");
            emptyLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; -fx-font-size: 16px; -fx-alignment: CENTER;");
            leaderboardList.getItems().add(emptyLabel);
        } else {
            // Show top 5 scores
            int displayCount = Math.min(5, topScores.size());
            for (int i = 0; i < displayCount; i++) {
                ScoreEntry entry = topScores.get(i);
                String rankText = String.format("%d. %s - %d", i + 1, entry.getName(), entry.getScore());
                Label scoreLabel = new Label(rankText);
                scoreLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-family: 'Public Pixel', 'Impact', 'Arial Black', 'Arial', sans-serif; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10px;");
                leaderboardList.getItems().add(scoreLabel);
            }
        }
    }
    
    @FXML
    private void onClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}

