package com.my.dreammusic.dream_music;

import com.jthemedetecor.OsThemeDetector;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class Dialog {

    public static final int OK = 1;
    public static final int CANCEL = 0;
    private Listener listener;
    private boolean cancelButton = false;
    private String title , message , btnCancelText = "Cancel" , btnOkText = "OK";
    private Image image;

    public Dialog(Listener listener){
        this.listener = listener;
    }

    public void setCancelButton(boolean cancelButton) {
        this.cancelButton = cancelButton;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setBtnCancelText(String btnCancelText) {
        this.btnCancelText = btnCancelText;
    }

    public void setBtnOkText(String btnOkText) {
        this.btnOkText = btnOkText;
    }

    public void show(){
        Stage window = new Stage();
        window.setTitle(title);
        window.initModality(Modality.APPLICATION_MODAL);

        window.setOnCloseRequest(e ->{
            listener.onResult(CANCEL);
        });

        BorderPane borderPane = new BorderPane();
        VBox vBox = new VBox(15);
        vBox.setAlignment(Pos.CENTER);
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(5 , 5, 5, 5));

        if (cancelButton){
            Button cancel = new Button(btnCancelText);
            cancel.getStyleClass().add("button-style-cancel");
            cancel.setMinWidth(60);
            cancel.setMinHeight(30);
            cancel.setOnAction(e ->{
                window.close();
                listener.onResult(CANCEL);
            });
            buttons.getChildren().add(cancel);
        }
        Button ok = new Button(btnOkText);
        ok.getStyleClass().add("button-style-ok");
        ok.setMinWidth(60);
        ok.setMinHeight(30);
        ok.setOnAction(e ->{
            window.close();
            listener.onResult(OK);
        });
        buttons.getChildren().add(ok);

        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);
        Label text = new Label(message);
        text.getStyleClass().add("message");
        vBox.getChildren().addAll(imageView , text);

        borderPane.setBottom(buttons);
        borderPane.setCenter(vBox);

        Scene scene = new Scene(borderPane , 400 , 250);

        String light = Dialog.class.getResource("Themes/dialog-light-theme.css").toExternalForm();
        String dark = Dialog.class.getResource("Themes/dialog-dark-theme.css").toExternalForm();
        // default theme
        scene.getStylesheets().add(light);

        final OsThemeDetector detector = OsThemeDetector.getDetector();
        Consumer<Boolean> darkThemeListener = isDark -> {
            Platform.runLater(() -> {
                if (isDark) {
                    scene.getStylesheets().set(0 , dark);
                } else {
                    scene.getStylesheets().set(0 , light);
                }
            });
        };
        darkThemeListener.accept(detector.isDark());
        detector.registerListener(darkThemeListener);

        window.setScene(scene);
        window.setMinWidth(400);
        window.setMinHeight(250);
        window.setResizable(false);
        window.showAndWait();
    }
}
