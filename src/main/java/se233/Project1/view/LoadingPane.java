package se233.Project1.view;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class LoadingPane {
    private final VBox container;
    private final List<ProgressBar> bars = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();

    public LoadingPane(VBox container) {
        this.container = container;
        showPlaceholder(); // ✅ show "(No file yet)" on startup
    }

    public void buildForFiles(List<String> fileNames) {
        container.getChildren().clear();  // ✅ Always clear everything first
        bars.clear();
        labels.clear();

        if (fileNames == null || fileNames.isEmpty()) {
            showPlaceholder();
            return;
        }

        for (String name : fileNames) {
            addProgressRow(name);
        }
    }


    /** Adds a single progress row */
    private void addProgressRow(String name) {
        Label lbl = new Label(name);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

        ProgressBar pb = new ProgressBar(0);
        pb.setPrefWidth(350);
        pb.setVisible(true);

        HBox row = new HBox(3, lbl, pb);
        row.setStyle("-fx-padding: 5;");

        container.getChildren().add(row);
        labels.add(lbl);
        bars.add(pb);
    }

    /** Show "(No file yet)" placeholder */
    public void showPlaceholder() {
        container.getChildren().clear();
        Label placeholder = new Label("(No file yet)");
        placeholder.setStyle("-fx-text-fill: gray; -fx-font-style: italic; -fx-padding: 5;");
        container.getChildren().add(placeholder);
    }

    /** Remove placeholder manually if needed */
    public void hidePlaceholder() {
        container.getChildren().removeIf(node ->
                node instanceof Label && ((Label) node).getText().equals("(No file yet)")
        );
    }

    public void hideAll() {
        for (int i = 0; i < bars.size(); i++) {
            bars.get(i).setVisible(false);
            labels.get(i).setVisible(false);
        }
    }

    public void showProgress(int index, String fileName) {
        if (index < bars.size()) {
            bars.get(index).setVisible(true);
            labels.get(index).setVisible(true);
            labels.get(index).setText(fileName);
            bars.get(index).setProgress(0);
        }
    }

    public void setFileName(int index, String name) {
        if (index < labels.size()) {
            labels.get(index).setText(name);
        }
    }

    public void setProgress(int index, double progress) {
        if (index < bars.size()) {
            bars.get(index).setProgress(progress);
        }
    }
    public void setBarColor(int index, String color) {
        if (index < bars.size()) {
            bars.get(index).setStyle("-fx-accent: " + color + ";");
        }
    }

}
