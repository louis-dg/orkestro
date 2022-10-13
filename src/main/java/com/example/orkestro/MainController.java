package com.example.orkestro;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainController {

    @FXML
    public Button playBtn;
    @FXML
    public Button forwardBtn;
    @FXML
    public Button rewindBtn;
    @FXML
    private VBox tracksPane = new VBox();
    @FXML
    private ListView<String> groupListView = new ListView<>();
    @FXML
    private ListView<String> tracksListView = new ListView<>();

    private Map<String, MediaPlayer> medias = new HashMap<>();
    private boolean isPlaying = false;
    private TimeSlider timeSlider = new TimeSlider(medias.values());
    private FileManager fileManager = new FileManager();
    private static final double DEFAULT_VOLUME = 0.5d;

    //https://docs.oracle.com/javase/8/javafx/api/javafx/fxml/doc-files/introduction_to_fxml.html#controllers
    @FXML
    public void initialize() {
        tracksListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tracksListView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null){
                setControlButonsDisable(false);
                stopAllMedias();
            }
            if (tracksListView.getSelectionModel().getSelectedItem() != null){
                updateMediaMap(groupListView.getSelectionModel().getSelectedItem(), tracksListView.getSelectionModel().getSelectedItem());
            }
            updatePlayerGUI();
        });

        groupListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        groupListView.getSelectionModel().selectedItemProperty().addListener((observableValue, previousValue, nextValue) -> {
            stopAllMedias();
            medias.clear();
            setControlButonsDisable(true);
            updateTrackListView(nextValue);
            tracksPane.getChildren().clear();
        });
    }

    private void updateMediaMap(String group, String track) {
        // build medias map
        medias.clear();
        try {
            if (fileManager.getBaseDir() != null) {
                File mediasDir = fileManager.getTrackDir(group, track);
                boolean first = true;
                for (File mediaFile : fileManager.getAudioFiles(mediasDir.listFiles())) {
                    Media media = new Media(mediaFile.toURI().toURL().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.dispose()); // MediaPlayer keeps a lock on the file. Use dispose to release it
                    // all media must have the same time duration, so we take any of them to manage the progress on the time slider
                    if (first){
                        first = false;
                        mediaPlayer.currentTimeProperty().addListener((observableValue, oldDuration, newDuration) -> {
                            timeSlider.update(mediaPlayer.getTotalDuration().toMillis(), mediaPlayer.getCurrentTime().toMillis());
                        });
                    }
                    medias.put(mediaFile.getName(), mediaPlayer);
                }
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateTrackListView(String group) {
        ObservableList<String> list = fileManager.buildTrackslist(fileManager.getGroupDir(group));
        tracksListView.setItems(list);
    }

    private void updatePlayerGUI() {
        // update sliders
        tracksPane.getChildren().clear();
        timeSlider.reset();
        if (medias.entrySet().size() > 0){
            for (Map.Entry<String, MediaPlayer> entry : medias.entrySet()) {
                tracksPane.getChildren().add(new Label(entry.getKey()));
                tracksPane.getChildren().add(buildVolumeSlider(entry.getValue()));
            }
            tracksPane.getChildren().add(timeSlider);
        }
    }

    @FXML
    protected void onPlayClick()
    {
        if (isPlaying) {
            stopAllMedias();
            timeSlider.reset();
        } else {
            playAllMedias();
            if (medias.values().size() > 0) {
                MediaPlayer player = medias.values().iterator().next();
                timeSlider.update(player.getTotalDuration().toMillis(), player.getCurrentTime().toMillis());
            }
        }
    }

    private Slider buildVolumeSlider(MediaPlayer mediaPlayer) {
        Slider slider = new Slider(0, 1 ,DEFAULT_VOLUME);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(0.25f);
        slider.setBlockIncrement(0.1f);
        slider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            mediaPlayer.setVolume(newValue.doubleValue());
        });
        return slider;
    }

    private void playAllMedias() {
        isPlaying = true;
        FontIcon icon = new FontIcon("fa-stop");
        icon.setIconColor(Paint.valueOf("red"));
        playBtn.setGraphic(icon);
        setFwrRwdButonsDisable(false);
        for (MediaPlayer mediaplayer: medias.values()) {
            mediaplayer.play();
        }
    }

    private void stopAllMedias() {
        isPlaying = false;
        FontIcon icon = new FontIcon("fa-play");
        icon.setIconColor(Paint.valueOf("darkgreen"));
        playBtn.setGraphic(icon);
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

    @FXML
    public void onMusicFolderClick(ActionEvent actionEvent) {
        fileManager.initBaseDir();
        groupListView.getItems().clear();
        groupListView.setItems(fileManager.initgroups());
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
    public void onAddGroupClick(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ajouter un groupe");
        dialog.setHeaderText("Ajouter un groupe");
        dialog.setContentText("Nom du groupe : ");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            groupListView.getItems().add(name);
            fileManager.getGroupDir(name).mkdir();
        });
    }

    @FXML
    public void onMinusGroupClick(ActionEvent actionEvent) {
        String selected = groupListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner un groupe", ButtonType.OK);
            alert.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer le groupe \"" + selected + "\" ?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response.equals(ButtonType.YES)){
                    fileManager.deleteGroupFolder(selected);
                    groupListView.getItems().remove(selected);
                    tracksPane.getChildren().clear();
                    tracksPane.getChildren().clear();
                    timeSlider.reset();
                }
            });
        }
    }

    @FXML
    public void onAddTrackClick(ActionEvent actionEvent) {
        String artist = groupListView.getSelectionModel().getSelectedItem();
        if (artist == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner un groupe", ButtonType.OK);
            alert.show();
        } else {

            List<File> selectedFiles;
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setMultiSelectionEnabled(true);
            jfc.setDialogTitle("Selectionnez les fichiers à importer");

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
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Le nom de morceau ne peut pas être vide", ButtonType.OK);
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner un morceau", ButtonType.OK);
            alert.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer le morceau \"" + selected + "\" ?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response.equals(ButtonType.YES)){
                    stopAllMedias();
//                    tracksListView.getItems().remove(selected);
                    tracksPane.getChildren().clear();
                    timeSlider.reset();
                    medias.values().stream().forEach(mediaPlayer -> mediaPlayer.dispose()); // MediaPlayer keeps a lock on the file. Use dispose to release it
                    medias.clear();
                    fileManager.deleteTrackFolder(groupListView.getSelectionModel().getSelectedItem(), selected);
                    updateTrackListView(groupListView.getSelectionModel().getSelectedItem());
                }
            });
        }
    }

}