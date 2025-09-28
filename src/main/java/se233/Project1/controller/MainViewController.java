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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.util.StringConverter;


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
    @FXML private Button advancedSettingButton;
    @FXML private Label newFile1Name;
    @FXML private Label newFile2Name;
    @FXML private Label newFile3Name;
    @FXML private Label newFile4Name;
    @FXML private Label newFile5Name;




    private DropPane dropPane;
    private ConvertSettingPane convertPane;
    private LoadingPane loadingPane;

    @FXML private ListView<String> inputListView;
    private final Map<String, String> filePathMap = new HashMap<>();
    private double selectedAdvancedBitrate = 128.0;
    private double selectedSampleRate = 44100.0;
    private boolean stereoEnabled = true;
    private String selectedBitDepth = "24-bit";
    private String selectedQuality = "Medium";
    private int selectedChannels = 2;
    private final ExecutorService executor = Executors.newFixedThreadPool(4); // 4 threads = 4 files at once




    private Popup advancedPopup;

    @FXML
    public void initialize() {
        convertPane = new ConvertSettingPane(fileSettingsContainer);

        loadingPane = new LoadingPane(file1ProgressBar, file2ProgressBar, file3ProgressBar, file4ProgressBar);
        loadingPane.attachLabels(newFile1Name, newFile2Name, newFile3Name, newFile4Name, newFile5Name);

        loadingPane.hideAll();

        dropPane = new DropPane(dropZone, dropFileLabel, filePathMap, convertPane::updateFileList);

        convertPane.setOnFileSelected(fileName -> {
            String format = convertPane.getFormatForFile(fileName);
            showAdvancedSettingsFor(format);
        });
    }



    @FXML
    public void changeFormat() {
        System.out.println("changeFormat called");
        Map<String, String> selectedFormats = convertPane.getSelectedFormats();
        if (selectedFormats.isEmpty()) {
            System.out.println("No files to convert.");
            return;
        }

        int index = 0;
        for (Map.Entry<String, String> entry : selectedFormats.entrySet()) {
            final int progressIndex = index;
            index++;

            String fileName = entry.getKey();
            String selectedFormat = entry.getValue();
            String inputPath = filePathMap.get(fileName);
            if (inputPath == null) continue;

            String outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + "." + selectedFormat;

            javafx.application.Platform.runLater(() -> loadingPane.showProgress(progressIndex, fileName));

            executor.submit(() -> {
                try {
                    ChangeFormatTask task = new ChangeFormatTask("/opt/homebrew/bin/ffmpeg", "/opt/homebrew/bin/ffprobe");

                    task.convertToFormatWithProgress(
                            inputPath,
                            outputPath,
                            selectedFormat,
                            (int) selectedAdvancedBitrate,
                            selectedChannels,
                            (int) selectedSampleRate,
                            progress -> updateProgressUI(progressIndex, fileName, progress)
                    );

                    System.out.println("✅ Converted: " + fileName + " → " + outputPath);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("❌ Conversion failed for: " + fileName);
                }
            });
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

//    private Node createSetting(String ext) {
//        VBox container = new VBox(5);
//        container.setStyle("-fx-padding: 10;");
//
//        Slider slider = new Slider();
//        Label currentLabel = new Label();
//
//        double[] values;
//        String[] labels;
//        double defaultValue;
//
//        switch (ext.toLowerCase()) {
//            case "mp3":
//                values = new double[]{64, 128, 192, 320};
//                labels = new String[]{"Economy", "Standard", "Good", "Best"};
//                defaultValue = 128; // Standard
//                break;
//            case "ogg":
//                values = new double[]{64, 128, 192, 320};
//                labels = new String[]{"Economy", "Standard", "Good", "Best"};
//                defaultValue = 192; // Good
//                break;
//            case "wav":
//                values = new double[]{20, 44.1, 48, 96};
//                labels = new String[]{"Tape", "CD", "DVD", "Extra High"};
//                defaultValue = 44.1; // CD
//                break;
//            default:
//                container.getChildren().add(new Label("No advanced settings for " + ext));
//                return container;
//        }
//
//        // ตั้งค่า slider
//        slider.setMin(values[0]);
//        slider.setMax(values[values.length - 1]);
//        slider.setValue(defaultValue);
//        slider.setShowTickMarks(true);
//        slider.setShowTickLabels(true);
//        slider.setSnapToTicks(true); // ปิด snap ปกติ
//        slider.setMajorTickUnit(values[1] - values[0]);
//        slider.setMinorTickCount(0);
//
//        // ฟังก์ชันหา closest value และ update label
//        Runnable updateLabel = () -> {
//            int index = 0;
//            double closest = values[0];
//            for (int i = 0; i < values.length; i++) {
//                if (Math.abs(slider.getValue() - values[i]) < Math.abs(slider.getValue() - closest)) {
//                    closest = values[i];
//                    index = i;
//                }
//            }
//            slider.setValue(values[index]);
//            currentLabel.setText("Selected: " + labels[index]);
//
//            // ✅ Save this to use during conversion
//            selectedAdvancedBitrate = values[index];
//        };
//
//
//        // เรียกครั้งแรก
//        updateLabel.run();
//
//        // listener
//        slider.valueProperty().addListener((obs, oldVal, newVal) -> updateLabel.run());
//
//        container.getChildren().addAll(new Label(ext.toUpperCase() + " Quality"), slider, currentLabel);
//        return container;
//    }
    private Node createSetting(String ext) {
        VBox container = new VBox(5);
        container.setStyle("-fx-padding: 10;");
        container.setPrefSize(200, 180);

        switch (ext.toLowerCase()) {
            case "mp3" -> buildMp3Settings(container);
            case "wav" -> buildWavSettings(container);
            case "m4a" -> buildM4aSettings(container);
            case "ogg" -> buildOggSettings(container);
            default -> container.getChildren().add(new Label("No advanced settings for " + ext));
        }

        return container;
    }


    private void showAdvancedSettingsFor(String format) {
        if (advancedPopup != null && advancedPopup.isShowing()) {
            advancedPopup.hide();
        }

        advancedPopup = new Popup();
        Node settingsUI = createSetting(format);

        VBox popupContent = new VBox(10, settingsUI);
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: gray;");
        popupContent.setPrefSize(300, 200);
        popupContent.setMinSize(300, 200);
        popupContent.setMaxSize(300, 200);
        advancedPopup.getContent().clear();
        advancedPopup.getContent().add(popupContent);

        Bounds bounds = advancedSettingButton.localToScreen(advancedSettingButton.getBoundsInLocal());
        advancedPopup.show(advancedSettingButton, bounds.getMaxX() + 5, bounds.getMinY());
    }
    private void buildMp3Settings(VBox container) {
        Label bitrateLabel = new Label("MP3 Quality");
        Slider bitrateSlider = new Slider();
        Label bitrateCurrentLabel = new Label();

        double[] bitrateValues = {64, 128, 192, 320};
        String[] bitrateLabels = {"Economy", "Standard", "Good", "Best"};

        bitrateSlider.setMin(0);
        bitrateSlider.setMax(3);
        bitrateSlider.setMajorTickUnit(1);
        bitrateSlider.setMinorTickCount(0);
        bitrateSlider.setSnapToTicks(true);
        bitrateSlider.setShowTickMarks(true);
        bitrateSlider.setShowTickLabels(true);

        bitrateSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return String.valueOf(bitrateValues[value.intValue()]);
            }

            @Override
            public Double fromString(String string) {
                return switch (string) {
                    case "64" -> 0.0;
                    case "128" -> 1.0;
                    case "192" -> 2.0;
                    case "320" -> 3.0;
                    default -> 1.0;
                };
            }
        });

        bitrateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();
            bitrateCurrentLabel.setText("Quality: " + bitrateValues[index] + " kbps (" + bitrateLabels[index] + ")");
            selectedAdvancedBitrate = bitrateValues[index];
        });

        bitrateSlider.setValue(1);
        bitrateCurrentLabel.setText("Quality: " + bitrateValues[1] + " kbps (" + bitrateLabels[1] + ")");
        selectedAdvancedBitrate = bitrateValues[1];

        Label sampleRateLabel = new Label("Sample Rate");
        ComboBox<String> sampleRateBox = new ComboBox<>();
        sampleRateBox.getItems().addAll("22050 Hz", "44100 Hz", "48000 Hz", "96000 Hz");
        sampleRateBox.setValue("44100 Hz");
        sampleRateBox.setOnAction(e -> {
            selectedSampleRate = Double.parseDouble(sampleRateBox.getValue().replace(" Hz", ""));
            System.out.println("Sample Rate: " + selectedSampleRate);
        });

        Label channelsLabel = new Label("Channels");
        ComboBox<String> channelsBox = new ComboBox<>();
        channelsBox.getItems().addAll("1 (Mono)", "2 (Stereo)");
        channelsBox.setValue("2 (Stereo)");
        channelsBox.setOnAction(e -> {
            selectedChannels = channelsBox.getValue().startsWith("1") ? 1 : 2;
            System.out.println("Channels: " + selectedChannels);
        });

        container.getChildren().addAll(
                bitrateLabel, bitrateSlider, bitrateCurrentLabel,
                sampleRateLabel, sampleRateBox,
                channelsLabel, channelsBox
        );
    }


    private void buildWavSettings(VBox container) {
        // --- Sample Rate ---
        Label srLabel = new Label("Sample Rate");
        ComboBox<String> sampleRateBox = new ComboBox<>();
        sampleRateBox.getItems().addAll("44100 Khz", "48000 Khz", "96000 Khz");
        sampleRateBox.setValue("48000 Khz");
        sampleRateBox.setOnAction(e -> selectedSampleRate = Double.parseDouble(sampleRateBox.getValue().replace(" Hz", "")));

        // --- Bit Depth ---
        Label bitDepthLabel = new Label("Bit Depth");
        ComboBox<String> bitDepthBox = new ComboBox<>();
        bitDepthBox.getItems().addAll("16-bit", "24-bit", "32-bit");
        bitDepthBox.setValue("24-bit");
        bitDepthBox.setOnAction(e -> selectedBitDepth = bitDepthBox.getValue());

        container.getChildren().addAll(srLabel, sampleRateBox, bitDepthLabel, bitDepthBox);
    }
    private void buildM4aSettings(VBox container){
        Label bitrateLabel = new Label("m4a Quality");
        Slider bitrateSlider = new Slider();
        Label bitrateCurrentLabel = new Label();

        double[] bitrateValues = {64, 128, 160, 265};
        String[] bitrateLabels = {"Economy", "Standard", "Good", "Best"};

        bitrateSlider.setMin(0);
        bitrateSlider.setMax(3);
        bitrateSlider.setMajorTickUnit(1);
        bitrateSlider.setMinorTickCount(0);
        bitrateSlider.setSnapToTicks(true);
        bitrateSlider.setShowTickMarks(true);
        bitrateSlider.setShowTickLabels(true);

        bitrateSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return String.valueOf(bitrateValues[value.intValue()]);
            }

            @Override
            public Double fromString(String string) {
                return switch (string) {
                    case "64" -> 0.0;
                    case "128" -> 1.0;
                    case "160" -> 2.0;
                    case "265" -> 3.0;
                    default -> 1.0;
                };
            }
        });

        bitrateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();
            bitrateCurrentLabel.setText("Quality: " + bitrateValues[index] + " kbps (" + bitrateLabels[index] + ")");
            selectedAdvancedBitrate = bitrateValues[index];
        });

        bitrateSlider.setValue(1);
        bitrateCurrentLabel.setText("Quality: " + bitrateValues[1] + " kbps (" + bitrateLabels[1] + ")");
        selectedAdvancedBitrate = bitrateValues[1];

        Label sampleRateLabel = new Label("Sample Rate");
        ComboBox<String> sampleRateBox = new ComboBox<>();
        sampleRateBox.getItems().addAll("8000 Khz", "11025 Khz", "12000 Khz", "16000 Khz", "22050 Khz", "24000 Khz", "3200 Khz", "44100 Khz", "48000 Khz");
        sampleRateBox.setValue("44100 Hz");
        sampleRateBox.setOnAction(e -> {
            selectedSampleRate = Double.parseDouble(sampleRateBox.getValue().replace(" Khz", ""));
            System.out.println("Sample Rate: " + selectedSampleRate);
        });

        Label channelsLabel = new Label("Channels");
        ComboBox<String> channelsBox = new ComboBox<>();
        channelsBox.getItems().addAll("1 (Mono)", "2 (Stereo)");
        channelsBox.setValue("2 (Stereo)");
        channelsBox.setOnAction(e -> {
            selectedChannels = channelsBox.getValue().startsWith("1") ? 1 : 2;
            System.out.println("Channels: " + selectedChannels);
        });

        container.getChildren().addAll(
                bitrateLabel, bitrateSlider, bitrateCurrentLabel,
                sampleRateLabel, sampleRateBox,
                channelsLabel, channelsBox
        );
    }
    private void buildOggSettings(VBox container) {
        Label bitrateLabel = new Label("Bitrate");
        Slider bitrateSlider = new Slider(0, 3, 1);
        double[] bitrates = {64, 128, 196, 320};
        bitrateSlider.setMajorTickUnit(1);
        bitrateSlider.setSnapToTicks(true);
        bitrateSlider.setShowTickMarks(true);
        bitrateSlider.setShowTickLabels(true);
        bitrateSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double val) {
                return bitrates[val.intValue()] + " kbps";
            }

            @Override
            public Double fromString(String s) {
                return 0.0;
            }
        });
        bitrateSlider.valueProperty().addListener((obs, oldV, newV) -> selectedAdvancedBitrate = bitrates[newV.intValue()]);

        // --- Quality Preset ---
        Label qualityLabel = new Label("Quality Preset");
        ComboBox<String> qualityBox = new ComboBox<>(FXCollections.observableArrayList("Low", "Medium", "High"));
        qualityBox.setValue("Medium");
        qualityBox.setOnAction(e -> selectedQuality = qualityBox.getValue());

        container.getChildren().addAll(bitrateLabel, bitrateSlider, qualityLabel, qualityBox);
    }
    private void updateProgressUI(int index, String fileName, double progress) {
        javafx.application.Platform.runLater(() -> {
            loadingPane.setFileName(index, fileName);
            loadingPane.setProgress(index, progress);
        });
    }

    @FXML
    public void onExit() {
        executor.shutdown();
    }




}



