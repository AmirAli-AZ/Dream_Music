package com.my.dreammusic.dream_music;

import java.io.*;

public class UserData implements Serializable {
    private String path;
    private int notSupportDarkCount;

    public UserData(String path , int notSupportDarkCount){
        this.path = path;
        this.notSupportDarkCount = notSupportDarkCount;
    }

    public UserData(){}

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setNotSupportDarkCount(int notSupportDarkCount) {
        this.notSupportDarkCount = notSupportDarkCount;
    }

    public int getNotSupportDarkCount() {
        return notSupportDarkCount;
    }

    @Serial
    private void readObject(ObjectInputStream objectInputStream) throws IOException {
        this.path = objectInputStream.readUTF();
        this.notSupportDarkCount = objectInputStream.read();
    }

    @Serial
    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        outputStream.writeUTF(path);
        outputStream.write(notSupportDarkCount);
    }
}
