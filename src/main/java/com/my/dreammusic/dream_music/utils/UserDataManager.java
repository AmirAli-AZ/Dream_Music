package com.my.dreammusic.dream_music.utils;

import com.my.dreammusic.dream_music.UserData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UserDataManager {

    public UserDataManager(){
        try {
            createDreamMusicFolder();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(UserData data){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getSerFilePath()))){
            oos.writeObject(data);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public UserData read(){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getSerFilePath()))) {
            return (UserData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    public static String getFolderPath() {
        return System.getProperty("user.home") + File.separator + "Dream Music";
    }

    public static String getSerFilePath() {
        return getFolderPath() + File.separator + "data.ser";
    }

    public static void createDreamMusicFolder() throws IOException {
        if (!Files.exists(Paths.get(getFolderPath())))
            Files.createDirectories(Paths.get(getFolderPath()));
    }
}
