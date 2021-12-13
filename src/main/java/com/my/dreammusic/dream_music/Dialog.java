package com.my.dreammusic.dream_music;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Dialog {

    public interface Listener{
        void onResult(int result);
    }

    public static final int OK = 1;
    public static final int CANCEL = 0;
    private Listener listener;
    private boolean cancelButton = false;
    private String title , message;
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

    public void show(){
        Stage window = new Stage();
        window.setTitle(title);
        window.initModality(Modality.APPLICATION_MODAL);

        window.setOnCloseRequest(e ->{
            listener.onResult(OK);
        });

        BorderPane borderPane = new BorderPane();
        VBox vBox = new VBox(15);
        vBox.setAlignment(Pos.CENTER);
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(5 , 5, 5, 5));

        if (cancelButton){
            Button cancel = new Button("Cancel");
            cancel.getStyleClass().add("button-style-cancel");
            cancel.setMinWidth(60);
            cancel.setMinHeight(30);
            cancel.setOnAction(e ->{
                listener.onResult(CANCEL);
                window.close();
            });
            buttons.getChildren().add(cancel);
        }
        Button ok = new Button("OK");
        ok.getStyleClass().add("button-style-ok");
        ok.setMinWidth(60);
        ok.setMinHeight(30);
        ok.setOnAction(e ->{
            listener.onResult(OK);
            window.close();
        });
        buttons.getChildren().add(ok);

        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);
        Text text = new Text(message);
        text.setStyle("-fx-font-size: 16px;");
        vBox.getChildren().addAll(imageView , text);

        borderPane.setBottom(buttons);
        borderPane.setCenter(vBox);

        Scene scene = new Scene(borderPane , 400 , 250);
        scene.getStylesheets().add(Dialog.class.getResource("Themes/dialog-theme.css").toExternalForm());
        window.setScene(scene);
        window.setMinWidth(400);
        window.setMinHeight(250);
        window.setResizable(false);
        window.showAndWait();
    }
}
