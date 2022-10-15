package com.orkestro;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Collection;

public class TimeSlider extends HBox {

    private Slider slider = new Slider(0, 1, 0);
    private Label labelTotalTime = new Label();
    private Label labelCurrentTime = new Label();


    public TimeSlider(Collection<MediaPlayer> medias) {
        setPadding(new Insets(30, 0, 0, 0));
        setSpacing(8);
        setAlignment(Pos.CENTER);
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

        labelTotalTime.setFont(new Font(13));
        labelCurrentTime.setFont(new Font(13));

        setHgrow(slider, Priority.ALWAYS);
        getChildren().add(labelCurrentTime);
        getChildren().add(slider);
        getChildren().add(labelTotalTime);
    }

    public void update(Double max, Double currentValue) {
        if (max != null && !Double.isNaN(max) && currentValue != null && !Double.isNaN(currentValue)) {
            slider.setMax(max);
            slider.setValue(currentValue);
            labelTotalTime.setText(getTimeFromDouble(max));
            labelCurrentTime.setText(getTimeFromDouble(currentValue));
        }
    }

    public void reset() {
        slider.setValue(0d);
        slider.setMax(0d);
        labelTotalTime.setText("");
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
