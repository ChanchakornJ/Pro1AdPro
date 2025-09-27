package se233.Project1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    @FXML private VBox fileSettingsContainer;
    @FXML private Button convertButton;



    private DropPane dropPane;
    private ConvertSettingPane convertPane;
    private LoadingPane loadingPane;

    @FXML private ListView<String> inputListView;
    private final Map<String, String> filePathMap = new HashMap<>();


    @FXML
    public void initialize() {
            convertPane = new ConvertSettingPane(qualitySlider, fileSettingsContainer, convertButton);
            loadingPane = new LoadingPane(file1ProgressBar, file2ProgressBar, file3ProgressBar, file4ProgressBar, file5ProgressBar);
            dropPane = new DropPane(dropZone, dropFileLabel, filePathMap, convertPane::updateFileList);
    }


    @FXML
    public void changeFormat() {
        System.out.println("changeFormat called");
        Map<String, String> selectedFormats = convertPane.getSelectedFormats();
        int bitrate = convertPane.getSelectedBitrate();

        if (selectedFormats.isEmpty()) {
            System.out.println("No files to convert.");
            return;
        }

        for (Map.Entry<String, String> entry : selectedFormats.entrySet()) {
            String fileName = entry.getKey();
            String selectedFormat = entry.getValue();
            String inputPath = filePathMap.get(fileName);

            if (inputPath == null) {
                System.out.println("File path not found for: " + fileName);
                continue;
            }

            String outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + "." + selectedFormat;

            try {
                ChangeFormatTask task = new ChangeFormatTask("/opt/homebrew/bin/ffmpeg", "/opt/homebrew/bin/ffprobe");

                task.convertToFormat(inputPath, outputPath, selectedFormat, bitrate);

                System.out.println("Converted: " + fileName + " → " + outputPath);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Conversion failed for: " + fileName);
            }
        }
    }

}



