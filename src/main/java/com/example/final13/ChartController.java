package com.example.final13;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalDate;
import java.util.prefs.Preferences;

public class ChartController {
    @FXML private ListView<String> recentTracksList;
    @FXML private BarChart<String, Number> playsChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private ListView<String> topSongsList;
    @FXML private ListView<String> topArtistsList;
    @FXML private ListView<String> topAlbumsList;
    @FXML private MenuButton dateRangeMenu;

    private int currentUserId;

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadData();
    }

    public void reloadFilteredData() {
        loadData(); // re-trigger chart with updated filters
    }

    @FXML
    public void initialize() {
        for (MenuItem item : dateRangeMenu.getItems()) {
            item.setOnAction(event -> {
                dateRangeMenu.setText(item.getText());
                loadData();
            });
        }

        xAxis.setLabel("Time Period");
        yAxis.setLabel("Songs Played");
        playsChart.setTitle("");
    }

    private void loadData() {
        String selectedRange = dateRangeMenu.getText();
        LocalDate startDate = calculateStartDate(selectedRange);

        loadRecentTracks(startDate);
        loadPlaysChart(startDate);
        loadTopSongs(startDate);
        loadTopArtists(startDate);
        loadTopAlbums(startDate);
    }

    private LocalDate calculateStartDate(String range) {
        return switch (range) {
            case "Last 7 days" -> LocalDate.now().minusDays(7);
            case "Last 30 days" -> LocalDate.now().minusDays(30);
            case "Last 90 days" -> LocalDate.now().minusDays(90);
            case "Last 180 days" -> LocalDate.now().minusDays(180);
            case "Last 365 days" -> LocalDate.now().minusDays(365);
            default -> LocalDate.MIN;
        };
    }

    private void loadRecentTracks(LocalDate startDate) {
        ObservableList<String> recentTracks = FXCollections.observableArrayList();
        String sql = "SELECT track_title, artist_name, listen_date " +
                "FROM mat_streams " +
                "WHERE users_fk = ? " +
                (startDate != LocalDate.MIN ? "AND listen_date >= ? " : "") +
                "ORDER BY listen_date DESC FETCH FIRST 20 ROWS ONLY";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            if (startDate != LocalDate.MIN) {
                stmt.setDate(2, Date.valueOf(startDate));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String artist = rs.getString("artist_name");
                String track = rs.getString("track_title");

                boolean showUnknowns = Preferences.userNodeForPackage(ChartController.class)
                        .getBoolean("showNoMetadata", true);

                if (!showUnknowns && (artist == null || artist.toLowerCase().contains("unknown"))) continue;

                recentTracks.add(String.format("%s - %s (%s)", artist, track,
                        rs.getDate("listen_date").toLocalDate()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        recentTracksList.setItems(recentTracks);
    }

    private void loadPlaysChart(LocalDate startDate) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Songs Played");

        String sql = "SELECT TRUNC(listen_date) as day, COUNT(*) as play_count " +
                "FROM mat_streams WHERE users_fk = ? " +
                (startDate != LocalDate.MIN ? "AND listen_date >= ? " : "") +
                "GROUP BY TRUNC(listen_date) ORDER BY day";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            if (startDate != LocalDate.MIN) {
                stmt.setDate(2, Date.valueOf(startDate));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                        rs.getDate("day").toLocalDate().toString(),
                        rs.getInt("play_count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        playsChart.getData().clear();
        playsChart.getData().add(series);
    }

    private void loadTopSongs(LocalDate startDate) {
        ObservableList<String> topSongs = FXCollections.observableArrayList();
        loadTopItems("track_title", "Songs", topSongs, startDate);
        topSongsList.setItems(topSongs);
    }

    private void loadTopArtists(LocalDate startDate) {
        ObservableList<String> topArtists = FXCollections.observableArrayList();
        loadTopItems("artist_name", "Artists", topArtists, startDate);
        topArtistsList.setItems(topArtists);
    }

    private void loadTopAlbums(LocalDate startDate) {
        ObservableList<String> topAlbums = FXCollections.observableArrayList();
        loadTopItems("album_title", "Albums", topAlbums, startDate);
        topAlbumsList.setItems(topAlbums);
    }

    private void loadTopItems(String column, String type, ObservableList<String> items, LocalDate startDate) {
        boolean showUnknowns = Preferences.userNodeForPackage(ChartController.class)
                .getBoolean("showNoMetadata", true);

        String sql = String.format(
                "SELECT %s, COUNT(*) as play_count FROM mat_streams " +
                        "WHERE users_fk = ? %s GROUP BY %s ORDER BY play_count DESC FETCH FIRST 10 ROWS ONLY",
                column,
                (startDate != LocalDate.MIN ? "AND listen_date >= ?" : ""),
                column
        );

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            if (startDate != LocalDate.MIN) {
                stmt.setDate(2, Date.valueOf(startDate));
            }

            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                String value = rs.getString(column);

                if (!showUnknowns && (value == null || value.toLowerCase().contains("unknown"))) {
                    continue;
                }

                items.add(String.format("%d. %s (%d plays)", rank++, value, rs.getInt("play_count")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
