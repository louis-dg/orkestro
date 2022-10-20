package com.orkestro;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class MainController {

    @FXML
    public Button playBtn;
    @FXML
    public Button forwardBtn;
    @FXML
    public Button rewindBtn;
    @FXML
    public Slider timeSlider;
    @FXML
    public Label labelCurrentTime;
    @FXML
    public Label labelTotalTime;
    @FXML
    private VBox volumePane = new VBox();
    @FXML
    private ListView<String> artistListView = new ListView<>();
    @FXML
    private ListView<String> tracksListView = new ListView<>();

    private Map<String, MediaPlayer> medias = new HashMap<>();
    private boolean isPlaying = false;
    private final FileManager fileManager = new FileManager();
    private final CacheManager cacheManager = new CacheManager();

    private static final double DEFAULT_VOLUME = 0.5d;
    private static final int PLAY_BUTTON_SIZE = 35;
    private static final FontIcon START_ICON = new FontIcon("fa-play-circle");
    private static final FontIcon STOP_ICON = new FontIcon("fa-stop-circle");

    //https://docs.oracle.com/javase/8/javafx/api/javafx/fxml/doc-files/introduction_to_fxml.html#controllers
    @FXML
    public void initialize() {
        START_ICON.setIconColor(Paint.valueOf("#008000"));
        START_ICON.setIconSize(PLAY_BUTTON_SIZE);
        STOP_ICON.setIconColor(Paint.valueOf("#bb0000"));
        STOP_ICON.setIconSize(PLAY_BUTTON_SIZE);

        tracksListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tracksListView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null){
                setControlButonsDisable(false);
                stopAllMedias();
            }
            if (tracksListView.getSelectionModel().getSelectedItem() != null){
                updateMediaMap(artistListView.getSelectionModel().getSelectedItem(), tracksListView.getSelectionModel().getSelectedItem());
            }
            updatePlayerGUI();
        });

        artistListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        artistListView.getSelectionModel().selectedItemProperty().addListener((observableValue, previousValue, nextValue) -> {
            stopAllMedias();
            medias.clear();
            setControlButonsDisable(true);
            updateTrackListView(nextValue);
            volumePane.getChildren().clear();
        });
        Logs.getLogger().info("End application initialization");
    }

    private void updateMediaMap(String artist, String track) {
        // build medias map
        medias.clear();
        try {
            if (fileManager.getBaseDir() != null) {
                File mediasDir = fileManager.getTrackDir(artist, track);
                boolean first = true;
                for (File mediaFile : fileManager.getAudioFiles(mediasDir.listFiles())) {
                    Media media = new Media(mediaFile.toURI().toURL().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.setOnEndOfMedia(() -> stopAllMedias());
                    // all media must have the same time duration, so we take any of them to manage the progress on the time slider
                    if (first){
                        first = false;
                        mediaPlayer.currentTimeProperty().addListener((observableValue, oldDuration, newDuration) -> {
                            updateTimeSlider(mediaPlayer.getTotalDuration().toMillis(), mediaPlayer.getCurrentTime().toMillis());
                        });
                    }
                    medias.put(mediaFile.getName(), mediaPlayer);
                }
            }
        } catch (MalformedURLException e) {
            Logs.getLogger().log(Level.SEVERE, "Could update media map", e);
            throw new RuntimeException(e);
        }
    }

    private void updateTrackListView(String artist) {
        ObservableList<String> list = fileManager.buildTrackslist(fileManager.getArtistDir(artist));
        tracksListView.setItems(list);
    }

    private void updatePlayerGUI() {
        // update sliders
        volumePane.getChildren().clear();
        resetTimeSlider();
        if (medias.entrySet().size() > 0){
            for (Map.Entry<String, MediaPlayer> entry : medias.entrySet()) {
                Label lbl = new Label(entry.getKey());
                lbl.setFont(new Font(13));
                volumePane.getChildren().add(lbl);
                volumePane.getChildren().add(buildVolumeSlider(entry.getValue()));
            }
        }
    }

    @FXML
    protected void onPlayClick()
    {
        if (isPlaying) {
            stopAllMedias();
            resetTimeSlider();
        } else {
            playAllMedias();
            if (medias.values().size() > 0) {
                MediaPlayer player = medias.values().iterator().next();
                updateTimeSlider(player.getTotalDuration().toMillis(), player.getCurrentTime().toMillis());
            }
        }
    }

    private Node buildVolumeSlider(MediaPlayer mediaPlayer) {
        FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL);
        flowPane.setAlignment(Pos.CENTER_LEFT);
        flowPane.setHgap(5);

        Slider slider = new Slider(0, 1, DEFAULT_VOLUME);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(0.25f);
        slider.setBlockIncrement(0.1f);
        if (cacheManager.getCache() != null) {
            Double cachedValue = cacheManager.getCache().getVolumeLevel(artistListView.getSelectionModel().getSelectedItem(), mediaPlayer.getMedia().getSource());
            if (cachedValue != null) {
                slider.setValue(cachedValue.doubleValue());
                mediaPlayer.setVolume(cachedValue);
            }
        }
        slider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            mediaPlayer.setVolume(newValue.doubleValue());
            cacheManager.updateCache(artistListView.getSelectionModel().getSelectedItem(), mediaPlayer.getMedia().getSource(), newValue.doubleValue());
        });
        slider.setPrefWidth(230);

        int iconSize = 20;
        FontIcon iconVolumeOff = new FontIcon("fa-volume-off");
        iconVolumeOff.setIconSize(iconSize);
        FontIcon iconVolumeUp = new FontIcon("fa-volume-up");
        iconVolumeUp.setIconSize(iconSize);
        Button volumeButton = new Button();
        AtomicBoolean isMuted = new AtomicBoolean(false);
        AtomicReference<Double> lastValue = new AtomicReference<>(slider.getValue());
        volumeButton.setOnAction(actionEvent -> {
            if (isMuted.get()){
                slider.setValue(lastValue.get());
                volumeButton.setGraphic(iconVolumeUp);
                isMuted.set(false);
            }else{
                lastValue.set(slider.getValue());
                slider.setValue(0);
                volumeButton.setGraphic(iconVolumeOff);
                isMuted.set(true);
            }
        });
        volumeButton.setGraphic(lastValue.get() == 0d ? iconVolumeOff : iconVolumeUp);
        volumeButton.setMinWidth(35);

        flowPane.getChildren().add(volumeButton);
        flowPane.getChildren().add(slider);
        return flowPane;
    }

    private void playAllMedias() {
        isPlaying = true;
        playBtn.setGraphic(STOP_ICON);
        setFwrRwdButonsDisable(false);
        for (MediaPlayer mediaplayer: medias.values()) {
            mediaplayer.play();
        }
    }

    private void stopAllMedias() {
        isPlaying = false;
        playBtn.setGraphic(START_ICON);
        setFwrRwdButonsDisable(true);
        for (MediaPlayer mediaplayer: medias.values()) {
            mediaplayer.stop();
        }
    }

    private void setControlButonsDisable(boolean disable) {
        playBtn.setDisable(disable);
        setFwrRwdButonsDisable(disable);
    }

    private void setFwrRwdButonsDisable(boolean disable) {
        forwardBtn.setDisable(disable);
        rewindBtn.setDisable(disable);
    }

    private void initBaseFolder() {
        boolean initDone = fileManager.initBaseDir();
        if (initDone) {
            artistListView.getItems().clear();
            artistListView.setItems(fileManager.initArtists());
            cacheManager.initCache(fileManager.getBaseDir());
        }
    }

    public void updateTimeSlider(Double max, Double currentValue) {
        if (max != null && !Double.isNaN(max) && currentValue != null && !Double.isNaN(currentValue)) {
            timeSlider.setMax(max);
            timeSlider.setValue(currentValue);
            labelTotalTime.setText(getTimeFromDouble(max));
            labelCurrentTime.setText(getTimeFromDouble(currentValue));
        }
    }

    public void resetTimeSlider() {
        timeSlider.setValue(0d);
        timeSlider.setMax(0d);
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

    @FXML
    public void onBaseFolderClick(ActionEvent actionEvent) {
        initBaseFolder();
    }

    @FXML
    public void onForwardClick(ActionEvent actionEvent) {
        for (MediaPlayer mediaplayer: medias.values()) {
            mediaplayer.seek(mediaplayer.getCurrentTime().add(Duration.seconds(5)));
        }
    }

    @FXML
    public void onRewindClick(ActionEvent actionEvent) {
        for (MediaPlayer mediaplayer: medias.values()) {
            mediaplayer.seek(mediaplayer.getCurrentTime().subtract(Duration.seconds(5)));
        }
    }

    @FXML
    public void onAddArtistClick(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(OrkestroApplication.getRessource("add_artist"));
        dialog.setHeaderText(OrkestroApplication.getRessource("add_artist"));
        dialog.setContentText(OrkestroApplication.getRessource("artist_name"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            artistListView.getItems().add(name);
            fileManager.getArtistDir(name).mkdir();
        });
    }

    @FXML
    public void onMinusArtistClick(ActionEvent actionEvent) {
        String selected = artistListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, OrkestroApplication.getRessource("choose_artist"), ButtonType.OK);
            alert.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, OrkestroApplication.getRessource("confirm_delete_artist")
                    + " \"" + selected + "\" ?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response.equals(ButtonType.YES)){
                    fileManager.deleteArtistFolder(selected);
                    artistListView.getItems().remove(selected);
                    volumePane.getChildren().clear();
                    resetTimeSlider();
                }
            });
        }
    }

    @FXML
    public void onAddTrackClick(ActionEvent actionEvent) {
        String artist = artistListView.getSelectionModel().getSelectedItem();
        if (artist == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, OrkestroApplication.getRessource("please_select_artist"), ButtonType.OK);
            alert.show();
        } else {

            List<File> selectedFiles;
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setMultiSelectionEnabled(true);
            jfc.setDialogTitle(OrkestroApplication.getRessource("select_files_to_import"));

            int returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                selectedFiles = fileManager.getAudioFiles(jfc.getSelectedFiles());
                //TODO : sanitize input names

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Ajouter un morceau");
                dialog.setHeaderText("Nom un morceau");
                Optional<String> result = dialog.showAndWait();
                List<File> finalSelectedFiles = selectedFiles;
                result.ifPresent(name -> {
                    if (name.isBlank()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, OrkestroApplication.getRessource("track_name_empty"), ButtonType.OK);
                        alert.show();
                        return;
                    }
                    fileManager.importTracks(finalSelectedFiles, artist, name);
                    updateTrackListView(artist);
                });
            }
        }
    }

    @FXML
    public void onMinusTrackClick(ActionEvent actionEvent) {
        String selected = tracksListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, OrkestroApplication.getRessource("please_select_track"), ButtonType.OK);
            alert.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, OrkestroApplication.getRessource("confirm_delete_track")
                    + " \"" + selected + "\" ?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response.equals(ButtonType.YES)){
                    stopAllMedias();
                    volumePane.getChildren().clear();
                    resetTimeSlider();
                    medias.values().stream().forEach(mediaPlayer -> mediaPlayer.dispose()); // MediaPlayer keeps a lock on the file. Use dispose to release it
                    medias.clear();
                    fileManager.deleteTrackFolder(artistListView.getSelectionModel().getSelectedItem(), selected);
                    updateTrackListView(artistListView.getSelectionModel().getSelectedItem());
                }
            });
        }
    }

    public void onCloseClick(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void onTimeSliderClick(MouseEvent mouseEvent) {
        timeSlider.setValueChanging(true);
        double value = (mouseEvent.getX()/timeSlider.getWidth())*timeSlider.getMax();
        timeSlider.setValue(value);
        for (MediaPlayer mediaplayer: medias.values()) {
            mediaplayer.seek(Duration.millis(value));
        }
        timeSlider.setValueChanging(false);
    }
}