package com.orkestro;

import com.orkestro.cache.CacheManager;
import com.orkestro.properties.PropertiesManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
    public Button addArtistBtn;
    @FXML
    public Menu openRecentMenu;
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
                resetSelectedMedia();
                if (tracksListView.getSelectionModel().getSelectedItem() != null){
                    updateMediaMap(artistListView.getSelectionModel().getSelectedItem(), tracksListView.getSelectionModel().getSelectedItem());
                }
                updatePlayerGUI();
                setControlButonsDisable(false);
            }
        });

        artistListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        artistListView.getSelectionModel().selectedItemProperty().addListener((observableValue, previousValue, nextValue) -> {
            resetSelectedMedia();
            updateTrackListView(nextValue);
        });
        initOpenRecentMenu();
        Logs.getLogger().info("End application initialization");
    }

    private void initOpenRecentMenu() {
        for (File file : PropertiesManager.getPropertiesManager().getLastMainFolders()) {
            MenuItem menuItem = new MenuItem(file.getName());
            menuItem.setOnAction(e -> initMainFolder(file));
            openRecentMenu.getItems().add(menuItem);
        }
    }

    private void resetSelectedMedia() {
        stopMedias(medias.values());
        medias.values().stream().forEach(mediaPlayer -> mediaPlayer.dispose()); // MediaPlayer keeps a lock on the file. Use dispose to release it
        medias.clear();
        volumePane.getChildren().clear();
        setControlButonsDisable(true);
    }

    private void updateMediaMap(String artist, String track) {
        // build medias map
        medias.clear();
        try {
            if (fileManager.getMainDir() != null) {
                File mediasDir = fileManager.getTrackDir(artist, track);
                boolean first = true;
                for (File mediaFile : fileManager.getAudioFiles(mediasDir.listFiles())) {
                    Media media = new Media(mediaFile.toURI().toURL().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    // all media must have the same time duration, so we take any of them to manage the progress on the time slider
                    if (first){
                        first = false;
                        mediaPlayer.currentTimeProperty().addListener((observableValue, oldDuration, newDuration) -> {
                            updateTimeSlider(mediaPlayer.getTotalDuration().toMillis(), mediaPlayer.getCurrentTime().toMillis());
                        });
                    }
                    medias.put(mediaFile.getName(), mediaPlayer);
                }
                for (MediaPlayer mediaPlayer : medias.values()){
                    mediaPlayer.setOnEndOfMedia(() -> stopMedias(medias.values()));
                }
                Logs.getLogger().info("Media map updated with artist '" + artist + "' and track '" + track + "'");
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
            stopMedias(medias.values());
        } else {
            if (medias.values().size() > 0) {
                playMedias(medias.values());
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
        volumeButton.setOnAction(actionEvent -> {
            if (mediaPlayer.isMute()){
                volumeButton.setGraphic(iconVolumeUp);
                mediaPlayer.setMute(false);
            }else{
                volumeButton.setGraphic(iconVolumeOff);
                mediaPlayer.setMute(true);
            }
        });
        volumeButton.setGraphic(mediaPlayer.isMute() ? iconVolumeOff : iconVolumeUp);
        volumeButton.setMinWidth(35);

        flowPane.getChildren().add(volumeButton);
        flowPane.getChildren().add(slider);
        return flowPane;
    }

    private void playMedias(Collection<MediaPlayer> medias) {
        isPlaying = true;
        playBtn.setGraphic(STOP_ICON);
        setFwrRwdButonsDisable(false);
        for (MediaPlayer mediaplayer: medias) {
            mediaplayer.play();
        }
    }

    private void stopMedias(Collection<MediaPlayer> medias) {
        isPlaying = false;
        for (MediaPlayer mediaplayer: medias) {
            mediaplayer.stop();
        }
        playBtn.setGraphic(START_ICON);
        setFwrRwdButonsDisable(true);
        resetTimeSlider();
    }

    private void setControlButonsDisable(boolean disable) {
        playBtn.setDisable(disable);
        setFwrRwdButonsDisable(disable);
    }

    private void setFwrRwdButonsDisable(boolean disable) {
        forwardBtn.setDisable(disable);
        rewindBtn.setDisable(disable);
    }

    private void initMainFolder(@Nullable File lastOpenedDir) {
        boolean initDone = lastOpenedDir == null ? fileManager.selectMainDir() : fileManager.initMainDir(lastOpenedDir);
        if (initDone) {
            artistListView.getItems().clear();
            artistListView.setItems(fileManager.initArtists());
            cacheManager.initCache(fileManager.getMainDir());
            addArtistBtn.setDisable(false);
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
        labelCurrentTime.setText("");
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
    public void onMainFolderClick(ActionEvent actionEvent) {
        initMainFolder(null);
    }

    @FXML
    public void onForwardClick(ActionEvent actionEvent) {
        if (medias != null) {
            for (MediaPlayer mediaplayer : medias.values()) {
                mediaplayer.seek(mediaplayer.getCurrentTime().add(Duration.seconds(5)));
            }
        }
    }

    @FXML
    public void onRewindClick(ActionEvent actionEvent) {
        if (medias != null) {
            for (MediaPlayer mediaplayer : medias.values()) {
                mediaplayer.seek(mediaplayer.getCurrentTime().subtract(Duration.seconds(5)));
            }
        }
    }

    @FXML
    public void onAddArtistClick(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(OrkestroApplication.getRessource("add_artist"));
        dialog.setHeaderText(OrkestroApplication.getRessource("add_artist"));
        dialog.setContentText(OrkestroApplication.getRessource("artist_name"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(artistName -> {
            artistListView.getItems().add(artistName);
            File newArtistFolder = fileManager.getArtistDir(artistName);
            boolean folderCreated = newArtistFolder.mkdir();
            if (folderCreated){
                Logs.getLogger().info("added artist '" + artistName + "'");
            } else {
                Logs.getLogger().severe("Could not create folder " + newArtistFolder.getAbsolutePath());
                Alert alert = new Alert(Alert.AlertType.ERROR, OrkestroApplication.getRessource("could_not_create_folder")
                        + " : \"" + newArtistFolder.getAbsolutePath(), ButtonType.OK);
                alert.show();
            }
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

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle(OrkestroApplication.getRessource("add_track"));
                dialog.setHeaderText(OrkestroApplication.getRessource("track_name"));
                Optional<String> result = dialog.showAndWait();
                List<File> finalSelectedFiles = selectedFiles;
                result.ifPresent(trackName -> {
                    if (trackName.isBlank()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, OrkestroApplication.getRessource("track_name_empty"), ButtonType.OK);
                        alert.show();
                        return;
                    }
                    boolean importOK = fileManager.importTracks(finalSelectedFiles, artist, trackName);
                    if (importOK){
                        updateTrackListView(artist);
                        Logs.getLogger().info("Imported track '" + trackName + "'");
                    } else {
                        Logs.getLogger().severe("Could not import track " + trackName + " with following files : "
                                + finalSelectedFiles.stream().map(File::toString).collect(Collectors.joining(", ")));
                        Alert alert = new Alert(Alert.AlertType.ERROR, OrkestroApplication.getRessource("could_not_import_track"), ButtonType.OK);
                        alert.show();
                    }
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
                    resetSelectedMedia();
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

    public void onKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.SPACE) {
            onPlayClick();
        } else if (keyEvent.getCode() == KeyCode.RIGHT) {
            onForwardClick(null);
        } else if (keyEvent.getCode() == KeyCode.LEFT) {
            onRewindClick(null);
        }
    }
}