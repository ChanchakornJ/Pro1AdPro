package se233.Project1.view;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;

public class ConvertSettingPane {
    private final Slider qualitySlider;
    private final ComboBox<String> formatComboBox;

    public ConvertSettingPane(Slider qualitySlider, ComboBox<String> formatComboBox) {
        this.qualitySlider = qualitySlider;
        this.formatComboBox = formatComboBox;
        setup();
    }

    private void setup() {
        qualitySlider.setMin(64);
        qualitySlider.setMax(320);
        qualitySlider.setShowTickMarks(true);
        qualitySlider.setShowTickLabels(true);
        qualitySlider.setValue(128);

        double[] stops = {64, 128, 196, 320};

        qualitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double snap = stops[0];
            for (double s : stops) if (Math.abs(s - newVal.doubleValue()) < Math.abs(snap - newVal.doubleValue())) snap = s;
            qualitySlider.setValue(snap);
        });
        formatComboBox.getItems().addAll("mp3", "wav", "ogg");
    }

}

