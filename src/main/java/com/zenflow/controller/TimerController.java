package com.zenflow.controller;

import com.zenflow.dao.SessionDAO;
import com.zenflow.db.DBHelper;
import com.zenflow.model.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.concurrent.*;

public class TimerController {

    @FXML private ProgressBar progressBar;
    @FXML private Label timeLabel;
    @FXML private Button startBtn;
    @FXML private Button pauseBtn;
    @FXML private Button stopBtn;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> tickHandle;

    private long durationSeconds = 25 * 60;
    private long remainingSeconds = durationSeconds;
    private volatile boolean running = false;


    private long sessionStartTs = -1;
    private Integer currentSessionId = null;
    private final SessionDAO sessionDAO = new SessionDAO();

    @FXML
    public void initialize() {
        loadConfiguredDuration();
        updateUI();
    }

    private void loadConfiguredDuration() {
        try (Connection c = DBHelper.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT value FROM app_config WHERE key='focus_minutes'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int minutes = Integer.parseInt(rs.getString("value"));
                if (minutes > 0) {
                    durationSeconds = minutes * 60L;
                    remainingSeconds = durationSeconds;
                }
            }
        } catch (Exception ignored) { }
    }

    @FXML
    public void onStart() {
        if (running) return;
        running = true;
        if (sessionStartTs < 0) {
            sessionStartTs = Instant.now().toEpochMilli();

            Session s = new Session();
            s.setStartTs(sessionStartTs);
            s.setType("FOCUS");
            s.setCompleted(0);
            currentSessionId = sessionDAO.addSession(s).intValue();
        }
        tickHandle = scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
        updateUI();
    }

    @FXML
    public void onPause() {
        if (!running) return;
        running = false;
        if (tickHandle != null) tickHandle.cancel(false);
        updateUI();
    }

    @FXML
    public void onStop() {
        if (tickHandle != null) tickHandle.cancel(false);
        running = false;

        long endTs = Instant.now().toEpochMilli();
        if (currentSessionId != null) {
            long elapsed = durationSeconds - remainingSeconds;
            boolean completed = remainingSeconds == 0;
            sessionDAO.endSession(currentSessionId, endTs, completed ? 1 : 0);
        }

        remainingSeconds = durationSeconds;
        sessionStartTs = -1;
        currentSessionId = null;
        updateUI();
    }

    private void tick() {
        if (!running) return;
        remainingSeconds = Math.max(0, remainingSeconds - 1);
        if (remainingSeconds <= 0) {

            running = false;
            if (tickHandle != null) tickHandle.cancel(false);
            Platform.runLater(this::onStop);
        } else {
            updateUILater();
        }
    }

    private void updateUILater() {
        Platform.runLater(this::updateUI);
    }

    private void updateUI() {
        double progress = 1.0 - ((double) remainingSeconds / (double) durationSeconds);
        progressBar.setProgress(progress);
        timeLabel.setText(formatSeconds(remainingSeconds));
        startBtn.setDisable(running);
        pauseBtn.setDisable(!running);
    }

    private String formatSeconds(long s) {
        long mm = s / 60;
        long ss = s % 60;
        return String.format("%02d:%02d", mm, ss);
    }
}
