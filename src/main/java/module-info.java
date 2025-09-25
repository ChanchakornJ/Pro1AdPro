module se233.project1 {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.fxml;

    requires java.desktop;
    requires ffmpeg;

    opens se233.Project1.controller to javafx.fxml;
    exports se233.Project1;
    exports se233.Project1.controller;
}
