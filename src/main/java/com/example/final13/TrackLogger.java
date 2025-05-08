package com.example.final13;

import com.example.final13.OracleConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TrackLogger {
    private static final String INSERT_STREAM_SQL =
            "INSERT INTO mat_streams (id, track_title, artist_name, album_title, listen_date, users_fk) " +
                    "VALUES (stream_seq.NEXTVAL, ?, ?, ?, ?, ?)";

    public static void logTrack(String title, String artist, String album, int userId) {
        // Additional validation
        if (title == null || title.isEmpty() ||
                artist == null || artist.isEmpty() ||
                album == null || album.isEmpty()) {
            System.err.println("Invalid metadata - not logging");
            return;
        }

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_STREAM_SQL)) {

            pstmt.setString(1, title);
            pstmt.setString(2, artist);
            pstmt.setString(3, album);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(5, userId);

            int rows = pstmt.executeUpdate();
            if (rows == 1) {
                System.out.println("Logged track: " + title);
            }
        } catch (SQLException e) {
            System.err.println("Error logging track: " + e.getMessage());
            // Log the error but don't crash
        }
    }
}