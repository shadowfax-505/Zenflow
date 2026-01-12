package com.zenflow.dao;

import com.zenflow.db.DBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ReminderDAO {

    public boolean addReminder(String day, String text) {
        if (day == null || day.isBlank()) return false;
        if (text == null || text.isBlank()) return false;

        String sql = "INSERT INTO reminders(day, text, created_ts) VALUES(?,?,?)";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, day);
            ps.setString(2, text);
            ps.setLong(3, Instant.now().toEpochMilli());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<String> getRemindersForDay(String day) {
        List<String> out = new ArrayList<>();
        String sql = "SELECT text FROM reminders WHERE day = ? ORDER BY created_ts DESC";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, day);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getString("text"));
                }
            }
        } catch (SQLException ignored) {
        }
        return out;
    }

    public boolean deleteReminder(String day, String text) {
        String sql = "DELETE FROM reminders WHERE day = ? AND text = ?";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, day);
            ps.setString(2, text);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}

