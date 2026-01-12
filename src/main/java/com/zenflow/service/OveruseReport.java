package com.zenflow.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class OveruseReport {

    private final Map<String, Integer> overusedAppsMinutes = new LinkedHashMap<>();

    public Map<String, Integer> getOverusedAppsMinutes() {
        return overusedAppsMinutes;
    }

    public boolean isEmpty() {
        return overusedAppsMinutes.isEmpty();
    }
}
