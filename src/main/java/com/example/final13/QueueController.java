package com.example.final13;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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

        // Always add the currently playing song first
        if (currentIndex >= 0 && currentIndex < allSongs.size()) {
            filteredQueue.add(allSongs.get(currentIndex));
        }

        // Add upcoming songs (up to MAX_QUEUE_SIZE - 1)
        int startIndex = currentIndex + 1;
        int endIndex = Math.min(startIndex + MAX_QUEUE_SIZE - 1, allSongs.size());

        // If loop is enabled, we'll wrap around to the beginning
        if (isLoopEnabled) {
            for (int i = startIndex; i < endIndex; i++) {
                if (i < allSongs.size()) {
                    filteredQueue.add(allSongs.get(i));
                } else {
                    // Wrap around to beginning
                    filteredQueue.add(allSongs.get(i - allSongs.size()));
                }
            }
        } else {
            // Just add the next songs normally
            for (int i = startIndex; i < endIndex && i < allSongs.size(); i++) {
                filteredQueue.add(allSongs.get(i));
            }
        }

        // Limit to MAX_QUEUE_SIZE
        while (filteredQueue.size() > MAX_QUEUE_SIZE) {
            filteredQueue.remove(filteredQueue.size() - 1);
        }

        queueTableView.setItems(filteredQueue);

        // Highlight the currently playing song
        if (!filteredQueue.isEmpty()) {
            queueTableView.getSelectionModel().select(0);
            queueTableView.scrollTo(0);
        }
    }
}