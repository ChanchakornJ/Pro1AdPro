package se233.Project1.view;

import javafx.scene.control.ProgressBar;

public class LoadingPane {
    private final ProgressBar[] bars;

    public LoadingPane(ProgressBar... bars) {
        this.bars = bars;
    }

    public void setProgress(int index, double progress) {
        bars[index].setProgress(progress);
    }
}

