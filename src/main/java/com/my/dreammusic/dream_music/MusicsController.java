package com.my.dreammusic.dream_music;

import com.my.dreammusic.dream_music.logging.Logger;
import com.my.dreammusic.dream_music.utils.NumericField;
import com.my.dreammusic.dream_music.utils.UserDataManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public ImageView play;

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

    @FXML
    public ProgressBar loading;

    protected MediaPlayer mediaPlayer;
    protected MiniPlayer miniPlayer;
    public boolean isPlaying;
    public boolean isMiniPlayerOpen;
    private boolean isChanging;
    private boolean repeatMode;
    private UserData userData;
    private int songPosition;
    private boolean isRandomPlayer;
    private final Image playImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/baseline_play_arrow_white.png")));
    private final Image pauseImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/baseline_pause_white.png")));
    private Stage mainStage;

    private final Slider rateSlider = new Slider();
    private final ContextMenu menu = new ContextMenu();
    private final NumericField numericField = new NumericField();

    private final StringProperty currentTimeProperty = new SimpleStringProperty();
    private final StringProperty totalTimeProperty = new SimpleStringProperty();
    private final BooleanProperty refreshing = new SimpleBooleanProperty(false);

    private static final Logger logger = Logger.getLogger(MusicsController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            logger.setWriter(UserDataManager.getLogsPath() + File.separator + logger.getName() + ".log", true);
        } catch (IOException e) {
            logger.error(e);
        }
        logger.info("Music Controller initialize");
        songBarVisibility(false);
        loadFolder();
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
            if (mediaPlayer != null && isChanging) {
                currentTimeProperty.set(calculateTime(Duration.seconds(t1.doubleValue())));
            }
        });

        volume.valueProperty().addListener((observableValue, number, t1) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume.getValue() * 0.01);
                long value = t1.longValue();
                if (value == 0) {
                    img_volume.setImage(new Image(Objects.requireNonNull(MusicsController.class.getResourceAsStream("icons/baseline_volume_mute_white.png"))));
                } else if (value > 50) {
                    img_volume.setImage(new Image(Objects.requireNonNull(MusicsController.class.getResourceAsStream("icons/baseline_volume_up_white.png"))));
                } else {
                    img_volume.setImage(new Image(Objects.requireNonNull(MusicsController.class.getResourceAsStream("icons/baseline_volume_down_white.png"))));
                }
            }
        });
        totalTime.textProperty().bind(totalTimeProperty);
        currentTime.textProperty().bind(currentTimeProperty);
        createMenu();
        getSongList();
    }

    public void listClick(MouseEvent mouseEvent) {
        // get selected item
        Song info = list.getSelectionModel().getSelectedItem();
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (!isPlaying) createMediaPlayer(info.getMedia());
            // list.getSelectionModel().clearSelection();
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
        if (mediaPlayer != null && mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (isPlaying) {
                pauseMedia();
                play.setImage(playImage);
            } else {
                playMedia();
                play.setImage(pauseImage);
            }
        }
    }

    @FXML
    public void rewindClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            rewindMedia();
        }
    }

    @FXML
    public void forwardClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            forwardMedia();
        }
    }

    @FXML
    public void repeatClick(MouseEvent mouseEvent) {
        if (!isRandomPlayer && mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (repeatMode) {
                repeatMode = false;
                repeat.setImage(new Image(Objects.requireNonNull(MusicsController.class.getResourceAsStream("icons/baseline_repeat_white.png"))));
            } else {
                repeatMode = true;
                repeat.setImage(new Image(Objects.requireNonNull(MusicsController.class.getResourceAsStream("icons/baseline_repeat_on_white.png"))));
            }
        }
    }

    public void getSongList() {
        // show completely
        if (!loading.isManaged())
            loading.setManaged(true);

        // do in background
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // create an object to synchronize the code
                Object ob = new Object();
                Platform.runLater(() -> list.getItems().clear());
                if (userData != null) {
                    // get list of files
                    File file = new File(userData.getPath());
                    File[] files = file.listFiles();
                    if (files != null && files.length > 0) {
                        // sort files
                        Arrays.sort(files);
                        for (File value : files) {
                            if (value.isFile() &&
                                    FilenameUtils.getExtension(value.getAbsolutePath()).equals("mp3") ||
                                    FilenameUtils.getExtension(value.getAbsolutePath()).equals("wav")) {
                                if (!refreshing.get())
                                    refreshing.set(true);
                                // set default values
                                Media media = new Media(value.toURI().toString());
                                MediaPlayer mp = new MediaPlayer(media);
                                final Song song = new Song();
                                song.setMedia(media);
                                song.setPath(value.getAbsolutePath());
                                song.setTitle(FilenameUtils.getBaseName(value.getAbsolutePath()));

                                // get file last modified time
                                Path path = Paths.get(value.getAbsolutePath());
                                FileTime fileTime = Files.getLastModifiedTime(path);
                                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                song.setDate(dateFormat.format(fileTime.toMillis()));

                                mp.setOnReady(() -> {
                                    // set values
                                    song.setAlbum((String) mp.getMedia().getMetadata().get("album"));
                                    song.setArtist((String) mp.getMedia().getMetadata().get("artist"));
                                    if (mp.getMedia().getMetadata().get("title") != null) {
                                        song.setTitle((String) mp.getMedia().getMetadata().get("title"));
                                    }
                                    song.setImage((Image) mp.getMedia().getMetadata().get("image"));
                                    Platform.runLater(() -> list.getItems().add(song));

                                    synchronized (ob) {
                                        ob.notify();
                                    }
                                });

                                synchronized (ob) {
                                    ob.wait();
                                }
                            }
                        }
                        if (refreshing.get())
                            refreshing.set(false);
                    }
                }
                System.gc();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // hide completely
                loading.setManaged(false);
            }

            @Override
            protected void failed() {
                super.failed();
                // hide completely
                loading.setManaged(false);
            }
        };
        // add task to thread and start
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public String calculateTime(Duration time) {
        long seconds = (long) time.toSeconds();
        long HH = seconds / 3600;
        long MM = (seconds % 3600) / 60;
        long SS = seconds % 60;
        if (seconds < 3600)
            return String.format("%02d:%02d", MM, SS);
        else
            return String.format("%02d:%02d:%02d", HH, MM, SS);
    }

    public void songBarVisibility(boolean b) {
        songBar.setVisible(b);
        songBar.setManaged(b);
    }

    public void playMedia() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.play();
            isPlaying = true;
            logger.info("play");
        }
    }

    public void pauseMedia() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            logger.info("pause");
        }
    }

    public void loadFolder() {
        UserDataManager manager = new UserDataManager();
        userData = manager.read();
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
        if (mouseEvent.getButton() == MouseButton.PRIMARY && !menu.isShowing()) {
            menu.show(moreOption, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            numericField.setValue((int) rateSlider.getValue());
        }
    }

    @FXML
    public void listKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.UP) {
            if (songBar.isVisible())
                rewindMedia();
        } else {
            if (songBar.isVisible())
                forwardMedia();
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
        randomPlayer.disableProperty().bind(Bindings.lessThan(Bindings.size(list.getItems()), 2));
        randomPlayer.disableProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue && isRandomPlayer){
                isRandomPlayer = false;
                randomPlayer.setText("Random Player : Off");
            }
        });
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
                    repeat.setImage(new Image(Objects.requireNonNull(MusicsController.class.getResourceAsStream("icons/baseline_repeat_white.png"))));
                }
            }
        });

        SeparatorMenuItem separator = new SeparatorMenuItem();
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                isPlaying = false;
                songBarVisibility(false);
                list.getSelectionModel().clearSelection();
            }
        });

        menu.getItems().addAll(rate, randomPlayer, separator, exit);
    }

    public void createMediaPlayer(Media media) {
        songPosition = list.getSelectionModel().getSelectedIndex();
        if (!songBar.isVisible()) songBarVisibility(true);
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnReady(() -> {
            totalTimeProperty.set(calculateTime(mediaPlayer.getMedia().getDuration()));
            progress.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
            play.setImage(pauseImage);
            mediaPlayer.setRate(rateSlider.getValue() * 0.01);
            mediaPlayer.play();
            isPlaying = true;
            logger.info("play");
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            if (!repeatMode) {
                if (!isRandomPlayer && !isChanging) {
                    if (isMiniPlayerOpen) {
                        mediaPlayer.seek(Duration.ZERO);
                        pauseMedia();
                        currentTimeProperty.set(calculateTime(Duration.ZERO));
                        play.setImage(playImage);
                        progress.setValue(0);
                    } else {
                        isPlaying = false;
                        play.setImage(playImage);
                        songBarVisibility(false);
                        list.getSelectionModel().clearSelection();
                    }
                } else {
                    isPlaying = false;
                    int randomPosition = pickRandom(list.getItems().size() - 1);
                    list.getSelectionModel().select(randomPosition);
                    if (isMiniPlayerOpen) {
                        miniPlayer.setMediaTitle(list.getItems().get(randomPosition).getTitle());
                    }
                    createMediaPlayer(list.getItems().get(randomPosition).getMedia());
                }
            } else {
                if (!isChanging && !isRandomPlayer) mediaPlayer.seek(Duration.ZERO);
            }
        });

        mediaPlayer.currentTimeProperty().addListener((observableValue, duration, t1) -> {
            if (!isChanging) {
                currentTimeProperty.set(calculateTime(t1));
                progress.setValue(t1.toSeconds());
            }
        });

    }

    private int pickRandom(int max) {
        return (int)(Math.floor(Math.random() * max + 1));
    }

    public class MiniPlayer extends Stage {

        private static final double height = 200, width = 400;
        private final BorderPane root = new BorderPane();
        private final Label mediaName = new Label();
        private final ImageView play2 = new ImageView();
        private double xOffset, yOffset;

        public MiniPlayer() {
            initStyle(StageStyle.UNDECORATED);
            setTitle("Mini Player");
            setMinHeight(height);
            setMinWidth(width);

            mediaName.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(mediaName, Priority.ALWAYS);
            mediaName.setStyle("-fx-text-fill : white; -fx-font-weight : bold; -fx-font-size : 16px;");

            Label totalTime2 = new Label();
            totalTime2.textProperty().bind(totalTimeProperty);
            totalTime2.setStyle("-fx-text-fill : white;");

            Label currentTime2 = new Label();
            currentTime2.textProperty().bind(currentTimeProperty);
            currentTime2.setStyle("-fx-text-fill : white;");

            play2.setImage(isPlaying ? pauseImage : playImage);
            play2.setPickOnBounds(true);
            play2.setFitWidth(35);
            play2.setFitHeight(35);
            play2.setOnMouseClicked(e -> {
                if (mediaPlayer != null && e.getButton() == MouseButton.PRIMARY) {
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

            ImageView forwardButton = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/baseline_skip_next_white.png"))));
            forwardButton.setPickOnBounds(true);
            forwardButton.setFitHeight(35);
            forwardButton.setFitWidth(35);
            forwardButton.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    forwardMedia();
                    if (play2.getImage().equals(playImage)) play2.setImage(pauseImage);
                }
            });

            ImageView rewindButton = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/baseline_skip_previous_white.png"))));
            rewindButton.setPickOnBounds(true);
            rewindButton.setFitHeight(35);
            rewindButton.setFitWidth(35);
            rewindButton.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    rewindMedia();
                    if (play2.getImage().equals(playImage)) play2.setImage(pauseImage);
                }
            });

            ImageView close = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/baseline_close_white.png"))));
            close.setPickOnBounds(true);
            close.setFitHeight(24);
            close.setFitWidth(24);
            close.setOnMouseClicked(e -> {
                getMainStage().show();
                isMiniPlayerOpen = false;
                close();
                logger.info("close mini player");
            });

            HBox controller = new HBox(3);
            controller.setPadding(new Insets(10, 10, 10, 10));
            controller.setAlignment(Pos.CENTER);
            controller.getChildren().addAll(currentTime2, rewindButton, play2, forwardButton, totalTime2);

            root.getStyleClass().add("animated-gradient");
            root.setPadding(new Insets(6, 6, 6, 6));

            HBox top = new HBox(5);
            top.setOnMousePressed(e -> {
                xOffset = getX() - e.getScreenX();
                yOffset = getY() - e.getScreenY();
            });

            top.setOnMouseDragged(e -> {
                setX(e.getScreenX() + xOffset);
                setY(e.getScreenY() + yOffset);
            });

            top.setAlignment(Pos.CENTER_LEFT);
            top.getChildren().addAll(mediaName, close);

            root.setTop(top);
            root.setCenter(controller);

            Scene scene = new Scene(root, width, height);

            String light = Objects.requireNonNull(getClass().getResource("Themes/light-theme.css")).toExternalForm();
            scene.getStylesheets().add(light);

            setScene(scene);
            getIcons().addAll(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/icon64x64.png"))),
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/icon32x32.png"))),
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/icon16x16.png")))
            );
            setOnCloseRequest(e -> {
                getMainStage().show();
                isMiniPlayerOpen = false;
                logger.info("close mini player");
            });
            iconifiedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (!t1) play2.setImage(isPlaying ? pauseImage : playImage);
            });
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

        public void setMediaTitle(String s) {
            mediaName.setText(s);
        }
    }

    public void createMiniPlayer() {
        miniPlayer = new MiniPlayer();
        miniPlayer.setMediaTitle(list.getItems().get(songPosition).getTitle());
        miniPlayer.show();
        isMiniPlayerOpen = true;
        logger.info("Mini Player Created");
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public void forwardMedia() {
        if (mediaPlayer != null) {
            int index = songPosition;
            int size = list.getItems().size();
            if (size >= 2) {
                mediaPlayer.stop();
                if (index == size - 1) {
                    index = 0;
                    list.getSelectionModel().selectFirst();
                    createMediaPlayer(list.getItems().get(index).getMedia());
                    if (isMiniPlayerOpen)
                        miniPlayer.setMediaTitle(list.getItems().get(index).getTitle());
                    logger.info("jump to first item");
                } else {
                    list.getSelectionModel().select(index + 1);
                    createMediaPlayer(list.getItems().get(index + 1).getMedia());
                    if (isMiniPlayerOpen)
                        miniPlayer.setMediaTitle(list.getItems().get(index + 1).getTitle());
                    logger.info("forward");
                }
            } else {
                mediaPlayer.seek(mediaPlayer.getMedia().getDuration());
                logger.info("forward seek to " + mediaPlayer.getMedia().getDuration().toSeconds());
            }
        }
    }

    public void rewindMedia() {
        if (mediaPlayer != null) {
            int index = songPosition;
            int size = list.getItems().size();
            if (size >= 2) {
                mediaPlayer.stop();
                if (index == 0) {
                    list.getSelectionModel().selectLast();
                    createMediaPlayer(list.getItems().get(size - 1).getMedia());
                    if (isMiniPlayerOpen)
                        miniPlayer.setMediaTitle(list.getItems().get(size - 1).getTitle());
                    logger.info("jump to last item");
                } else {
                    list.getSelectionModel().select(index - 1);
                    createMediaPlayer(list.getItems().get(index - 1).getMedia());
                    if (isMiniPlayerOpen)
                        miniPlayer.setMediaTitle(list.getItems().get(index - 1).getTitle());
                    logger.info("rewind");
                }
            } else {
                mediaPlayer.seek(Duration.ZERO);
                logger.info("rewind seek to zero");
            }
        }
    }

    public boolean isRefreshing(){
        return refreshing.get();
    }

    public BooleanProperty getRefreshingProperty() {
        return refreshing;
    }
}