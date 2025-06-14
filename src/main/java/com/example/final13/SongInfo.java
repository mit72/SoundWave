package com.example.final13;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class SongInfo {
    private final String title;
    private final String artist;
    private final String album;
    private final String duration;
    private final String path;

    public SongInfo(String title, String artist, String album, String duration, String path) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.path = path;
    }

    // In SongInfo.java
    private BooleanProperty queued = new SimpleBooleanProperty();

    public boolean isQueued() {
        return queued.get();
    }

    public BooleanProperty queuedProperty() {
        return queued;
    }

    public void setQueued(boolean queued) {
        this.queued.set(queued);
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getDuration() { return duration; }
    public String getPath() { return path; }
}

