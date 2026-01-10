package com.zenflow.controller;

import com.zenflow.MainApp;
import com.zenflow.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService = UserService.getInstance();

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        if (userService.authenticate(user, pass)) {
            try {
                Parent main = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/main_layout.fxml")));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(main, 900, 600);
                scene.getStylesheets().add(Objects.requireNonNull(MainApp.class.getResource(MainApp.STYLE_SHEET)).toExternalForm());
                stage.setTitle("ZenFlow - " + user);
                stage.setScene(scene);


                MainApp.startWindowMonitor(stage);
            } catch (IOException e) {
                messageLabel.setText("Failed to load main layout.");
            }
        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    private void openAdmin(ActionEvent event) {

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Admin authentication");
        dialog.setHeaderText("Please enter admin username and password to continue.");

        ButtonType loginButtonType = new ButtonType("Authenticate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        TextField userField = new TextField();
        userField.setPromptText("admin username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Username:"), 0, 0);
        grid.add(userField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(userField.getText(), passField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pair -> {
            String u = pair.getKey();
            String p = pair.getValue();
            if (u != null && p != null && userService.authenticate(u, p) && userService.isAdmin(u)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin.fxml"));
                    Parent adminRoot = loader.load();
                    Stage adminStage = new Stage();
                    adminStage.initOwner(((Node) event.getSource()).getScene().getWindow());
                    adminStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                    adminStage.setTitle("ZenFlow - Admin");
                    adminStage.setScene(new Scene(adminRoot));
                    adminStage.showAndWait();
                } catch (IOException e) {
                    messageLabel.setText("Failed to open admin window.");
                }
            } else {
                messageLabel.setText("Invalid admin credentials.");
            }
        });
    }
}
