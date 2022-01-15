package com.my.dreammusic.dream_music;

import javafx.scene.image.Image;
import javafx.scene.media.Media;

public class Song {
    private String title = "Music" ,
            artist = "unknown" ,
            album = "album" ,
            path = "path" ,
            date = "date";

    private Image image;
    private Media media;

    public Song() {
    }

    public Song(String title, String artist, String album, String path , String date, Image image , Media media) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.date = date;
        this.image = image;
        this.media = media;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getPath() {
        return path;
    }

    public String getDate() {
        return date;
    }

    public Image getImage() {
        return image;
    }

    public Media getMedia() {
        return media;
    }
}