package com.my.dreammusic.dream_music;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class FolderConfigController implements Initializable {
    @FXML
    private TextField path;
    @FXML
    private AnchorPane container;
    @FXML
    public Button create;

    private File musicFolder , dreamMusicData;
    private UserData userData = new UserData();
    private Stage stage = new Stage();
    public boolean openHome = false;
    private Listener listener;
    //result
    public int OK = 1 , CANCEL = 0;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        create.getStyleClass().add("button-style-ok");
        String s = getUserPath() + File.separator + "Music";
        path.setText(s);
        musicFolder = new File(s);
        dreamMusicData = new File(getUserPath() + File.separator + "Dream Music");
        if (!dreamMusicData.exists()){
            dreamMusicData.mkdirs();
        }
    }

    @FXML
    public void pick(MouseEvent mouseEvent){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(getUserPath()));
        File file = directoryChooser.showDialog(container.getScene().getWindow());
        if (file != null){
            musicFolder = file;
            path.setText(musicFolder.getAbsolutePath());
        }
    }

    @FXML
    public void createFolder(){
        if (!(path.getText().equals(musicFolder.getAbsolutePath()))){
            if (isValidFolder(path.getText())){
                musicFolder = new File(path.getText());
            }
        }
        if (!musicFolder.exists()){
            musicFolder.mkdirs();
        }
        userData.setPath(musicFolder.getAbsolutePath());
        try {
            File data = new File(dreamMusicData.getAbsolutePath() + File.separator + "data.ser");
            FileOutputStream fileOut = new FileOutputStream(data);
            ObjectOutputStream ob = new ObjectOutputStream(fileOut);
            ob.writeObject(userData);
            ob.close();
            fileOut.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        Stage window = (Stage) container.getScene().getWindow();
        window.close();
        if (openHome){
            try {
                stage.setTitle("Dream Music");
                stage.setOnCloseRequest(e ->{
                    Platform.exit();
                    System.exit(0);
                });
                final double width = 760.0;
                final double height = 500.0;

                FXMLLoader loader = new FXMLLoader(FolderConfigController.class.getResource("home.fxml"));
                Scene scene = new Scene(loader.load(), width , height);
                scene.getStylesheets().add(FolderConfigController.class.getResource("Themes/light-theme.css").toExternalForm());

                stage.setMinWidth(width);
                stage.setMinHeight(height);

                stage.setScene(scene);
                stage.show();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        if (listener != null){
            listener.onResult(OK);
        }
    }

    public String getUserPath(){
        return System.getProperty("user.home");
    }

    public boolean isValidFolder(String s){
        if(s.length() == 0)
            return false;
        else
            return new File(s).isDirectory();
    }

    public void setOpenHome(boolean openHome){
        this.openHome = openHome;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public void shutDown(){
       listener.onResult(CANCEL);
    }
}