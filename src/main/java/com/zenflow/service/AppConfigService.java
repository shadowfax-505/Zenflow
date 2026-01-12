package com.zenflow.service;

import com.zenflow.db.DBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple DB-backed key/value configuration.
 * Keys live in app_config.
 */
public class AppConfigService {

    public String get(String key) {
        String sql = "SELECT value FROM app_config WHERE key = ?";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("value");
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    public boolean set(String key, String value) {
        String sql = "INSERT INTO app_config(key, value) VALUES(?, ?) " +
                "ON CONFLICT(key) DO UPDATE SET value=excluded.value";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
