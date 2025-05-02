package com.example.final13;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainHomeController {

    // window resizing and main structure
    @FXML private Button fileSelect;
    @FXML private Button LogOut;
    @FXML private Button exitButton;
    @FXML private Button minimizeButton;
    @FXML private Button windowedModeButton;
    @FXML private HBox titleBar;
    @FXML private VBox centerContainer;
    @FXML private AnchorPane windowButtonsAnchor;
    @FXML private Button playPauseButton;
    @FXML private ImageView playPauseImage;

    @FXML private ImageView albumArtImageView;
    @FXML private Label titleLabel;
    @FXML private Label artistLabel;
    @FXML private Label albumLabel;
    @FXML private Slider timeSlider;
    @FXML private Slider audioSlider;
    @FXML private Button muteButton;
    @FXML private ImageView muteImage;
    @FXML private Label currentTime;
    @FXML private Label fullTime;

    private Stage stage;
    private final Border border = new Border();
    private final Image playImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/play.png")).toExternalForm());
    private final Image pauseImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/pause.png")).toExternalForm());
    private final Image muteImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/volume-mute.png")).toExternalForm());
    private final Image unmuteImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/volume-down.png")).toExternalForm());
    private final Image volupImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/volume-up.png")).toExternalForm());
    private boolean isMuted = false;
    private double currentSliderValue = 0.5;
    private List<File> playlist = new ArrayList<>();
    private int currentTrackIndex = -1; // -1 means no track selected
    private File currentlyPlayingFile;
    private double currentVolume = 0.5;



    @FXML
    private void handleFileSelect(ActionEvent event) {
        currentSliderValue = audioSlider.getValue();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Music File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac")
        );

        // Allow multiple file selection
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            // Add all selected files to the playlist
            playlist.addAll(selectedFiles);

            // If nothing is currently playing, start playing the first file
            if (currentTrackIndex == -1) {
                currentTrackIndex = 0;
                playCurrentTrack();
            }
        }
    }

    private void playCurrentTrack() {
        if (currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
            File fileToPlay = playlist.get(currentTrackIndex);
            playFile(fileToPlay);
        }
    }

    private void playFile(File file) {
        currentlyPlayingFile = file;
        MusicPlayerManager.playFile(file);
        setupMediaPlayer();

        //audio
        MediaPlayer player = MusicPlayerManager.getMediaPlayer();
        if (player != null) {
            player.setVolume(currentVolume);
            audioSlider.setValue(currentVolume); // Update slider position
        }
    }



    private void setupMediaPlayer() {
        MediaPlayer player = MusicPlayerManager.getMediaPlayer();
        playPauseImage.setImage(pauseImg);

        player.setVolume(currentVolume);
        audioSlider.setValue(currentVolume);

        audioSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentVolume = newValue.doubleValue();
            if (!isMuted) {
                player.setVolume(currentVolume);
            }
            if (audioSlider.getValue() > 0 && audioSlider.getValue() <= 0.5) {
                isMuted = false;
                muteImage.setImage(unmuteImg);
            } else if (audioSlider.getValue() == 0) {
                isMuted = true;
                muteImage.setImage(muteImg);
            } else if (audioSlider.getValue() > 0.5) {
                isMuted = false;
                muteImage.setImage(volupImg);
            }
        });

        Media media = MusicPlayerManager.getCurrentMedia();
        if (media != null) {
            player.setOnReady(() -> {
                updateNowPlayingUI();
                fullTime.setText(formatTime(player.getTotalDuration()));
                currentTime.setText(formatTime(player.getCurrentTime()));
                timeSlider.setMax(player.getTotalDuration().toSeconds());
            });

            player.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
                if (!timeSlider.isValueChanging()) {
                    currentTime.setText(formatTime(newTime));
                    timeSlider.setValue(newTime.toSeconds());
                }
            });

            timeSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                if (isChanging) {
                    Duration draggedTime = Duration.seconds(timeSlider.getValue());
                    currentTime.setText(formatTime(draggedTime));
                } else {
                    player.seek(Duration.seconds(timeSlider.getValue()));
                }
            });

            timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (timeSlider.isValueChanging()) {
                    Duration draggedTime = Duration.seconds(newVal.doubleValue());
                    currentTime.setText(formatTime(draggedTime));
                }
            });

            timeSlider.setOnMouseReleased(event2 -> {
                player.seek(Duration.seconds(timeSlider.getValue()));
                Duration clickedTime = Duration.seconds(timeSlider.getValue());
                currentTime.setText(formatTime(clickedTime));
            });

            media.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
                if (change.wasAdded()) {
                    updateNowPlayingUI();
                }
            });

            player.setOnEndOfMedia(() -> {
                playPauseImage.setImage(playImg);
                playNext(); // Automatically play next when current finishes
            });
        }
    }




    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    @FXML
    private void toggleMute() {
        MediaPlayer mediaPlayer = MusicPlayerManager.getMediaPlayer();
        if(!isMuted)
            currentSliderValue = audioSlider.getValue();
        if (mediaPlayer == null) {
            return;  // No media player loaded
        }

        isMuted = !isMuted;  // Toggle the mute state
        if (isMuted) {
            mediaPlayer.setVolume(0);  // Mute the audio
            audioSlider.setValue(0);
        } else {
            audioSlider.setValue(currentSliderValue);
            mediaPlayer.setVolume(audioSlider.getValue());
        }

        if (muteButton != null) {
            if(!isMuted && audioSlider.getValue() > 0 && audioSlider.getValue() <=0.5) {
                muteImage.setImage(unmuteImg);
            } else if (!isMuted && audioSlider.getValue() > 0.5) {
                muteImage.setImage(volupImg);
            } else if (isMuted && audioSlider.getValue() == 0) {
                muteImage.setImage(muteImg);
            }
        }
    }

    @FXML
    private void playNext() {
        if (playlist.isEmpty()) return;

        if (currentTrackIndex < playlist.size() - 1) {
            currentTrackIndex++;
            playCurrentTrack();
        } else {
            // Optional: loop back to start
            // currentTrackIndex = 0;
            // playCurrentTrack();
            System.out.println("End of playlist reached");
        }
    }

    @FXML
    private void playPrevious() {
        if (playlist.isEmpty()) return;

        if (currentTrackIndex > 0) {
            currentTrackIndex--;
            playCurrentTrack();
        } else {
            // Optional: loop to end
            // currentTrackIndex = playlist.size() - 1;
            // playCurrentTrack();
            System.out.println("Beginning of playlist reached");
        }
    }


    private void updateNowPlayingFallbacks(Media media) {
        String fallbackTitle;
        try {
            String source = media.getSource();
            String fileName = new File(new java.net.URI(source)).getName();
            fallbackTitle = fileName.replaceFirst("[.][^.]+$", "");
        } catch (Exception e) {
            fallbackTitle = "Unknown Title";
        }

        String fallbackArtist = "Unknown Artist";
        String fallbackAlbum = "Unknown Album";
        Image fallbackImage = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/music-note.png")).toExternalForm());

        if (titleLabel != null) titleLabel.setText(fallbackTitle);
        if (artistLabel != null) artistLabel.setText(fallbackArtist);
        if (albumLabel != null) albumLabel.setText(fallbackAlbum);
        if (albumArtImageView != null) albumArtImageView.setImage(fallbackImage);
    }


    //spremeni ui
    private void updateNowPlayingUI() {
        Media media = MusicPlayerManager.getCurrentMedia();
        if (media != null) {
            ObservableMap<String, Object> metadata = media.getMetadata();

            // Set fallback values first
            String fallbackTitle = getFallbackTitle(media);
            Image fallbackImage = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/music-note.png")).toExternalForm());

            // Update UI with fallbacks
            if (titleLabel != null) titleLabel.setText(fallbackTitle);
            if (albumArtImageView != null) albumArtImageView.setImage(fallbackImage);

            // Then try to update with actual metadata
            updateUIFromMetadata(metadata, media);

            // Listen for future metadata changes
            metadata.addListener((MapChangeListener<String, Object>) change -> {
                if (change.wasAdded()) {
                    updateUIFromMetadata(metadata, media);
                }
            });
        }
    }

    private String getFallbackTitle(Media media) {
        try {
            String source = media.getSource();
            String fileName = new File(new URI(source)).getName();
            return fileName.replaceFirst("[.][^.]+$", "");
        } catch (Exception e) {
            return "Unknown Title";
        }
    }


    private void updateUIFromMetadata(ObservableMap<String, Object> metadata, Media media) {
        Object title = metadata.get("title");
        Object artist = metadata.get("artist");
        Object album = metadata.get("album");
        Object image = metadata.get("image");

        // Only update if metadata exists
        if (title != null && !title.toString().isEmpty()) {
            titleLabel.setText(title.toString());
        }

        if (artist != null && !artist.toString().isEmpty()) {
            artistLabel.setText(artist.toString());
        } else {
            artistLabel.setText("Unknown Artist");
        }

        if (album != null && !album.toString().isEmpty()) {
            albumLabel.setText(album.toString());
        } else {
            albumLabel.setText("Unknown Album");
        }

        if (image instanceof Image) {
            albumArtImageView.setImage((Image) image);
        } else {
            // Set fallback image if no image in metadata
            Image fallbackImage = new Image(Objects.requireNonNull(
                    getClass().getResource("/com/example/final13/img/music-note.png")).toExternalForm());
            albumArtImageView.setImage(fallbackImage);
        }
    }


    @FXML
    private void togglePlayPause() {
        MediaPlayer mediaPlayer = MusicPlayerManager.getMediaPlayer();

        if (mediaPlayer == null) {
            System.out.println("No media loaded.");
            return;
        }

        switch (mediaPlayer.getStatus()) {
            case PLAYING:
                mediaPlayer.pause();
                playPauseImage.setImage(playImg);
                break;
            case PAUSED:
            case READY:
            case STOPPED:
                mediaPlayer.play();
                playPauseImage.setImage(pauseImg);
                break;
            default:
                System.out.println("MediaPlayer status: " + mediaPlayer.getStatus());
        }
    }

    private void disposeMediaPlayer() {
        MediaPlayer mediaPlayer = MusicPlayerManager.getMediaPlayer();

        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null; // Reset the media player
        }
    }


    @FXML
    private void switchToSignIn(ActionEvent event) throws IOException {
        disposeMediaPlayer();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("sign-in.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        boolean isMaximized = stage.isMaximized();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();

        Scene newScene = new Scene(root, currentWidth, currentHeight);
        stage.setScene(newScene);

        SignInController controller = loader.getController();
        controller.setStage(stage);

        if (isMaximized) {
            stage.setMaximized(true);
        }

        stage.show();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        border.setStage(stage, titleBar);
    }

    @FXML public void closeWindow() { stage.close(); }

    @FXML private void minimizeWindow() { stage.setIconified(true); }

    @FXML private void toggleMaximize() { stage.setMaximized(!stage.isMaximized()); }

}
