package com.orkestro;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class FileManager {

    private static final String CACHE_FILENAME = "orkestro.cache";

    private TracksCache cache;

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
        jfc.setDialogTitle("Choisissez un dossier de base");
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.isDirectory()){
                baseDir = selectedFile;
                initCache(baseDir);
                return true;
            }
        }
        return false;
    }

    /**
     * Initialize the cache system from the given folder
     * @param folder
     */
    private void initCache(File folder) {
        File cacheFile = new File(folder, CACHE_FILENAME);
        if (cacheFile.exists()) {
            try {
                cache = deserializeCache(cacheFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                cacheFile.createNewFile();
                cache = new TracksCache();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Update the cached volume value for the given artist and audioFileName
     * @param artist
     * @param audioFileName
     * @param volumeValue
     */
    public void updateCache(String artist, String audioFileName, double volumeValue) {
        if (cache != null) {
            File cacheFile = new File(baseDir, CACHE_FILENAME);
            try {
                cache.setVolumeLevel(artist, audioFileName, volumeValue);
                serializeCache(cache, cacheFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
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

    public void deleteTrackFolder(String group, String track) {
        deleteFolder(getTrackDir(group, track));
    }

    public void deleteGroupFolder(String group) {
        deleteFolder(getGroupDir(group));
    }

    public void deleteFolder(File file) {
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            System.out.println("Could not delete " + file.getAbsolutePath());
            throw new RuntimeException(e);
        }
    }

    public void importTracks(List<File> selectedFiles, String artist, String track) {
        if (getBaseDir() != null) {
            File artistDir = getGroupDir(artist);
            if (!artistDir.exists()) {
                artistDir.mkdirs();
            }
            File songDir = getTrackDir(artist, track);
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
        }
    }

    public File getGroupDir(String group) {
        return new File(getBaseDir().getAbsolutePath() + File.separator + group);
    }

    public File getTrackDir(String group, String track) {
        return new File(getGroupDir(group).getAbsolutePath() + File.separator + track);
    }

    public File getBaseDir() {
        return baseDir;
    }

    public TracksCache getCache() {
        return cache;
    }

    /**
     * Deserialize the TracksCache from the given file
     * @param tracksCache
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public TracksCache deserializeCache(File tracksCache) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(tracksCache);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object obj = ois.readObject();
        ois.close();
        if (obj instanceof TracksCache) {
            TracksCache cache = (TracksCache) obj;
            if (cache.getCachedVolumes() != null) {
                return cache;
            }
        }
        System.out.println("Error in deserialization");
        return null;
    }

    /**
     * Serialize the TracksCache in the given file
     * @param obj
     * @param tracksCache
     * @throws IOException
     * @throws URISyntaxException
     */
    public void serializeCache(Object obj, File tracksCache) throws IOException, URISyntaxException {
        FileOutputStream fos = new FileOutputStream(tracksCache);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(obj);
        oos.flush();
        fos.close();
    }
}
