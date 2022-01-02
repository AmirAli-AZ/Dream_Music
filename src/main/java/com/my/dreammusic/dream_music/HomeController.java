package com.my.dreammusic.dream_music;

import com.jthemedetecor.OsThemeDetector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
    public BorderPane container;
    @FXML
    private HBox tab_musics;

    private MusicsController musicsController;
    private Scene scene;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //img_music.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_music_black.png")));

        String light = HomeController.class.getResource("Themes/light-theme.css").toExternalForm();
        String dark = HomeController.class.getResource("Themes/dark-theme.css").toExternalForm();
        final OsThemeDetector detector = OsThemeDetector.getDetector();
        Consumer<Boolean> darkThemeListener = isDark -> {
            Platform.runLater(() -> {
                if (isDark) {
                    container.getScene().getStylesheets().set(0, dark);
                    img_music.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_music_white.png")));
                } else {
                    container.getScene().getStylesheets().set(0, light);
                    img_music.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_music_black.png")));
                }
            });
        };
        darkThemeListener.accept(detector.isDark() && OsThemeDetector.isSupported());
        detector.registerListener(darkThemeListener);

        title1.getStyleClass().add("title");
        items.getStyleClass().add("right-border");
        try {
            FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("musics.fxml"));
            container.setCenter(loader.load());
            musicsController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void musics(MouseEvent mouseEvent) {
        if (!musicsController.isPlaying) {
            if (new File(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser").exists()) {
                if (musicsController.list.getItems().size() > 0) {
                /*
                this listener check , if media player listeners is running or no with 0 and 1
                 */
                    musicsController.setListener(result -> {
                        if (result == MusicsController.OK) {
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
                }
            } else {
                Stage window = (Stage) container.getScene().getWindow();
                window.close();
                try {
                    openFolderConfig(new Stage(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Dialog dialog = new Dialog(result -> {
                if (result == Dialog.OK) {
                    if (new File(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser").exists()) {
                        musicsController.pauseMedia();
                        musicsController.songBarVisibility(false);
                        musicsController.refresh();
                    } else {
                        musicsController.pauseMedia();
                        musicsController.songBarVisibility(false);
                        Stage window = (Stage) container.getScene().getWindow();
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

    @FXML
    public void folderConfigAction() {
        if (!musicsController.isPlaying) {
            try {
                Stage stage = new Stage();
                stage.setTitle("Folder Config");

                FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("folderConfig.fxml"));
                Scene scene = new Scene(loader.load(), 611, 288);
                scene.getStylesheets().add(HomeController.class.getResource("Themes/light-theme.css").toExternalForm());

                FolderConfigController controller = loader.getController();
                controller.setOpenHome(false);

                controller.setListener(result -> {
                    if (result == controller.OK) {
                        if (musicsController.isPlaying) {
                            musicsController.pauseMedia();
                            musicsController.songBarVisibility(false);
                        }
                        musicsController.loadFolder();
                        musicsController.refresh();
                        controller.setListener(null);
                    }
                });

                stage.setOnCloseRequest(e -> {
                    controller.shutDown();
                });
                stage.setResizable(false);
                stage.setScene(scene);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Dialog dialog = new Dialog(result -> {
                if (result == Dialog.OK) {
                    musicsController.pauseMedia();
                    musicsController.songBarVisibility(false);
                    try {
                        Stage stage = new Stage();
                        stage.setTitle("Folder Config");

                        FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("folderConfig.fxml"));
                        Scene scene = new Scene(loader.load(), 611, 288);
                        scene.getStylesheets().add(HomeController.class.getResource("Themes/light-theme.css").toExternalForm());

                        FolderConfigController controller = loader.getController();
                        controller.setOpenHome(false);

                        controller.setListener(result1 -> {
                            if (result1 == controller.OK) {
                                if (musicsController.isPlaying) {
                                    musicsController.pauseMedia();
                                    musicsController.songBarVisibility(false);
                                }
                                musicsController.loadFolder();
                                musicsController.refresh();

                                controller.setListener(null);
                            }
                        });

                        stage.setOnCloseRequest(e -> {
                            controller.shutDown();
                        });

                        stage.setResizable(false);
                        stage.setScene(scene);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            dialog.setTitle("Warning");
            dialog.setMessage("Music is playing , pause music.");
            dialog.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_warning.png")));
            dialog.show();
        }
    }

    @FXML
    public void downloadAction() {
        Thread thread = new Thread(() -> {
            Platform.runLater(() -> {
                if (isConnected()) {
                    try {
                        showDownloader(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    showNoConnectionDialog();
                }
            });
        });
        thread.start();
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
        if (openHome) stage.show();
        else stage.showAndWait();
    }

    public boolean isConnected() {
        try {
            final URL url = new URL("http://www.google.com");
            final URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private void showNoConnectionDialog() {
        Dialog dialog = new Dialog(result -> {
            if (result == Dialog.OK) {
                Thread thread = new Thread(() -> {
                    Platform.runLater(() -> {
                        if (isConnected()) {
                            try {
                                showDownloader(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            showNoConnectionDialog();
                        }
                    });
                });
                thread.start();
            }
        });
        dialog.setTitle("No Connection");
        dialog.setMessage("Check your internet connection");
        dialog.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_warning.png")));
        dialog.setBtnOkText("Try Again");
        dialog.show();
    }

    public void showDownloader(boolean showAndWait) throws IOException {
        Stage stage = new Stage();
        stage.setTitle("Downloader");
        FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("downloaderUI.fxml"));
        Scene scene = new Scene(loader.load(), 600, 400);
        scene.getStylesheets().add(FolderConfigController.class.getResource("Themes/dialog-light-theme.css").toExternalForm());
        DownloaderUIController downloaderUIController = loader.getController();
        stage.setOnCloseRequest(e -> {
            if (downloaderUIController.thread != null &&
                    downloaderUIController.downloader != null &&
                    downloaderUIController.thread.isAlive()) {
                downloaderUIController.downloader.exit();
            }
        });
        stage.setMinHeight(400);
        stage.setMinWidth(600);
        stage.setScene(scene);
        if (showAndWait) stage.showAndWait();
        else stage.show();
    }
}
