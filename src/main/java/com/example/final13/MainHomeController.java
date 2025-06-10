package com.example.final13;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.util.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainHomeController {

    // window resizing and main structure
    @FXML private Button fileSelect;
    @FXML private Button LogOut;
    @FXML private Button exitButton;
    @FXML private Button minimizeButton;
    @FXML private Button windowedModeButton;
    @FXML private HBox titleBar;
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
    @FXML BorderPane mainBorderPane;
    @FXML VBox centerContainer;
    @FXML Button folderSelect;
    @FXML private Button addFilesButton;

    @FXML private TableView<SongInfo> songTable;
    @FXML private TableColumn<SongInfo, String> titleColumn;
    @FXML private TableColumn<SongInfo, String> artistColumn;
    @FXML private TableColumn<SongInfo, String> albumColumn;
    @FXML private TableColumn<SongInfo, String> durationColumn;

    private final ObservableList<SongInfo> songData = FXCollections.observableArrayList();

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
    List<File> playlist = new ArrayList<>();
    private int currentTrackIndex = -1;
    private File currentlyPlayingFile;
    private double currentVolume = 0.5;
    private boolean isLoopEnabled = false;
    boolean isShuffleEnabled = false;
    List<File> shuffledPlaylist = new ArrayList<>();
    private Parent initialCenterContent;
    private Parent currentView;
    private QueueController queueController;
    private Timeline playbackLogger;
    private Duration playbackDuration = Duration.ZERO;
    private boolean hasLoggedCurrentTrack = false;
    private Timeline playbackTimer;
    private String currentlyPlayingTrackId = "";
    private boolean log = true;

    private int currentUserId = -1; // This should come from your login system

    public void setCurrentUserId(int id) {
        this.currentUserId = id;
    }

    public void setLog(boolean log){
        this.log = log;
    }

    public boolean getLog(){
        return log;
    }

    public void setLoggingEnabled(boolean enabled) {
        this.log = enabled;
    }

    @FXML
    public void initialize() {
        // Store initial center content
        initialCenterContent = (Parent) mainBorderPane.getCenter();
        restoreInitialView();
        bindSliderFill(timeSlider);
        bindSliderFill(audioSlider);
        timeSlider.setDisable(true);

        // Set button actions
        setupNavigationButtons();

        // Set home as default selected
        setActiveButton(homeButton);

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        songTable.setItems(songData);
        songTable.getColumns().forEach(column -> column.setReorderable(false));

        songTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            double tableWidth = newVal.doubleValue();
            titleColumn.setPrefWidth(tableWidth * 0.40);    // 40%
            artistColumn.setPrefWidth(tableWidth * 0.25);   // 25%
            albumColumn.setPrefWidth(tableWidth * 0.25);    // 25%
            durationColumn.setPrefWidth(tableWidth * 0.10); // 10%
        });

        String appDataPath = System.getenv("LOCALAPPDATA");
        if (appDataPath != null) {
            File userInfoFile = new File(appDataPath, "SoundWave/userinfo.properties");
            if (userInfoFile.exists()) {
                try (FileInputStream in = new FileInputStream(userInfoFile)) {
                    Properties props = new Properties();
                    props.load(in);
                    String musicFolderPath = props.getProperty("MusicFolder");
                    if (musicFolderPath != null && !musicFolderPath.isEmpty()) {
                        File musicFolder = new File(musicFolderPath);
                        if (musicFolder.exists() && musicFolder.isDirectory()) {
                            loadMusicFromFolder(musicFolder);
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        File defaultMusicFolder = new File(System.getProperty("user.home") + "/Music");

        // If the folder exists and is a directory, load it
        if (defaultMusicFolder.exists() && defaultMusicFolder.isDirectory()) {
            loadMusicFromFolder(defaultMusicFolder);
        }

        songTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SongInfo selected = songTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    File file = new File(selected.getPath());

                    // Update currentTrackIndex
                    for (int i = 0; i < playlist.size(); i++) {
                        if (playlist.get(i).equals(file)) {
                            currentTrackIndex = i;
                            break;
                        }
                    }

                    playFile(file);
                }
            }
        });

    }

    private void loadMusicFromFolder(File folder) {
        // Use a Task to load the files in the background (same as before)
        Task<Void> loadFilesTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                File[] files = folder.listFiles(f -> f.getName().endsWith(".mp3"));
                if (files != null) {
                    playlist.clear();
                    songData.clear();

                    // Add all files to the playlist
                    for (File f : files) {
                        playlist.add(f);
                        SongInfo songInfo = extractMetadata(f);
                        songData.add(songInfo);
                    }
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
            }

            @Override
            protected void failed() {
                super.failed();
                //e.printStackTrace();
            }
        };

        Thread thread = new Thread(loadFilesTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void setupNavigationButtons() {
        homeButton.setOnAction(event -> {
            mainBorderPane.setCenter(initialCenterContent);
            setActiveButton(homeButton);
        });

        queueButton.setOnAction(e -> {
            setActiveButton(queueButton);
            loadViewQue("/com/example/final13/queue-view.fxml");
        });

        chartsButton.setOnAction(e -> {
            setActiveButton(chartsButton);
            loadViewChart("/com/example/final13/chart-view.fxml");
        });

        profileButton.setOnAction(e -> {
            setActiveButton(profileButton);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/final13/profile-view.fxml"));
                Parent view = loader.load();

                ProfileController profileController = loader.getController();
                profileController.setCurrentUser(currentUserId, "");
                profileController.setMainController(this);

                mainBorderPane.setCenter(view);
            } catch (IOException ex) {
                showHomeView();
                setActiveButton(homeButton);
            }
        });
    }

    private void loadViewQue(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Get the controller and set it up
            queueController = loader.getController();
            queueController.setMainController(this);
            updateQueueView(); // Initialize the queue view

            mainBorderPane.setCenter(view);
            currentView = view;

        } catch (IOException e) {
            //e.printStackTrace();
            showHomeView();
            setActiveButton(homeButton);
        }
    }

    public void playSelectedFromQueue(SongInfo selected) {
        File file = new File(selected.getPath());

        // Find the index of the selected song in the playlist
        for (int i = 0; i < playlist.size(); i++) {
            if (playlist.get(i).equals(file)) {
                currentTrackIndex = i;
                playFile(file);
                break;
            }
        }
    }

    private void updateQueueView() {
        if (queueController != null) {
            queueController.updateQueue(songData, currentTrackIndex, isLoopEnabled);
        }
    }

    @FXML
    private void loadViewChart(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // If loading charts view, set the user ID
            if (fxmlPath.equals("/com/example/final13/chart-view.fxml")) {
                ChartController controller = loader.getController();
                controller.setCurrentUserId(currentUserId);
            }

            mainBorderPane.setCenter(view);
            currentView = view;
        } catch (IOException e) {
            e.printStackTrace();
            showHomeView();
            setActiveButton(homeButton);
        }
    }

    private void setActiveButton(Button activeButton) {
        Button[] navButtons = {homeButton,  queueButton, chartsButton, profileButton}; /* libraryButton*/

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

    // Add method to restore original view if needed
    public void restoreInitialView() {
        mainBorderPane.setCenter(initialCenterContent);
    }

    @FXML
    private void handleFileSelect(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Music File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            // Clear existing songs
            playlist.clear();
            songData.clear();

            // Load files asynchronously
            loadFilesAsync(selectedFiles, true);
        }
    }

    // New method to add files without clearing
    @FXML
    private void handleAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Music Files");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            // Load files asynchronously without clearing
            loadFilesAsync(selectedFiles, false);
        }
    }

    //now thread ka ce ne freeza
    private void loadFilesAsync(List<File> files, boolean clearBeforeAdd) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (clearBeforeAdd) {
                    Platform.runLater(() -> {
                        playlist.clear();
                        songData.clear();
                    });
                }

                for (File file : files) {
                    if (isCancelled()) break;

                    SongInfo songInfo = extractMetadata(file);
                    Platform.runLater(() -> {
                        playlist.add(file);
                        songData.add(songInfo);
                    });
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            if (isShuffleEnabled) {
                shuffledPlaylist = new ArrayList<>(playlist);
                Collections.shuffle(shuffledPlaylist);
            }

            if (currentTrackIndex == -1 && !playlist.isEmpty()) {
                currentTrackIndex = 0;
                playCurrentTrack();
            }

            updateQueueView();
        });

        task.setOnFailed(e -> {
            // Handle error
            System.err.println("Error loading files: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }


    @FXML
    public void handleFolderSelect() {
        DirectoryChooser chooser = new DirectoryChooser();
        File dir = chooser.showDialog(stage);
        if (dir != null && dir.isDirectory()) {
            // Use a Task to load the files in the background
            Task<Void> loadFilesTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // Get all mp3 files in the directory
                    File[] files = dir.listFiles(f -> f.getName().endsWith(".mp3"));
                    if (files != null) {
                        // Clear the playlist and song data
                        playlist.clear();
                        songData.clear();

                        // Add all the files to the playlist
                        for (File f : files) {
                            playlist.add(f);
                            // You might want to add metadata extraction here
                            SongInfo songInfo = extractMetadata(f);
                            songData.add(songInfo);
                        }
                    }
                    return null;
                }

                // After loading files, update the UI
                @Override
                protected void succeeded() {
                    super.succeeded();


                    // Auto-play the first song if none is playing
                    if (currentTrackIndex == -1) {
                        currentTrackIndex = 0;
                        playCurrentTrack();
                    }
                }

                @Override
                protected void failed() {
                    super.failed();
                    // Handle any errors that occurred during loading
                    //e.printStackTrace();
                }
            };

            // Start the background task
            Thread thread = new Thread(loadFilesTask);
            thread.setDaemon(true); // Allow the thread to exit when the application exits
            thread.start();
        }
    }


    private SongInfo extractMetadata(File file) {
        String fileName = file.getName();
        String baseName = fileName.replaceFirst("[.][^.]+$", "");

        // Create arrays to hold values (arrays are effectively final)
        final String[] metadata = {
                baseName,            // title
                "Unknown Artist",    // artist
                "Unknown Album",     // album
                "Unknown"            // duration
        };

        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer tempPlayer = new MediaPlayer(media);
            CountDownLatch latch = new CountDownLatch(1);

            tempPlayer.setOnReady(() -> {
                try {
                    // Title extraction
                    if (media.getMetadata().containsKey("title")) {
                        metadata[0] = media.getMetadata().get("title").toString();
                    } else if (media.getMetadata().containsKey("dc:title")) {
                        metadata[0] = media.getMetadata().get("dc:title").toString();
                    }

                    // Artist extraction
                    if (media.getMetadata().containsKey("artist")) {
                        metadata[1] = media.getMetadata().get("artist").toString();
                    } else if (media.getMetadata().containsKey("author")) {
                        metadata[1] = media.getMetadata().get("author").toString();
                    } else if (media.getMetadata().containsKey("dc:creator")) {
                        metadata[1] = media.getMetadata().get("dc:creator").toString();
                    }

                    // Album extraction
                    if (media.getMetadata().containsKey("album")) {
                        metadata[2] = media.getMetadata().get("album").toString();
                    } else if (media.getMetadata().containsKey("dc:album")) {
                        metadata[2] = media.getMetadata().get("dc:album").toString();
                    }

                    // Duration formatting
                    if (media.getDuration() != null && !media.getDuration().isUnknown()) {
                        metadata[3] = formatTime(media.getDuration());
                    }
                } finally {
                    latch.countDown();
                }
            });

            // Start playback briefly to trigger metadata loading
            tempPlayer.play();
            tempPlayer.stop();

            // Wait for metadata with timeout
            if (!latch.await(3, TimeUnit.SECONDS)) {
                System.out.println("Timeout waiting for metadata: " + fileName);
            }

            tempPlayer.dispose();
        } catch (Exception e) {
            System.err.println("Error extracting metadata for: " + fileName);
            e.printStackTrace();
        }

        return new SongInfo(metadata[0], metadata[1], metadata[2], metadata[3], file.getAbsolutePath());
    }

    private void playCurrentTrack() {
        if (currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
            File fileToPlay = playlist.get(currentTrackIndex);
            playFile(fileToPlay);
        }
    }

    void playFile(File file) {
        // Reset tracking for new track
        resetPlaybackTracking();

        currentlyPlayingFile = file;
        currentlyPlayingTrackId = file.getAbsolutePath(); // Unique identifier for the track
        MusicPlayerManager.playFile(file);
        setupMediaPlayer();
        updateQueueView();

        // Reset the logged flag when a new file starts playing
        hasLoggedCurrentTrack = false;

        // Start playback tracking
        startPlaybackTracking();

        // Set audio properties
        MediaPlayer player = MusicPlayerManager.getMediaPlayer();
        if (player != null) {
            player.setVolume(currentVolume);
            audioSlider.setValue(currentVolume);
        }
    }

    private void logCurrentTrack() {
        if (!log || currentlyPlayingFile == null || hasLoggedCurrentTrack) return;


        // Create task for metadata extraction and logging
        Task<Void> loggingTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                SongInfo currentInfo = extractMetadata(currentlyPlayingFile);

                // Use filename as title if metadata is missing
                String titleToLog = currentInfo.getTitle();
                if (titleToLog.startsWith("Unknown") || titleToLog.isEmpty()) {
                    titleToLog = currentlyPlayingFile.getName()
                            .replaceFirst("[.][^.]+$", ""); // Remove file extension
                }

                TrackLogger.logTrack(
                        titleToLog,
                        currentInfo.getArtist(),
                        currentInfo.getAlbum(),
                        currentUserId
                );
                return null;
            }
        };

        new Thread(loggingTask).start();
        hasLoggedCurrentTrack = true;
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
        resetPlaybackTracking();

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
                resetPlaybackTracking();
                playNext();
            });

            if (media != null) {
                media.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
                    if (change.wasAdded()) {
                        logCurrentTrack();
                    }
                });
            }

            player.setOnPaused(() -> {
                if (playbackTimer != null) {
                    playbackTimer.pause();
                }
            });

            player.setOnPlaying(() -> {
                if (playbackTimer != null) {
                    playbackTimer.play();
                } else {
                    startPlaybackTracking();
                }
            });

            player.setOnStopped(() -> {
                resetPlaybackTracking();
            });

            player.setOnEndOfMedia(() -> {
                resetPlaybackTracking();
                playNext();
            });
        }
    }

    private void startPlaybackTracking() {
        playbackDuration = Duration.ZERO;
        hasLoggedCurrentTrack = false;

        playbackTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    playbackDuration = playbackDuration.add(Duration.seconds(1));

                    // Log after 10 seconds of continuous playback
                    if (playbackDuration.greaterThanOrEqualTo(Duration.seconds(10)) && !hasLoggedCurrentTrack) {
                        logCurrentTrack();
                        hasLoggedCurrentTrack = true;
                    }
                })
        );
        playbackTimer.setCycleCount(Timeline.INDEFINITE);
        playbackTimer.play();
    }

    private void stopPlaybackTracking() {
        if (playbackLogger != null) {
            playbackLogger.stop();
        }
    }

    private void resetPlaybackTracking() {
        if (playbackTimer != null) {
            playbackTimer.stop();
        }
        playbackDuration = Duration.ZERO;
        hasLoggedCurrentTrack = false;
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
            // Only reshuffle if we're at the end and shuffle is enabled
            if (isShuffleEnabled) {
                // Instead of reshuffling, just start from the beginning of the current shuffled playlist
                // This maintains the same shuffle order until manually reshuffled
                currentTrackIndex = 0;
            }
        } else {
            System.out.println("End of playlist reached");
            return;
        }

        currentlyPlayingFile = currentPlaylist.get(currentTrackIndex);
        playFile(currentlyPlayingFile);
        updateQueueView();
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
        updateQueueView();
    }


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
        updateQueueView(); // Add this line
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
        updateShuffleButtonAppearance();

        if (isShuffleEnabled) {
            // Create a new shuffled playlist
            shuffledPlaylist = new ArrayList<>(playlist);
            Collections.shuffle(shuffledPlaylist);

            // If a song is currently playing, move it to the front
            if (currentlyPlayingFile != null) {
                // Find the current song in the shuffled playlist
                for (int i = 0; i < shuffledPlaylist.size(); i++) {
                    if (shuffledPlaylist.get(i).equals(currentlyPlayingFile)) {
                        // Move it to the front and update currentTrackIndex
                        File current = shuffledPlaylist.remove(i);
                        shuffledPlaylist.add(0, current);
                        currentTrackIndex = 0;
                        break;
                    }
                }
            }
        } else {
            // When turning off shuffle, find the current song in the original playlist
            if (currentlyPlayingFile != null) {
                for (int i = 0; i < playlist.size(); i++) {
                    if (playlist.get(i).equals(currentlyPlayingFile)) {
                        currentTrackIndex = i;
                        break;
                    }
                }
            }
        }

        updateQueueView();
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
                stopPlaybackTracking();
                break;
            case PAUSED:
            case READY:
            case STOPPED:
                mediaPlayer.play();
                playPauseImage.setImage(pauseImg);
                startPlaybackTracking();
                break;
            default:
                System.out.println("MediaPlayer status: " + mediaPlayer.getStatus());
        }
    }

    void disposeMediaPlayer() {
        resetPlaybackTracking();
        MediaPlayer mediaPlayer = MusicPlayerManager.getMediaPlayer();
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }

    @FXML
    private void switchToSignIn(ActionEvent event) throws IOException {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Sign Out");
        confirmAlert.setHeaderText("Sign Out");
        confirmAlert.setContentText("You will be signed out of your profile and will need to log in again");

        ButtonType result = confirmAlert.showAndWait().orElse(ButtonType.CANCEL);

        if (result == ButtonType.OK) {
            // Clean up media player
            disposeMediaPlayer();

            // Clear user info
            String appDataPath = System.getenv("LOCALAPPDATA");
            if (appDataPath != null) {
                File file = new File(appDataPath, "SoundWave/userinfo.properties");
                if (file.exists()) {
                    file.delete();
                }
            }

            // Load the new scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Store window state
            boolean wasMaximized = stage.isMaximized();
            double width = stage.getWidth();
            double height = stage.getHeight();

            // Create new scene
            Scene newScene = new Scene(root);
            stage.setScene(newScene);

            // Restore window state
            if (wasMaximized) {
                stage.setMaximized(true);
            } else {
                stage.setWidth(width);
                stage.setHeight(height);
            }

            HelloController controller = loader.getController();
            controller.setStage(stage);
        }
    }



    public void setStage(Stage stage) {
        this.stage = stage;
        // Don't try to access scene here - it's not set yet
    }

    public void initializeStage() {
        if (stage != null && stage.getScene() != null) {
            HBox titleBar = (HBox) stage.getScene().lookup("#titleBar");
            if (titleBar != null) {
                border.setStage(stage, titleBar);
            }
        }
    }

    @FXML public void closeWindow() { stage.close(); }

    @FXML private void minimizeWindow() { stage.setIconified(true); }

    @FXML private void toggleMaximize() { stage.setMaximized(!stage.isMaximized()); }


    //debuging method
    public void printAllMetadata(File file) {
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer tempPlayer = new MediaPlayer(media);

            tempPlayer.setOnReady(() -> {
                System.out.println("\nMetadata for: " + file.getName());
                media.getMetadata().forEach((key, value) -> {
                    System.out.println(key + ": " + value);
                });
                tempPlayer.dispose();
            });

            tempPlayer.play();
            tempPlayer.stop();
        } catch (Exception e) {
            System.err.println("Error printing metadata: " + e.getMessage());
        }
    }
}