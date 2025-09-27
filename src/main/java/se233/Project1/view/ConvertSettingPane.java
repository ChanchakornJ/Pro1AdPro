package se233.Project1.view;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.util.StringConverter;

public class ConvertSettingPane {
    private final Slider qualitySlider;
    private final ComboBox<String> formatComboBox;

    public ConvertSettingPane(Slider qualitySlider, ComboBox<String> formatComboBox) {
        this.qualitySlider = qualitySlider;
        this.formatComboBox = formatComboBox;
        setup();
    }

        private void setup () {
            qualitySlider.setMin(0);
            qualitySlider.setMax(3);
            qualitySlider.setMajorTickUnit(1);
            qualitySlider.setMinorTickCount(0);
            qualitySlider.setSnapToTicks(true);
            qualitySlider.setShowTickMarks(true);
            qualitySlider.setShowTickLabels(true);

            qualitySlider.setLabelFormatter(new StringConverter<Double>() {
                @Override
                public String toString(Double value) {
                    switch (value.intValue()) {
                        case 0:
                            return "64";
                        case 1:
                            return "128";
                        case 2:
                            return "196";
                        case 3:
                            return "320";
                        default:
                            return "";
                    }
                }

                @Override
                public Double fromString(String string) {
                    switch (string) {
                        case "64":
                            return 0.0;
                        case "128":
                            return 1.0;
                        case "196":
                            return 2.0;
                        case "320":
                            return 3.0;
                    }
                    return 0.0;
                }
            });

            qualitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int[] kbps = {64, 128, 196, 320};
                int bitrate = kbps[newVal.intValue()];
                System.out.println("Selected bitrate: " + bitrate + " kbps");
            });

            qualitySlider.setValue(1);
            formatComboBox.getItems().addAll("mp3", "wav", "ogg");
            formatComboBox.setValue("mp3");
        }
        public String getSelectedFormat() {
            return formatComboBox.getValue();
        }


    }


