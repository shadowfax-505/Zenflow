package com.zenflow.controller;

import com.zenflow.dao.SessionDAO;
import com.zenflow.model.Session;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryController {

    @FXML private TableView<Session> table;
    @FXML private TableColumn<Session, String> colDate;
    @FXML private TableColumn<Session, String> colStart;
    @FXML private TableColumn<Session, String> colEnd;
    @FXML private TableColumn<Session, Integer> colCompleted;

    private final SessionDAO dao = new SessionDAO();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(c -> new SimpleStringProperty(formatDate(c.getValue().getStartTs())));
        colStart.setCellValueFactory(c -> new SimpleStringProperty(formatTime(c.getValue().getStartTs())));
        colEnd.setCellValueFactory(c -> new SimpleStringProperty(formatTime(c.getValue().getEndTs())));
        colCompleted.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCompleted()).asObject());

        refreshTable();
    }

    private void refreshTable() {
        long now = System.currentTimeMillis();
        List<Session> list = dao.getSessionsByDateRange(0, now);
        table.setItems(FXCollections.observableList(list));
    }

    private String formatDate(long epochMs) {
        LocalDate d = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDate();
        return DATE_FMT.format(d);
    }

    private String formatTime(long epochMs) {
        LocalDateTime dt = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDateTime();
        return TIME_FMT.format(dt);
    }
}
