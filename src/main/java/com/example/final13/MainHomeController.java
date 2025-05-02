package com.example.final13;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
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
    @FXML private Button loopButton;
    @FXML private Button shuffleButton;
    @FXML private ImageView loopImg;
    @FXML private ImageView shuffleImg;
    @FXML private Button profileButton, chartsButton, queueButton, libraryButton, homeButton;
    @FXML private BorderPane mainBorderPane;

    private Stage stage;
    private final Border border = new Border();
    private final Image playImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/playalt.png")).toExternalForm());
    private final Image pauseImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/pause.png")).toExternalForm());
    private final Image muteImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/volume-mute.png")).toExternalForm());
    private final Image unmuteImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/volume-down.png")).toExternalForm());
    private final Image volupImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/volume-up.png")).toExternalForm());
    private final Image shuffleEnabled = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/shuffle-enabled.png")).toExternalForm());
    private final Image shuffleDisabled = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/shuffle.png")).toExternalForm());
    private final Image loopEnabled = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/arrows-repeat-enabled.png")).toExternalForm());
    private final Image loopDisabled = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/arrows-repeat.png")).toExternalForm());
    private boolean isMuted = false;
    private double currentSliderValue = 0.5;
    private List<File> playlist = new ArrayList<>();
    private int currentTrackIndex = -1;
    private File currentlyPlayingFile;
    private double currentVolume = 0.5;
    private boolean isLoopEnabled = false;
    private boolean isShuffleEnabled = false;
    private List<File> shuffledPlaylist = new ArrayList<>();
    private Parent initialCenterContent;
    private Parent currentView;

    @FXML
    public void initialize() {
        // Store initial center content
        initialCenterContent = (Parent) mainBorderPane.getCenter();
        bindSliderFill(timeSlider);
        bindSliderFill(audioSlider);
        timeSlider.setDisable(true);
        // Set button actions
        setupNavigationButtons();

        // Set home as default selected
        setActiveButton(homeButton);
    }

    private void setupNavigationButtons() {
        homeButton.setOnAction(e -> {
            setActiveButton(homeButton);
            showHomeView();
        });

        libraryButton.setOnAction(e -> {
            setActiveButton(libraryButton);
            loadViewLib("/com/example/final13/library-view.fxml");
        });

        queueButton.setOnAction(e -> {
            setActiveButton(queueButton);
            loadView("/com/example/final13/queue-view.fxml");
        });

        chartsButton.setOnAction(e -> {
            setActiveButton(chartsButton);
            loadView("/com/example/final13/chart-view.fxml");
        });

        profileButton.setOnAction(e -> {
            setActiveButton(profileButton);
            loadView("/com/example/final13/profile-view.fxml");
        });
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Get controller and initialize if needed
            Object controller = loader.getController();
            /*if (controller instanceof LibraryController) {
                ((LibraryController) controller).initializeWithData(playlist);
            }*/
            // Add similar blocks for other controllers

            mainBorderPane.setCenter(view);
            currentView = view;

        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to home view
            showHomeView();
            setActiveButton(homeButton);
        }
    }

    private void loadViewLib(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));  // <-- Use the parameter
            ScrollPane view = loader.load();
            mainBorderPane.setCenter(view);

            view.prefWidthProperty().bind(mainBorderPane.widthProperty());
            view.prefHeightProperty().bind(mainBorderPane.heightProperty());

        } catch (IOException e) {
            e.printStackTrace();
            showHomeView();
            setActiveButton(homeButton);
        }
    }

    private void setActiveButton(Button activeButton) {
        Button[] navButtons = {homeButton, libraryButton, queueButton, chartsButton, profileButton};

        for (Button button : navButtons) {
            if (button == activeButton) {
                button.getStyleClass().removeAll("leftMainButton", "creamyText");
                button.getStyleClass().addAll("leftMainButtonSelected", "selectedText");
            } else {
                button.getStyleClass().removeAll("leftMainButtonSelected", "selectedText");
                button.getStyleClass().addAll("leftMainButton", "creamyText");
            }
        }
    }

    private void showHomeView() {
        mainBorderPane.setCenter(initialCenterContent);
        currentView = initialCenterContent;
    }

    @FXML
    private void loadLibraryView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/final13/library-view.fxml"));
            Parent libraryView = loader.load();
            mainBorderPane.setCenter(libraryView);

            // Get controller if needed
            LibraryController libraryController = loader.getController();
            // libraryController.setMainController(this);

        } catch (IOException e) {
            e.printStackTrace();
            // Optionally revert to initial content on error
            mainBorderPane.setCenter(initialCenterContent);
        }

    }

    // Add method to restore original view if needed
    public void restoreInitialView() {
        mainBorderPane.setCenter(initialCenterContent);
    }

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
            playlist.addAll(selectedFiles);

            if (isShuffleEnabled) {
                shuffledPlaylist = new ArrayList<>(playlist);
                Collections.shuffle(shuffledPlaylist);
            }

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

    private void bindSliderFill(Slider slider) {
        // Listener to update the slider fill color
        ChangeListener<Number> listener = (obs, oldVal, newVal) -> {
            double percentage = (newVal.doubleValue() - slider.getMin()) /
                    (slider.getMax() - slider.getMin()) * 100;

            // Use Platform.runLater to ensure this runs after the UI is fully loaded
            Platform.runLater(() -> {
                Node track = slider.lookup(".track");
                if (track != null) {
                    track.setStyle(
                            "-fx-background-color: linear-gradient(to right, " +
                                    "#89CFF0 0%, " +
                                    "#89CFF0 " + percentage + "%, " +
                                    "#555 " + percentage + "%, " +
                                    "#555 100%);"
                    );
                }
            });
        };

        // Apply initial styling after the UI is ready
        Platform.runLater(() -> {
            slider.applyCss();  // Ensure the CSS is applied and the track node exists
            listener.changed(null, null, slider.getValue());  // Apply the initial value
        });

        // Add listener to handle future changes
        slider.valueProperty().addListener(listener);
    }

    private void setupMediaPlayer() {
        MediaPlayer player = MusicPlayerManager.getMediaPlayer();
        playPauseImage.setImage(pauseImg);

        timeSlider.setDisable(false);

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
                playNext();
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

        List<File> currentPlaylist = isShuffleEnabled ? shuffledPlaylist : playlist;

        if (currentTrackIndex < currentPlaylist.size() - 1) {
            currentTrackIndex++;
        } else if (isLoopEnabled) {
            currentTrackIndex = 0;
            if (isShuffleEnabled) {
                // Reshuffle when looping back
                Collections.shuffle(shuffledPlaylist);
            }
        } else {
            System.out.println("End of playlist reached");
            return;
        }

        currentlyPlayingFile = currentPlaylist.get(currentTrackIndex);
        playFile(currentlyPlayingFile);
    }

    @FXML
    private void playPrevious() {
        MediaPlayer player = MusicPlayerManager.getMediaPlayer();

        if (player != null && player.getCurrentTime().toSeconds() > 10) {
            player.seek(Duration.ZERO);
            return;
        }

        List<File> currentPlaylist = isShuffleEnabled ? shuffledPlaylist : playlist;

        if (currentTrackIndex > 0) {
            currentTrackIndex--;
            currentlyPlayingFile = currentPlaylist.get(currentTrackIndex);
            playFile(currentlyPlayingFile);
        } else if (isLoopEnabled) {
            currentTrackIndex = currentPlaylist.size() - 1;
            currentlyPlayingFile = currentPlaylist.get(currentTrackIndex);
            playFile(currentlyPlayingFile);
        }
    }

    /*
    extra fallbacks

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

    */


    //spremeni ui
    private void updateNowPlayingUI() {
        Media media = MusicPlayerManager.getCurrentMedia();
        if (media != null) {
            ObservableMap<String, Object> metadata = media.getMetadata();

            // Set fallback values first
            String fallbackTitle = getFallbackTitle(media);
            Image fallbackImage = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/music-note.png")).toExternalForm());

            // Update UI with fallbacks initially
            if (titleLabel != null) titleLabel.setText(fallbackTitle);
            if (albumArtImageView != null) albumArtImageView.setImage(fallbackImage);

            // Check if metadata is available
            if (!metadata.isEmpty()) {
                // If metadata is available, update UI with it
                updateUIFromMetadata(metadata);
            }

            // Listen for future metadata changes
            metadata.addListener((MapChangeListener<String, Object>) change -> {
                if (change.wasAdded()) {
                    // Check if metadata exists and update UI
                    updateUIFromMetadata(metadata);
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


    private void updateUIFromMetadata(ObservableMap<String, Object> metadata) {
        Object title = metadata.get("title");
        Object artist = metadata.get("artist");
        Object album = metadata.get("album");
        Object image = metadata.get("image");

        // Only update if metadata exists and is not empty
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
    private void toggleLoop() {
        isLoopEnabled = !isLoopEnabled;
        updateLoopButtonAppearance();
    }

    private void updateLoopButtonAppearance() {
        if (isLoopEnabled) {
            loopImg.setImage(loopEnabled);
        } else {
            loopImg.setImage(loopDisabled);
        }
    }

    @FXML
    private void toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled;

        // Update appearance immediately
        updateShuffleButtonAppearance();

        if (isShuffleEnabled) {
            // Create shuffled playlist while keeping current track
            shuffledPlaylist = new ArrayList<>(playlist);
            Collections.shuffle(shuffledPlaylist);

            // If a track is currently playing, move it to the front
            if (currentlyPlayingFile != null && shuffledPlaylist.contains(currentlyPlayingFile)) {
                shuffledPlaylist.remove(currentlyPlayingFile);
                shuffledPlaylist.add(0, currentlyPlayingFile);
                currentTrackIndex = 0;
            }
        }
    }

    private void updateShuffleButtonAppearance() {
        if (isShuffleEnabled) {
            shuffleImg.setImage(shuffleEnabled);
        } else {
            shuffleImg.setImage(shuffleDisabled);
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
