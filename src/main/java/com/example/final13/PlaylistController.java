package com.example.final13;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.stage.FileChooser;
import javafx.util.Duration;



import java.io.*;
import java.util.List;

public class PlaylistController {
    @FXML private TableView<Track> playlistTableView;
    @FXML private TableColumn<Track, Integer> numberColumn;
    @FXML private TableColumn<Track, String> titleColumn;
    @FXML private TableColumn<Track, String> artistColumn;
    @FXML private TableColumn<Track, String> albumColumn;
    @FXML private TableColumn<Track, String> lengthColumn;
    @FXML private MenuButton menuButton;
    @FXML private Button playlistNameLabel;

    private final ObservableList<Track> tracks = FXCollections.observableArrayList();
    private Playlist currentPlaylist;
    private MainHomeController mainController;

    @FXML
    public void initialize() {

        numberColumn.setCellValueFactory(cellData ->
                cellData.getValue().numberProperty().asObject());
        titleColumn.setCellValueFactory(cellData ->
                cellData.getValue().titleProperty());
        artistColumn.setCellValueFactory(cellData ->
                cellData.getValue().artistProperty());
        albumColumn.setCellValueFactory(cellData ->
                cellData.getValue().albumProperty());
        lengthColumn.setCellValueFactory(cellData ->
                cellData.getValue().formattedDurationProperty());

        // Set column resize policies
        numberColumn.prefWidthProperty().bind(playlistTableView.widthProperty().multiply(0.05));
        titleColumn.prefWidthProperty().bind(playlistTableView.widthProperty().multiply(0.35));
        artistColumn.prefWidthProperty().bind(playlistTableView.widthProperty().multiply(0.2));
        albumColumn.prefWidthProperty().bind(playlistTableView.widthProperty().multiply(0.25));
        lengthColumn.prefWidthProperty().bind(playlistTableView.widthProperty().multiply(0.15));

        // Disable column reordering
        playlistTableView.getColumns().forEach(column -> column.setReorderable(false));

        // Set the items to the TableView
        playlistTableView.setItems(tracks);

        // Style menu items
        for (MenuItem item : menuButton.getItems()) {
            item.setStyle("-fx-text-fill: #89CFF0;");
        }
        menuButton.setStyle("-fx-text-fill: #89CFF0;");


    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem removeItem = new MenuItem("Remove from playlist");
        removeItem.setOnAction(e -> {
            Track selected = playlistTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                removeTrack(selected);
            }
        });

        contextMenu.getItems().add(removeItem);
        playlistTableView.setContextMenu(contextMenu);
    }

    private void removeTrack(Track track) {
        currentPlaylist.getMediaFiles().remove(track.getFile());
        tracks.remove(track);
        // Renumber remaining tracks
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).numberProperty().set(i + 1);
        }
        playlistTableView.refresh();
    }

    public void initializeWithPlaylist(Playlist playlist) {
        this.currentPlaylist = playlist;
        updateUI();
    }

    public void setMainController(MainHomeController mainController) {
        this.mainController = mainController;
    }

    private void updateUI() {
        if (currentPlaylist != null) {
            playlistNameLabel.setText(currentPlaylist.getName());
            tracks.clear();

            // Create Track objects and load metadata
            int trackNumber = 1;
            for (File file : currentPlaylist.getMediaFiles()) {
                Track track = new Track(trackNumber++, file);
                tracks.add(track);

                // Load metadata asynchronously
                loadTrackMetadata(track);
            }
        }
    }

    private void loadTrackMetadata(Track track) {
        Media media = new Media(track.getFile().toURI().toString());

        // Listener for metadata changes
        media.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
            if (change.wasAdded()) {
                Platform.runLater(() -> {
                    track.updateMetadata(media);
                    playlistTableView.refresh();
                });
            }
        });



        // Error handling
        media.setOnError(() -> {
            System.err.println("Media error: " + media.getError().getMessage());
        });
    }

    @FXML
    private void handlePlaySelected() {
        Track selectedTrack = playlistTableView.getSelectionModel().getSelectedItem();
        if (selectedTrack != null && mainController != null) {
            mainController.playFile(selectedTrack.getFile());
        }
    }

    @FXML
    private void handleFileSelect(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(
                playlistTableView.getScene().getWindow()
        );

        if (files != null && !files.isEmpty()) {
            addFilesToPlaylist(files);
        }
    }

    private void addFilesToPlaylist(List<File> files) {
        if (currentPlaylist == null) {
            currentPlaylist = new Playlist("New Playlist");
        }

        int startNumber = tracks.size() + 1;
        int addedCount = 0;

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            String filePath = file.getAbsolutePath();

            // Check if file already exists in playlist
            boolean isDuplicate = currentPlaylist.getMediaFiles().stream()
                    .anyMatch(f -> f.getAbsolutePath().equals(filePath));

            if (!isDuplicate) {
                currentPlaylist.addMediaFile(file);
                Track track = new Track(startNumber + addedCount, file);
                tracks.add(track);
                loadTrackMetadata(track);
                addedCount++;
            }
        }

        if (addedCount < files.size()) {
            showAlert("Duplicate Files",
                    (files.size() - addedCount) + " duplicate files were not added.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}