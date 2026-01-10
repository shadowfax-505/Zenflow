package com.zenflow.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class MainLayoutController {

    @FXML private StackPane contentPane;

    private Node timerNode, dashboardNode, historyNode, settingsNode;
    private DashboardController dashboardController;

    @FXML
    public void initialize() {
        try {
            timerNode = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/timer.fxml")));

            FXMLLoader dashLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/fxml/dashboard.fxml")));
            dashboardNode = dashLoader.load();
            dashboardController = dashLoader.getController();

            historyNode = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/history.fxml")));
            settingsNode = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/settings.fxml")));

            showNode(timerNode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showNode(Node n) {
        if (n == null) return;
        contentPane.getChildren().clear();
        contentPane.getChildren().add(n);
    }

    @FXML public void showTimer() { showNode(timerNode); }

    @FXML
    public void showDashboard() {
        showNode(dashboardNode);
        if (dashboardController != null) {
            dashboardController.refreshDashboard();
        }
    }

    @FXML public void showHistory() { showNode(historyNode); }
    @FXML public void showSettings() { showNode(settingsNode); }
}
