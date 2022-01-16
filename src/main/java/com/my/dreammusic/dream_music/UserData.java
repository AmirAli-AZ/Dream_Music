package com.my.dreammusic.dream_music;

import java.io.*;

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

    @Serial
    private void readObject(ObjectInputStream objectInputStream) throws IOException {
        this.path = objectInputStream.readUTF();
    }

    @Serial
    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        outputStream.writeUTF(path);
    }
}
