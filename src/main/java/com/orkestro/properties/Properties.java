package com.orkestro.properties;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Class storing application properties
 */
public class Properties implements Serializable {

    private static final long serialVersionUID = -2504535837641014754L;

    private LinkedList<String> lastMainFolders = new LinkedList<>();

    public List<String> getLastMainFolders() {
        return lastMainFolders;
    }

    public void addLastMainFolder(String lastMainFolder) {
        if (!lastMainFolders.contains(lastMainFolder)){
            lastMainFolders.addFirst(lastMainFolder);
        }
        if (lastMainFolders.size() > 3) {
            lastMainFolders.removeLast();
        }
    }

}
