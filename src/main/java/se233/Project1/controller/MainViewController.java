package se233.Project1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import se233.Project1.view.ConvertSettingPane;
import se233.Project1.view.DropPane;
import se233.Project1.view.LoadingPane;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainViewController {

    @FXML private AnchorPane dropZone;
    @FXML private Label dropFileLabel;

    @FXML private Slider qualitySlider;
    @FXML private ComboBox<String> formatComboBox;

    @FXML private ProgressBar file1ProgressBar;
    @FXML private ProgressBar file2ProgressBar;
    @FXML private ProgressBar file3ProgressBar;
    @FXML private ProgressBar file4ProgressBar;
    @FXML private ProgressBar file5ProgressBar;

    private DropPane dropPane;
    private ConvertSettingPane convertPane;
    private LoadingPane loadingPane;

    private final ListView<String> inputListView = new ListView<>();
    private final Button mp3 = new Button("Convert to MP3");
    private final Map<String, String> filePathMap = new HashMap<>();
    private final VBox root = new VBox(10, inputListView, mp3);

    @FXML
    public void initialize() {
        dropPane = new DropPane(dropZone, dropFileLabel);
        convertPane = new ConvertSettingPane(qualitySlider, formatComboBox);
        loadingPane = new LoadingPane(file1ProgressBar, file2ProgressBar, file3ProgressBar, file4ProgressBar, file5ProgressBar);

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
                    filePathMap.put(file.getName(), file.getAbsolutePath());
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
                System.out.println("Converted: " + outputPath);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Conversion failed for: " + inputPath);
            }
        } else {
            System.out.println("No file selected.");
        }
    }

    public VBox getRoot() {
        return root;
    }
}
