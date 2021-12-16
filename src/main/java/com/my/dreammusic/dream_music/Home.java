package com.my.dreammusic.dream_music;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Home extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private File dreamMusicData;

    @Override
    public void start(Stage stage) throws Exception {
        dreamMusicData = new File(System.getProperty("user.home") + File.separator + "Dream Music");
        if (!dreamMusicData.exists()){
            dreamMusicData.mkdirs();
        }
        File data = new File(dreamMusicData.getAbsolutePath() + File.separator + "data.ser");
        if (data.exists()){
            stage.setTitle("Dream Music");
            stage.setOnCloseRequest(e ->{
                Platform.exit();
                System.exit(0);
            });
            final double width = 760.0;
            final double height = 500.0;

            FXMLLoader loader = new FXMLLoader(Home.class.getResource("home.fxml"));
            Scene scene = new Scene(loader.load(), width , height);
            scene.getStylesheets().add(Home.class.getResource("Themes/light-theme.css").toExternalForm());

            stage.setMinWidth(width);
            stage.setMinHeight(height);

            stage.setScene(scene);
            stage.show();
        }else {
            openFolderConfig(stage , true);
        }
    }
    public void openFolderConfig(Stage stage , boolean openHome) throws IOException {
        stage.setTitle("Folder Config");
        FXMLLoader loader = new FXMLLoader(Home.class.getResource("folderConfig.fxml"));
        Scene scene = new Scene(loader.load(), 611, 288);
        scene.getStylesheets().add(Home.class.getResource("Themes/dialog-theme.css").toExternalForm());

        FolderConfigController controller = loader.getController();
        controller.setOpenHome(openHome);

        stage.setResizable(false);
        stage.setScene(scene);
        if (openHome)stage.show();
        else stage.showAndWait();
    }
}
