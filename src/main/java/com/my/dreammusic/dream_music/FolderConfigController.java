package com.my.dreammusic.dream_music;

import com.jthemedetecor.OsThemeDetector;
import com.my.dreammusic.dream_music.logging.Logger;
import com.my.dreammusic.dream_music.utils.UserDataManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Objects;
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

    private File musicFolder;
    private final UserData userData = new UserData();
    public boolean openHome = false;
    private Listener listener;
    private SystemTray tray;
    private TrayIcon trayIcon;

    private static final Logger logger = Logger.getLogger(FolderConfigController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            logger.setWriter(UserDataManager.getLogsPath() + File.separator + logger.getName() + ".log" , true);
        } catch (IOException ex) {
            logger.error(ex);
        }
        logger.info("Folder Config initialize");
        if (OsThemeDetector.isSupported()) {
            String light = Objects.requireNonNull(FolderConfigController.class.getResource("Themes/dialog-light-theme.css")).toExternalForm();
            String dark = Objects.requireNonNull(FolderConfigController.class.getResource("Themes/dialog-dark-theme.css")).toExternalForm();

            final OsThemeDetector detector = OsThemeDetector.getDetector();
            Consumer<Boolean> darkThemeListener = isDark -> Platform.runLater(() -> {
                if (isDark) {
                    container.getScene().getStylesheets().set(0, dark);
                    img_folderPicker.setImage(new Image(Objects.requireNonNull(FolderConfigController.class.getResourceAsStream("icons/baseline_folder_white.png"))));
                } else {
                    container.getScene().getStylesheets().set(0, light);
                    img_folderPicker.setImage(new Image(Objects.requireNonNull(FolderConfigController.class.getResourceAsStream("icons/baseline_folder_black.png"))));
                }
            });
            darkThemeListener.accept(detector.isDark());
            detector.registerListener(darkThemeListener);
        }

        create.getStyleClass().add("button-style-ok");
        String s = getUserPath() + File.separator + "Music";
        path.setText(s);
        musicFolder = new File(s);

        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();
            java.awt.Image image = Toolkit.getDefaultToolkit().createImage(DownloaderUIController.class.getResource("icons/icon64x64.png"));
            trayIcon = new TrayIcon(image, "Dream Music");
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> removeTrayIcon());
            logger.info("create system tray");
        }
    }

    @FXML
    public void pick(MouseEvent mouseEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(getUserPath()));
        File dir = directoryChooser.showDialog(container.getScene().getWindow());
        if (dir != null) {
            musicFolder = dir;
            path.setText(musicFolder.getAbsolutePath());
        }
    }

    @FXML
    public void createFolder() throws IOException {
        createNewData();
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
        if (listener != null) listener.onResult(Listener.CANCEL);
    }


    private boolean isValidPath(String path) {
        if (path.length() == 0) return false;
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException e) {
            return false;
        }
        return true;
    }

    private void openHome(boolean b) throws IOException {
        if (b) Home.openHome(new Stage());
    }

    private void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (SystemTray.isSupported()) {
            try {
                if (tray.getTrayIcons().length == 0) tray.add(trayIcon);
                trayIcon.displayMessage(title, message, type);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeTrayIcon() {
        if (SystemTray.isSupported() && tray.getTrayIcons().length > 0) {
            tray.remove(trayIcon);
            logger.info("remove system tray");
        }
    }

    public void pathKeyPressed(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            createNewData();
        }
    }

    private void createNewData() throws IOException {
        boolean wrongPath = false;
        if (!(path.getText().equals(musicFolder.getAbsolutePath()))) {
            if (isValidPath(path.getText())) {
                musicFolder = new File(path.getText());
            } else {
                path.setText(musicFolder.getAbsolutePath());
                showNotification("Invalid Path", "you typed a wrong path", TrayIcon.MessageType.WARNING);
                wrongPath = true;
            }
        }
        if (!wrongPath) {
            UserDataManager manager = new UserDataManager();
            if (!musicFolder.exists())
                Files.createDirectories(Paths.get(musicFolder.getAbsolutePath()));
            userData.setPath(musicFolder.getAbsolutePath());
            manager.write(userData);
            logger.info("new music folder location saved");
            removeTrayIcon();
            ((Stage) container.getScene().getWindow()).close();
            openHome(openHome);
            if (listener != null)
                listener.onResult(Listener.OK);
        }
    }
}