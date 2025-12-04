package com.comp2042.view;

import com.comp2042.model.SoundManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    
    @FXML
    private Slider musicSlider;
    
    @FXML
    private Slider sfxSlider;
    
    @FXML
    private Button backButton;
    
    private SoundManager soundManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get SoundManager singleton instance
        soundManager = SoundManager.getInstance();
        
        // Initialize SoundManager if not already initialized (loads sounds)
        // This ensures sounds are loaded even if user goes to settings before starting a game
        // initialize() is idempotent - safe to call multiple times
        soundManager.initialize();
        
        // Get current volumes from SoundManager
        double currentMusicVolume = soundManager.getMusicVolume();
        double currentSfxVolume = soundManager.getSfxVolume();
        
        // Set slider values to match current volumes
        musicSlider.setValue(currentMusicVolume);
        sfxSlider.setValue(currentSfxVolume);
        
        // Add listeners to sliders
        musicSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            soundManager.setMusicVolume(newValue.doubleValue());
        });
        
        sfxSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            soundManager.setSfxVolume(newValue.doubleValue());
        });
    }
    
    @FXML
    private void onBack(ActionEvent event) {
        try {
            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene currentScene = ((Node) event.getSource()).getScene();
            
            // Check if we should return to game or main menu
            Boolean returnToGame = (Boolean) currentScene.getProperties().get("returnToGame");
            
            if (returnToGame != null && returnToGame) {
                // Return to game scene (paused game)
                Scene gameScene = (Scene) currentScene.getProperties().get("gameScene");
                if (gameScene != null) {
                    stage.setScene(gameScene);
                } else {
                    // Fallback: load gameLayout if gameScene reference is lost
                    URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
                    FXMLLoader fxmlLoader = new FXMLLoader(location);
                    Parent root = fxmlLoader.load();
                    Scene scene = new Scene(root, 650, 600);
                    stage.setScene(scene);
                }
            } else {
                // Return to main menu
                URL location = getClass().getClassLoader().getResource("com/comp2042/view/MainMenu.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(location);
                Parent root = fxmlLoader.load();
                Scene scene = new Scene(root, 650, 600);
                stage.setScene(scene);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

