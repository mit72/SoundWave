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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

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
    private boolean isSeeking = false;


    @FXML
    private void handleFileSelect(ActionEvent event) {
        currentSliderValue = audioSlider.getValue();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Music File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            MusicPlayerManager.playFile(selectedFile);
            MediaPlayer player = MusicPlayerManager.getMediaPlayer();
            playPauseImage.setImage(pauseImg);
            player.setVolume(currentSliderValue);

            // Volume control via audioSlider
            audioSlider.setValue(currentSliderValue);
            audioSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (!isMuted) {
                    player.setVolume(newValue.doubleValue());
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

                    // Set up the full time and current time
                    fullTime.setText(formatTime(player.getTotalDuration()));
                    currentTime.setText(formatTime(player.getCurrentTime()));

                    // Set slider max based on total duration
                    timeSlider.setMax(player.getTotalDuration().toSeconds());
                });

                // Listen for current time updates and update the slider ONLY if NOT being dragged
                player.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
                    if (!timeSlider.isValueChanging()) { // Only update slider if not dragging
                        currentTime.setText(formatTime(newTime));
                        timeSlider.setValue(newTime.toSeconds());
                    }
                });

                // Prevent slider updates during dragging
                timeSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                    if (isChanging) {
                        // While dragging, update the current time label based on the slider position
                        javafx.util.Duration draggedTime = javafx.util.Duration.seconds(timeSlider.getValue());
                        currentTime.setText(formatTime(draggedTime));
                    } else {
                        // After dragging, seek to the new time
                        player.seek(javafx.util.Duration.seconds(timeSlider.getValue()));
                    }
                });

                // Live update currentTime label during dragging
                timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (timeSlider.isValueChanging()) {
                        javafx.util.Duration draggedTime = javafx.util.Duration.seconds(newVal.doubleValue());
                        currentTime.setText(formatTime(draggedTime));
                    }
                });

                // Handle slider click event
                timeSlider.setOnMouseReleased(event2 -> {
                    player.seek(javafx.util.Duration.seconds(timeSlider.getValue()));
                    javafx.util.Duration clickedTime = javafx.util.Duration.seconds(timeSlider.getValue());
                    currentTime.setText(formatTime(clickedTime));
                });

                // Listen for metadata updates
                media.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
                    if (change.wasAdded()) {
                        updateNowPlayingUI();
                    }
                });

                // Handle end of media
                player.setOnEndOfMedia(() -> {
                    playPauseImage.setImage(playImg);
                });
            }
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

            // Immediate UI update with current metadata
            updateUIFromMetadata(metadata, media);

            // Listen for future metadata changes
            metadata.addListener((MapChangeListener<String, Object>) change -> {
                if (change.wasAdded()) {
                    updateUIFromMetadata(metadata, media);
                }
            });
        }
    }

    private void updateUIFromMetadata(ObservableMap<String, Object> metadata, Media media) {
        Object title = metadata.get("title");
        Object artist = metadata.get("artist");
        Object album = metadata.get("album");
        Object image = metadata.get("image");

        if (titleLabel != null) {
            if (title != null && !title.toString().isBlank()) {
                titleLabel.setText(title.toString());
            } else {
                try {
                    URI uri = new URI(media.getSource()); // Convert string to URI
                    Path path = Paths.get(uri); // Convert URI to Path
                    String fileName = path.getFileName().toString(); // Get filename
                    fileName = fileName.substring(0, fileName.length()-4);
                    titleLabel.setText(fileName);
                } catch (URISyntaxException | IllegalArgumentException e) {
                    titleLabel.setText("Unknown Title");
                    e.printStackTrace();
                }
            }
        }


        if (artistLabel != null) {
            artistLabel.setText(artist != null ? artist.toString() : "Unknown Artist");
        }

        if (albumLabel != null) {
            albumLabel.setText(album != null ? album.toString() : "Unknown Album");
        }

        if (albumArtImageView != null && image instanceof Image) {
            albumArtImageView.setImage((Image) image);
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
