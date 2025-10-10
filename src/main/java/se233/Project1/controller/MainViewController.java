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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import se233.Project1.model.FileSettings;
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



    @FXML private ProgressBar file1ProgressBar;
    @FXML private ProgressBar file2ProgressBar;
    @FXML private ProgressBar file3ProgressBar;
    @FXML private ProgressBar file4ProgressBar;
    @FXML private VBox fileSettingsContainer;
    @FXML private Button convertButton;
    @FXML private Button advancedSettingButton;
    @FXML private Label newFile1Name;
    @FXML private Label newFile2Name;
    @FXML private Label newFile3Name;
    @FXML private Label newFile4Name;
    @FXML private Label newFile5Name;
    @FXML private Button browseOutputButton;

    private File outputDirectory;



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
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Map<String, FileSettings> fileSettingsMap = new HashMap<>();
    private String currentFileName;
    private final Map<String, Node> settingsUIMap = new HashMap<>();
    private File selectedImageFile;
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
            showAdvancedSettingsFor(fileName, format);
        });

    }



    @FXML
    public void changeFormat() {
        System.out.println("changeFormat called");
        Map<String, String> selectedFormats = convertPane.getSelectedFormats();
        if (selectedFormats.isEmpty() || filePathMap.isEmpty()) {
            Alert noFileAlert = new Alert(Alert.AlertType.WARNING);
            noFileAlert.setTitle("No Files Selected");
            noFileAlert.setHeaderText("Nothing to convert");
            noFileAlert.setContentText("Please add at least one file before converting.");
            noFileAlert.showAndWait();
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

            String outputPath;
            if (outputDirectory != null) {
                outputPath = new File(outputDirectory,
                        fileName.substring(0, fileName.lastIndexOf('.')) + "." + selectedFormat
                ).getAbsolutePath();
            } else {
                outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + "." + selectedFormat;
            }

            javafx.application.Platform.runLater(() -> loadingPane.showProgress(progressIndex, fileName));

            executor.submit(() -> {
                try {
                    ChangeFormatTask task = new ChangeFormatTask("/opt/homebrew/bin/ffmpeg", "/opt/homebrew/bin/ffprobe");

                    FileSettings settings = fileSettingsMap.getOrDefault(fileName, new FileSettings(128, 44100, 2));
                    task.convertToFormatWithProgress(
                            inputPath,
                            outputPath,
                            selectedFormat,
                            (int) settings.bitrate,
                            settings.channels,
                            (int) settings.sampleRate,
                            settings.isVBR,
                            selectedImageFile,
                            progress -> updateProgressUI(progressIndex, fileName, progress)
                    );


                    System.out.println("Converted: " + fileName + " → " + outputPath);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Conversion failed for: " + fileName);
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


    private Node createSetting(String ext) {
        VBox container = new VBox(5);
        container.setStyle("-fx-padding: 10;");
        container.setPrefSize(300, 180);

        switch (ext.toLowerCase()) {
            case "mp3" -> buildMp3Settings(container);
            case "wav" -> buildWavSettings(container);
            case "m4a" -> buildM4aSettings(container);
            case "flac" -> buildFlacSettings(container);
            case "ogg" -> buildOggSettings(container);
            case "mp4" -> buildMp4Settings(container);

            default -> container.getChildren().add(new Label("No advanced settings for " + ext));
        }

        return container;
    }


    private void showAdvancedSettingsFor(String fileName, String format) {
        this.currentFileName = fileName;

        // ✅ Create or retrieve the settings object for this file
        FileSettings fs = fileSettingsMap.computeIfAbsent(fileName, f -> new FileSettings(128, 44100, 2));

        // ✅ If the format changed, clear the old UI so a new one is built
        if (fs.format != null && !fs.format.equalsIgnoreCase(format)) {
            settingsUIMap.remove(fileName);
        }

        // ✅ Always store the current format for reference
        fs.format = format;

        // 🔁 Now continue with the UI logic
        if (advancedPopup != null && advancedPopup.isShowing()) {
            advancedPopup.hide();
        }

        Node settingsUI = settingsUIMap.computeIfAbsent(fileName, f -> createSetting(format));

        advancedPopup = new Popup();
        VBox popupContent = new VBox(10, settingsUI);
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: gray;");
        popupContent.setPrefSize(300, 300);
        popupContent.setMinSize(300, 300);
        popupContent.setMaxSize(300, 300);

        advancedPopup.getContent().clear();
        advancedPopup.getContent().add(popupContent);

        Bounds bounds = advancedSettingButton.localToScreen(advancedSettingButton.getBoundsInLocal());
        advancedPopup.show(advancedSettingButton, bounds.getMaxX() + 5, bounds.getMinY());
    }


    private void buildMp3Settings(VBox container) {
        // --- 🔊 Always-visible Quality Slider ---
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
            fileSettingsMap.computeIfAbsent(currentFileName, f -> new FileSettings(128, 44100, 2)).bitrate = bitrateValues[index];
        });

        bitrateSlider.setValue(1);
        bitrateCurrentLabel.setText("Quality: " + bitrateValues[1] + " kbps (" + bitrateLabels[1] + ")");

        // --- ⚙️ Bitrate Mode Section (NEW) ---
        Label modeLabel = new Label("Bitrate Mode");
        RadioButton constantRadio = new RadioButton("Constant");
        RadioButton variableRadio = new RadioButton("Variable");
        ToggleGroup modeGroup = new ToggleGroup();
        constantRadio.setToggleGroup(modeGroup);
        variableRadio.setToggleGroup(modeGroup);
        constantRadio.setSelected(true);

        HBox modeRow = new HBox(10, modeLabel, constantRadio, variableRadio);

// --- Bitrate selection row (SAME LINE) ---
        Label bitrateChoiceLabel = new Label("Bitrate:");
        ComboBox<String> constantBox = new ComboBox<>();
        constantBox.getItems().addAll("32 kbps", "40 kbps", "48 kbps", "56 kbps", "64 kbps", "80 kbps", "96 kbps", "112 kbps", "128 kbps" , "160 kbps",  "192 kbps", "224 kbps", "256 kbps", "320 kbps");
        constantBox.setValue(bitrateValues[1] + " kbps");

        bitrateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();
            String bitrateText = bitrateValues[index] + " kbps (" + bitrateLabels[index] + ")";
            bitrateCurrentLabel.setText("Quality: " + bitrateText);
            fileSettingsMap.computeIfAbsent(currentFileName, f -> new FileSettings(128, 44100, 2)).bitrate = bitrateValues[index];

            constantBox.setValue(bitrateValues[index] + " kbps"); // 🔄 Keep ComboBox in sync
        });

        ComboBox<String> variableBox = new ComboBox<>();
        variableBox.getItems().addAll("V0 (Highest)", "V1", "V2", "V3", "V4", "V5 (Lowest)");
        variableBox.setValue("V2");
        variableBox.setVisible(false);

// Put both dropdowns in same HBox so layout stays the same
        HBox bitrateChoiceRow = new HBox(10, bitrateChoiceLabel, constantBox, variableBox);

// Toggle between the two dropdowns
        modeGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean isConstant = constantRadio.isSelected();
            constantBox.setVisible(isConstant);
            variableBox.setVisible(!isConstant);

            FileSettings fs = fileSettingsMap.computeIfAbsent(currentFileName, f -> new FileSettings(128, 44100, 2));
            fs.isVBR = !isConstant;
        });

