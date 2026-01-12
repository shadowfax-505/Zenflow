package com.zenflow.controller;

import com.zenflow.service.AppConfigService;
import com.zenflow.service.AppEventBus;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML private TextField focusField;
    @FXML private Label focusStatus;

    @FXML private TextField overuseSocialField;
    @FXML private TextField overuseEntertainmentField;
    @FXML private TextField overuseProductivityField;
    @FXML private TextField overuseOtherField;
    @FXML private Label overuseStatus;

    private final AppConfigService config = new AppConfigService();

    @FXML
    public void initialize() {
        loadFocusDuration();
        loadOveruseThresholds();
    }

    @FXML
    public void onSaveFocusMinutes() {
        String txt = focusField.getText() == null ? "" : focusField.getText().trim();
        try {
            int minutes = Integer.parseInt(txt);
            if (minutes <= 0) throw new NumberFormatException();

            String current = config.get("focus_minutes");
            if (current != null && current.trim().equals(String.valueOf(minutes))) {
                focusStatus.setText("No change (already " + minutes + " min)");
                return;
            }

            boolean ok = config.set("focus_minutes", String.valueOf(minutes));
            focusStatus.setText(ok ? "Focus duration saved: " + minutes + " min" : "Failed to save focus duration");
            if (ok) {
                AppEventBus.publishConfigChanged("focus_minutes", String.valueOf(minutes));
            }
        } catch (NumberFormatException ex) {
            focusStatus.setText("Enter a positive integer");
        }
    }

    @FXML
    public void onReloadFocusMinutes() {
        loadFocusDuration();
        focusStatus.setText("Reloaded focus duration");
    }

    private void loadFocusDuration() {
        String value = config.get("focus_minutes");
        if (value != null && !value.isBlank()) {
            focusField.setText(value);
            focusStatus.setText("Focus duration: " + value + " min");
        } else {
            if (focusField.getText() == null || focusField.getText().isBlank()) {
                focusField.setText("25");
            }
            focusStatus.setText("Focus duration: " + focusField.getText() + " min");
        }
    }

    @FXML
    public void onSaveOveruseThresholds() {
        try {
            int social = parsePositiveIntOrDefault(overuseSocialField.getText(), 60);
            int ent = parsePositiveIntOrDefault(overuseEntertainmentField.getText(), 60);
            int prod = parsePositiveIntOrDefault(overuseProductivityField.getText(), 180);
            int other = parsePositiveIntOrDefault(overuseOtherField.getText(), 120);

            boolean ok = true;
            ok &= config.set("overuse_social_minutes", String.valueOf(social));
            ok &= config.set("overuse_entertainment_minutes", String.valueOf(ent));
            ok &= config.set("overuse_productivity_minutes", String.valueOf(prod));
            ok &= config.set("overuse_other_minutes", String.valueOf(other));

            if (ok) {
                overuseStatus.setText("Overuse thresholds saved.");
                AppEventBus.publishConfigChanged("overuse_social_minutes", String.valueOf(social));
                AppEventBus.publishConfigChanged("overuse_entertainment_minutes", String.valueOf(ent));
                AppEventBus.publishConfigChanged("overuse_productivity_minutes", String.valueOf(prod));
                AppEventBus.publishConfigChanged("overuse_other_minutes", String.valueOf(other));
            } else {
                overuseStatus.setText("Failed to save thresholds.");
            }
        } catch (Exception ex) {
            overuseStatus.setText("Enter positive integers.");
        }
    }

    @FXML
    public void onReloadOveruseThresholds() {
        loadOveruseThresholds();
        overuseStatus.setText("Reloaded thresholds");
    }

    private void loadOveruseThresholds() {
        overuseSocialField.setText(String.valueOf(parsePositiveIntOrDefault(config.get("overuse_social_minutes"), 60)));
        overuseEntertainmentField.setText(String.valueOf(parsePositiveIntOrDefault(config.get("overuse_entertainment_minutes"), 60)));
        overuseProductivityField.setText(String.valueOf(parsePositiveIntOrDefault(config.get("overuse_productivity_minutes"), 180)));
        overuseOtherField.setText(String.valueOf(parsePositiveIntOrDefault(config.get("overuse_other_minutes"), 120)));
    }

    private int parsePositiveIntOrDefault(String txt, int def) {
        if (txt == null) return def;
        String t = txt.trim();
        if (t.isEmpty()) return def;
        int v = Integer.parseInt(t);
        if (v <= 0) return def;
        return v;
    }
}
