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
import org.apache.commons.io.FileUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML
    public Button playBtn;
    @FXML
    public Button forwardBtn;
    @FXML
    public Button rewindBtn;
    @FXML
    public Button musicImportBtn;
    @FXML
    private VBox tracksPane = new VBox();
    @FXML
    private ListView<String> groupListView = new ListView<>();
    @FXML
    private ListView<String> tracksListView = new ListView<>();

    private Map<String, MediaPlayer> medias = new HashMap<>();
    private boolean isPlaying = false;
    private TimeSlider timeSlider = new TimeSlider(medias);
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
            File groupDir = new File(fileManager.getBaseDir().getAbsolutePath() + File.separator + nextValue);
            ObservableList<String> list = fileManager.buildTrackslist(groupDir);
            tracksListView.setItems(list);
            tracksPane.getChildren().clear();
        });
    }

    private void updateMediaMap(String group, String track) {
        // build medias map
        medias.clear();
        try {
            if (fileManager.getBaseDir() != null) {
                File mediasDir = new File(fileManager.getBaseDir() + File.separator + group + File.separator + track);
                boolean first = true;
                for (File mediaFile : fileManager.getAudioFiles(mediasDir.listFiles())) {
                    Media media = new Media(mediaFile.toURI().toURL().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    // all media must have the same time duration, so we take any of them to manage the progress on the time slider
                    if (first){
                        first = false;
                        mediaPlayer.currentTimeProperty().addListener((observableValue, oldDuration, newDuration) -> {
                            timeSlider.update(mediaPlayer);
                        });
                    }
                    medias.put(mediaFile.getName(), mediaPlayer);
                }
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePlayerGUI() {
        // update sliders
        tracksPane.getChildren().clear();
        timeSlider.reset();
        for (Map.Entry<String, MediaPlayer> entry : medias.entrySet()) {
            tracksPane.getChildren().add(new Label(entry.getKey()));
            tracksPane.getChildren().add(buildVolumeSlider(entry.getValue()));
        }
        tracksPane.getChildren().add(timeSlider);
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
                timeSlider.update(medias.values().iterator().next());
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
        boolean res = fileManager.initBaseDir();
        if (res) {
            musicImportBtn.setDisable(false);
        }
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

    public void onMusicImportClick(ActionEvent actionEvent) {
        Dialog dialog = new Dialog<>();
        dialog.setTitle("Import de musique");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField artistField = new TextField();
        artistField.setPromptText("Artiste");
        TextField songNameField = new TextField();
        songNameField.setPromptText("Nom du morceau");

        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setMultiSelectionEnabled(true);

        Button fileChooserBtn = new Button("Select. fichiers");
        fileChooserBtn.setOnAction(event -> {
            if (artistField.getText().isBlank() || songNameField.getText().isBlank()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Le nom d'artiste et le nom de morceau ne peuvent pas être vides", ButtonType.OK);
                alert.show();
                //TODO : sanitize input names
            }
            int returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                List<File> selectedFiles = fileManager.getAudioFiles(jfc.getSelectedFiles());
                if (fileManager.getBaseDir() != null) {
                    File artistDir;
                    if (Arrays.stream(fileManager.getBaseDir().listFiles()).anyMatch(file -> file.getName().equalsIgnoreCase(artistField.getText()))) {
                        artistDir = Arrays.stream(fileManager.getBaseDir().listFiles()).filter(file -> file.getName().equalsIgnoreCase(artistField.getText())).findFirst().get();
                    }
                    else {
                        artistDir = new File(fileManager.getBaseDir().getAbsolutePath() + File.separator + artistField.getText());
                        artistDir.mkdir();
                    }
                    File songDir = new File(artistDir.getAbsolutePath() + File.separator + songNameField.getText());
                    if (songDir.exists()){
                        songDir.delete();
                    }
                    songDir.mkdir();
                    for (File newAudioFile : selectedFiles) {
                        try {
                            Files.copy(newAudioFile.toPath(), new File(songDir.getAbsolutePath() + File.separator + newAudioFile.getName()).toPath());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    groupListView.getItems().clear();
                    groupListView.setItems(fileManager.initgroups());
                }
            }
        });

        dialogPane.setContent(new VBox(8, artistField, songNameField, fileChooserBtn));
        dialog.show();
    }

    public void onMinusTrackClick(ActionEvent actionEvent) {
        String selected = tracksListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner un morceau", ButtonType.OK);
            alert.show();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous vraiment supprimer le morceau \"" + selected + "\" ?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response.equals(ButtonType.YES)){
                    fileManager.deleteTrackFolder(groupListView.getSelectionModel().getSelectedItem(), selected);
                    tracksListView.getItems().remove(selected);
                    tracksPane.getChildren().clear();
                    timeSlider.reset();
                }
            });
        }
    }

    public void onAddTrackClick(ActionEvent actionEvent) {
        //TODO
    }

    public void onMinusGroupClick(ActionEvent actionEvent) {
        //TODO
    }

    public void onAddGroupClick(ActionEvent actionEvent) {
        //TODO
    }
}