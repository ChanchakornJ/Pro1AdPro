package se233.Project1.view;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import javafx.application.Platform;

public class ConvertSettingPane {

    private final VBox fileSettingsContainer;
    private final Map<String, ComboBox<String>> formatSelectors = new HashMap<>();
    private final Set<String> selectedFiles = new HashSet<>();

    private Consumer<String> onFileSelected;

    public ConvertSettingPane(VBox fileSettingsContainer) {
        this.fileSettingsContainer = fileSettingsContainer;
        createDefaultRow();
    }

    public void setOnFileSelected(Consumer<String> listener) {
        this.onFileSelected = listener;
    }

    // --------------------------
    // 🧩 Default placeholder row
    // --------------------------
    private void createDefaultRow() {
        Label defaultLabel = new Label("(No file yet)");
        ComboBox<String> defaultCombo = new ComboBox<>();
        defaultCombo.getItems().addAll("mp3", "wav", "m4a", "flac", "ogg", "mp4");
        defaultCombo.setValue("mp3");

        HBox row = new HBox(10, defaultLabel, defaultCombo);
        fileSettingsContainer.getChildren().add(row);
        formatSelectors.put("(No file yet)", defaultCombo);
    }

    // --------------------------
    // 🔄 Called when files dropped
    // --------------------------
    public void updateFileList(List<File> newFiles) {
        if (newFiles == null || newFiles.isEmpty()) return;

        fileSettingsContainer.getChildren().removeIf(node -> node instanceof HBox &&
                ((HBox) node).getChildren().stream().anyMatch(n -> n instanceof Label && ((Label) n).getText().equals("(No file yet)"))
        );

        for (File file : newFiles) {
            String fileName = file.getName();

            if (formatSelectors.containsKey(fileName)) continue;

            CheckBox selectBox = new CheckBox();
            Label nameLabel = new Label(fileName);
            nameLabel.setStyle("-fx-cursor: hand; -fx-text-fill: #0077cc;");

            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.getItems().addAll("mp3", "wav", "m4a", "flac", "ogg", "mp4");
            comboBox.setValue("mp3");

            Button deleteButton = new Button("✖");
            deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #cc0000; -fx-font-size: 14px; -fx-cursor: hand;");
            deleteButton.setOnAction(e -> removeFile(fileName));

            selectBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) selectedFiles.add(fileName);
                else selectedFiles.remove(fileName);
            });

            nameLabel.setOnMouseClicked(e -> {
                if (onFileSelected != null) onFileSelected.accept(fileName);
            });
            comboBox.setOnAction(e -> {
                if (onFileSelected != null) onFileSelected.accept(fileName);
            });

            HBox row = new HBox(10, selectBox, nameLabel, comboBox, deleteButton);
            fileSettingsContainer.getChildren().add(row);

            formatSelectors.put(fileName, comboBox);
        }
    }


    // --------------------------
    // 🔍 Retrieve all formats
    // --------------------------
    public Map<String, String> getSelectedFormats() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, ComboBox<String>> entry : formatSelectors.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getValue());
        }
        return result;
    }

    // --------------------------
    // 🎯 Get or set single file format
    // --------------------------
    public String getFormatForFile(String fileName) {
        ComboBox<String> comboBox = formatSelectors.get(fileName);
        return comboBox != null ? comboBox.getValue() : "mp3";
    }

    public void setFormatForFile(String fileName, String format) {
        ComboBox<String> comboBox = formatSelectors.get(fileName);
        if (comboBox != null) comboBox.setValue(format);
    }

    // --------------------------
    // 🧠 Selected files tracking
    // --------------------------
    public Set<String> getSelectedFiles() {
        return Collections.unmodifiableSet(selectedFiles);
    }

    // --------------------------
    // ⚙️ Apply to all files
    // --------------------------
    public void applyFormatToAll(String format) {
        Platform.runLater(() -> {
            for (Map.Entry<String, ComboBox<String>> entry : formatSelectors.entrySet()) {
                ComboBox<String> combo = entry.getValue();

                // Temporarily remove listener to prevent interference
                combo.setOnAction(null);
                combo.setValue(format);
            }

            // Reattach listeners after setting all formats
            for (Map.Entry<String, ComboBox<String>> entry : formatSelectors.entrySet()) {
                String fileName = entry.getKey();
                ComboBox<String> combo = entry.getValue();

                combo.setOnAction(e -> {
                    if (onFileSelected != null) onFileSelected.accept(fileName);
                });
            }

            // Force layout refresh just in case
            fileSettingsContainer.requestLayout();
        });
    }
    public void refreshUI() {
        // Force all combo boxes to visually update
        for (Map.Entry<String, ComboBox<String>> entry : formatSelectors.entrySet()) {
            ComboBox<String> combo = entry.getValue();
            combo.getSelectionModel().select(combo.getValue());
        }
        // This forces the VBox to re-layout and show changes
        fileSettingsContainer.requestLayout();
    }
    private void removeFile(String fileName) {
        // 1. Remove from tracking maps
        formatSelectors.remove(fileName);
        selectedFiles.remove(fileName);

        // 2. Remove corresponding row from VBox
        fileSettingsContainer.getChildren().removeIf(node -> {
            if (node instanceof HBox hbox) {
                for (var n : hbox.getChildren()) {
                    if (n instanceof Label label && label.getText().equals(fileName)) {
                        return true;
                    }
                }
            }
            return false;
        });

        // 3. Show default placeholder if empty
        if (formatSelectors.isEmpty()) {
            createDefaultRow();
        }
    }

}
