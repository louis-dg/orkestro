module com.example.orkestro {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires javafx.media;
    requires java.desktop;

    opens com.example.orkestro to javafx.fxml;
    exports com.example.orkestro;
}