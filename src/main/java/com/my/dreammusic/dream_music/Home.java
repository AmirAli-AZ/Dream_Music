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

    @Override
    public void start(Stage stage) throws Exception {
        File data = new File(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser");
        if (data.exists()){
            stage.setTitle("Dream Music");

            final double width = 850.0;
            final double height = 600.0;

            FXMLLoader loader = new FXMLLoader(Home.class.getResource("home.fxml"));
            Scene scene = new Scene(loader.load(), width , height);
            scene.getStylesheets().add(Home.class.getResource("Themes/light-theme.css").toExternalForm());

            HomeController homeController = loader.getController();
            homeController.setScene(scene);

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
        controller.setScene(scene);

        stage.setResizable(false);
        stage.setScene(scene);
        if (openHome)stage.show();
        else stage.showAndWait();
    }
}
