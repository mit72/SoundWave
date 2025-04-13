package com.example.final13;

import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public class MainHomeController {

    // window resizing and main structure
    @FXML private Button fileSelect;
    @FXML private Button LogOut;
    @FXML private Button exitButton;
    @FXML private Button minimizeButton;
    @FXML private Button windowedModeButton;
    @FXML private HBox titleBar;
    @FXML private VBox centerContainer; // ⭐ used for swapping content
    @FXML private AnchorPane windowButtonsAnchor;
    @FXML private Button playPauseButton;
    @FXML private ImageView playPauseImage;

    // Optional: connect to your UI elements for metadata display
    @FXML private ImageView albumArtImageView;
    @FXML private Label titleLabel;
    @FXML private Label artistLabel;
    @FXML private Label albumLabel;

    private Stage stage;
    private final Border border = new Border();
    private final Image playImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/play.png")).toExternalForm());
    private final Image pauseImg = new Image(Objects.requireNonNull(getClass().getResource("/com/example/final13/img/pause.png")).toExternalForm());

    // ⭐ NEW: Select and play audio file
    @FXML
    private void handleFileSelect(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Music File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            MusicPlayerManager.playFile(selectedFile);
            playPauseImage.setImage(pauseImg);
            updateNowPlayingUI();
        }
    }

    // ⭐ NEW: Dynamically swap center content
    public void switchCenterView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            centerContainer.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //spremeni ui
    private void updateNowPlayingUI() {
        Media media = MusicPlayerManager.getCurrentMedia();
        if (media != null) {
            media.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
                if (change.wasAdded()) {
                    String key = change.getKey();
                    Object value = change.getValueAdded();
                    if ("title".equals(key) && titleLabel != null) {
                        if (value != null && !value.toString().isBlank()) {
                            titleLabel.setText(value.toString());
                        } else {
                            // Fallback: use filename
                            String fileName = Paths.get(media.getSource()).getFileName().toString();
                            titleLabel.setText(fileName);
                        }
                    } else if ("artist".equals(key) && artistLabel != null) {
                        artistLabel.setText(value.toString());
                    } else if ("image".equals(key) && albumArtImageView != null) {
                        albumArtImageView.setImage((Image) value);
                    } else if("album".equals(key) && albumLabel != null){
                        albumLabel.setText(value.toString());
                    }
                }
            });
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


    @FXML
    private void switchToSignIn(ActionEvent event) throws IOException {
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
