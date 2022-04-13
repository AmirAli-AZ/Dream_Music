package com.my.dreammusic.dream_music;

import com.jthemedetecor.OsThemeDetector;
import com.my.dreammusic.dream_music.logging.Logger;
import com.my.dreammusic.dream_music.utils.Downloader;
import com.my.dreammusic.dream_music.utils.UserDataManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.ResourceBundle;
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
    protected Downloader downloader;
    private SystemTray tray;
    private TrayIcon trayIcon;

    private static final Logger logger = Logger.getLogger(DownloaderUIController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            logger.setWriter(UserDataManager.getLogsPath() + File.separator + logger.getName() + ".log" , true);
        } catch (IOException ex) {
            logger.error(ex);
        }
        logger.info("Downloader UI initialize");
        if (OsThemeDetector.isSupported()) {
            String light = Objects.requireNonNull(DownloaderUIController.class.getResource("Themes/dialog-light-theme.css")).toExternalForm();
            String dark = Objects.requireNonNull(DownloaderUIController.class.getResource("Themes/dialog-dark-theme.css")).toExternalForm();
            final OsThemeDetector detector = OsThemeDetector.getDetector();
            Consumer<Boolean> darkThemeListener = isDark -> Platform.runLater(() -> {
                if (isDark) {
                    container.getScene().getStylesheets().set(0, dark);
                } else {
                    container.getScene().getStylesheets().set(0, light);
                }
            });
            darkThemeListener.accept(detector.isDark());
            detector.registerListener(darkThemeListener);
        }

        progress.prefWidthProperty().bind(hbox1.widthProperty());
        hbox1.setVisible(false);

        UserDataManager manager = new UserDataManager();
        UserData d = manager.read();
        if (d != null) userData = d;

        if(SystemTray.isSupported()){
            tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage(DownloaderUIController.class.getResource("icons/icon64x64.png"));
            trayIcon = new TrayIcon(image, "Dream Music");
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> removeTrayIcon());
            logger.info("create system tray");
        }

        Platform.runLater(() -> {
            Stage window = (Stage) container.getScene().getWindow();
            window.setOnHiding(windowEvent -> {
                if (downloader != null && !downloader.isCancelled())
                    downloader.exit();
                removeTrayIcon();
                logger.close();
            });
        });
    }

    @FXML
    public void downloadClick() {
        logger.info("download button clicked");
        download(urlInput.getText());
    }

    public boolean isURLSupport(String s) {
        try {
            URL url = new URL(s);
            boolean isURLFile = !("file".equals(url.getProtocol()) && new File(url.toURI()).isDirectory());
            return ((FilenameUtils.getExtension(s).equals("mp3") || FilenameUtils.getExtension(s).equals("wav")) && isURLFile);
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    @FXML
    public void handleDragDropped(DragEvent event) {
        logger.info("text drag dropped");
        Dragboard dragboard = event.getDragboard();
        if (event.getTransferMode() == TransferMode.COPY &&
                dragboard.hasString()) {
            urlInput.setText(dragboard.getString());
            event.setDropCompleted(true);
        }
        event.consume();
    }

    @FXML
    public void handleDragOver(DragEvent event) {
        logger.info("text drag over");
        if (event.getDragboard().hasString()){
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    public void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (SystemTray.isSupported()) {
            logger.info("show notification");
            try {
                if (tray.getTrayIcons().length == 0) tray.add(trayIcon);
                trayIcon.displayMessage(title, message, type);
            }catch (AWTException e){
                e.printStackTrace();
            }
        }
    }

    private void removeTrayIcon(){
        if (SystemTray.isSupported() && tray.getTrayIcons().length > 0){
            tray.remove(trayIcon);
            logger.info("remove system tray");
        }
    }

    @FXML
    public void urlInputKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            download(event.getText());
        }
    }

    private void download(String url){
        if (isURLSupport(url)) {
            if (userData == null || !new File(userData.getPath()).exists()){
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Pick Download Location");
                chooser.setInitialDirectory(new File(System.getProperty("user.home")));
                File file = chooser.showDialog(container.getScene().getWindow());
                if (file != null){
                    userData = new UserData();
                    userData.setPath(file.getAbsolutePath());
                }
                else return;
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
                    showNotification("Download Failed", "Music Download Failed", TrayIcon.MessageType.ERROR);
                    logger.warn("Download Failed");
                }

                @Override
                public void onCompleted() {
                    hbox1.setVisible(false);
                    downloadButton.setDisable(false);
                    showNotification("Download Failed", "Music Downloaded", TrayIcon.MessageType.INFO);
                    logger.info("Download Failed");
                }
            });
            downloader.setDownloadPath(new File(userData.getPath()));
            downloader.setFileURL(url);
            progress.progressProperty().bind(downloader.progressProperty());
            thread = new Thread(downloader);
            thread.setDaemon(true);
            thread.start();
        } else {
            showNotification("Invalid data", "Please enter valid URL or mp3 / wav expansion" , TrayIcon.MessageType.WARNING);
        }
    }
}
