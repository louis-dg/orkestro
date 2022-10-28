package com.orkestro.properties;


import com.orkestro.Logs;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class managing application properties
 */
public class PropertiesManager {

    private Properties properties = new Properties();
    private static final File PROPERTIES_FILE = new File("orkestroProperties");

    /**
     * singleton
     */
    private static PropertiesManager instance;

    private PropertiesManager() throws IOException, ClassNotFoundException {
        // si le fichier properties n'existe pas, on le crée.
        if (!PROPERTIES_FILE.exists()) {
            serialize();
        } else { // si le fichier properties existe, on le charge
            Properties props = deserialize();
            if (props != null) {
                properties = props;
            }
        }
    }

    public static PropertiesManager getPropertiesManager() {
        if (instance == null) {
            try {
                instance = new PropertiesManager();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public Properties deserialize() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(PROPERTIES_FILE);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object obj = ois.readObject();
        ois.close();
        if (obj instanceof Properties) {
            Properties prop = (Properties) obj;
            return prop;
        }
        Logs.getLogger().warning("Error during cache deserialization");
        return null;
    }

    public void serialize() throws IOException {
        FileOutputStream fos = new FileOutputStream(PROPERTIES_FILE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(properties);
        oos.flush();
        fos.close();
    }

    public List<File> getLastMainFolders() {
        return properties.getLastMainFolders().stream().map(s -> new File(s)).collect(Collectors.toList());
    }

    public void addLastMainFolder(File lastOpenedDataFile) {
        properties.addLastMainFolder(lastOpenedDataFile.getAbsolutePath());
        try {
            serialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
