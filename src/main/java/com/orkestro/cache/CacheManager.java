package com.orkestro.cache;

import com.orkestro.Logs;

import java.io.*;
import java.net.URISyntaxException;
import java.util.logging.Level;

/**
 * A manager for the cache system
 */
public class CacheManager {

    private static final String CACHE_FILENAME = "orkestro.cache";

    private TracksCache cache;

    private File cacheFile;

    public CacheManager() {
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
        Logs.getLogger().warning("Error during cache deserialization");
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

    /**
     * Initialize the cache system from the given folder
     * @param folder
     */
    public void initCache(File folder) {
        cacheFile = new File(folder, CACHE_FILENAME);
        if (cacheFile.exists()) {
            try {
                cache = deserializeCache(cacheFile);
            } catch (IOException e) {
                Logs.getLogger().log(Level.WARNING, "Could not deserialize cache", e);
            } catch (ClassNotFoundException e) {
                Logs.getLogger().log(Level.WARNING, "Could not deserialize cache", e);
            }
        } else {
            try {
                cacheFile.createNewFile();
                cache = new TracksCache();
            } catch (IOException e) {
                Logs.getLogger().log(Level.WARNING, "Could not create cache file", e);
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
            try {
                cache.setVolumeLevel(artist, audioFileName, volumeValue);
                serializeCache(cache, cacheFile);
            } catch (IOException e) {
                Logs.getLogger().log(Level.WARNING, "Could not update cache", e);
            } catch (URISyntaxException e) {
                Logs.getLogger().log(Level.WARNING, "Could not update cache", e);
            }
        }
    }
}
