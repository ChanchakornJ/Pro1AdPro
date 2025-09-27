package se233.Project1.view;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.util.Map;

public class DropPane {
    private final AnchorPane dropZone;
    private final Label dropFileLabel;
    private final ListView<String> inputListView;
    private final Map<String, String> filePathMap;

    public DropPane(AnchorPane dropZone, Label dropFileLabel, ListView<String> inputListView, Map<String, String> filePathMap) {
        this.dropZone = dropZone;
        this.dropFileLabel = dropFileLabel;
        this.inputListView = inputListView;
        this.filePathMap = filePathMap;
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                dropFileLabel.setText("Release to drop files"); // optional UX feedback
            }
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    inputListView.getItems().add(file.getName());
                    filePathMap.put(file.getName(), file.getAbsolutePath());
                }
                dropFileLabel.setText("Files loaded!");
            }
            event.setDropCompleted(success);
            event.consume();
        });

        dropZone.setOnDragExited(e -> dropFileLabel.setText("Drop the file here"));
    }
        public String getSelectedFileName() {
            return inputListView.getSelectionModel().getSelectedItem();
        }

        public String getSelectedFilePath() {
            String fileName = getSelectedFileName();
            return fileName != null ? filePathMap.get(fileName) : null;
        }
}
