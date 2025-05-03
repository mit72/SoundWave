package com.example.final13;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.Serializable;

public class Playlist implements Serializable {
    private String name;
    private ObservableList<File> mediaFiles = FXCollections.observableArrayList();

    public Playlist(String name) {
        this.name = name;
    }

    // Getter for playlist name
    public String getName() {
        return name;
    }

    // Setter for playlist name
    public void setName(String name) {
        this.name = name;
    }

    // Getter for media files
    public ObservableList<File> getMediaFiles() {
        return mediaFiles;
    }

    // Add a file to the playlist
    public void addMediaFile(File file) {
        mediaFiles.add(file);
    }

    // Remove a file from the playlist
    public void removeMediaFile(File file) {
        mediaFiles.remove(file);
    }

    public boolean containsFile(File file) {
        return mediaFiles.stream()
                .anyMatch(f -> f.getAbsolutePath().equals(file.getAbsolutePath()));
    }
}