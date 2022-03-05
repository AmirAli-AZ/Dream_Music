package com.my.dreammusic.dream_music;

import com.jthemedetecor.OsThemeDetector;
import com.my.dreammusic.dream_music.logging.Logger;
import com.my.dreammusic.dream_music.utils.OSUtils;
import com.my.dreammusic.dream_music.utils.UserDataManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class HomeController implements Initializable {

    @FXML
    private ImageView img_music;
    @FXML
    private Label title1;
    @FXML
    private VBox items;
    @FXML
    public BorderPane borderLayout;
    @FXML
    private HBox tab_musics;

    private MusicsController musicsController;
    private static final Logger logger = Logger.getLogger(HomeController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            logger.setWriter(UserDataManager.getLogsPath() + File.separator + logger.getName() + ".log" , true);
        } catch (IOException e) {
            logger.error(e);
        }
        logger.info("Home initialize");
        if (OsThemeDetector.isSupported()) {
            String light = HomeController.class.getResource("Themes/light-theme.css").toExternalForm();
            String dark = HomeController.class.getResource("Themes/dark-theme.css").toExternalForm();
            final OsThemeDetector detector = OsThemeDetector.getDetector();
            Consumer<Boolean> darkThemeListener = isDark -> Platform.runLater(() -> {
                if (isDark) {
                    logger.info("theme changed to dark");
                    borderLayout.getScene().getStylesheets().set(0, dark);
                    img_music.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_music_white.png")));
                } else {
                    logger.info("theme changed to light");
                    borderLayout.getScene().getStylesheets().set(0, light);
                    img_music.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_music_black.png")));
                }
            });
            darkThemeListener.accept(detector.isDark());
            detector.registerListener(darkThemeListener);
        }

        title1.getStyleClass().add("title");
        items.getStyleClass().add("right-border");

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("musics.fxml"));
                borderLayout.setCenter(loader.load());
                musicsController = loader.getController();
                musicsController.getRefreshingProperty().addListener((observableValue, oldValue, newValue) -> tab_musics.setDisable(newValue));
                musicsController.setMainStage((Stage) borderLayout.getScene().getWindow());

                if (!OsThemeDetector.isSupported()) {
                    UserDataManager manager = new UserDataManager();
                    UserData data = manager.read();

                    if (data.getNotSupportDarkCount() == 0) {
                        Dialog dialog = new Dialog(null);
                        dialog.setTitle("Warning");
                        dialog.setMessage("Your Device Not Support Dark Theme");
                        dialog.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_warning.png")));
                        dialog.setBtnOkText("OK");
                        dialog.show();

                        data.setNotSupportDarkCount(1);
                        manager.write(data);
                    }
                    logger.warn("theme not supported");
                }
            } catch (IOException e) {
                logger.error(e);
            }
        });


    }

    @FXML
    public void musics(MouseEvent mouseEvent) {
        if (musicsController != null && mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (!musicsController.isPlaying) {
                if (new File(UserDataManager.getSerFilePath()).exists()) {
                    musicsController.getSongList();
                } else {
                    ((Stage)borderLayout.getScene().getWindow()).close();
                    try {
                        openFolderConfig(new Stage(), true);
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }
            } else {
                Dialog dialog = new Dialog(result -> {
                    if (result == Listener.OK) {
                        if (new File(UserDataManager.getSerFilePath()).exists()) {
                            musicsController.pauseMedia();
                            musicsController.songBarVisibility(false);
                            musicsController.getSongList();
                            logger.info("musics refreshed");
                        } else {
                            musicsController.pauseMedia();
                            musicsController.songBarVisibility(false);
                            ((Stage)borderLayout.getScene().getWindow()).close();
                            try {
                                openFolderConfig(new Stage(), true);
                            } catch (IOException e) {
                                logger.error(e);
                            }
                        }
                    }
                });
                dialog.setTitle("Warning");
                dialog.setMessage("Music is playing , are you sure to refresh musics?");
                dialog.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_warning.png")));
                dialog.setCancelButton(true);
                dialog.setBtnCancelText("No");
                dialog.setBtnOkText("Yes");
                dialog.show();
            }
        }
    }

    @FXML
    public void folderConfigAction() throws IOException {
        if (musicsController != null) {
            Stage stage = new Stage();
            stage.setTitle("Folder Config");

            FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("folderConfig.fxml"));
            Scene scene = new Scene(loader.load(), 611, 288);
            scene.getStylesheets().add(HomeController.class.getResource("Themes/light-theme.css").toExternalForm());

            FolderConfigController controller = loader.getController();
            controller.setOpenHome(false);

            controller.setListener(result -> {
                if (result == Listener.OK) {
                    if (musicsController.isPlaying) {
                        musicsController.mediaPlayer.stop();
                        musicsController.isPlaying = false;
                    }
                    musicsController.songBarVisibility(false);
                    musicsController.loadFolder();
                    musicsController.getSongList();
                    if (musicsController.miniPlayer != null && musicsController.isMiniPlayerOpen) {
                        ((Stage) borderLayout.getScene().getWindow()).show();
                        musicsController.miniPlayer.close();
                    }
                    controller.setListener(null);
                }
            });

            stage.setOnCloseRequest(e -> {
                controller.removeTrayIcon();
                controller.shutDown();
            });
            stage.setResizable(false);
            stage.setScene(scene);
            stage.getIcons().addAll(
                    new Image(HomeController.class.getResourceAsStream("icons/icon64x64.png")),
                    new Image(HomeController.class.getResourceAsStream("icons/icon32x32.png")),
                    new Image(HomeController.class.getResourceAsStream("icons/icon16x16.png"))
            );
            stage.show();
        }
    }

    @FXML
    public void downloadAction() throws IOException {
        showDownloader();
    }

    @FXML
    public void miniPlayerAction() {
        if (musicsController != null && musicsController.isPlaying || musicsController.songBar.isVisible()) {
            musicsController.createMiniPlayer();
            borderLayout.getScene().getWindow().hide();
        }
    }

    @FXML
    public void openGithubPage() throws URISyntaxException, IOException {
        OSUtils.browse(new URI("https://amirali-az.github.io/Dream_Music"));
    }

    @FXML
    public void openAbout() throws IOException {
        Stage stage = new Stage();
        stage.setTitle("About");
        FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("about.fxml"));
        Scene scene = new Scene(loader.load(), 810, 575);
        stage.setScene(scene);
        stage.getIcons().addAll(
                new Image(HomeController.class.getResourceAsStream("icons/icon64x64.png")),
                new Image(HomeController.class.getResourceAsStream("icons/icon32x32.png")),
                new Image(HomeController.class.getResourceAsStream("icons/icon16x16.png"))
        );
        stage.setMinHeight(575);
        stage.setMinWidth(810);
        stage.show();
    }

    public void openFolderConfig(Stage stage, boolean openHome) throws IOException {
        stage.setTitle("Folder Config");
        FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("folderConfig.fxml"));
        Scene scene = new Scene(loader.load(), 611, 288);
        scene.getStylesheets().add(HomeController.class.getResource("Themes/light-theme.css").toExternalForm());

        FolderConfigController controller = loader.getController();
        controller.setOpenHome(openHome);

        stage.setResizable(false);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            controller.removeTrayIcon();
        });
        stage.getIcons().addAll(
                new Image(HomeController.class.getResourceAsStream("icons/icon64x64.png")),
                new Image(HomeController.class.getResourceAsStream("icons/icon32x32.png")),
                new Image(HomeController.class.getResourceAsStream("icons/icon16x16.png"))
        );
        stage.show();
    }

    private void showDownloader() throws IOException {
        Stage stage = new Stage();
        stage.setTitle("Downloader");
        FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("downloaderUI.fxml"));
        Scene scene = new Scene(loader.load(), 600, 400);
        scene.getStylesheets().add(FolderConfigController.class.getResource("Themes/dialog-light-theme.css").toExternalForm());
        DownloaderUIController downloaderUIController = loader.getController();
        stage.setOnCloseRequest(e -> {
            if (downloaderUIController.downloader != null &&
                    !downloaderUIController.downloader.isCancelled()) {
                downloaderUIController.downloader.exit();
            }
            downloaderUIController.removeTrayIcon();
        });
        stage.setMinHeight(400);
        stage.setMinWidth(600);
        stage.setScene(scene);
        stage.getIcons().addAll(
                new Image(HomeController.class.getResourceAsStream("icons/icon64x64.png")),
                new Image(HomeController.class.getResourceAsStream("icons/icon32x32.png")),
                new Image(HomeController.class.getResourceAsStream("icons/icon16x16.png"))
        );
        stage.show();
    }
}
