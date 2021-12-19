package com.my.dreammusic.dream_music;

import com.jthemedetecor.OsThemeDetector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class FolderConfigController implements Initializable {
    @FXML
    private TextField path;
    @FXML
    private AnchorPane container;
    @FXML
    public Button create;
    @FXML
    private ImageView img_folderPicker;

    private File musicFolder, dreamMusicData;
    private UserData userData = new UserData();
    public boolean openHome = false;
    private Listener listener;
    //result
    public final int OK = 1, CANCEL = 0;
    private Scene scene;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        create.getStyleClass().add("button-style-ok");
        String s = getUserPath() + File.separator + "Music";
        path.setText(s);

        musicFolder = new File(s);
        dreamMusicData = new File(getUserPath() + File.separator + "Dream Music");
        if (!dreamMusicData.exists()) {
            dreamMusicData.mkdirs();
        }

        String light = FolderConfigController.class.getResource("Themes/dialog-light-theme.css").toExternalForm();
        String dark = FolderConfigController.class.getResource("Themes/dialog-dark-theme.css").toExternalForm();

        final OsThemeDetector detector = OsThemeDetector.getDetector();
        Consumer<Boolean> darkThemeListener = isDark -> {
            Platform.runLater(() -> {
                if (isDark){
                   getScene().getStylesheets().set(0, dark);
                   img_folderPicker.setImage(new Image(FolderConfigController.class.getResourceAsStream("icons/baseline_folder_white.png")));
                }else {
                    getScene().getStylesheets().set(0, light);
                    img_folderPicker.setImage(new Image(FolderConfigController.class.getResourceAsStream("icons/baseline_folder_black.png")));
                }
            });
        };
        darkThemeListener.accept(detector.isDark());
        detector.registerListener(darkThemeListener);
    }

    @FXML
    public void pick(MouseEvent mouseEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(getUserPath()));
        File file = directoryChooser.showDialog(container.getScene().getWindow());
        if (file != null) {
            musicFolder = file;
            path.setText(musicFolder.getAbsolutePath());
        }
    }

    @FXML
    public void createFolder() {
        try {
            if (!(path.getText().equals(musicFolder.getAbsolutePath()))) {
                musicFolder = new File(path.getText());
            }
            if (!musicFolder.exists()) {
                Files.createDirectories(Paths.get(musicFolder.getAbsolutePath()));
            }
            userData.setPath(musicFolder.getAbsolutePath());

            File data = new File(dreamMusicData.getAbsolutePath() + File.separator + "data.ser");
            FileOutputStream fileOut = new FileOutputStream(data);
            ObjectOutputStream ob = new ObjectOutputStream(fileOut);
            ob.writeObject(userData);
            ob.close();
            fileOut.close();

            ((Stage)container.getScene().getWindow()).close();
            if (openHome) {
                Stage stage = new Stage();
                stage.setTitle("Dream Music");
                stage.setOnCloseRequest(e -> {
                    Platform.exit();
                    System.exit(0);
                });
                final double width = 760.0;
                final double height = 500.0;

                FXMLLoader loader = new FXMLLoader(FolderConfigController.class.getResource("home.fxml"));
                Scene scene = new Scene(loader.load(), width, height);

                String light = FolderConfigController.class.getResource("Themes/light-theme.css").toExternalForm();
                String dark = FolderConfigController.class.getResource("Themes/dark-theme.css").toExternalForm();
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (listener != null) {
            listener.onResult(OK);
        }
    }

    public String getUserPath() {
        return System.getProperty("user.home");
    }

    public void setOpenHome(boolean openHome) {
        this.openHome = openHome;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void shutDown() {
        if (listener != null){
            listener.onResult(CANCEL);
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }
}