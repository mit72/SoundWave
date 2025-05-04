package com.example.final13;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class FolderBrowserView {
    private final ListView<File> songListView = new ListView<>();
    private MediaPlayer currentPlayer;

    public FolderBrowserView() {
        songListView.setCellFactory(lv -> new SongListCell());
        songListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double-click to play
                playSelectedSong();
            }
        });
    }

    public ListView<File> getView() {
        return songListView;
    }

    public void loadFolder(File folder) {
        if (folder != null && folder.isDirectory()) {
            File[] audioFiles = folder.listFiles(file ->
                    file.getName().toLowerCase().endsWith(".mp3") ||
                            file.getName().toLowerCase().endsWith(".wav") ||
                            file.getName().toLowerCase().endsWith(".m4a"));
            songListView.setItems(FXCollections.observableArrayList(audioFiles));
        }
    }

    private void playSelectedSong() {
        File selectedFile = songListView.getSelectionModel().getSelectedItem();
        if (selectedFile != null) {
            if (currentPlayer != null) {
                currentPlayer.stop();
            }
            Media media = new Media(selectedFile.toURI().toString());
            currentPlayer = new MediaPlayer(media);
            currentPlayer.play();
        }
    }
}
