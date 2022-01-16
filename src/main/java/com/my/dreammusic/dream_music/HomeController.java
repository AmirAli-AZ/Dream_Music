package com.my.dreammusic.dream_music;

import com.jthemedetecor.OsThemeDetector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.Objects;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String light = HomeController.class.getResource("Themes/light-theme.css").toExternalForm();
        String dark = HomeController.class.getResource("Themes/dark-theme.css").toExternalForm();
        final OsThemeDetector detector = OsThemeDetector.getDetector();
        Consumer<Boolean> darkThemeListener = isDark -> {
            Platform.runLater(() -> {
                if (isDark) {
                    borderLayout.getScene().getStylesheets().set(0, dark);
                    img_music.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_music_white.png")));
                } else {
                    borderLayout.getScene().getStylesheets().set(0, light);
                    img_music.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_music_black.png")));
                }
            });
        };
        darkThemeListener.accept(detector.isDark() && OsThemeDetector.isSupported());
        detector.registerListener(darkThemeListener);

        title1.getStyleClass().add("title");
        items.getStyleClass().add("right-border");

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("musics.fxml"));
                borderLayout.setCenter(loader.load());
                musicsController = loader.getController();
                musicsController.setMainStage((Stage) borderLayout.getScene().getWindow());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void musics(MouseEvent mouseEvent) {
        if (musicsController != null) {
            if (!musicsController.isPlaying) {
                if (new File(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser").exists()) {
                /*
                this listener check , if media player listeners is running or no with 0 and 1
                 */
                    musicsController.setListener(result -> {
                        if (result == Listener.OK) {
                            tab_musics.setDisable(false);
                            // disable listener
                            musicsController.setListener(null);
                        } else {
                            tab_musics.setDisable(true);
                        }
                        System.out.println(result);
                    });
                    // handle listener
                    musicsController.refresh();
                } else {
                    Stage window = (Stage) borderLayout.getScene().getWindow();
                    window.close();
                    try {
                        openFolderConfig(new Stage(), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Dialog dialog = new Dialog(result -> {
                    if (result == Listener.OK) {
                        if (new File(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser").exists()) {
                            musicsController.pauseMedia();
                            musicsController.songBarVisibility(false);
                            musicsController.refresh();
                        } else {
                            musicsController.pauseMedia();
                            musicsController.songBarVisibility(false);
                            Stage window = (Stage) borderLayout.getScene().getWindow();
                            window.close();
                            try {
                                openFolderConfig(new Stage(), true);
                            } catch (IOException e) {
                                e.printStackTrace();
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
    public void folderConfigAction() {
        if (musicsController != null) {
            try {
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
                        musicsController.refresh();
                        if (musicsController.miniPlayer != null && musicsController.miniPlayer.isShowing()){
                            ((Stage)borderLayout.getScene().getWindow()).show();
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void downloadAction() {
        try {
            showDownloader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void miniPlayerAction() {
        if (musicsController != null && musicsController.isPlaying || Objects.requireNonNull(musicsController).songBar.isVisible()) {
            musicsController.createMiniPlayer();
            Stage stage = (Stage) borderLayout.getScene().getWindow();
            stage.hide();
        }
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
