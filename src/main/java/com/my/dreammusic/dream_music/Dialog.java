package com.my.dreammusic.dream_music;

import com.jthemedetecor.OsThemeDetector;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class Dialog {

    private boolean cancelButton = false;
    private String message = "" , btnCancelText = "Cancel" , btnOkText = "OK";
    private Image image;
    private Listener listener;
    private final Window owner;
    private final Stage window = new Stage();
    private boolean autoClose = true;

    public Dialog(Listener listener , Window owner){
        this.listener = listener;
        this.owner = owner;
    }

    public Dialog(Window owner){
        this.owner = owner;
    }

    public void setCancelButton(boolean cancelButton) {
        this.cancelButton = cancelButton;
    }

    public void setTitle(String title) {
        window.setTitle(title);
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

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }

    public void close(){
        window.close();
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public void show(){
        Scene scene = createLayout();
        // set themes
        if (OsThemeDetector.isSupported()) {
            String light = Dialog.class.getResource("Themes/dialog-light-theme.css").toExternalForm();
            String dark = Dialog.class.getResource("Themes/dialog-dark-theme.css").toExternalForm();
            // default theme
            scene.getStylesheets().add(light);

            final OsThemeDetector detector = OsThemeDetector.getDetector();
            Consumer<Boolean> darkThemeListener = isDark -> Platform.runLater(() -> {
                if (isDark) {
                    scene.getStylesheets().set(0, dark);
                } else {
                    scene.getStylesheets().set(0, light);
                }
            });
            darkThemeListener.accept(detector.isDark());
            detector.registerListener(darkThemeListener);
        }

        if (owner != null) window.initOwner(owner);
        window.initModality(Modality.APPLICATION_MODAL);

        window.setOnCloseRequest(e -> {
            if (!autoClose) e.consume();
            if (listener != null) listener.onResult(Listener.CANCEL);
        });

        window.setScene(scene);
        window.setMinWidth(400);
        window.setMinHeight(250);
        window.setResizable(false);
        window.getIcons().addAll(
                new Image(Dialog.class.getResourceAsStream("icons/icon64x64.png")),
                new Image(Dialog.class.getResourceAsStream("icons/icon32x32.png")),
                new Image(Dialog.class.getResourceAsStream("icons/icon16x16.png"))
        );
        window.show();
    }

    private Scene createLayout() {
        BorderPane borderPane = new BorderPane();
        VBox vBox = new VBox(15);
        vBox.setAlignment(Pos.CENTER);
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(5));

        if (cancelButton){
            Button cancel = new Button(btnCancelText);
            cancel.setCursor(Cursor.HAND);
            cancel.getStyleClass().add("button-style-cancel");
            cancel.setMinWidth(60);
            cancel.setMinHeight(30);
            cancel.setOnAction(e ->{
                if (autoClose) window.close();
                if (listener != null) listener.onResult(Listener.CANCEL);
            });
            buttons.getChildren().add(cancel);
        }
        Button ok = new Button(btnOkText);
        ok.setCursor(Cursor.HAND);
        ok.getStyleClass().add("button-style-ok");
        ok.setMinWidth(60);
        ok.setMinHeight(30);
        ok.setOnAction(e ->{
            if (autoClose) window.close();
            if (listener != null) listener.onResult(Listener.OK);
        });
        buttons.getChildren().add(ok);

        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);
        Label text = new Label(message);
        text.setWrapText(true);
        text.getStyleClass().add("message");
        vBox.getChildren().addAll(imageView , text);

        borderPane.setBottom(buttons);
        borderPane.setCenter(vBox);

        return new Scene(borderPane , 400 , 250);
    }
}
