module com.orkestro {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.media;
    requires java.desktop;
    requires org.apache.commons.io;
    requires org.kordamp.ikonli.javafx;
    requires java.logging;
    requires org.jetbrains.annotations;

    opens com.orkestro to javafx.fxml;
    exports com.orkestro;
    exports com.orkestro.properties;
}