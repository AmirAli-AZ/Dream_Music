package com.my.dreammusic.dream_music;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UserDataManager {
    public static String folderPath = System.getProperty("user.home") + File.separator + "Dream Music";
    public static String serFilePath = folderPath + File.separator + "data.ser";

    public UserDataManager(){
        if (!new File(folderPath).exists()){
            try {
                Files.createDirectories(Paths.get(folderPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(UserData data){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFilePath))){
            oos.writeObject(data);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public UserData read(){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serFilePath))) {
            return (UserData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
}
