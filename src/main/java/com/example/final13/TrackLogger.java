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
        // Ensure we have valid values for all fields
        title = (title == null || title.isEmpty()) ? "Unknown" : title;
        artist = (artist == null || artist.isEmpty() || artist.equals("Unknown Artist")) ?
                "Unknown Artist" : artist;
        album = (album == null || album.isEmpty() || album.equals("Unknown Album")) ?
                "Unknown Album" : album;

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
        }
    }
}