package com.orkestro;


import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Singleton handling logging in the application
 */
public class Logs {
    private static Logs instance;

    private Logger logger;

    private Logs() {
        logger = Logger.getLogger("OrkestroLogger");

        try {
            InputStream configFile = getClass().getResourceAsStream("logs.properties");
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (IOException ex) {
            System.out.println("WARNING: Could not open configuration file");
            System.out.println("WARNING: Logging not configured (console output only)");
        }
    }

    public static Logger getLogger() {
        if (instance == null) {
            instance = new Logs();
        }
        return instance.logger;
    }
}