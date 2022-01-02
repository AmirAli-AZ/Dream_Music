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

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
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
    private boolean isDarkMode = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String light = FolderConfigController.class.getResource("Themes/dialog-light-theme.css").toExternalForm();
        String dark = FolderConfigController.class.getResource("Themes/dialog-dark-theme.css").toExternalForm();

        final OsThemeDetector detector = OsThemeDetector.getDetector();
        Consumer<Boolean> darkThemeListener = isDark -> {
            Platform.runLater(() -> {
                if (isDark){
                    container.getScene().getStylesheets().set(0, dark);
                    isDarkMode = true;
                    img_folderPicker.setImage(new Image(FolderConfigController.class.getResourceAsStream("icons/baseline_folder_white.png")));
                }else {
                    container.getScene().getStylesheets().set(0, light);
                    isDarkMode = false;
                    img_folderPicker.setImage(new Image(FolderConfigController.class.getResourceAsStream("icons/baseline_folder_black.png")));
                }
            });
        };
        darkThemeListener.accept(detector.isDark() && OsThemeDetector.isSupported());
        detector.registerListener(darkThemeListener);

        create.getStyleClass().add("button-style-ok");
        String s = getUserPath() + File.separator + "Music";
        path.setText(s);

        musicFolder = new File(s);
        dreamMusicData = new File(getUserPath() + File.separator + "Dream Music");
        if (!dreamMusicData.exists()) {
            dreamMusicData.mkdirs();
        }
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
            boolean wrongPath = false;
            if (!(path.getText().equals(musicFolder.getAbsolutePath()))) {
                if (isValidPath(path.getText())) {
                    musicFolder = new File(path.getText());
                }else {
                    path.setText(musicFolder.getAbsolutePath());
                    try {
                        showNotification("Invalid Path" , "you typed a wrong path" , TrayIcon.MessageType.WARNING);
                    } catch (AWTException e) {
                        e.printStackTrace();
                    }
                    wrongPath = true;
                }
            }
            if (!wrongPath){
                if (!musicFolder.exists()) {
                    Files.createDirectories(Paths.get(musicFolder.getAbsolutePath()));
                }
                writeData();
                ((Stage)container.getScene().getWindow()).close();
                openHome(openHome);
                if (listener != null) {
                    listener.onResult(OK);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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


    private boolean isValidPath(String path){
        if (path.length() == 0 || path == null){
            return false;
        }
        try {
            Paths.get(path);
        }catch (InvalidPathException | NullPointerException e){
            return false;
        }
        return true;
    }

    private void openHome(boolean b) throws IOException{
        if (b) {
            Stage stage = new Stage();
            stage.setTitle("Dream Music");

            FXMLLoader loader = new FXMLLoader(FolderConfigController.class.getResource("home.fxml"));
            Scene scene = new Scene(loader.load(), Home.width, Home.height);
            scene.getStylesheets().add(FolderConfigController.class.getResource("Themes/light-theme.css").toExternalForm());

            stage.setMinWidth(Home.width);
            stage.setMinHeight(Home.height);
            stage.setScene(scene);

            stage.setOnCloseRequest(e -> {
                Platform.exit();
                System.exit(0);
            });
            stage.show();
        }
    }

    private void writeData() throws IOException{
        userData.setPath(musicFolder.getAbsolutePath());
        File data = new File(dreamMusicData.getAbsolutePath() + File.separator + "data.ser");
        FileOutputStream fileOut = new FileOutputStream(data);
        ObjectOutputStream ob = new ObjectOutputStream(fileOut);
        ob.writeObject(userData);
        ob.close();
        fileOut.close();
    }
    private void showNotification(String title, String message, TrayIcon.MessageType type) throws AWTException {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            java.awt.Image image = Toolkit.getDefaultToolkit().createImage(FolderConfigController.class.getResource("icons/icon.png"));
            TrayIcon trayIcon = new TrayIcon(image, "Tray Notification");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Dream Music");
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, type);
        } else {
            System.err.println("System tray not supported!");
        }
    }
}