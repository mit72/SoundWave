package com.example.final13;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class MusicPlayerManager {
    private static MediaPlayer mediaPlayer;
    private static Media currentMedia;

    public static void playFile(File file) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        currentMedia = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(currentMedia);
        mediaPlayer.play();
    }

    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public static Media getCurrentMedia() {
        return currentMedia;
    }

    public static boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }
}

