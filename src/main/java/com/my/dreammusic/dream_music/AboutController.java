package com.my.dreammusic.dream_music;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.events.EventTarget;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable {

    @FXML
    private WebView webview;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        webview.setContextMenuEnabled(false);
        WebEngine engine = webview.getEngine();
        String page = AboutController.class.getResource("Docs/doc.html").toExternalForm();
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
                            Desktop.getDesktop().browse(new URI(href));
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                        evt.preventDefault();
                    } , false);
                }
            }
        });
        engine.load(page);
    }
}