package com.comp2042.view;

import com.comp2042.model.GameSettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the name entry dialog that appears before starting a game.
 */
public class NameEntryController implements Initializable {
    
    @FXML
    private TextField nameField;
    
    @FXML
    private javafx.scene.control.Button submitButton;
    
    /**
     * Initializes the controller with the current player name if set.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set current player name if available
        String currentName = GameSettings.getPlayerName();
        if (currentName != null && !currentName.equals("Player")) {
            nameField.setText(currentName);
        }
        // Request focus on the text field so user can type immediately
        nameField.requestFocus();
    }
    
    /**
     * Handles the submit button action.
     * Saves the player name and closes the dialog.
     * 
     * @param event The action event
     */
    @FXML
    private void onSubmit(ActionEvent event) {
        // Get text from nameField, use "Player" if empty
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            name = "Player";
        }
        
        // Save player name to GameSettings
        GameSettings.setPlayerName(name);
        
        // Close the stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}

