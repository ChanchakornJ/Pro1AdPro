package se233.Project1.view;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConvertSettingPane {

    private final VBox fileSettingsContainer;
    private final Map<String, ComboBox<String>> formatSelectors = new HashMap<>();
    private Consumer<String> onFileSelected;


    public ConvertSettingPane(VBox fileSettingsContainer) {
        this.fileSettingsContainer = fileSettingsContainer;
        createDefaultRow();
    }

    public void setOnFileSelected(Consumer<String> listener) {
        this.onFileSelected = listener;
    }


//    private void setupSlider() {
//        qualitySlider.setMin(0);
//        qualitySlider.setMax(3);
//        qualitySlider.setMajorTickUnit(1);
//        qualitySlider.setMinorTickCount(0);
//        qualitySlider.setSnapToTicks(true);
//        qualitySlider.setShowTickMarks(true);
//        qualitySlider.setShowTickLabels(true);
//
//        qualitySlider.setLabelFormatter(new StringConverter<Double>() {
//            @Override
//            public String toString(Double value) {
//                return switch (value.intValue()) {
//                    case 0 -> "64";
//                    case 1 -> "128";
//                    case 2 -> "196";
//                    case 3 -> "320";
//                    default -> "";
//                };
//            }
//
//            @Override
//            public Double fromString(String string) {
//                return switch (string) {
//                    case "64" -> 0.0;
//                    case "128" -> 1.0;
//                    case "196" -> 2.0;
//                    case "320" -> 3.0;
//                    default -> 1.0;
//                };
//            }
//        });
//        qualitySlider.setValue(1);
//    }



    private void createDefaultRow() {
        Label defaultLabel = new Label("(No file yet)");
        ComboBox<String> defaultCombo = new ComboBox<>();
        defaultCombo.getItems().addAll("mp3", "wav", "m4a", "ogg");
        defaultCombo.setValue("mp3");


        HBox row = new HBox(10, defaultLabel, defaultCombo);
        fileSettingsContainer.getChildren().add(row);
        formatSelectors.put("(No file yet)", defaultCombo);
    }

    public void updateFileList(List<File> files) {
        fileSettingsContainer.getChildren().clear();
        formatSelectors.clear();

        for (File file : files) {
            String fileName = file.getName();
            Label nameLabel = new Label(fileName);
            nameLabel.setStyle("-fx-cursor: hand; -fx-text-fill: #0077cc;");

            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.getItems().addAll("mp3", "wav", "m4a", "ogg");
            comboBox.setValue("mp3");

            nameLabel.setOnMouseClicked(e -> {
                if (onFileSelected != null) {
                    onFileSelected.accept(fileName);
                }
            });

            HBox row = new HBox(10, nameLabel, comboBox);
            fileSettingsContainer.getChildren().add(row);
            formatSelectors.put(fileName, comboBox);
        }

        if (files.isEmpty()) {
            createDefaultRow();
        }
    }

    public Map<String, String> getSelectedFormats() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, ComboBox<String>> entry : formatSelectors.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getValue());
        }
        return result;
    }

    public String getFormatForFile(String fileName) {
        ComboBox<String> comboBox = formatSelectors.get(fileName);
        return comboBox != null ? comboBox.getValue() : "mp3";
    }

}
