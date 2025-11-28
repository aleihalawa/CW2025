package com.comp2042.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the custom font
        try {
            URL fontUrl = getClass().getClassLoader().getResource("public-pixel-font/PublicPixel-rv0pA.ttf");
            if (fontUrl != null) {
                Font.loadFont(fontUrl.toExternalForm(), 12);
                System.out.println("Custom font loaded successfully");
            } else {
                System.err.println("Could not find PublicPixel-rv0pA.ttf font file");
            }
        } catch (Exception e) {
            System.err.println("Error loading custom font: " + e.getMessage());
        }

        // Load MainMenu.fxml first
        URL location = getClass().getClassLoader().getResource("com/comp2042/view/MainMenu.fxml");
        ResourceBundle resources = null;
        FXMLLoader fxmlLoader = new FXMLLoader(location, resources);
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("TetrisJFX");
        Scene scene = new Scene(root, 650, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
