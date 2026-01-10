package com.zenflow.controller;

import com.zenflow.service.AnalyticsService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private Label totalTimeLabel;
    @FXML private Label sessionCountLabel;
    @FXML private Label streakLabel;

    @FXML private BarChart<String, Number> hourlyChart;
    @FXML private PieChart appPieChart;

    private final AnalyticsService analyticsService = new AnalyticsService();
    private Timeline refreshTimeline;

    @FXML
    public void initialize() {
        refreshDashboard();
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> refreshDashboard()));
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
    }

    private String formatMinutes(long minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        return String.format("%dh %02dm", hours, mins);
    }
}
