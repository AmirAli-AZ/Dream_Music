package com.my.dreammusic.dream_music;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Home extends Application {

    public static final double width = 850.0;
    public static final double height = 600.0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        File data = new File(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser");
        stage.getIcons().addAll(
                new Image(Home.class.getResourceAsStream("icons/icon64x64.png")),
                new Image(Home.class.getResourceAsStream("icons/icon32x32.png")),
                new Image(Home.class.getResourceAsStream("icons/icon16x16.png"))
        );
        if (data.exists()){
            stage.setTitle("Dream Music");
            FXMLLoader loader = new FXMLLoader(Home.class.getResource("home.fxml"));
            Scene scene = new Scene(loader.load(), width , height);
            scene.getStylesheets().add(Home.class.getResource("Themes/light-theme.css").toExternalForm());

            stage.setMinWidth(width);
            stage.setMinHeight(height);
            stage.setScene(scene);

            stage.setOnCloseRequest(e ->{
                Platform.exit();
                System.exit(0);
            });
            stage.show();
        }else {
            openFolderConfig(stage , true);
        }
    }
    public void openFolderConfig(Stage stage , boolean openHome) throws IOException {
        stage.setTitle("Folder Config");
        FXMLLoader loader = new FXMLLoader(Home.class.getResource("folderConfig.fxml"));
        Scene scene = new Scene(loader.load(), 611, 288);
        scene.getStylesheets().add(Home.class.getResource("Themes/light-theme.css").toExternalForm());

        FolderConfigController controller = loader.getController();
        controller.setOpenHome(openHome);

        stage.setResizable(false);
        stage.setOnCloseRequest(e ->{
            controller.removeTrayIcon();
        });
        stage.setScene(scene);
        stage.show();
    }
}
