package com.my.dreammusic.dream_music;

import com.jthemedetecor.OsThemeDetector;
import com.my.dreammusic.dream_music.Utils.Downloader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.apache.commons.io.FilenameUtils;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class DownloaderUIController implements Initializable {

    @FXML
    private Button downloadButton;

    @FXML
    private Label percentage;

    @FXML
    private ProgressBar progress;

    @FXML
    private TextField urlInput;

    @FXML
    private HBox hbox1;

    @FXML
    private AnchorPane container;

    private UserData userData;
    public Thread thread;
    public Downloader downloader;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        String light = DownloaderUIController.class.getResource("Themes/dialog-light-theme.css").toExternalForm();
        String dark = DownloaderUIController.class.getResource("Themes/dialog-dark-theme.css").toExternalForm();
        final OsThemeDetector detector = OsThemeDetector.getDetector();
        Consumer<Boolean> darkThemeListener = isDark -> {
            Platform.runLater(() -> {
                if (isDark) {
                    container.getScene().getStylesheets().set(0, dark);
                } else {
                    container.getScene().getStylesheets().set(0, light);
                }
            });
        };
        darkThemeListener.accept(detector.isDark() && OsThemeDetector.isSupported());
        detector.registerListener(darkThemeListener);

        progress.prefWidthProperty().bind(hbox1.widthProperty());
        hbox1.setVisible(false);

        if (new File(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser").exists()) {
            try {
                userData = read();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            userData = new UserData();
            userData.setPath(System.getProperty("user.home"));
        }
    }

    @FXML
    public void downloadAction() {
        String url = urlInput.getText();
        if (isConnected()) {
            if (isUrlSupport(url)) {
                if (new File(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser").exists()){
                    userData = new UserData();
                    userData.setPath(System.getProperty("user.home"));
                }
                hbox1.setVisible(true);
                downloadButton.setDisable(true);
                downloader = new Downloader(new Downloader.DownloaderListener() {
                    @Override
                    public void onProgress(int progress) {
                        Platform.runLater(() -> {
                            percentage.setText(progress + "%");
                        });
                    }

                    @Override
                    public void onFailed() {
                        downloadButton.setDisable(false);
                        hbox1.setVisible(false);
                        try {
                            showNotification("Download Failed", "Music Download Failed", TrayIcon.MessageType.WARNING);
                        } catch (AWTException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCompleted() {
                        hbox1.setVisible(false);
                        downloadButton.setDisable(false);
                        try {
                            showNotification("Download Completed", "Music Downloaded", TrayIcon.MessageType.INFO);
                        } catch (AWTException e) {
                            e.printStackTrace();
                        }
                    }
                });
                downloader.setDownloadPath(new File(userData.getPath()));
                downloader.setFileURL(url);
                progress.progressProperty().bind(downloader.progressProperty());
                thread = new Thread(downloader);
                thread.setDaemon(true);
                thread.start();
            } else {
                try {
                    showNotification("Invalid data", "Please enter valid URL or mp3 / wav expansion", TrayIcon.MessageType.WARNING);
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            }
        } else {
            showNoConnectionDialog();
        }
    }

    public boolean isURL(String s) {
        try {
            URL url = new URL(s);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private void showNotification(String title, String message, TrayIcon.MessageType type) throws AWTException {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            java.awt.Image image = Toolkit.getDefaultToolkit().createImage(DownloaderUIController.class.getResource("icons/icon.png"));
            TrayIcon trayIcon = new TrayIcon(image, "Tray Notification");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Dream Music");
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, type);
        } else {
            System.err.println("System tray not supported!");
        }
    }

    public UserData read() throws IOException, ClassNotFoundException {
        UserData data;
        FileInputStream fileIn = new FileInputStream(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser");
        ObjectInputStream ob = new ObjectInputStream(fileIn);
        data = (UserData) ob.readObject();
        ob.close();
        fileIn.close();
        return data;
    }

    public boolean isConnected() {
        try {
            final URL url = new URL("http://www.google.com");
            final URLConnection conn = url.openConnection();
            conn.connect();
            conn.getInputStream().close();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }

    private void showNoConnectionDialog() {
        Dialog dialog = new Dialog(result -> {
            if (result == Dialog.OK) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            downloadButton.fire();
                        });
                    }
                }, 900);
            }
        });
        dialog.setTitle("No Connection");
        dialog.setMessage("Check your internet connection");
        dialog.setImage(new Image(DownloaderUIController.class.getResourceAsStream("icons/ic_warning.png")));
        dialog.setBtnOkText("Try Again");
        dialog.show();
    }

    public boolean isUrlSupport(String url){
        try {
            URL url1 = new URL(url);
            boolean isURLFile = !("file".equals(url1.getProtocol()) && new File(url1.toURI()).isDirectory());
            if (isURLFile){
                return (FilenameUtils.getExtension(url).equals("mp3") || FilenameUtils.getExtension(url).equals("wav"));
            }
        } catch (MalformedURLException | URISyntaxException e) {
           return false;
        }
        return false;
    }
}
