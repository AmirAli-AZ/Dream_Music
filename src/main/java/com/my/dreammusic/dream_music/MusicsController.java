package com.my.dreammusic.dream_music;

import com.my.dreammusic.dream_music.Utils.NumericField;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;

public class MusicsController implements Initializable {

    @FXML
    public ListView<Song> list;
    @FXML
    public AnchorPane songBar;
    @FXML
    public Slider progress;
    @FXML
    public Slider volume;
    @FXML
    public Label currentTime;
    @FXML
    public Label totalTime;
    @FXML
    public ImageView rewind;
    @FXML
    public ImageView play;
    @FXML
    public ImageView forward;
    @FXML
    public ImageView img_volume;
    @FXML
    public ImageView repeat;
    @FXML
    public HBox hbox;
    @FXML
    public VBox container;
    @FXML
    public ImageView moreOption;

    public MediaPlayer mediaPlayer;
    public boolean isPlaying = false;
    private boolean isChanging = false;
    private boolean repeatMode = false;
    private final Object object = new Object();
    private UserData userData;
    private Listener listener;
    private int songPosition = 0;
    private boolean isRandomPlayer = false;
    private final Image playImage = new Image(getClass().getResourceAsStream("icons/baseline_play_arrow_white.png"));
    private final Image pauseImage = new Image(getClass().getResourceAsStream("icons/baseline_pause_white.png"));

    private final Slider rateSlider = new Slider();
    private final ContextMenu menu = new ContextMenu();
    private final NumericField numericField = new NumericField();

    public MiniPlayer miniPlayer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadFolder();
        songBarVisibility(false);
        getSongList();
        list.setCellFactory(song -> {
            SongListCell songListCell = new SongListCell();
            songListCell.setOnMouseClicked(e -> {
                if (!songListCell.isEmpty()) {
                    listClick(e);
                    e.consume();
                }
            });
            return songListCell;
        });

        ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();

