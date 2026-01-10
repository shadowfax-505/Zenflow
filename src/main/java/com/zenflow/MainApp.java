package com.zenflow;

import com.zenflow.ui.Toast;
import com.zenflow.db.DBHelper;
import com.zenflow.service.ActiveWindowMonitor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static ActiveWindowMonitor windowMonitor;
    public static final String STYLE_SHEET = "/css/dark-theme.css";

    @Override
    public void start(Stage primaryStage) throws Exception {


        try {
            DBHelper.initDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
            return;
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 600, 320);
        scene.getStylesheets().add(
                getClass().getResource(STYLE_SHEET).toExternalForm()
        );

        primaryStage.setScene(scene);
        primaryStage.setTitle("ZenFlow - Login");
        primaryStage.show();
    }


    public static synchronized void startWindowMonitor(Stage primaryStage) {
        if (windowMonitor != null) return;
        windowMonitor = new ActiveWindowMonitor(keyword ->
                Platform.runLater(() -> Toast.show(primaryStage, "Distraction detected: " + keyword))
        );
        windowMonitor.start();
    }

    @Override
    public void stop() {
        if (windowMonitor != null) windowMonitor.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
