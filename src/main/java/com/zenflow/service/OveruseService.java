package com.zenflow.service;

import java.util.Locale;
import java.util.Map;

/**
 * Computes "overused apps" using today's app usage minutes vs configured per-category thresholds.
 *
 * Categorization is heuristic-based (process/app name contains keywords).
 */
public class OveruseService {

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final AppConfigService configService = new AppConfigService();

    public OveruseReport getOveruseReportToday() {
        int socialLimit = getLimit("overuse_social_minutes", 60);
        int entertainmentLimit = getLimit("overuse_entertainment_minutes", 60);
        int productivityLimit = getLimit("overuse_productivity_minutes", 180);
        int otherLimit = getLimit("overuse_other_minutes", 120);

        // Compute per-app minutes
        Map<String, Integer> perApp = analyticsService.getAllAppsMinutesToday();

        // Aggregate per category
        int socialTotal = 0;
        int entTotal = 0;
        int prodTotal = 0;
        int otherTotal = 0;

        for (Map.Entry<String, Integer> e : perApp.entrySet()) {
            String app = e.getKey() == null ? "(unknown)" : e.getKey();
            int minutes = e.getValue() == null ? 0 : e.getValue();
            if (minutes <= 0) continue;

            OveruseCategory cat = categorize(app);
            switch (cat) {
                case SOCIAL -> socialTotal += minutes;
                case ENTERTAINMENT -> entTotal += minutes;
                case PRODUCTIVITY -> prodTotal += minutes;
                case OTHER -> otherTotal += minutes;
            }
        }

        // Determine which categories are "over" and compute per-app overuse list.
        boolean socialOver = socialTotal > socialLimit;
        boolean entOver = entTotal > entertainmentLimit;
        boolean prodOver = prodTotal > productivityLimit;
        boolean otherOver = otherTotal > otherLimit;

        OveruseReport report = new OveruseReport();
        for (Map.Entry<String, Integer> e : perApp.entrySet()) {
            String app = e.getKey() == null ? "(unknown)" : e.getKey();
            int minutes = e.getValue() == null ? 0 : e.getValue();
            if (minutes <= 0) continue;

            OveruseCategory cat = categorize(app);
            boolean include = switch (cat) {
                case SOCIAL -> socialOver;
                case ENTERTAINMENT -> entOver;
                case PRODUCTIVITY -> prodOver;
                case OTHER -> otherOver;
            };
            if (include) {
                report.put(app, minutes, cat);
            }
        }

        return report;
    }

    private int getLimit(String key, int def) {
        String v = configService.get(key);
        try {
            if (v == null || v.isBlank()) return def;
            int n = Integer.parseInt(v.trim());
            return n > 0 ? n : def;
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    OveruseCategory categorize(String app) {
        String a = app.toLowerCase(Locale.ROOT);

        // Social
        if (a.contains("discord") || a.contains("slack") || a.contains("whatsapp") || a.contains("telegram") ||
                a.contains("messenger") || a.contains("instagram") || a.contains("facebook") || a.contains("xcode ghost")) {
            return OveruseCategory.SOCIAL;
        }

        // Entertainment
        if (a.contains("youtube") || a.contains("netflix") || a.contains("prime") || a.contains("spotify") ||
                a.contains("music") || a.contains("tv") || a.contains("twitch") || a.contains("steam") || a.contains("game")) {
            return OveruseCategory.ENTERTAINMENT;
        }

        // Productivity
        if (a.contains("idea") || a.contains("intellij") || a.contains("code") || a.contains("pycharm") ||
                a.contains("webstorm") || a.contains("terminal") || a.contains("iterm") || a.contains("notion") ||
                a.contains("word") || a.contains("excel") || a.contains("powerpoint") || a.contains("docs") ||
                a.contains("sheets") || a.contains("slides") || a.contains("chrome") || a.contains("safari") || a.contains("firefox")) {
            return OveruseCategory.PRODUCTIVITY;
        }

        return OveruseCategory.OTHER;
    }
}

