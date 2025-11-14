package com.comp2042;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gameLayout.fxml"));
        Parent root = fxmlLoader.load();
        GuiController controller = fxmlLoader.getController();

        primaryStage.setTitle("TetrisJFX - Ultimate Edition");

        // Base design size
        final double baseWidth = 800.0;
        final double baseHeight = 700.0;

        // Initial window size
        final double initialWidth = 1000.0;
        final double initialHeight = 800.0;

        // Setup scalable content
        Group content = new Group(root);
        StackPane container = new StackPane(content);
        StackPane.setAlignment(content, Pos.CENTER);

        // Calculate initial scale
        double initialScaleX = initialWidth / baseWidth;
        double initialScaleY = initialHeight / baseHeight;
        double initialScale = Math.min(initialScaleX, initialScaleY);

        Scale scale = new Scale(initialScale, initialScale, 0, 0);
        scale.setPivotX(baseWidth / 2.0);
        scale.setPivotY(baseHeight / 2.0);
        content.getTransforms().add(scale);

        Scene scene = new Scene(container, initialWidth, initialHeight);

        // Apply styles
        scene.getStylesheets().add(getClass().getResource("/window_style.css").toExternalForm());

        // Responsive scaling
        setupSceneScaling(scene, scale, baseWidth, baseHeight);

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(true);

        // Add window icon (optional)
        // primaryStage.getIcons().add(new Image("/icon.png"));

        primaryStage.show();

        // Initialize game controller
        new GameController(controller);
    }

    private void setupSceneScaling(Scene scene, Scale scale, double baseWidth, double baseHeight) {
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double sx = newVal.doubleValue() / baseWidth;
            double sy = scene.getHeight() / baseHeight;
            double s = Math.min(sx, sy);
            scale.setX(s);
            scale.setY(s);
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double sx = scene.getWidth() / baseWidth;
            double sy = newVal.doubleValue() / baseHeight;
            double s = Math.min(sx, sy);
            scale.setX(s);
            scale.setY(s);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}