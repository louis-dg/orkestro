package com.example.orkestro;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URISyntaxException;
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

    private static final File BASE_DIR = initBaseDir();
    private static final String TRACKS_DIRNAME = "tracks";
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

            // build medias map
            File tracksDir = new File(BASE_DIR + File.separator + groupListView.getSelectionModel().getSelectedItem()
                    + File.separator + tracksListView.getSelectionModel().getSelectedItem());
            medias.clear();
            try {
                for (File trackFile : tracksDir.listFiles()) {
                    //getClass().getResource("tracks/gojira/Sphinx/08 Sphinx_bass_mixed.mp3")
                    // use "/" because Media doesn't accept "\"
                    Media media = new Media(getClass().getResource(TRACKS_DIRNAME + "/" + groupListView.getSelectionModel().getSelectedItem() + "/"
                            + tracksListView.getSelectionModel().getSelectedItem() + "/" + trackFile.getName()).toURI().toString());
                    medias.put(trackFile.getName(), new MediaPlayer(media));
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            // update sliders
            tracksPane.getChildren().clear();
            for (Map.Entry<String, MediaPlayer> entry : medias.entrySet()) {
                tracksPane.getChildren().add(new Label(entry.getKey()));
                tracksPane.getChildren().add(buildSlider(entry.getValue()));
            }
        });

        groupListView.setItems(initgroups());
        groupListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        groupListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String previousValue, String nextValue) {
                File dir = new File(BASE_DIR.getAbsolutePath() + File.separator + nextValue);
                ObservableList<String> list = buildTrackslist(dir);
                tracksListView.setItems(list);
            }
        });
    }

    private ObservableList<String> initgroups()
    {
        List<String> groupDirectories = new ArrayList<>();
        for (File file : BASE_DIR.listFiles()) {
            if(file.isDirectory()) {
                groupDirectories.add(file.getName());
            }
        }
        return FXCollections.observableArrayList(groupDirectories);
    }

    private ObservableList<String> buildTrackslist(File dir)
    {
        List<String> tracks = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if(file.isDirectory()) {
                tracks.add(file.getName());
            }
        }
        return FXCollections.observableArrayList(tracks);
    }

    private static File initBaseDir() {
        try {
            return new File(MainController.class.getResource(TRACKS_DIRNAME).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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

}