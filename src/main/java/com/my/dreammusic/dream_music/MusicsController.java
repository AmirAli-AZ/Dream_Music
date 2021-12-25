package com.my.dreammusic.dream_music;

import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
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

    private MediaPlayer mediaPlayer;
    public boolean isPlaying = false;
    private boolean isChanging = false;
    private boolean repeatMode = false;
    private final Object object = new Object();
    private UserData userData;
    public static final int OK = 1, CANCEL = 0;
    private Listener listener;
    private int songPosition = 0;

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

        progress.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (mediaPlayer != null && isChanging) currentTime.setText(calculateTime(Duration.seconds(t1.doubleValue())));
            }
        });

        volume.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
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
            }
        });
    }

    public void listClick(MouseEvent mouseEvent) {
        Song info = list.getSelectionModel().getSelectedItem();
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (!isPlaying) {
                songPosition = list.getSelectionModel().getSelectedIndex();
                songBarVisibility(true);
                mediaPlayer = new MediaPlayer(info.getMedia());

                mediaPlayer.setOnReady(new Runnable() {
                    @Override
                    public void run() {
                        totalTime.setText(calculateTime(mediaPlayer.getMedia().getDuration()));
                        progress.setMin(0);
                        progress.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
                        play.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_pause_white.png")));
                        mediaPlayer.setVolume(volume.getValue() * 0.01);
                        playMedia();
                    }
                });

                mediaPlayer.setOnEndOfMedia(new Runnable() {
                    @Override
                    public void run() {
                        if (!repeatMode) {
                            if (!isChanging) {
                                isPlaying = false;
                                play.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_play_arrow_white.png")));
                                songBarVisibility(false);
                                list.getSelectionModel().clearSelection();
                            }
                        } else {
                            if (!isChanging) mediaPlayer.seek(Duration.ZERO);
                        }
                    }
                });

                mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                    @Override
                    public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration t1) {
                        if (!isChanging) {
                            currentTime.setText(calculateTime(t1));
                            progress.setValue(t1.toSeconds());
                        }
                    }
                });
            }
            //list.getSelectionModel().clearSelection();
        } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            if (mediaPlayer != null) {
                int clickedPosition = list.getSelectionModel().getSelectedIndex();
                if (isPlaying) {
                    if (clickedPosition == songPosition) {
                        pauseMedia();
                        play.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_play_arrow_white.png")));
                        if (songBar.isVisible()) songBarVisibility(false);
                        list.getSelectionModel().clearSelection();
                    }
                } else {
                    if (clickedPosition == songPosition) {
                        if (songBar.isVisible()) songBarVisibility(false);
                        list.getSelectionModel().clearSelection();
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
                play.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_play_arrow_white.png")));
            } else {
                playMedia();
                play.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_pause_white.png")));
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
        if (repeatMode) {
            repeatMode = false;
            repeat.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_repeat_white.png")));
        } else {
            repeatMode = true;
            repeat.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_repeat_on_white.png")));
        }
    }

    public void createMedia(File file) {
        if (listener != null) {
            listener.onResult(CANCEL);
        }
        Media media = new Media(file.toURI().toString());
        MediaPlayer mp = new MediaPlayer(media);
        final Song song = new Song();
        song.setMedia(media);
        song.setPath(file.getAbsolutePath());
        song.setTitle(getFileName(file));
        try {
            Path path = Paths.get(file.getAbsolutePath());
            FileTime fileTime = Files.getLastModifiedTime(path);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            song.setDate(dateFormat.format(fileTime.toMillis()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        mp.setOnReady(new Runnable() {
            @Override
            public void run() {
                song.setAlbum((String) mp.getMedia().getMetadata().get("album"));
                song.setArtist((String) mp.getMedia().getMetadata().get("artist"));
                if (mp.getMedia().getMetadata().get("title") != null) {
                    song.setTitle((String) mp.getMedia().getMetadata().get("title"));
                }
                song.setImage((Image) mp.getMedia().getMetadata().get("image"));
                list.getItems().add(song);
                if (listener != null) {
                    listener.onResult(OK);
                }

                synchronized (object) {
                    object.notify();
                }
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
    public void sliderPressed(Event event){
        if (mediaPlayer != null) isChanging = true;
    }

    @FXML
    public void sliderReleased(Event event){
        if (mediaPlayer != null){
            mediaPlayer.seek(Duration.seconds(progress.getValue()));
            isChanging = false;
        }
    }
}