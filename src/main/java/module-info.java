module com.my.dreammusic.dream_music {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.jthemedetector;
    requires java.desktop;
    requires org.apache.commons.io;
    requires javafx.web;
    requires jdk.xml.dom;

    opens com.my.dreammusic.dream_music to javafx.fxml;
    exports com.my.dreammusic.dream_music;
}