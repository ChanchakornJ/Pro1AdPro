package se233.Project1.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
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
    @FXML private Button advancedSettingButton; // ปุ่มจาก SceneBuilder




    private DropPane dropPane;
    private ConvertSettingPane convertPane;
    private LoadingPane loadingPane;

    @FXML private ListView<String> inputListView;
    private final Map<String, String> filePathMap = new HashMap<>();

    private Popup advancedPopup;

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


    @FXML
    private void handleAdvancedSetting(ActionEvent event) {
        if (advancedPopup == null) {
            // popup สร้างครั้งเดียว
            advancedPopup = new Popup();

            // ตัวอย่าง: สมมติไฟล์เป็น mp3
            Node settingsUI = createSetting("mp3");

            VBox popupContent = new VBox(10, settingsUI);
            popupContent.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: gray;");

            advancedPopup.getContent().add(popupContent);
        }

        if (!advancedPopup.isShowing()) {
            Bounds bounds = advancedSettingButton.localToScreen(advancedSettingButton.getBoundsInLocal());
            advancedPopup.show(advancedSettingButton, bounds.getMaxX() + 5, bounds.getMinY());
        } else {
            advancedPopup.hide();
        }
    }

    private Node createSetting(String ext) {
        VBox container = new VBox(5);
        container.setStyle("-fx-padding: 10;");

        Slider slider = new Slider();
        Label currentLabel = new Label();

        double[] values;
        String[] labels;
        double defaultValue;

        switch (ext.toLowerCase()) {
            case "mp3":
                values = new double[]{64, 128, 192, 320};
                labels = new String[]{"Economy", "Standard", "Good", "Best"};
                defaultValue = 128; // Standard
                break;
            case "ogg":
                values = new double[]{64, 128, 192, 320};
                labels = new String[]{"Economy", "Standard", "Good", "Best"};
                defaultValue = 192; // Good
                break;
            case "wav":
                values = new double[]{20, 44.1, 48, 96};
                labels = new String[]{"Tape", "CD", "DVD", "Extra High"};
                defaultValue = 44.1; // CD
                break;
            default:
                container.getChildren().add(new Label("No advanced settings for " + ext));
                return container;
        }

        // ตั้งค่า slider
        slider.setMin(values[0]);
        slider.setMax(values[values.length - 1]);
        slider.setValue(defaultValue);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setSnapToTicks(true); // ปิด snap ปกติ
        slider.setMajorTickUnit(values[1] - values[0]);
        slider.setMinorTickCount(0);

        // ฟังก์ชันหา closest value และ update label
        Runnable updateLabel = () -> {
            int index = 0;
            double closest = values[0];
            for (int i = 0; i < values.length; i++) {
                if (Math.abs(slider.getValue() - values[i]) < Math.abs(slider.getValue() - closest)) {
                    closest = values[i];
                    index = i;
                }
            }
            slider.setValue(values[index]); // snap ให้ตรงค่าใน array
            currentLabel.setText("Selected: " + labels[index]);
        };

        // เรียกครั้งแรก
        updateLabel.run();

        // listener
        slider.valueProperty().addListener((obs, oldVal, newVal) -> updateLabel.run());

        container.getChildren().addAll(new Label(ext.toUpperCase() + " Quality"), slider, currentLabel);
        return container;
    }

}



