module com.example.orkestro {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.media;
    requires java.desktop;

    opens com.example.orkestro to javafx.fxml;
    exports com.example.orkestro;
}