package com.example.demo;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class Database {

    private Connection connection;
    private final AurexCountdown plugin;

    public Database(AurexCountdown plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File dbFile = new File(dataFolder, "countdown.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            plugin.getLogger().info("Database connected.");
            createTable();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to database!");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database disconnected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS countdowns (" +
                     "uuid TEXT PRIMARY KEY, " +
                     "totalTime BIGINT NOT NULL, " +
                     "remainingTime BIGINT NOT NULL, " +
                     "paused BOOLEAN NOT NULL)";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveCountdown(UUID uuid, Countdown countdown) {
        String sql = "REPLACE INTO countdowns (uuid, totalTime, remainingTime, paused) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setLong(2, countdown.getTotalTime());
            ps.setLong(3, countdown.getRemainingTime());
            ps.setBoolean(4, countdown.isPaused());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CountdownData loadCountdown(UUID uuid) {
        String sql = "SELECT * FROM countdowns WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new CountdownData(rs.getLong("totalTime"), rs.getLong("remainingTime"), rs.getBoolean("paused"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteCountdown(UUID uuid) {
        String sql = "DELETE FROM countdowns WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class CountdownData {
        private final long totalTime;
        private final long remainingTime;
        private final boolean paused;

        public CountdownData(long totalTime, long remainingTime, boolean paused) {
            this.totalTime = totalTime;
            this.remainingTime = remainingTime;
            this.paused = paused;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public long getRemainingTime() {
            return remainingTime;
        }

        public boolean isPaused() {
            return paused;
        }
    }
}
