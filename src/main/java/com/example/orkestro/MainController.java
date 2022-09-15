package com.example.orkestro;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URISyntaxException;

public class MainController {

    @FXML
    private AnchorPane tracksPane;

    @FXML
    private ListView groupListView;

    @FXML
    private ListView tracksListView;

    private MediaPlayer mediaPlayer = null;

    // TODO utiliser un slider pour afficher la progression
    // Volume : getMediaplayer().setVolume(0.1d);
    // Déplacement getMediaplayer().seek(Duration.minutes(1));


    public MainController() {
        tracksPane = new AnchorPane();
        groupListView = new ListView<>();
        tracksListView = new ListView<>();
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