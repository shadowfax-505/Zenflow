package com.zenflow.service;

import com.zenflow.db.DBHelper;

import java.sql.*;
import java.time.*;
import java.util.*;

public class AnalyticsService {


    public long getTotalFocusMinutesToday() {
        long startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String sql = "SELECT SUM(COALESCE(end_ts, strftime('%s','now') * 1000) - start_ts) AS ms_sum " +
                "FROM sessions WHERE type='FOCUS' AND start_ts >= ?";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, startOfDay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long ms = rs.getLong("ms_sum");
                    return ms <= 0 ? 0 : ms / 60000;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }


    public int getCompletedSessionsToday() {
        long startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String sql = "SELECT COUNT(*) AS cnt FROM sessions WHERE type='FOCUS' AND completed=1 AND start_ts >= ?";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, startOfDay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return 0;
    }


    public int getLongestStreak() {

        String sql = "SELECT DISTINCT date(start_ts / 1000, 'unixepoch') AS day " +
                "FROM sessions WHERE type='FOCUS' AND completed=1 ORDER BY day DESC";
        List<LocalDate> days = new ArrayList<>();
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String dayStr = rs.getString("day");
                days.add(LocalDate.parse(dayStr));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }

        if (days.isEmpty()) return 0;

        int longest = 1, current = 1;
        for (int i = 1; i < days.size(); i++) {
            if (days.get(i).equals(days.get(i - 1).minusDays(1))) {
                current++;
            } else {
                longest = Math.max(longest, current);
                current = 1;
            }
        }
        longest = Math.max(longest, current);
        return longest;
    }


    public Map<String, Integer> getUsageMinutesByHourToday() {
        long startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String sql = "SELECT start_ts, end_ts FROM window_usage WHERE start_ts >= ?";
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int h = 0; h < 24; h++) map.put(String.format("%02d", h), 0);

        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, startOfDay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long s = rs.getLong("start_ts");
                    long e = rs.getLong("end_ts");
                    if (e <= s) continue;

                    long start = Math.max(s, startOfDay);
                    long end = Math.max(start, e);
                    Instant cur = Instant.ofEpochMilli(start);
                    while (cur.toEpochMilli() < end) {
                        ZonedDateTime z = cur.atZone(ZoneId.systemDefault());
                        ZonedDateTime endOfHour = z.withMinute(59).withSecond(59).withNano(999_999_999);
                        long to = Math.min(end, endOfHour.toInstant().toEpochMilli());
                        int minutes = (int) Math.ceil((to - cur.toEpochMilli()) / 60000.0);
                        String hourKey = String.format("%02d", z.getHour());
                        map.put(hourKey, map.getOrDefault(hourKey, 0) + minutes);
                        cur = endOfHour.toInstant().plusMillis(1);
                    }
                }
            }
        } catch (SQLException ex) { throw new RuntimeException(ex); }

        return map;
    }

    public List<Map.Entry<String, Integer>> getTopAppsToday(int topN) {
        long startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String sql = "SELECT COALESCE(process_name, window_title) AS app, SUM(end_ts - start_ts) as ms_sum " +
                "FROM window_usage WHERE start_ts >= ? GROUP BY app ORDER BY ms_sum DESC LIMIT ?";
        List<Map.Entry<String, Integer>> out = new ArrayList<>();
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, startOfDay);
            ps.setInt(2, topN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String app = rs.getString("app");
                    long ms = rs.getLong("ms_sum");
                    out.add(new AbstractMap.SimpleEntry<>(app == null ? "(unknown)" : app, (int) (ms / 60000)));
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }
}
