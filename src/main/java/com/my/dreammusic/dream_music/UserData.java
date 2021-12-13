package com.my.dreammusic.dream_music;

import java.io.Serializable;

public class UserData implements Serializable {
    private String path;

    public UserData(String path){
        this.path = path;
    }

    public UserData(){}

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
