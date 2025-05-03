package com.example.final13;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {
    private static final String SAVE_FILE = "playlists.dat";
    private List<Playlist> playlists;

    public PlaylistManager() {
        playlists = loadPlaylists();
    }

    public void savePlaylists() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(playlists);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Playlist> loadPlaylists() {
        File file = new File(SAVE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(file))) {
                return (List<Playlist>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
        savePlaylists();
    }

    public List<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }
}
