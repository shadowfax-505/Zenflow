package com.zenflow.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class OveruseReport {

    private final Map<String, Integer> overusedAppsMinutes = new LinkedHashMap<>();
    private final Map<String, OveruseCategory> overusedAppsCategory = new LinkedHashMap<>();

    public Map<String, Integer> getOverusedAppsMinutes() {
        return overusedAppsMinutes;
    }

    public Map<String, OveruseCategory> getOverusedAppsCategory() {
        return overusedAppsCategory;
    }

    public void put(String app, int minutes, OveruseCategory category) {
        if (app == null) app = "(unknown)";
        overusedAppsMinutes.put(app, minutes);
        overusedAppsCategory.put(app, category == null ? OveruseCategory.OTHER : category);
    }

    public OveruseCategory getCategory(String app) {
        if (app == null) return OveruseCategory.OTHER;
        return overusedAppsCategory.getOrDefault(app, OveruseCategory.OTHER);
    }

    public boolean isEmpty() {
        return overusedAppsMinutes.isEmpty();
    }
}
