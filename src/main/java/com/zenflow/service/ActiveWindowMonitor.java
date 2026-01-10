package com.zenflow.service;

import com.zenflow.db.DBHelper;
import com.zenflow.platform.MacActiveWindowProvider;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ActiveWindowMonitor {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final MacActiveWindowProvider provider = new MacActiveWindowProvider();

    private volatile boolean running = false;
    private ScheduledFuture<?> handle;

    private String currentApp = null;
    private long currentStart = -1L;


    private final Consumer<String> alertCallback;

    public ActiveWindowMonitor(Consumer<String> alertCallback) {
        this.alertCallback = alertCallback;
    }

    public void start() {
        if (running) return;
        running = true;
        handle = scheduler.scheduleAtFixedRate(this::pollOnce, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        running = false;
        if (handle != null) handle.cancel(false);
        flushCurrent();
        scheduler.shutdown();
    }

    private void pollOnce() {
        try {
            Optional<String> infoOpt = provider.getActiveWindowInfo();
            String info = infoOpt.orElse(null);

            long now = Instant.now().toEpochMilli();

            if (info == null) {
                if (currentApp != null) { flushCurrentToDb(now); }
                currentApp = null; currentStart = -1L;
                return;
            }

            if (currentApp == null) {
                currentApp = info; currentStart = now;
                blockIfDisallowed(info);
            } else if (!currentApp.equals(info)) {
                flushCurrentToDb(now);
                currentApp = info; currentStart = now;
                blockIfDisallowed(info);
            } else {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void blockIfDisallowed(String info) {
    }

    private void flushCurrentToDb(long now) {
        try (Connection c = DBHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO window_usage(start_ts,end_ts,process_name,window_title) VALUES(?,?,?,?)")) {

            String[] parts = currentApp.split("\\|", 2);
            String process = parts.length > 0 ? parts[0] : null;
            String title = parts.length > 1 ? parts[1] : null;

            ps.setLong(1, currentStart);
            ps.setLong(2, now);
            ps.setString(3, process);
            ps.setString(4, title);
            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void flushCurrent() {
        if (currentApp == null) return;
        flushCurrentToDb(Instant.now().toEpochMilli());
        currentApp = null; currentStart = -1L;
    }

    private void checkAndAlert(String info) {
    }
}
