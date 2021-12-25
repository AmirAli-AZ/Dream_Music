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
import java.net.URL;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //img_music.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_music_black.png")));
        title1.getStyleClass().add("title");
        items.getStyleClass().add("right-border");
        try {
            FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("musics.fxml"));
            container.setCenter(loader.load());
            musicsController = loader.getController();
        }catch (IOException e){
            e.printStackTrace();
        }
        final OsThemeDetector detector = OsThemeDetector.getDetector();
        Consumer<Boolean> darkThemeListener = isDark -> {
            Platform.runLater(() -> {
                if (isDark){
                    img_music.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_music_white.png")));
                }else {
                    img_music.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_music_black.png")));
                }
            });
        };
        darkThemeListener.accept(detector.isDark());
        detector.registerListener(darkThemeListener);

    }

    @FXML
    public void musics(MouseEvent mouseEvent) {
        if (!musicsController.isPlaying) {
            if (new File(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser").exists()) {
                if (musicsController.list.getItems().size() > 0) {
                /*
                this listener check , if media player listeners is running or no with 0 and 1
                 */
                    musicsController.setListener(new Listener() {
                        @Override
                        public void onResult(int result) {
                            if (result == MusicsController.OK) {
                                tab_musics.setDisable(false);
                                // disable listener
                                musicsController.setListener(null);
                            } else {
                                tab_musics.setDisable(true);
                            }
                            System.out.println(result);
                        }
                    });
                    // handle listener
                    musicsController.refresh();
                }
            } else {
                Stage window = (Stage) container.getScene().getWindow();
                window.close();
                try {
                    openFolderConfig(new Stage() , true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Dialog dialog = new Dialog(new Listener() {
                @Override
                public void onResult(int result) {
                    if (result == Dialog.OK){
                        if (new File(System.getProperty("user.home") + File.separator + "Dream Music" + File.separator + "data.ser").exists()) {
                            musicsController.pauseMedia();
                            musicsController.songBarVisibility(false);
                            musicsController.refresh();
                        }else {
                            musicsController.pauseMedia();
                            musicsController.songBarVisibility(false);
                            Stage window = (Stage) container.getScene().getWindow();
                            window.close();
                            try {
                                openFolderConfig(new Stage() , true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
        if (!musicsController.isPlaying){
            try {
                Stage stage = new Stage();
                stage.setTitle("Folder Config");

                FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("folderConfig.fxml"));
                Scene scene = new Scene(loader.load(), 611, 288);
                scene.getStylesheets().add(HomeController.class.getResource("Themes/light-theme.css").toExternalForm());

                FolderConfigController controller = loader.getController();
                controller.setOpenHome(false);

                controller.setListener(new Listener() {
                    @Override
                    public void onResult(int result) {
                        if (result == controller.OK){
                            if (musicsController.isPlaying){
                                musicsController.pauseMedia();
                                musicsController.songBarVisibility(false);
                            }
                            musicsController.loadFolder();
                            musicsController.refresh();
                            controller.setListener(null);
                        }
                    }
                });
                controller.setScene(scene);
                stage.setOnCloseRequest(e ->{
                    controller.shutDown();
                });
                stage.setResizable(false);
                stage.setScene(scene);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Dialog dialog = new Dialog(new Listener() {
                @Override
                public void onResult(int result) {
                    if (result == Dialog.OK){
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

                            controller.setListener(new Listener() {
                                @Override
                                public void onResult(int result) {
                                    if (result == controller.OK){
                                        if (musicsController.isPlaying){
                                            musicsController.pauseMedia();
                                            musicsController.songBarVisibility(false);
                                        }
                                        musicsController.loadFolder();
                                        musicsController.refresh();

                                        controller.setListener(null);
                                    }
                                }
                            });
                            controller.setScene(scene);
                            stage.setOnCloseRequest(e ->{
                                controller.shutDown();
                            });

                            stage.setResizable(false);
                            stage.setScene(scene);
                            stage.show();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
            dialog.setTitle("Warning");
            dialog.setMessage("Music is playing , pause music.");
            dialog.setImage(new Image(HomeController.class.getResourceAsStream("icons/ic_warning.png")));
            dialog.show();
        }
    }

    public void openFolderConfig(Stage stage , boolean openHome) throws IOException {
        stage.setTitle("Folder Config");
        FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("folderConfig.fxml"));
        Scene scene = new Scene(loader.load(), 611, 288);
        scene.getStylesheets().add(HomeController.class.getResource("Themes/light-theme.css").toExternalForm());

        FolderConfigController controller = loader.getController();
        controller.setOpenHome(openHome);
        controller.setScene(scene);

        stage.setResizable(false);
        stage.setScene(scene);
        if (openHome) stage.show();
        else stage.showAndWait();
    }

}
