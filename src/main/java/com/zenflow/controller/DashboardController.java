package com.zenflow.controller;

import com.zenflow.service.AnalyticsService;
import com.zenflow.service.OveruseCategory;
import com.zenflow.service.OveruseReport;
import com.zenflow.service.OveruseService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private Label totalTimeLabel;
    @FXML private Label sessionCountLabel;
    @FXML private Label streakLabel;

    @FXML private BarChart<String, Number> hourlyChart;
    @FXML private PieChart appPieChart;

    @FXML private ListView<String> overuseList;
    @FXML private Label overuseHint;

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final OveruseService overuseService = new OveruseService();
    private Timeline refreshTimeline;

    // Fixed palette used for deterministic pie slice colors.
    private static final String[] PIE_PALETTE = new String[] {
            "#6ab0ff", "#a78bfa", "#34d399", "#fbbf24", "#f87171", "#60a5fa",
            "#fb7185", "#22c55e", "#f97316", "#38bdf8", "#c084fc", "#eab308"
    };

    @FXML
    public void initialize() {
        // Make sure colors stay stable even as data nodes are created asynchronously by JavaFX.
        if (appPieChart != null) {
            appPieChart.getData().addListener((ListChangeListener<PieChart.Data>) c ->
                    Platform.runLater(this::applyStablePieColors));
        }

        refreshDashboard();
        refreshTimeline = new Timeline(new KeyFrame(Duration.minutes(1), e -> refreshDashboard()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    public void refreshDashboard() {
        long focusMinutes = analyticsService.getTotalFocusMinutesToday();
        totalTimeLabel.setText(formatMinutes(focusMinutes));

        int sessions = analyticsService.getCompletedSessionsToday();
        sessionCountLabel.setText(sessions + " Sessions");

        int streak = analyticsService.getLongestStreak();
        streakLabel.setText(streak + " Days");

        hourlyChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Minutes");
        for (Map.Entry<String, Integer> e : analyticsService.getUsageMinutesByHourToday().entrySet()) {
            if (e.getValue() > 0) {
                series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
            }
        }
        if (!series.getData().isEmpty()) {
            hourlyChart.getData().add(series);
        }

        appPieChart.getData().clear();
        List<Map.Entry<String, Integer>> topApps = analyticsService.getTopAppsToday(6);
        for (Map.Entry<String, Integer> entry : topApps) {
            int minutes = entry.getValue();
            if (minutes <= 0) continue;
            appPieChart.getData().add(new PieChart.Data(entry.getKey(), minutes));
        }
        if (appPieChart.getData().isEmpty()) {
            appPieChart.getData().add(new PieChart.Data("No data", 1));
        }

        // Ensure colors are consistent after refresh.
        Platform.runLater(this::applyStablePieColors);

        refreshOveruse();
    }

    private void applyStablePieColors() {
        if (appPieChart == null) return;
        for (PieChart.Data d : appPieChart.getData()) {
            if (d == null) continue;
            String name = d.getName() == null ? "" : d.getName();
            int idx = Math.floorMod(name.hashCode(), PIE_PALETTE.length);
            String color = PIE_PALETTE[idx];

            if (d.getNode() != null) {
                d.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
        }
    }

    private void refreshOveruse() {
        if (overuseList == null) return;

        OveruseReport report = overuseService.getOveruseReportToday();
        overuseList.getItems().clear();

        if (report == null || report.isEmpty()) {
            overuseList.getItems().add("No overuse detected (within your limits).");
            if (overuseHint != null) overuseHint.setText("Limits are configured in Settings.");
            return;
        }

        for (Map.Entry<String, Integer> e : report.getOverusedAppsMinutes().entrySet()) {
            OveruseCategory cat = report.getCategory(e.getKey());
            overuseList.getItems().add(e.getKey() + " — " + e.getValue() + " min (" + cat + ")");
        }
        if (overuseHint != null) overuseHint.setText("Overuse is based on your Settings thresholds.");
    }

    private String formatMinutes(long minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        return String.format("%dh %02dm", hours, mins);
    }
}
