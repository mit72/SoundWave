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

    private MainHomeController mainController;
    private ObservableList<SongInfo> filteredQueue = FXCollections.observableArrayList();
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

    public void updateQueue(ObservableList<SongInfo> allSongs, int currentIndex, boolean isLoopEnabled) {
        filteredQueue.clear();

        if (allSongs.isEmpty()) {
            queueTableView.setItems(filteredQueue);
            return;
        }

        // Get the current playlist (shuffled or normal)
        List<File> currentPlaylist = mainController.isShuffleEnabled ?
                mainController.shuffledPlaylist :
                mainController.playlist;

        // Find the current song in the playlist
        if (currentIndex >= 0 && currentIndex < currentPlaylist.size()) {
            File currentFile = currentPlaylist.get(currentIndex);
            // Find the corresponding SongInfo
            for (SongInfo info : allSongs) {
                if (info.getPath().equals(currentFile.getAbsolutePath())) {
                    filteredQueue.add(info);
                    break;
                }
            }
        }

        // Add upcoming songs (up to MAX_QUEUE_SIZE - 1)
        int songsToAdd = MAX_QUEUE_SIZE - 1;
        int nextIndex = currentIndex + 1;

        if (isLoopEnabled) {
            // Show the entire playlist once (excluding current song if it's already shown)
            int startIndex = nextIndex % currentPlaylist.size();
            int added = 0;

            // Add songs until we reach the current song again or hit the limit
            for (int i = 0; i < currentPlaylist.size() - 1 && added < songsToAdd; i++) {
                int index = (startIndex + i) % currentPlaylist.size();
                File file = currentPlaylist.get(index);

                // Find the corresponding SongInfo
                for (SongInfo info : allSongs) {
                    if (info.getPath().equals(file.getAbsolutePath())) {
                        filteredQueue.add(info);
                        added++;
                        break;
                    }
                }
            }
        } else {
            // Normal mode - just show next songs
            for (int i = nextIndex; i < currentPlaylist.size() && filteredQueue.size() < MAX_QUEUE_SIZE; i++) {
                File file = currentPlaylist.get(i);
                // Find the corresponding SongInfo
                for (SongInfo info : allSongs) {
                    if (info.getPath().equals(file.getAbsolutePath())) {
                        filteredQueue.add(info);
                        break;
                    }
                }
            }
        }

        queueTableView.setItems(filteredQueue);

        // Highlight the currently playing song
        if (!filteredQueue.isEmpty()) {
            queueTableView.getSelectionModel().clearAndSelect(0);
            queueTableView.scrollTo(0);
        }
    }
}