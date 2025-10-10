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




    private void createDefaultRow() {
        Label defaultLabel = new Label("(No file yet)");
        ComboBox<String> defaultCombo = new ComboBox<>();
        defaultCombo.getItems().addAll("mp3", "wav", "m4a", "ogg", "mp4");
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
            comboBox.getItems().addAll("mp3", "wav", "m4a", "flac", "ogg", "mp4");
            comboBox.setValue("mp3");

            // 🔥 Fire callback when filename clicked
            nameLabel.setOnMouseClicked(e -> {
                if (onFileSelected != null) {
                    onFileSelected.accept(fileName);
                }
            });

            // ✅ 🔥 Fire callback when format is changed
            comboBox.setOnAction(e -> {
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