// Save bitrate choices
        constantBox.setOnAction(e -> {
            double bitrate = Double.parseDouble(constantBox.getValue().replace(" kbps", ""));
            fileSettingsMap.computeIfAbsent(currentFileName, f -> new FileSettings(128, 44100, 2)).bitrate = bitrate;
        });

        variableBox.setOnAction(e -> {
            int vbrLevel = switch (variableBox.getValue()) {
                case "V0 (Highest)" -> 0;
                case "V1" -> 1;
                case "V2" -> 2;
                case "V3" -> 3;
                case "V4" -> 4;
                case "V5 (Lowest)" -> 5;
                default -> 2;
            };
            fileSettingsMap.computeIfAbsent(currentFileName, f -> new FileSettings(128, 44100, 2)).bitrate = vbrLevel;
        });

        // --- 📶 Sample Rate ---
        Label sampleRateLabel = new Label("Sample Rate");
        ComboBox<String> sampleRateBox = new ComboBox<>();
        sampleRateBox.getItems().addAll("32000 Hz", "44100 Hz", "48000 Hz");
        sampleRateBox.setValue("44100 Hz");
        sampleRateBox.setOnAction(e -> {
            double sr = Double.parseDouble(sampleRateBox.getValue().replace(" Hz", ""));
            fileSettingsMap.computeIfAbsent(currentFileName, f -> new FileSettings(128, 44100, 2)).sampleRate = sr;
        });

        // --- 🔊 Channels ---
        Label channelsLabel = new Label("Channels");
        ComboBox<String> channelsBox = new ComboBox<>();
        channelsBox.getItems().addAll("1 (Mono)", "2 (Stereo)");
        channelsBox.setValue("2 (Stereo)");
        channelsBox.setOnAction(e -> {
            int ch = channelsBox.getValue().startsWith("1") ? 1 : 2;
            fileSettingsMap.computeIfAbsent(currentFileName, f -> new FileSettings(128, 44100, 2)).channels = ch;
        });
        modeGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean isConstant = constantRadio.isSelected();
            constantBox.setVisible(isConstant);
            variableBox.setVisible(!isConstant);

            FileSettings fs = fileSettingsMap.computeIfAbsent(currentFileName, f -> new FileSettings(128, 44100, 2));
            fs.isVBR = !isConstant;
        });


        container.getChildren().addAll(
                bitrateLabel, bitrateSlider, bitrateCurrentLabel,
                modeRow,
                bitrateChoiceRow,
                sampleRateLabel, sampleRateBox,
                channelsLabel, channelsBox
        );

    }



    private void buildWavSettings(VBox container) {

        Label bitrateLabel = new Label("wav Quality");
        Slider bitrateSlider = new Slider();
        Label bitrateCurrentLabel = new Label();

        double[] bitrateValues = {20, 44.1, 48, 96};
        String[] bitrateLabels = {"Tape", "CD Quality", "DVD", "Extra High"};

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
                    case "20" -> 0.0;
                    case "44.1" -> 1.0;
                    case "48" -> 2.0;
                    case "96" -> 3.0;
                    default -> 1.0;
                };
            }
        });

        bitrateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();
            bitrateCurrentLabel.setText("Quality: " + bitrateValues[index] + " Khz (" + bitrateLabels[index] + ")");
            selectedAdvancedBitrate = bitrateValues[index];
        });

        bitrateSlider.setValue(2);
        bitrateCurrentLabel.setText("Quality: " + bitrateValues[1] + " Khz (" + bitrateLabels[1] + ")");
        selectedAdvancedBitrate = bitrateValues[1];

        // --- Sample Rate ---
        Label srLabel = new Label("Sample Rate");
        ComboBox<String> sampleRateBox = new ComboBox<>();
        sampleRateBox.getItems().addAll(
                "8000 Hz",
                "11025 Hz",
                "16000 Hz",
                "22050 Hz",
                "32000 Hz",
                "44100 Hz",
                "48000 Hz",
                "88200 Hz",
                "96000 Hz"
        );
        sampleRateBox.setValue("48000 Hz");
        sampleRateBox.setOnAction(e -> {
            double sr = Double.parseDouble(sampleRateBox.getValue().replace(" Hz", ""));
            selectedSampleRate = sr;
            fileSettingsMap.computeIfAbsent(currentFileName, f -> new FileSettings(128, 44100, 2)).sampleRate = sr;
        });

        Label channelsLabel = new Label("Channels");
        ComboBox<String> channelsBox = new ComboBox<>();
        channelsBox.getItems().addAll("1 (Mono)", "2 (Stereo)");
        channelsBox.setValue("2 (Stereo)");
        channelsBox.setOnAction(e -> {
            int ch = channelsBox.getValue().startsWith("1") ? 1 : 2;
            fileSettingsMap.computeIfAbsent(currentFileName, f -> new FileSettings(128, 44100, 2)).channels = ch;
        });

        container.getChildren().addAll(bitrateLabel, bitrateSlider, bitrateCurrentLabel,srLabel, sampleRateBox, channelsLabel, channelsBox);
    }
    private void buildM4aSettings(VBox container){
        Label bitrateLabel = new Label("m4a Quality");
        Slider bitrateSlider = new Slider();
        Label bitrateCurrentLabel = new Label();

        double[] bitrateValues = {64, 128, 160, 256};
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
                    case "256" -> 3.0;
                    default -> 2.0;
                };
            }
        });

        bitrateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();
            bitrateCurrentLabel.setText("Quality: " + bitrateValues[index] + " kbps (" + bitrateLabels[index] + ")");
            selectedAdvancedBitrate = bitrateValues[index];
        });

        bitrateSlider.setValue(2);
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
        Label bitrateLabel = new Label("Ogg Quality");
        Slider bitrateSlider = new Slider();
        Label bitrateCurrentLabel = new Label();

        double[] bitrateValues = {64, 128, 160, 256};
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
                    case "256" -> 3.0;
                    default -> 2.0;
                };
            }
        });

        bitrateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();
            bitrateCurrentLabel.setText("Quality: " + bitrateValues[index] + " kbps (" + bitrateLabels[index] + ")");
            selectedAdvancedBitrate = bitrateValues[index];
        });

        bitrateSlider.setValue(2);
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

    public void buildFlacSettings(VBox container){
        Label bitrateLabel = new Label("Bitrate: 128 kbps");
        selectedAdvancedBitrate = 32;
        Label sampleRateLabel = new Label("Sample Rate");
        ComboBox<String> sampleRateBox = new ComboBox<>();
        sampleRateBox.getItems().addAll("8000 Khz", "11025 Khz", "12000 Khz", "16000 Khz", "22050 Khz", "24000 Khz", "32000 Khz", "44100 Khz", "48000 Khz");
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
                bitrateLabel,
                sampleRateLabel, sampleRateBox,
                channelsLabel, channelsBox
        );

    }
    public void buildMp4Settings(VBox container) {
        Label bitrateLabel = new Label("Mp4 Quality");
        Slider bitrateSlider = new Slider();
        Label bitrateCurrentLabel = new Label();

        double[] bitrateValues = {64, 128, 160, 256};
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
                    case "256" -> 3.0;
                    default -> 2.0;
                };
            }
        });

        bitrateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();
            bitrateCurrentLabel.setText("Quality: " + bitrateValues[index] + " kbps (" + bitrateLabels[index] + ")");
            selectedAdvancedBitrate = bitrateValues[index];
        });

        bitrateSlider.setValue(2);
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

        Label imageLabel = new Label("Thumbnail Image:");
        Label selectedImageLabel = new Label("No image selected");
        Button chooseImageButton = new Button("Choose Image");

        chooseImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Thumbnail Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());
            if (file != null) {
                selectedImageFile = file;
                selectedImageLabel.setText("✅ " + file.getName());
            }
        });

        HBox imageRow = new HBox(10, imageLabel, chooseImageButton, selectedImageLabel);

        container.getChildren().addAll(
                bitrateLabel, bitrateSlider, bitrateCurrentLabel,
                sampleRateLabel, sampleRateBox,
                channelsLabel, channelsBox,
                imageRow
        );
    }
    private void updateProgressUI(int index, String fileName, double progress) {
        javafx.application.Platform.runLater(() -> {
            loadingPane.setFileName(index, fileName);
            loadingPane.setProgress(index, progress);
        });
    }
    @FXML
    private void handleBrowseOutput(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Folder");

        File selectedDir = directoryChooser.showDialog(browseOutputButton.getScene().getWindow());
        if (selectedDir != null) {
            outputDirectory = selectedDir; // ✅ store as File

            // Change button text to show the folder name
            String folderName = selectedDir.getName();
            if (folderName.length() > 20) {
                folderName = folderName.substring(0, 17) + "...";
            }
            browseOutputButton.setText("📁 " + folderName);
        }
    }





    @FXML
    public void onExit() {
        executor.shutdown();
    }




}



