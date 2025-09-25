package se233.Project1.view;

import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;

public class DropPane {
    private final AnchorPane dropZone;
    private final Label dropFileLabel;

    public DropPane(AnchorPane dropZone, Label dropFileLabel) {
        this.dropZone = dropZone;
        this.dropFileLabel = dropFileLabel;
        init();
    }

    private void init() {
        dropZone.setOnDragOver(e -> {
            e.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
        });
        dropZone.setOnDragDropped(e -> {
            dropFileLabel.setText("File dropped!");
        });
    }
}
