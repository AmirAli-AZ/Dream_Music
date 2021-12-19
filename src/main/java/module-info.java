module com.my.dreammusic.dream_music {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.jthemedetector;


    opens com.my.dreammusic.dream_music to javafx.fxml;
    exports com.my.dreammusic.dream_music;
}