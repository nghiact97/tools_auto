package com.javarpa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

/**
 * Main entry point for JavaRPA Tool.
 * A Robotic Process Automation desktop application built with JavaFX 21.
 */
public class App extends Application {

    public static final String APP_NAME = "JavaRPA Tool";
    public static final String APP_VERSION = "1.0.0";

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load main FXML layout
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/main.fxml")
        );
        Scene scene = new Scene(loader.load(), 900, 650);

        // Apply dark theme CSS
        scene.getStylesheets().add(
            Objects.requireNonNull(
                getClass().getResource("/css/dark-theme.css")
            ).toExternalForm()
        );

        // Configure primary stage
        primaryStage.setTitle(APP_NAME + " v" + APP_VERSION);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(true);

        // Try to set app icon
        try {
            primaryStage.getIcons().add(
                new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/images/icon.png")
                ))
            );
        } catch (Exception ignored) {}

        primaryStage.show();
        primaryStage.centerOnScreen();
    }

    @Override
    public void stop() throws Exception {
        // Cleanup on exit
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
