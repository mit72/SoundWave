package com.example.final13;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.util.List;

public class QueueController {
    @FXML private TableView<SongInfo> queueTableView;
    @FXML private TableColumn<SongInfo, String> titleColumn;
    @FXML private TableColumn<SongInfo, String> artistColumn;
    @FXML private TableColumn<SongInfo, String> albumColumn;
    @FXML private TableColumn<SongInfo, String> durationColumn;
    @FXML private TableColumn<SongInfo, Boolean> queuedColumn; // Add this column

    private MainHomeController mainController;
    private ObservableList<SongInfo> filteredQueue = FXCollections.observableArrayList();
    private ObservableList<SongInfo> mainPlaylist = FXCollections.observableArrayList(); // Add this field
    private static final int MAX_QUEUE_SIZE = 50;

    public void initialize() {
        // Set up the table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));


        // Make columns non-reorderable
        queueTableView.getColumns().forEach(column -> column.setReorderable(false));
        queueTableView.getColumns().forEach(column -> column.setResizable(false));

        // Set column width ratios
        queueTableView.widthProperty().addListener((obs, oldVal, newVal) -> {
            double tableWidth = newVal.doubleValue();
            titleColumn.setPrefWidth(tableWidth * 0.40);
            artistColumn.setPrefWidth(tableWidth * 0.25);
            albumColumn.setPrefWidth(tableWidth * 0.25);
            durationColumn.setPrefWidth(tableWidth * 0.10);
        });

        // Double-click to play song
        queueTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SongInfo selected = queueTableView.getSelectionModel().getSelectedItem();
                if (selected != null && mainController != null) {
                    mainController.playSelectedFromQueue(selected);
                }
            }
        });
    }

    public void setMainController(MainHomeController mainController) {
        this.mainController = mainController;
    }

    public void updateQueue(ObservableList<SongInfo> mainPlaylist,
                            int currentIndex,
                            boolean loopEnabled,
                            List<File> queuedSongs) {

        filteredQueue.clear();

        // Add queued songs first with blue text
        for (File file : queuedSongs) {
            SongInfo songInfo = findOrCreateSongInfo(file, mainPlaylist);
            songInfo.setQueued(true);
            filteredQueue.add(songInfo);
        }

        // Add upcoming songs from playlist with default color
        List<File> currentPlaylist = mainController.isShuffleEnabled ?
                mainController.shuffledPlaylist :
                mainController.playlist;

        if (currentIndex >= 0 && currentIndex < currentPlaylist.size()) {
            // Your existing logic for adding upcoming songs...
            for (int i = currentIndex + 1; i < currentPlaylist.size(); i++) {
                SongInfo song = findOrCreateSongInfo(currentPlaylist.get(i), mainPlaylist);
                song.setQueued(false);
                filteredQueue.add(song);
            }
        }

        queueTableView.setItems(filteredQueue);

        if (!filteredQueue.isEmpty()) {
            queueTableView.getSelectionModel().clearAndSelect(0);
            queueTableView.scrollTo(0);
        }
    }

    private SongInfo findOrCreateSongInfo(File file, ObservableList<SongInfo> mainPlaylist) {
        // Check if this file exists in main playlist
        for (SongInfo info : mainPlaylist) {
            if (info.getPath().equals(file.getAbsolutePath())) {
                return info;
            }
        }
        // If not found, create a new one with basic info
        String fileName = file.getName().replaceFirst("[.][^.]+$", "");
        return new SongInfo(fileName, "Unknown Artist", "Unknown Album", "0:00", file.getAbsolutePath());
    }

    // Helper method to add a song from playlist to the queue display
    private void addSongFromPlaylist(File file) {
        // Find the corresponding SongInfo in main playlist
        for (SongInfo info : mainPlaylist) {
            if (info.getPath().equals(file.getAbsolutePath())) {
                info.setQueued(false); // Not a queued song
                filteredQueue.add(info);
                break;
            }
        }
    }
}