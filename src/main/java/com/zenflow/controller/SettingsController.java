package com.zenflow.controller;

import com.zenflow.db.DBHelper;
import com.zenflow.platform.MacActiveWindowProvider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

public class SettingsController {

    @FXML private TextField focusField;
    @FXML private Label focusStatus;
    @FXML private ComboBox<String> whitelistCombo;
    @FXML private Label whitelistStatus;

    private final ObservableList<String> whitelistItems = FXCollections.observableArrayList();
    private final MacActiveWindowProvider windowProvider = new MacActiveWindowProvider();

    @FXML
    public void initialize() {
        loadWhitelist();
        loadRecentAppsForWhitelist();
        loadFocusDuration();
    }

    @FXML
    public void onSaveWhitelist() {
        String value = whitelistCombo.getEditor().getText() == null ? "" : whitelistCombo.getEditor().getText().trim();
        if (saveConfig("whitelist_app", value)) {
            whitelistStatus.setText("Saved whitelist: " + (value.isEmpty() ? "(none)" : value));
            if (!value.isEmpty() && !whitelistItems.contains(value)) {
                whitelistItems.add(0, value);
                whitelistCombo.setItems(whitelistItems);
            }
        } else {
            whitelistStatus.setText("Failed to save whitelist");
        }
    }

    @FXML
    public void onRefreshWhitelistList() {
        loadRecentAppsForWhitelist();
        whitelistStatus.setText("Refreshed recent apps");
    }

    @FXML
    public void onSaveFocusMinutes() {
        String txt = focusField.getText() == null ? "" : focusField.getText().trim();
        try {
            int minutes = Integer.parseInt(txt);
            if (minutes <= 0) throw new NumberFormatException();
            boolean ok = saveConfig("focus_minutes", String.valueOf(minutes));
            focusStatus.setText(ok ? "Focus duration saved: " + minutes + " min" : "Failed to save focus duration");
        } catch (NumberFormatException ex) {
            focusStatus.setText("Enter a positive integer");
        }
    }

    @FXML
    public void onReloadFocusMinutes() {
        loadFocusDuration();
        focusStatus.setText("Reloaded focus duration");
    }

    private void loadWhitelist() {
        String value = loadConfig("whitelist_app");
        if (value != null) {
            whitelistCombo.getEditor().setText(value);
            whitelistStatus.setText(value.isEmpty() ? "No whitelist set" : "Saved whitelist: " + value);
        }
    }

    private void loadRecentAppsForWhitelist() {
        whitelistItems.clear();
        Set<String> seen = new LinkedHashSet<>();

        String current = loadConfig("whitelist_app");
        if (current != null && !current.isBlank()) {
            seen.add(current);
        }


        for (int i = 0; i < 3; i++) {
            windowProvider.getActiveWindowInfo().ifPresent(info -> {
                String proc = info.split("\\|", 2)[0];
                if (!proc.isBlank()) seen.add(proc);
            });
        }


        String sql = "SELECT DISTINCT process_name FROM window_usage WHERE process_name IS NOT NULL ORDER BY id DESC LIMIT 15";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String proc = rs.getString("process_name");
                if (proc != null && !proc.isBlank()) seen.add(proc);
            }
        } catch (SQLException ignored) { }

        whitelistItems.addAll(seen);
        whitelistCombo.setItems(whitelistItems);
    }

    private void loadFocusDuration() {
        String value = loadConfig("focus_minutes");
        if (value != null) {
            focusField.setText(value);
            focusStatus.setText("Focus duration: " + value + " min");
        }
    }

    private boolean saveConfig(String key, String value) {
        String sql = "INSERT INTO app_config(key, value) VALUES(?, ?) " +
                "ON CONFLICT(key) DO UPDATE SET value=excluded.value";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private String loadConfig(String key) {
        String sql = "SELECT value FROM app_config WHERE key = ?";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("value");
            }
        } catch (SQLException e) {

        }
        return null;
    }

    @FXML
    public void onRemoveWhitelist() {
        String current = loadConfig("whitelist_app");
        boolean ok = saveConfig("whitelist_app", "");
        if (ok) {
            whitelistCombo.getEditor().clear();
            whitelistItems.remove(current);
            whitelistStatus.setText("Whitelist removed");
        } else {
            whitelistStatus.setText("Failed to remove whitelist");
        }
    }
}
