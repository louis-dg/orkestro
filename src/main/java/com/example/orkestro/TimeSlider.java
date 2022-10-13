package com.example.orkestro;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.Collection;

public class TimeSlider extends HBox {

    private Slider slider = new Slider(0, 1, 0);

    private Label label = new Label();

    public TimeSlider(Collection<MediaPlayer> medias) {
        setPadding(new Insets(30, 0, 0, 0));
        // on click, go to the corresponding time of the audio file
        slider.setOnMouseClicked(event -> {
            slider.setValueChanging(true);
            double value = (event.getX()/slider.getWidth())*slider.getMax();
            slider.setValue(value);
            for (MediaPlayer mediaplayer: medias) {
                mediaplayer.seek(Duration.millis(value));
            }
            slider.setValueChanging(false);
        });

        setHgrow(slider, Priority.ALWAYS);
        getChildren().add(slider);
        getChildren().add(label);
    }

    public void update(Double max, Double currentValue) {
        if (max != null && !Double.isNaN(max) && currentValue != null && !Double.isNaN(currentValue)) {
            slider.setMax(max);
            slider.setValue(currentValue);
            label.setText(getTimeFromDouble(currentValue) + "/" + getTimeFromDouble(max));
        }
    }

    public void reset() {
        slider.setValue(0d);
        slider.setMax(0d);
        label.setText("");
    }

    /**
     *  Build a string from time double value
     * @param nbMillis
     * @return
     */
    private String getTimeFromDouble(double nbMillis) {
        long totalSeconds = Math.round(nbMillis) / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        String strSeconds = String.format("%02d", seconds);
        return minutes + ":" + strSeconds;
    }

}
