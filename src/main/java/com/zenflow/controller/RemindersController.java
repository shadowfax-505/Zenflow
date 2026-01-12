package com.zenflow.controller;

import com.zenflow.dao.ReminderDAO;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RemindersController {

    @FXML private DatePicker datePicker;
    @FXML private ListView<String> remindersList;
    @FXML private TextField reminderText;
    @FXML private Label statusLabel;

    private final ReminderDAO dao = new ReminderDAO();
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
            datePicker.valueProperty().addListener((obs, oldV, newV) -> loadForSelectedDate());
        }
        loadForSelectedDate();
    }

    @FXML
    public void onAdd() {
        String day = getSelectedDay();
        String text = reminderText.getText() == null ? "" : reminderText.getText().trim();
        if (day == null) {
            statusLabel.setText("Select a date first.");
            return;
        }
        if (text.isEmpty()) {
            statusLabel.setText("Enter reminder text.");
            return;
        }

        boolean ok = dao.addReminder(day, text);
        if (ok) {
            reminderText.clear();
            loadForSelectedDate();
            statusLabel.setText("Reminder added.");
        } else {
            statusLabel.setText("Failed to add reminder.");
        }
    }

    @FXML
    public void onDeleteSelected() {
        String day = getSelectedDay();
        String selected = remindersList.getSelectionModel().getSelectedItem();
        if (day == null) {
            statusLabel.setText("Select a date first.");
            return;
        }
        if (selected == null) {
            statusLabel.setText("Select a reminder to delete.");
            return;
        }

        boolean ok = dao.deleteReminder(day, selected);
        if (ok) {
            loadForSelectedDate();
            statusLabel.setText("Deleted.");
        } else {
            statusLabel.setText("Failed to delete.");
        }
    }

    private void loadForSelectedDate() {
        String day = getSelectedDay();
        remindersList.getItems().clear();
        if (day == null) return;
        remindersList.getItems().setAll(dao.getRemindersForDay(day));
    }

    private String getSelectedDay() {
        LocalDate d = datePicker == null ? null : datePicker.getValue();
        if (d == null) return null;
        return DAY_FMT.format(d);
    }
}
