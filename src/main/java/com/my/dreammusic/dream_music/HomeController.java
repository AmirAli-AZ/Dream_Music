package com.my.dreammusic.dream_music;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.my.dreammusic.dream_music.MusicsController.isPlaying;

public class HomeController implements Initializable {

    @FXML
    private ImageView img_music;
    @FXML
    private Label title1;
    @FXML
    private VBox items;
    @FXML
    private BorderPane container;


    @FXML
    public void musics(MouseEvent mouseEvent){
        if (!isPlaying){
            File dreamMusicData = new File(System.getProperty("user.home") + File.separator + "Dream Music");
            if (!dreamMusicData.exists()){
                dreamMusicData.mkdirs();
            }
            File data = new File(dreamMusicData.getAbsolutePath() + File.separator + "data.ser");
            if (data.exists()){
                try {
                    FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("musics.fxml"));
                    container.setCenter(loader.load());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                Stage window = (Stage) container.getScene().getWindow();
                window.close();
                try {
                    openFolderConfig(new Stage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            Dialog dialog = new Dialog(new Dialog.Listener() {
                @Override
                public void onResult(int result) {
                    System.out.println(result);
                }
            });
            dialog.setTitle("Music is playing");
            dialog.setMessage("You can't refresh musics when a music is playing.");
            dialog.setImage(new Image(Home.class.getResourceAsStream("icons/ic_warning.png")));
            dialog.show();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        img_music.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_music.png")));
        title1.getStyleClass().add("title");
        items.getStyleClass().add("left-border");

        try {
            FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("musics.fxml"));
            container.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openFolderConfig(Stage stage) throws IOException{
        stage.setTitle("Folder Config");
        stage.setOnCloseRequest(e ->{
            Platform.exit();
            System.exit(0);
        });
        FXMLLoader loader = new FXMLLoader(Home.class.getResource("folderConfig.fxml"));
        Scene scene = new Scene(loader.load() , 611 , 288);
        scene.getStylesheets().add(Home.class.getResource("Themes/dialog-theme.css").toExternalForm());
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}
