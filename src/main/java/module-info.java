module com.my.dreammusic.dream_music {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.jthemedetector;
    requires java.desktop;
    requires org.apache.commons.io;

    opens com.my.dreammusic.dream_music to javafx.fxml;
    exports com.my.dreammusic.dream_music;
}