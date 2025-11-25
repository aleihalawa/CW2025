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
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {

    @FXML
    private ImageView backgroundImage;
    
    @FXML
    private Button startButton;
    
    @FXML
    private Button levelButton;
    
    @FXML
    private Button instructionsButton;
    
    @FXML
    private Button settingsButton;
    
    @FXML
    private Button quitButton;
    
    @FXML
    private Label highScoreLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load background image
        try {
            URL imageUrl = getClass().getClassLoader().getResource("MainMenu.jpeg");
            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                backgroundImage.setImage(image);
            } else {
                System.err.println("Could not find MainMenu.jpeg in resources");
            }
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Add hover effects to all buttons
        setupButtonHoverEffect(startButton);
        setupButtonHoverEffect(levelButton);
        setupButtonHoverEffect(instructionsButton);
        setupButtonHoverEffect(settingsButton);
        setupButtonHoverEffect(quitButton);
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
        try {
            // Load gameLayout.fxml
            URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location);
            Parent root = fxmlLoader.load();
            GuiController controller = fxmlLoader.getController();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create new scene and set it on the stage
            Scene scene = new Scene(root, 500, 700);
            stage.setScene(scene);

            // Initialize the game controller
            new GameController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onLevelSelect(ActionEvent event) {
        System.out.println("Feature coming soon");
    }

    @FXML
    private void onInstructions(ActionEvent event) {
        System.out.println("Feature coming soon");
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


