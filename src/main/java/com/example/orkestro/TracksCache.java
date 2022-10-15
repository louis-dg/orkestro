package com.example.orkestro;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache meant to keep the sound volume of each audio file played by the application
 */
public class TracksCache implements Serializable {

    private static final long serialVersionUID = -1005576391088804421L;

    private Map<String, Double> cachedVolumes = new HashMap<>();

    public TracksCache() {
    }

    public void setVolumeLevel(String artist, String audioFile, double volume) {
        cachedVolumes.put(getCacheKey(artist, audioFile), volume);
    }

    public Double getVolumeLevel(String artist, String audioFile) {
        String cacheKey = getCacheKey(artist, audioFile);
        if (cachedVolumes.containsKey(cacheKey)) {
            return cachedVolumes.get(cacheKey);
        }
        return null;
    }

    public Map<String, Double> getCachedVolumes() {
        return cachedVolumes;
    }

    private String getCacheKey(String artist, String audioFile) {
        return artist + audioFile;
    }

}
