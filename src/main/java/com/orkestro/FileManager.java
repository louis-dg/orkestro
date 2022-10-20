package com.orkestro;

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

    private File baseDir = null;

    public FileManager() {
    }

    /**
     * @return a list of subdirectories (representing the list of artists) from the base directory
     */
    public ObservableList<String> initArtists() {
        List<String> artistDirectories = new ArrayList<>();
        if (baseDir != null) {
            for (File file : baseDir.listFiles()) {
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

    public boolean initBaseDir() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setDialogTitle(OrkestroApplication.getRessource("choose_base_dir"));
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.isDirectory()) {
                baseDir = selectedFile;
                return true;
            }
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

    public void importTracks(List<File> selectedFiles, String artist, String track) {
        if (getBaseDir() != null) {
            File artistDir = getArtistDir(artist);
            if (!artistDir.exists()) {
                artistDir.mkdirs();
            }
            File songDir = getTrackDir(artist, track);
            if (songDir.exists()) {
                songDir.delete();
            }
            songDir.mkdir();
            for (File newAudioFile : selectedFiles) {
                try {
                    Files.copy(newAudioFile.toPath(), new File(songDir.getAbsolutePath() + File.separator + newAudioFile.getName()).toPath());
                } catch (IOException e) {
                    Logs.getLogger().log(Level.SEVERE, "Could not copy new audio file " + newAudioFile.getAbsolutePath()
                            + " to " + new File(songDir.getAbsolutePath() + File.separator + newAudioFile.getName()).getAbsolutePath(), e);
                }
            }
        }
    }

    public File getArtistDir(String artist) {
        return new File(getBaseDir().getAbsolutePath() + File.separator + artist);
    }

    public File getTrackDir(String artist, String track) {
        return new File(getArtistDir(artist).getAbsolutePath() + File.separator + track);
    }

    public File getBaseDir() {
        return baseDir;
    }
}
