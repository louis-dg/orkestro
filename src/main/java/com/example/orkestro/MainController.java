package com.example.orkestro;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML
    private AnchorPane tracksPane = new AnchorPane();

    @FXML
    private ListView<String> groupListView = new ListView<>();

    @FXML
    private ListView<String> tracksListView = new ListView<>();

    private MediaPlayer mediaPlayer = null;

    private static final File BASE_DIR = initBaseDir();

    // TODO utiliser un slider pour afficher la progression
    // Volume : getMediaplayer().setVolume(0.1d);
    // Déplacement getMediaplayer().seek(Duration.minutes(1));

    /**
     * Il faut deviner qu'il faut appeler cette méthode :
     * https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
     * https://docs.oracle.com/javase/8/javafx/api/javafx/fxml/doc-files/introduction_to_fxml.html#controllers
     */
    @FXML
    public void initialize() {
        tracksListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

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
            return new File(MainController.class.getResource("tracks").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onPlayClick()
    {
//        getMediaplayer(playText.getText());
//        getMediaplayer().play();
    }

    @FXML
    protected void onStopClick()
    {
        getMediaplayer().stop();
    }


    private MediaPlayer getMediaplayer()
    {
        return getMediaplayer(null);
    }

    private MediaPlayer getMediaplayer(String mediaName)
    {
        if (mediaName != null && mediaPlayer == null)
        {
            try {
                Media hit = new Media(getClass().getResource(mediaName).toURI().toString());
                mediaPlayer = new MediaPlayer(hit);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return mediaPlayer;
    }

}