package se233.Project1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se233.Project1.controller.MainViewController;

public class Launcher extends Application {
    @Override
    public void start(Stage stage) {
        MainViewController controller = new MainViewController();
        Scene scene = new Scene(controller.getRoot(), 500, 400);
        stage.setScene(scene);
        stage.setTitle("File Converter");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
