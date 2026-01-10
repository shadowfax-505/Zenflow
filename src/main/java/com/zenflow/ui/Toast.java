package com.zenflow.ui;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toast {

    public static void show(Stage ownerStage, String message) {
        Platform.runLater(() -> {
            Label label = new Label(message);
            label.setStyle("""
                -fx-background-color: rgba(30,30,30,0.9);
                -fx-text-fill: white;
                -fx-padding: 10 16 10 16;
                -fx-background-radius: 8;
                -fx-font-size: 13px;
            """);

            StackPane root = new StackPane(label);
            root.setAlignment(Pos.CENTER);

            Popup popup = new Popup();
            popup.getContent().add(root);
            popup.setAutoFix(true);
            popup.setAutoHide(true);

            popup.show(ownerStage);

            FadeTransition fade = new FadeTransition(Duration.seconds(3), root);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> popup.hide());
            fade.play();
        });
    }
}
