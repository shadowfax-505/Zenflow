package com.zenflow.controller;

import com.zenflow.service.UserService;
import com.zenflow.ui.ConfirmDialog;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminController {
    @FXML private ListView<String> usersList;
    @FXML private TextField newUserField;
    @FXML private PasswordField newPassField;
    @FXML private PasswordField changePassField;
    @FXML private CheckBox adminCheckbox;
    @FXML private Label statusLabel;

    private final UserService userService = UserService.getInstance();

    @FXML
    private void initialize() {
        refreshList();
        usersList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                changePassField.clear();
                adminCheckbox.setSelected(false);
                return;
            }
            adminCheckbox.setSelected(userService.isAdmin(newV));
            statusLabel.setText("");
        });
    }

    @FXML
    private void addUser() {
        String u = newUserField.getText();
        String p = newPassField.getText();
        if (userService.addUser(u, p)) {
            statusLabel.setText("User added: " + u);
            newUserField.clear();
            newPassField.clear();
            refreshList();
        } else {
            statusLabel.setText("Failed to add user (exists or invalid)." );
        }
    }

    @FXML
    private void changePassword() {
        String selected = usersList.getSelectionModel().getSelectedItem();
        String newPass = changePassField.getText();
        if (selected == null) {
            statusLabel.setText("Select a user first.");
            return;
        }
        if (userService.changePassword(selected, newPass)) {
            statusLabel.setText("Password changed for: " + selected);
            changePassField.clear();
        } else {
            statusLabel.setText("Failed to change password.");
        }
    }

    @FXML
    private void deleteUser() {
        String selected = usersList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a user to delete.");
            return;
        }
        if ("admin".equals(selected)) {
            statusLabel.setText("Cannot delete default admin user.");
            return;
        }

        boolean ok = ConfirmDialog.show("Delete user", "Are you sure you want to delete user '" + selected + "'? This cannot be undone.");
        if (!ok) {
            statusLabel.setText("Deletion cancelled.");
            return;
        }
        if (userService.removeUser(selected)) {
            statusLabel.setText("User deleted: " + selected);
            refreshList();
        } else {
            statusLabel.setText("Failed to delete user.");
        }
    }

    @FXML
    private void toggleAdmin() {
        String selected = usersList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a user first.");
            adminCheckbox.setSelected(false);
            return;
        }
        boolean makeAdmin = adminCheckbox.isSelected();
        if (userService.setAdmin(selected, makeAdmin)) {
            statusLabel.setText("Updated admin role for: " + selected);
            Platform.runLater(this::refreshList);
        } else {
            statusLabel.setText("Failed to update admin role.");
            adminCheckbox.setSelected(!makeAdmin);
        }
    }

    private void refreshList() {
        usersList.getItems().setAll(userService.listUsers());
    }
}
