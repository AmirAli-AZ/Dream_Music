package com.my.dreammusic.dream_music;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private ListView<Song> list;
    @FXML
    private AnchorPane songBar;
    @FXML
    private Slider progress;
    @FXML
    private Slider volume;
    @FXML
    private Label currentTime;
    @FXML
    private Label totalTime;
    @FXML
    private ImageView rewind;
    @FXML
    private ImageView play;
    @FXML
    private ImageView forward;
    @FXML
    private ImageView img_volume;
    @FXML
    private ImageView repeat;
    @FXML
    private HBox hbox;

    private ObservableList<Song> songList;
    private MediaPlayer mediaPlayer;
    public static boolean isPlaying = false;
    private boolean isChanging = false;
    private boolean repeatMode = false;
    private final Object object = new Object();
    private UserData userData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            userData = read();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        songBarVisibility(false);
        songList = FXCollections.observableArrayList();
        getSongList();
        list.setItems(songList);
        list.setCellFactory(songListView -> new SongListCell());

        ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();

        KeyValue keyValue1 = new KeyValue(baseColor, Color.valueOf("#2196F3"));
        KeyValue keyValue2 = new KeyValue(baseColor, Color.valueOf("#21dd8f"));
        KeyFrame keyFrame1 = new KeyFrame(Duration.ZERO, keyValue1);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(1000), keyValue2);
        Timeline timeline = new Timeline(keyFrame1, keyFrame2);

        baseColor.addListener((obs, oldColor, newColor) -> {
            if (isPlaying){
                songBar.setStyle(String.format("-gradient-base: #%02x%02x%02x; ",
                        (int)(newColor.getRed()* 255),
                        (int)(newColor.getGreen()* 255),
                        (int)(newColor.getBlue()* 255)));
            }
        });

        timeline.setAutoReverse(true);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    @FXML
    public void listClick(MouseEvent mouseEvent) {
        Song info = list.getSelectionModel().getSelectedItem();
        if (info != null) {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                if (!isPlaying) {
                    songBarVisibility(true);
                    mediaPlayer = new MediaPlayer(info.getMedia());

                    mediaPlayer.setOnReady(new Runnable() {
                        @Override
                        public void run() {
                            totalTime.setText(calculateTime(info.getMedia().getDuration()));
                            progress.setMin(0);
                            progress.setMax(info.getMedia().getDuration().toSeconds());
                            isPlaying = true;
                            play.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_pause_white.png")));
                            mediaPlayer.setVolume(volume.getValue() * 0.01);
                            mediaPlayer.play();
                        }
                    });

                    mediaPlayer.setOnEndOfMedia(new Runnable() {
                        @Override
                        public void run() {
                            if (!repeatMode) {
                                isPlaying = false;
                                play.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_play_arrow_white.png")));
                                songBarVisibility(false);
                                list.getSelectionModel().clearSelection();
                            } else {
                                mediaPlayer.seek(Duration.ZERO);
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

                    progress.valueProperty().addListener(new InvalidationListener() {
                        @Override
                        public void invalidated(Observable observable) {
                            if (progress.isValueChanging()) {
                                isChanging = true;
                                currentTime.setText(calculateTime(Duration.seconds(progress.getValue())));
                                mediaPlayer.seek(Duration.seconds(progress.getValue()));
                            }
                            isChanging = false;
                        }
                    });

                    volume.valueProperty().addListener(new ChangeListener<Number>() {
                        @Override
                        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                            mediaPlayer.setVolume(volume.getValue() * 0.01);
                            long value = t1.longValue();
                            if (value > 50) {
                                img_volume.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_volume_up_white.png")));
                            }
                            if (value == 0) {
                                img_volume.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_volume_mute_white.png")));
                            } else if (value <= 50) {
                                img_volume.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_volume_down_white.png")));
                            }
                        }
                    });
                }
                //list.getSelectionModel().clearSelection();
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                if (mediaPlayer != null) {
                    if (isPlaying) {
                        mediaPlayer.dispose();
                        isPlaying = false;
                        play.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_play_arrow_white.png")));
                    }
                    songBarVisibility(false);
                    list.getSelectionModel().clearSelection();
                }
            }
        }
    }

    @FXML
    public void playMusic(MouseEvent mouseEvent) {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                isPlaying = false;
                play.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_play_arrow_white.png")));
            } else {
                mediaPlayer.play();
                isPlaying = true;
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
        Media media = new Media(file.toURI().toString());
        MediaPlayer mp = new MediaPlayer(media);
        final Song song = new Song();
        song.setMedia(media);
        song.setPath(file.getAbsolutePath());
        try {
            Path path = Paths.get(file.getAbsolutePath());
            FileTime fileTime = Files.getLastModifiedTime(path);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            song.setDate(dateFormat.format(fileTime.toMillis()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        song.setImage(new Image(MusicsController.class.getResourceAsStream("icons/baseline_person_black.png")));
        mp.setOnReady(new Runnable() {
            @Override
            public void run() {
                song.setAlbum((String) mp.getMedia().getMetadata().get("album"));
                song.setArtist((String) mp.getMedia().getMetadata().get("artist"));
                song.setTitle((String) mp.getMedia().getMetadata().get("title"));
                if (mp.getMedia().getMetadata().get("image") != null) {
                    song.setImage((Image) mp.getMedia().getMetadata().get("image"));
                }
                songList.add(song);

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
            File file = new File(userData.getPath());
            File[] files = file.listFiles();
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
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        System.gc();
    }

    public String getExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }

    public String calculateTime(Duration time) {
        long s = (long) time.toSeconds();
        return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    private void songBarVisibility(boolean b) {
        songBar.setVisible(b);
        songBar.setManaged(b);
        progress.setManaged(b);
        img_volume.setManaged(b);
        volume.setManaged(b);
        hbox.setManaged(b);
        currentTime.setManaged(b);
        rewind.setManaged(b);
        play.setManaged(b);
        forward.setManaged(b);
        totalTime.setManaged(b);
        repeat.setManaged(b);
    }

    private UserData read() throws IOException, ClassNotFoundException {
        UserData data;
        FileInputStream fileIn = new FileInputStream(getUserPath() + File.separator + "Dream Music" + File.separator + "data.ser");
        ObjectInputStream ob = new ObjectInputStream(fileIn);
        data = (UserData) ob.readObject();
        ob.close();
        fileIn.close();
        return data;
    }

}