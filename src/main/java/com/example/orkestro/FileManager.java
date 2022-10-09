package com.example.orkestro;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class FileManager {

    private File baseDir = null;

    public FileManager() {}

    /**
     * @return a list of sudirectories (representing the list of music groups) of base directory
     */
    public ObservableList<String> initgroups()
    {
        List<String> groupDirectories = new ArrayList<>();
        if (baseDir != null) {
            for (File file : baseDir.listFiles()) {
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
    public ObservableList<String> buildTrackslist(File groupDir)
    {
        List<String> tracks = new ArrayList<>();
        for (File file : groupDir.listFiles()) {
            if(file.isDirectory()) {
                tracks.add(file.getName());
            }
        }
        return FXCollections.observableArrayList(tracks);
    }

    public boolean initBaseDir() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.isDirectory()){
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

    public File getBaseDir() {
        return baseDir;
    }
}
