package se233.Project1.view;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class LoadingPane {
    private final ProgressBar[] bars;
    private final Label[] labels;

    public LoadingPane(ProgressBar... bars) {
        this.bars = bars;
        this.labels = new Label[bars.length];


    }

    public void attachLabels(Label... labels) {
        for (int i = 0; i < labels.length && i < this.labels.length; i++) {
            this.labels[i] = labels[i];
        }
    }

    public void hideAll() {
        for (ProgressBar bar : bars) {
            bar.setVisible(false);
            bar.setProgress(0);
        }
        if (labels != null) {
            for (Label label : labels) {
                label.setVisible(false);
                label.setText("");
            }
        }
    }

    public void showProgress(int index, String fileName) {
        if (index < bars.length) {
            bars[index].setVisible(true);
            bars[index].setProgress(0);
        }
        if (labels != null && index < labels.length) {
            labels[index].setVisible(true);
            labels[index].setText(fileName);
        }
    }

    public void setFileName(int index, String name) {
        if (labels != null && index < labels.length) {
            labels[index].setVisible(true);
            labels[index].setText(name);
        }
    }

    public void setProgress(int index, double progress) {
        if (index < bars.length) {
            bars[index].setProgress(progress);
        }
    }
}
