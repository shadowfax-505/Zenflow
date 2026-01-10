package com.zenflow.db;

import java.io.*;
import java.sql.*;

public class DBHelper {

    private static final String DB_DIR = System.getProperty("user.home") + "/.zenflow";
    private static final String DB_PATH = DB_DIR + "/zenflow.db";
    private static String dbUrl = "jdbc:sqlite:" + DB_PATH;

    public static void setDbUrl(String url) {
        dbUrl = url;
    }


    public static Connection getConnection() throws SQLException {
        loadDriver();
        ensureDbDirExists();
        return DriverManager.getConnection(dbUrl);
    }


    public static void initDatabase() {
        ensureDbDirExists();

        try (Connection conn = getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL");
            }

            runInitScript(conn);
        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }
    }


    private static void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }


    private static void ensureDbDirExists() {
        File dir = new File(DB_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Failed to create database directory: " + DB_DIR);
        }
    }


    private static void runInitScript(Connection conn) throws Exception {
        InputStream is = DBHelper.class.getResourceAsStream("/db/DBInit.sql");
        if (is == null) {
            throw new RuntimeException("DBInit.sql not found in resources");
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        String[] statements = sb.toString().split(";");
        for (String s : statements) {
            String sql = s.trim();
            if (!sql.isEmpty()) {
                try (Statement st = conn.createStatement()) {
                    st.execute(sql);
                }
            }
        }
    }
}
