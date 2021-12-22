package com.my.dreammusic.dream_music;

import com.jthemedetecor.OsThemeDetector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.util.function.Consumer;

public class SongListCell extends ListCell<Song> implements Consumer<Boolean> {

    @FXML
    private Label title;
    @FXML
    private Label date;
    @FXML
    private ImageView img;
    @FXML
    private HBox container;

    private FXMLLoader loader;
    private final OsThemeDetector osThemeDetector = OsThemeDetector.getDetector();

    @Override
    protected void updateItem(Song song, boolean b) {
        super.updateItem(song, b);
        if (b || song == null){
            setText(null);
            setGraphic(null);
        }else {
            if (loader == null){
                loader = new FXMLLoader(SongListCell.class.getResource("listCell.fxml"));
                loader.setController(this);
                try {
                    loader.load();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            title.getStyleClass().add("cell-text-color");
            title.setText(song.getTitle());
            Font font = Font.font(Font.getDefault().getName() , FontWeight.BOLD, FontPosture.REGULAR, 13);
            title.setFont(font);
            date.setText(song.getDate());
            if (song.getImage() != null){
                img.setImage(song.getImage());
            }else {
                this.accept(osThemeDetector.isDark());
                osThemeDetector.registerListener(this);
            }
            setText(null);
            setGraphic(container);
        }
    }

    @Override
    public void accept(Boolean aBoolean) {
        Platform.runLater(() ->{
            if (loader != null) {
                if (aBoolean)
                    img.setImage(new Image(SongListCell.class.getResourceAsStream("icons/baseline_person_white.png")));
                else img.setImage(new Image(SongListCell.class.getResourceAsStream("icons/baseline_person_black.png")));
            }
        });
    }
}