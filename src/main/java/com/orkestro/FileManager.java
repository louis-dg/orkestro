package com.orkestro;

import com.orkestro.properties.PropertiesManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FileManager {

    private File mainDir = null;

    public FileManager() {
    }

    /**
     * @return a list of subdirectories (representing the list of artists) from the main directory
     */
    public ObservableList<String> initArtists() {
        List<String> artistDirectories = new ArrayList<>();
        if (mainDir != null) {
            for (File file : mainDir.listFiles()) {
                if (file.isDirectory()) {
                    artistDirectories.add(file.getName());
                }
            }
        }
        return FXCollections.observableArrayList(artistDirectories);
    }

    /**
     * @param artistDir the artistDir directory
     * @return the list of tracks names from a artistDir directory
     */
    public ObservableList<String> buildTrackslist(File artistDir) {
        List<String> tracks = new ArrayList<>();
        for (File file : artistDir.listFiles()) {
            if (file.isDirectory()) {
                tracks.add(file.getName());
            }
        }
        return FXCollections.observableArrayList(tracks);
    }

    public boolean selectMainDir() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setDialogTitle(OrkestroApplication.getRessource("choose_base_dir"));
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            return initMainDir(selectedFile);
        }
        return false;
    }

    public boolean initMainDir(File file) {
        if (file.isDirectory()) {
            mainDir = file;
            Logs.getLogger().info("Main directory initialized with " + mainDir.getAbsolutePath());
            PropertiesManager.getPropertiesManager().addLastMainFolder(mainDir);
            return true;
        }
        return false;
    }

    public static List<File> getAudioFiles(File[] files) {
        Set<String> audioExtension = new HashSet<>();
        audioExtension.add("mp3");
        audioExtension.add("m4a");
        audioExtension.add("wav");
        audioExtension.add("wma");
        audioExtension.add("ogg");
        return Arrays.stream(files).filter(file -> audioExtension.contains(FilenameUtils.getExtension(file.getName()))).collect(Collectors.toList());
    }

    public void deleteTrackFolder(String artist, String track) {
        deleteFolder(getTrackDir(artist, track));
    }

    public void deleteArtistFolder(String artist) {
        deleteFolder(getArtistDir(artist));
    }

    public void deleteFolder(File file) {
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            Logs.getLogger().log(Level.WARNING, "Could not delete " + file.getAbsolutePath(), e);
        }
    }

    public boolean importTracks(List<File> selectedFiles, String artist, String track) {
        if (getMainDir() != null) {
            File artistDir = getArtistDir(artist);
            if (!artistDir.exists()) {
                artistDir.mkdirs();
            }
            File trackDir = getTrackDir(artist, track);
            if (trackDir.exists()) {
                try {
                    FileUtils.deleteDirectory(trackDir);
                } catch (IOException e) {
                    Logs.getLogger().severe("Could not delete existing track directory " + trackDir);
                }
            }
            trackDir.mkdir();
            for (File newAudioFile : selectedFiles) {
                try {
                    Files.copy(newAudioFile.toPath(), new File(trackDir.getAbsolutePath() + File.separator + newAudioFile.getName()).toPath());
                } catch (IOException e) {
                    Logs.getLogger().log(Level.SEVERE, "Could not copy new audio file " + newAudioFile.getAbsolutePath()
                            + " to " + new File(trackDir.getAbsolutePath() + File.separator + newAudioFile.getName()).getAbsolutePath(), e);
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public File getArtistDir(String artist) {
        return new File(getMainDir().getAbsolutePath() + File.separator + artist);
    }

    public File getTrackDir(String artist, String track) {
        return new File(getArtistDir(artist).getAbsolutePath() + File.separator + track);
    }

    public File getMainDir() {
        return mainDir;
    }
}
