package com.my.dreammusic.dream_music;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String issuesLink = "https://github.com/AmirAli-AZ/Dream_Music/issues";

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        show("Error" , e);
    }

    public void show(String title , Throwable e) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color : white;");

        HBox header = new HBox();
        header.setPadding(new Insets(5));
        Text text1 = new Text("an error occurred, please report it on ");
        text1.setFont(Font.font(16));
        Hyperlink link = new Hyperlink("github repository");
        link.setBorder(Border.EMPTY);
        link.setPadding(new Insets(4, 0, 4, 0));
        link.setFont(Font.font(16));
        link.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(new URI(issuesLink));
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            }
        });
        TextFlow textFlow = new TextFlow(text1 , link);
        HBox.setHgrow(textFlow , Priority.ALWAYS);
        textFlow.setMaxWidth(Double.MAX_VALUE);
        ImageView icon = new ImageView(new Image(ExceptionHandler.class.getResourceAsStream("icons/ic_error64x64.png")));
        icon.setFitWidth(50);
        icon.setFitHeight(50);
        header.getChildren().addAll(textFlow , icon);

        dialogPane.setHeader(header);
        dialogPane.getButtonTypes().add(ButtonType.OK);

        StackPane content = new StackPane();
        content.setPadding(new Insets(5));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        TextArea errorDetails = new TextArea(sw.toString());
        errorDetails.setEditable(false);
        content.getChildren().add(errorDetails);

        dialogPane.setExpandableContent(content);
        Stage stage = (Stage)dialogPane.getScene().getWindow();
        stage.getIcons().addAll(
                new Image(ExceptionHandler.class.getResourceAsStream("icons/ic_error64x64.png")),
                new Image(ExceptionHandler.class.getResourceAsStream("icons/ic_error32x32.png")),
                new Image(ExceptionHandler.class.getResourceAsStream("icons/ic_error16x16.png"))
        );
        dialog.showAndWait();
    }
}
