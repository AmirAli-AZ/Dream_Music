package com.my.dreammusic.dream_music;

import com.jthemedetecor.OsThemeDetector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class Home extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        File data = new File(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser");
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
            String light = Home.class.getResource("Themes/light-theme.css").toExternalForm();
            String dark = Home.class.getResource("Themes/dark-theme.css").toExternalForm();
            // default theme
            scene.getStylesheets().add(light);

            final OsThemeDetector detector = OsThemeDetector.getDetector();
            Consumer<Boolean> darkThemeListener = isDark -> {
                Platform.runLater(() -> {
                    if (isDark) {
                        scene.getStylesheets().set(0 , dark);
                    } else {
                        scene.getStylesheets().set(0 , light);
                    }
                });
            };
            darkThemeListener.accept(detector.isDark());
            detector.registerListener(darkThemeListener);

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
