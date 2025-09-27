package se233.Project1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import se233.Project1.view.ConvertSettingPane;
import se233.Project1.view.DropPane;
import se233.Project1.view.LoadingPane;

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

    @FXML private ListView<String> inputListView;
    private final Map<String, String> filePathMap = new HashMap<>();

    @FXML
    public void initialize() {
        convertPane = new ConvertSettingPane(qualitySlider, formatComboBox);
        loadingPane = new LoadingPane(file1ProgressBar, file2ProgressBar, file3ProgressBar, file4ProgressBar, file5ProgressBar);
        dropPane = new DropPane(dropZone, dropFileLabel, inputListView, filePathMap);

    }

    public void changeFormat() {
        String selectedFilePath = dropPane.getSelectedFilePath();
        String selectedFormat = formatComboBox.getValue();
        if (selectedFilePath != null) {
            String outputPath = selectedFilePath.substring(0, selectedFilePath.lastIndexOf('.')) + "." +selectedFilePath;
            try {
                ChangeFormatTask task = new ChangeFormatTask("/opt/homebrew/bin/ffmpeg", "/opt/homebrew/bin/ffprobe");
                task.convertToMp3(selectedFilePath, outputPath);
                System.out.println("Converted: " + outputPath);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Conversion failed for: " + selectedFilePath);
            }
        } else {
            System.out.println("No file selected.");
        }
    }

}