        KeyValue keyValue1 = new KeyValue(baseColor, Color.valueOf("#2196F3"));
        KeyValue keyValue2 = new KeyValue(baseColor, Color.valueOf("#21dd8f"));
        KeyFrame keyFrame1 = new KeyFrame(Duration.ZERO, keyValue1);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(1000), keyValue2);
        Timeline timeline = new Timeline(keyFrame1, keyFrame2);

        baseColor.addListener((obs, oldColor, newColor) -> {
            if (isPlaying) {
                songBar.setStyle(String.format("-gradient-base: #%02x%02x%02x; ",
                        (int) (newColor.getRed() * 255),
                        (int) (newColor.getGreen() * 255),
                        (int) (newColor.getBlue() * 255)));
            }
        });

        timeline.setAutoReverse(true);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        progress.valueProperty().addListener((observableValue, number, t1) -> {
            if (mediaPlayer != null && isChanging)
                currentTime.setText(calculateTime(Duration.seconds(t1.doubleValue())));
        });

        volume.valueProperty().addListener((observableValue, number, t1) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume.getValue() * 0.01);
                long value = t1.longValue();
                if (value == 0) {
                    img_volume.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_volume_mute_white.png")));
                } else {
                    if (value > 50) {
                        img_volume.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_volume_up_white.png")));
                    } else {
                        img_volume.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_volume_down_white.png")));
                    }
                }
            }
        });
        createMenu();
    }

    public void listClick(MouseEvent mouseEvent) {
        Song info = list.getSelectionModel().getSelectedItem();
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (!isPlaying) {
                MediaPlayer(info.getMedia());
            }
            //list.getSelectionModel().clearSelection();
        } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            if (mediaPlayer != null) {
                int clickedPosition = list.getSelectionModel().getSelectedIndex();
                if (isPlaying) {
                    if (clickedPosition == songPosition) {
                        mediaPlayer.stop();
                        isPlaying = false;
                        play.setImage(playImage);
                        if (songBar.isVisible()) songBarVisibility(false);
                        list.getSelectionModel().clearSelection();
                        if (miniPlayer != null && miniPlayer.isShowing()) {
                            miniPlayer.close();
                        }
                    }
                } else {
                    if (clickedPosition == songPosition) {
                        if (songBar.isVisible()) songBarVisibility(false);
                        list.getSelectionModel().clearSelection();
                        if (miniPlayer != null && miniPlayer.isShowing()) {
                            miniPlayer.close();
                        }
                    }
                }
            }
        }
    }

    @FXML
    public void playMusic(MouseEvent mouseEvent) {
        if (mediaPlayer != null) {
            if (isPlaying) {
                pauseMedia();
                play.setImage(playImage);
                if (miniPlayer != null && miniPlayer.isShowing()) {
                    miniPlayer.setImage(playImage);
                }
            } else {
                playMedia();
                play.setImage(pauseImage);
                if (miniPlayer != null && miniPlayer.isShowing()) {
                    miniPlayer.setImage(pauseImage);
                }
            }
        }
    }

    @FXML
    public void rewindClick(MouseEvent mouseEvent) {
        if (mediaPlayer != null) {
            double newValue = mediaPlayer.getCurrentTime().toSeconds() - 5;
            if (newValue >= 0) {
                mediaPlayer.seek(Duration.seconds(newValue));
            }
        }
    }

    @FXML
    public void forwardClick(MouseEvent mouseEvent) {
        if (mediaPlayer != null) {
            double newValue = mediaPlayer.getCurrentTime().toSeconds() + 5;
            if (newValue <= mediaPlayer.getMedia().getDuration().toSeconds()) {
                mediaPlayer.seek(Duration.seconds(newValue));
            }
        }
    }

    @FXML
    public void repeatClick(MouseEvent mouseEvent) {
        if (!isRandomPlayer) {
            if (repeatMode) {
                repeatMode = false;
                repeat.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_repeat_white.png")));
            } else {
                repeatMode = true;
                repeat.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_repeat_on_white.png")));
            }
        }
    }

    public void createMedia(File file) {
        if (listener != null) {
            listener.onResult(Listener.CANCEL);
        }
        Media media = new Media(file.toURI().toString());
        MediaPlayer mp = new MediaPlayer(media);
        final Song song = new Song();
        song.setMedia(media);
        song.setPath(file.getAbsolutePath());
        song.setTitle(getFileName(file));
        song.setDate("");
        try {
            Path path = Paths.get(file.getAbsolutePath());
            FileTime fileTime = Files.getLastModifiedTime(path);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            song.setDate(dateFormat.format(fileTime.toMillis()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        mp.setOnReady(() -> {
            song.setAlbum((String) mp.getMedia().getMetadata().get("album"));
            song.setArtist((String) mp.getMedia().getMetadata().get("artist"));
            if (mp.getMedia().getMetadata().get("title") != null) {
                song.setTitle((String) mp.getMedia().getMetadata().get("title"));
            }
            song.setImage((Image) mp.getMedia().getMetadata().get("image"));
            list.getItems().add(song);
            if (listener != null) {
                listener.onResult(Listener.OK);
            }

            synchronized (object) {
                object.notify();
            }
        });
    }

    public String getUserPath() {
        return System.getProperty("user.home");
    }

    public void getSongList() {
        try {
            list.getItems().clear();
            File file = new File(userData.getPath());
            File[] files = file.listFiles();
            if (files != null) {
                Arrays.sort(files);
                if (files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isFile()) {
                            if (getExtension(files[i]).equals(".mp3") || getExtension(files[i]).equals(".wav")) {
                                createMedia(files[i]);
                                synchronized (object) {
                                    object.wait(100);
                                }
                            }
                        }
                    }

                }
            }
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        System.gc();
    }

    public String getExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) return "";
        return name.substring(lastIndexOf);
    }

    public String getFileName(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) return name;
        return name.substring(0, lastIndexOf);
    }

    public String calculateTime(Duration time) {
        long s = (long) time.toSeconds();
        return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    public void songBarVisibility(boolean b) {
        songBar.setVisible(b);
        songBar.setManaged(b);
    }

    public UserData read() throws IOException, ClassNotFoundException {
        UserData data;
        FileInputStream fileIn = new FileInputStream(getUserPath() + File.separator + "Dream Music" + File.separator + "data.ser");
        ObjectInputStream ob = new ObjectInputStream(fileIn);
        data = (UserData) ob.readObject();
        ob.close();
        fileIn.close();
        return data;
    }

    public void playMedia() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.play();
            isPlaying = true;
        }
    }

    public void pauseMedia() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void refresh() {
        getSongList();
    }

    public void loadFolder() {
        try {
            userData = read();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @FXML
    public void sliderPressed(Event event) {
        if (mediaPlayer != null) isChanging = true;
    }

    @FXML
    public void sliderReleased(Event event) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(progress.getValue()));
            isChanging = false;
        }
    }

    @FXML
    public void moreOptionClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (!menu.isShowing()) {
                menu.show(moreOption, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                numericField.setValue((int) rateSlider.getValue());
            }
        }
    }

    private void createMenu() {
        Menu rate = new Menu("Rate");
        rateSlider.setMin(25);
        rateSlider.setMax(200);
        rateSlider.setValue(100);
        VBox vBox = new VBox(3);

        ContextMenu textFieldOptions = new ContextMenu();
        MenuItem defaultValue = new MenuItem("Default Value");
        defaultValue.setOnAction(e -> {
            if (rateSlider.getValue() == 100) {
                if (numericField.getValue() != 100)
                    numericField.setValue((int) rateSlider.getValue());
            } else {
                rateSlider.setValue(100);
            }
        });
        textFieldOptions.getItems().add(defaultValue);
        numericField.setContextMenu(textFieldOptions);
        numericField.setValue(100);

        numericField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (numericField.getValue() <= 200 && !(numericField.getValue() < 25))
                    rateSlider.setValue(numericField.getValue());
                else
                    numericField.setValue((int) rateSlider.getValue());
            }
        });

        rateSlider.valueProperty().addListener((observableValue, number, t1) -> {
            if (mediaPlayer != null) {
                double value = t1.doubleValue();
                mediaPlayer.setRate(value * 0.01);
                numericField.setValue((int) value);
            }
        });
        vBox.getChildren().addAll(numericField, rateSlider);

        CustomMenuItem rateSettings = new CustomMenuItem(vBox);
        rateSettings.setHideOnClick(false);
        rate.getItems().add(rateSettings);

        MenuItem randomPlayer = new MenuItem("Random Player : Off");
        randomPlayer.setOnAction(e -> {
            if (isRandomPlayer) {
                isRandomPlayer = false;
                randomPlayer.setText("Random Player : Off");
            } else {
                isRandomPlayer = true;
                randomPlayer.setText("Random Player : On");
                // disable repeat mode if it's on
                if (repeatMode) {
                    repeatMode = false;
                    repeat.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_repeat_white.png")));
                }
            }
        });

        menu.getItems().addAll(rate, randomPlayer);
    }

    private void MediaPlayer(Media media) {
        songPosition = list.getSelectionModel().getSelectedIndex();
        if (!songBar.isVisible()) songBarVisibility(true);
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnReady(() -> {
            totalTime.setText(calculateTime(mediaPlayer.getMedia().getDuration()));
            progress.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
            play.setImage(pauseImage);
            mediaPlayer.setRate(rateSlider.getValue() * 0.01);
            playMedia();
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            if (!repeatMode) {
                if (!isChanging) {
                    if (!isRandomPlayer) {
                        isPlaying = false;
                        play.setImage(playImage);
                        songBarVisibility(false);
                        list.getSelectionModel().clearSelection();
                        if (miniPlayer != null && miniPlayer.isShowing()) {
                            miniPlayer.close();
                        }
                    } else {
                        isPlaying = false;
                        int randomPosition = pickRandom(0, list.getItems().size() - 1);
                        list.getSelectionModel().select(randomPosition);
                        if (miniPlayer != null && miniPlayer.isShowing()) {
                            miniPlayer.setMediaTitle(list.getItems().get(randomPosition).getTitle());
                        }
                        MediaPlayer(list.getItems().get(randomPosition).getMedia());
                    }
                }
            } else {
                if (!isChanging && !isRandomPlayer) mediaPlayer.seek(Duration.ZERO);
            }
        });

        mediaPlayer.currentTimeProperty().addListener((observableValue, duration, t1) -> {
            if (!isChanging) {
                currentTime.setText(calculateTime(t1));
                progress.setValue(t1.toSeconds());
            }
        });

    }

    private int pickRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max + 1 - min) + min;
    }

    public class MiniPlayer extends Stage {

        private static final double height = 200, width = 400;
        private final VBox root = new VBox(10);
        private final HBox controller = new HBox(3);
        private final Label mediaName = new Label();
        private final ImageView play2 = new ImageView();
        private final Label currentTime2 = new Label("0:00:00");
        private final Label totalTime2 = new Label("0:00:00");

        public MiniPlayer() {
            setTitle("Mini Player");
            setMinHeight(height);
            setMinWidth(width);

            mediaName.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 16));
            mediaName.getStyleClass().add("white-color");
            totalTime2.getStyleClass().add("white-color");
            totalTime2.textProperty().bind(totalTime.textProperty());
            currentTime2.textProperty().bind(currentTime.textProperty());
            currentTime2.getStyleClass().add("white-color");

            play2.setImage(isPlaying ? pauseImage : playImage);
            play2.setFitWidth(40);
            play2.setFitHeight(40);
            play2.setOnMouseClicked(e -> {
                if (mediaPlayer != null) {
                    if (!isPlaying) {
                        if (songBar.isVisible()) {
                            playMedia();
                            play2.setImage(pauseImage);
                            play.setImage(pauseImage);
                        }
                    } else {
                        if (songBar.isVisible()) {
                            pauseMedia();
                            play2.setImage(playImage);
                            play.setImage(playImage);
                        }
                    }
                }
            });
            controller.setPadding(new Insets(10, 10, 10, 10));
            controller.setAlignment(Pos.CENTER);
            VBox.setVgrow(controller , Priority.ALWAYS);
            controller.getChildren().addAll(currentTime2, play2, totalTime2);

            root.getStyleClass().add("animated-gradient");
            root.setPadding(new Insets(6, 6, 6, 6));
            root.getChildren().addAll(mediaName, controller);

            Scene scene = new Scene(root, width, height);

            String light = getClass().getResource("Themes/light-theme.css").toExternalForm();
            scene.getStylesheets().add(light);

            setScene(scene);
            setResizable(false);
            getIcons().addAll(
                    new Image(getClass().getResourceAsStream("icons/icon64x64.png")),
                    new Image(getClass().getResourceAsStream("icons/icon32x32.png")),
                    new Image(getClass().getResourceAsStream("icons/icon16x16.png"))
            );
            setAnimation();
        }

        public void setAnimation() {
            ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();

            KeyValue keyValue1 = new KeyValue(baseColor, Color.valueOf("#2196F3"));
            KeyValue keyValue2 = new KeyValue(baseColor, Color.valueOf("#21dd8f"));
            KeyFrame keyFrame1 = new KeyFrame(Duration.ZERO, keyValue1);
            KeyFrame keyFrame2 = new KeyFrame(Duration.millis(1000), keyValue2);
            Timeline timeline = new Timeline(keyFrame1, keyFrame2);

            baseColor.addListener((obs, oldColor, newColor) -> {
                if (isPlaying) {
                    root.setStyle(String.format("-gradient-base: #%02x%02x%02x; ",
                            (int) (newColor.getRed() * 255),
                            (int) (newColor.getGreen() * 255),
                            (int) (newColor.getBlue() * 255)));
                }
            });

            timeline.setAutoReverse(true);
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }

        public void setMediaTitle(String s){
            mediaName.setText(s);
        }

        public void setImage(Image img){
            play2.setImage(img);
        }
    }

    public void createMiniPlayer(){
        miniPlayer = new MiniPlayer();
        miniPlayer.setMediaTitle(list.getItems().get(songPosition).getTitle());
        miniPlayer.show();
    }

}