package com.example.final13;

public class SongInfo {
    private final String title;
    private final String artist;
    private final String album;
    private final String duration;
    private final String path; // Internal use only

    public SongInfo(String title, String artist, String album, String duration, String path) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.path = path;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getDuration() { return duration; }
    public String getPath() { return path; } // still needed to play
}


