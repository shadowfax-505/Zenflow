package com.zenflow.dao;

import com.zenflow.db.DBHelper;
import com.zenflow.model.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    public Long addSession(Session s) {
        String sql = "INSERT INTO sessions(start_ts, type, completed) VALUES(?,?,?)";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, s.getStartTs());
            ps.setString(2, s.getType());
            ps.setInt(3, s.getCompleted());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void endSession(int id, long endTs, int completed) {
        String sql = "UPDATE sessions SET end_ts = ?, completed = ? WHERE id = ?";
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, endTs);
            ps.setInt(2, completed);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Session> getSessionsByDateRange(long fromTs, long toTs) {
        String sql = "SELECT id, start_ts, end_ts, type, completed FROM sessions WHERE start_ts BETWEEN ? AND ? ORDER BY start_ts DESC";
        List<Session> out = new ArrayList<>();
        try (Connection c = DBHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, fromTs);
            ps.setLong(2, toTs);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Session s = new Session();
                    s.setId(rs.getInt("id"));
                    s.setStartTs(rs.getLong("start_ts"));
                    s.setEndTs(rs.getLong("end_ts"));
                    s.setType(rs.getString("type"));
                    s.setCompleted(rs.getInt("completed"));
                    out.add(s);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }
}
