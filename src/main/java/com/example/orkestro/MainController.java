package com.example.orkestro;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML
    public Button playBtn;

    @FXML
    public Button stopBtn;

    @FXML
    private VBox tracksPane = new VBox();

    @FXML
    private ListView<String> groupListView = new ListView<>();

    @FXML
    private ListView<String> tracksListView = new ListView<>();

    private Map<String, MediaPlayer> medias = new HashMap<>();

    private static File BASE_DIR = null;
    private static final double DEFAULT_VOLUME = 0.5d;

    // Déplacement getMediaplayer().seek(Duration.minutes(1));

    //https://docs.oracle.com/javase/8/javafx/api/javafx/fxml/doc-files/introduction_to_fxml.html#controllers
    @FXML
    public void initialize() {
        tracksListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tracksListView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            // enable buttons
            if(newValue != null){
                playBtn.setDisable(false);
                stopBtn.setDisable(false);
            }
            if (tracksListView.getSelectionModel().getSelectedItem() != null){
                updateMediaMap(groupListView.getSelectionModel().getSelectedItem(), tracksListView.getSelectionModel().getSelectedItem());
            }
            updatePlayerGUI();
        });

        groupListView.setItems(initgroups());
        groupListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        groupListView.getSelectionModel().selectedItemProperty().addListener((observableValue, previousValue, nextValue) -> {
            medias.clear();
            playBtn.setDisable(true);
            stopBtn.setDisable(true);
            File groupDir = new File(BASE_DIR.getAbsolutePath() + File.separator + nextValue);
            ObservableList<String> list = buildTrackslist(groupDir);
            tracksListView.setItems(list);
        });
    }

    /**
     * @return a list of sudirectories (representing the list of music groups) of base directory
     */
    private ObservableList<String> initgroups()
    {
        List<String> groupDirectories = new ArrayList<>();
        if (BASE_DIR != null) {
            for (File file : BASE_DIR.listFiles()) {
                if(file.isDirectory()) {
                    groupDirectories.add(file.getName());
                }
            }
        }
        return FXCollections.observableArrayList(groupDirectories);
    }

    /**
     * @param groupDir the group directory
     * @return the list of tracks names from a group directory
     */
    private ObservableList<String> buildTrackslist(File groupDir)
    {
        List<String> tracks = new ArrayList<>();
        for (File file : groupDir.listFiles()) {
            if(file.isDirectory()) {
                tracks.add(file.getName());
            }
        }
        return FXCollections.observableArrayList(tracks);
    }

    private void initBaseDir() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.isDirectory()){
                BASE_DIR = selectedFile;
            }
        }
    }

    private void updateMediaMap(String group, String track) {
        // build medias map
        medias.clear();
        try {
            if (BASE_DIR != null) {
                File mediasDir = new File(BASE_DIR + File.separator + group + File.separator + track);
                for (File mediaFile : mediasDir.listFiles()) {
                    Media media = new Media(mediaFile.toURI().toURL().toString());
                    medias.put(mediaFile.getName(), new MediaPlayer(media));
                }
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePlayerGUI() {
        // update sliders
        tracksPane.getChildren().clear();
        for (Map.Entry<String, MediaPlayer> entry : medias.entrySet()) {
            tracksPane.getChildren().add(new Label(entry.getKey()));
            tracksPane.getChildren().add(buildSlider(entry.getValue()));
        }
    }

    @FXML
    protected void onPlayClick()
    {
        for (MediaPlayer mediaplayer: medias.values()) {
            mediaplayer.setVolume(DEFAULT_VOLUME);
            mediaplayer.play();
        }
    }

    private Slider buildSlider(MediaPlayer mediaPlayer) {
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

    @FXML
    protected void onStopClick()
    {
        for (MediaPlayer mediaplayer: medias.values()) {
            mediaplayer.stop();
        }
    }

    public void onMusicFolderClick(ActionEvent actionEvent) {
        initBaseDir();
        groupListView.getItems().clear();
        groupListView.setItems(initgroups());
        updatePlayerGUI();
    }
}