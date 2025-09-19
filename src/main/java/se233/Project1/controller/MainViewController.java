package se233.Project1.controller;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainViewController {
    private final ListView<String> inputListView = new ListView<>();
    private final Button mp3 = new Button("Convert to MP3");
    private final Map<String, String> filePathMap = new HashMap<>();
    VBox root = new VBox(10, inputListView, mp3);

    public MainViewController(){
        inputListView.setOnDragOver(event -> {
            if (event.getGestureSource() != inputListView && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        inputListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    inputListView.getItems().add(file.getName());
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
        mp3.setOnAction(e -> changeFormat());
    }





    public void changeFormat() {
        String selected = inputListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String inputPath = filePathMap.get(selected);
            String outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + ".mp3";
            try {
                ChangeFormatTask task = new ChangeFormatTask("/opt/homebrew/bin/ffmpeg", "/opt/homebrew/bin/ffprobe");
                task.convertToMp3(inputPath, outputPath);
                System.out.println("✅ Converted: " + outputPath);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("❌ Conversion failed for: " + inputPath);
            }
        } else {
            System.out.println("No file selected.");
        }
    }
    public VBox getRoot() {
        return root;
    }


}