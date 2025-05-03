package com.example.final13;

import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.util.Duration;

import java.io.File;

import javafx.beans.property.*;

public class Track {
    private final IntegerProperty number;
    private final File file;
    private final StringProperty title;
    private final StringProperty artist;
    private final StringProperty album;
    private final ObjectProperty<Duration> duration;

    public Track(int number, File file) {
        this.number = new SimpleIntegerProperty(number);
        this.file = file;
        this.title = new SimpleStringProperty(file.getName().replaceFirst("[.][^.]+$", ""));
        this.artist = new SimpleStringProperty("Unknown Artist");
        this.album = new SimpleStringProperty("Unknown Album");
        this.duration = new SimpleObjectProperty<>(Duration.ZERO);
    }

    // Property getters for JavaFX binding
    public IntegerProperty numberProperty() {
        return number;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty artistProperty() {
        return artist;
    }

    public StringProperty albumProperty() {
        return album;
    }

    public StringProperty formattedDurationProperty() {
        return new SimpleStringProperty(getFormattedDuration());
    }

    // Regular getters
    public int getNumber() {
        return number.get();
    }

    public File getFile() {
        return file;
    }

    public String getTitle() {
        return title.get();
    }

    public String getArtist() {
        return artist.get();
    }

    public String getAlbum() {
        return album.get();
    }

    public Duration getDuration() {
        return duration.get();
    }

    public String getFormattedDuration() {
        Duration d = duration.get();
        return String.format("%02d:%02d",
                (int) d.toMinutes(),
                (int) d.toSeconds() % 60);
    }

    // Method to update metadata
// In Track class
    public void updateMetadata(Media media) {
        ObservableMap<String, Object> metadata = media.getMetadata();

        if (metadata.containsKey("title")) {
            title.set(metadata.get("title").toString());
        }
        if (metadata.containsKey("artist")) {
            artist.set(metadata.get("artist").toString());
        }
        if (metadata.containsKey("album")) {
            album.set(metadata.get("album").toString());
        }

        // Only update duration if it's valid
        if (!media.getDuration().equals(Duration.UNKNOWN)) {
            duration.set(media.getDuration());
        }
    }
}