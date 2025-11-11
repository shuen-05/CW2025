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

        URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
        ResourceBundle resources = null;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gameLayout.fxml"));
        Parent root = fxmlLoader.load();
        GuiController c = fxmlLoader.getController();

        primaryStage.setTitle("TetrisJFX");

        // Base design size
        final double baseWidth = 400.0;
        final double baseHeight = 520.0;

        // Wrap root so we can scale everything uniformly
        Group content = new Group(root);
        StackPane container = new StackPane(content);
        StackPane.setAlignment(content, Pos.CENTER);

        // Scale transform bound to scene size
        Scale scale = new Scale(1.0, 1.0, 0, 0);
        // Scale around the visual center so it stays centered while resizing
        scale.setPivotX(baseWidth / 2.0);
        scale.setPivotY(baseHeight / 2.0);
        content.getTransforms().add(scale);

        Scene scene = new Scene(container, baseWidth, baseHeight);
        // Ensure global styles (including .root background) apply to the Scene root
        scene.getStylesheets().add(getClass().getResource("/window_style.css").toExternalForm());

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
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
        new GameController(c);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
