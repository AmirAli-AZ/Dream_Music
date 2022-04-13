package com.my.dreammusic.dream_music;

import com.my.dreammusic.dream_music.utils.OSUtils;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.events.EventTarget;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AboutController implements Initializable {

    @FXML
    private AnchorPane root;

    @FXML
    private WebView webview;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        webview.setContextMenuEnabled(false);
        WebEngine engine = webview.getEngine();
        String page = Objects.requireNonNull(AboutController.class.getResource("Docs/doc.html")).toExternalForm();
        engine.getLoadWorker().stateProperty().addListener((observableValue, state, t1) -> {
            if (t1 == Worker.State.SUCCEEDED){
                NodeList nodeList = engine.getDocument().getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++){
                    Node node= nodeList.item(i);
                    EventTarget eventTarget = (EventTarget) node;
                    eventTarget.addEventListener("click", evt -> {
                        EventTarget target = evt.getCurrentTarget();
                        HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                        String href = anchorElement.getHref();
                        try {
                            OSUtils.browse(new URI(href));
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                        evt.preventDefault();
                    } , false);
                }
            }
        });
        createContextMenu(webview);
        engine.load(page);
    }

    private void createContextMenu(WebView webview){
        ContextMenu menu = new ContextMenu();

        Label l1 = new Label("Copy");
        l1.setWrapText(true);
        l1.setMinWidth(80);
        MenuItem item1 = new MenuItem();
        item1.setGraphic(l1);
        item1.setOnAction(e ->{
            String selection = (String) webview.getEngine()
                    .executeScript("window.getSelection().toString()");
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selection);
            clipboard.setContent(content);
        });

        menu.getItems().add(item1);

        webview.setOnMouseClicked(e ->{
            String selection = (String) webview.getEngine()
                    .executeScript("window.getSelection().toString()");
            if (e.getButton() == MouseButton.SECONDARY){
                if (selection.length() > 0) menu.show(webview , e.getScreenX() , e.getScreenY());
            }else {
                menu.hide();
            }
        });
    }
}
