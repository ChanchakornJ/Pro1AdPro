package se233.Project1.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadingPane {
    private final VBox container;
    private final List<ProgressBar> bars = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private final Map<String, Integer> fileIndexMap = new HashMap<>();


    public LoadingPane(VBox container) {
        this.container = container;

    }

    public void buildForFiles(List<String> fileNames) {
        container.getChildren().clear();
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


    private void addProgressRow(String name) {
        Label lbl = new Label(name);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
        lbl.setMinWidth(150);
        lbl.setMaxWidth(300);
        lbl.setTooltip(new javafx.scene.control.Tooltip(name));


        ProgressBar pb = new ProgressBar(0);
        pb.setPrefWidth(500);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setVisible(true);
        labels.add(lbl);
        bars.add(pb);
        fileIndexMap.put(name, bars.size() - 1);


        HBox row = new HBox(10, lbl, pb);
        row.setStyle("-fx-padding: 8; -fx-alignment: center-left;");
        row.setFillHeight(true);
        VBox.setMargin(row, new Insets(8, 0, 8, 0));

        HBox.setHgrow(pb, javafx.scene.layout.Priority.ALWAYS);

        container.getChildren().add(row);
        labels.add(lbl);
        bars.add(pb);
    }



    public void showPlaceholder() {
        container.getChildren().clear();

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
    public void removeFileProgress(int index) {
        if (index < 0 || index >= container.getChildren().size()) return;

        String fileName = labels.get(index).getText();

        container.getChildren().remove(index);
        bars.remove(index);
        labels.remove(index);
        fileIndexMap.remove(fileName);

        // Rebuild the index map so all indexes are accurate
        for (int i = 0; i < labels.size(); i++) {
            fileIndexMap.put(labels.get(i).getText(), i);
        }
    }


    public void removeFileProgressWithDelay(int index) {
        if (index < 0 || index >= bars.size()) return;

        ProgressBar pb = bars.get(index);
        Label lbl = labels.get(index);

        pb.setStyle("-fx-accent: #cc0000;");

        new Thread(() -> {
            try {
                Thread.sleep(1500); // wait 1.5s before removal
            } catch (InterruptedException ignored) {}

            Platform.runLater(() -> removeFileProgress(index));
        }).start();
    }
    public void markFailed(int index, String message) {
        if (index < labels.size()) {
            Label lbl = labels.get(index);
            lbl.setText("Invalid, " + message);
            lbl.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }

    }
    // Instantly remove a progress bar by file name
    public void removeFileProgressByName(String fileName) {
        for (int i = 0; i < labels.size(); i++) {
            if (labels.get(i).getText().equals(fileName)) {
                removeFileProgress(i);
                break;
            }
        }
    }








}
