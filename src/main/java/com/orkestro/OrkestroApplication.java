package com.orkestro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class OrkestroApplication extends Application {

    private static Locale locale = Locale.getDefault();

    @Override
    public void start(Stage stage) throws IOException {
        Logs.getLogger().info("Starting Orkestro application");
        FXMLLoader fxmlLoader = new FXMLLoader(OrkestroApplication.class.getResource("main-view.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("com.orkestro.i18n.orkestro", locale));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Orkestro");
        stage.setScene(scene);
        stage.show();
    }

    public static String getRessource(String key) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("com.orkestro.i18n.orkestro", locale);
        return resourceBundle.getString(key);
    }

    public static void launch(String[] args) {
        launch();
    }
}