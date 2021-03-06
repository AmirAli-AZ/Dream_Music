package com.my.dreammusic.dream_music;

import com.my.dreammusic.dream_music.utils.UserDataManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Home extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        if (new File(UserDataManager.getSerFilePath()).exists())
            openHome(stage);
        else
            openFolderConfig(stage , true);
    }

    public static void openFolderConfig(Stage stage , boolean openHome) throws IOException {
        FXMLLoader loader = new FXMLLoader(Home.class.getResource("folderConfig.fxml"));
        Scene scene = new Scene(loader.load(), 611, 288);
        scene.getStylesheets().add(Objects.requireNonNull(Home.class.getResource("Themes/dialog-light-theme.css")).toExternalForm());

        FolderConfigController controller = loader.getController();
        controller.setOpenHome(openHome);

        stage.setTitle("FolderConfig");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(Home.class.getResourceAsStream("icons/icon64x64.png"))),
                new Image(Objects.requireNonNull(Home.class.getResourceAsStream("icons/icon32x32.png"))),
                new Image(Objects.requireNonNull(Home.class.getResourceAsStream("icons/icon16x16.png")))
        );
        stage.show();
    }

    public static void openHome(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Home.class.getResource("home.fxml"));
        Scene scene = new Scene(loader.load(), getMinWidth() , getMinHeight());
        scene.getStylesheets().add(Objects.requireNonNull(Home.class.getResource("Themes/light-theme.css")).toExternalForm());

        stage.setTitle("Dream Music");
        stage.setScene(scene);
        stage.setMinHeight(getMinHeight());
        stage.setMinWidth(getMinWidth());
        stage.setOnCloseRequest(e ->{
            Platform.exit();
            System.exit(0);
        });
        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(Home.class.getResourceAsStream("icons/icon64x64.png"))),
                new Image(Objects.requireNonNull(Home.class.getResourceAsStream("icons/icon32x32.png"))),
                new Image(Objects.requireNonNull(Home.class.getResourceAsStream("icons/icon16x16.png")))
        );
        stage.show();
    }

    public static double getMinWidth() {
        Rectangle2D rectangle2D = Screen.getPrimary().getVisualBounds();
        return rectangle2D.getWidth() / 2.5;
    }

    public static double getMinHeight() {
        Rectangle2D rectangle2D = Screen.getPrimary().getVisualBounds();
        return rectangle2D.getHeight() / 1.8;
    }
}
