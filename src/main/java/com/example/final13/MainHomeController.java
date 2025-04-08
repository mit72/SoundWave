package com.example.final13;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;


public class MainHomeController {



    //window resizing and main structure
    @FXML private Button fileSelect;
    @FXML private Button LogOut;
    @FXML private Button exitButton;
    @FXML private Button minimizeButton;
    @FXML private Button windowedModeButton;
    @FXML private HBox titleBar;
    @FXML private VBox centerContainer;
    @FXML private AnchorPane windowButtonsAnchor;

    private Stage stage;

    private final Border border = new Border();

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    private void loadMedia(File audioFile) {
        if (mediaPlayer != null) {
            mediaPlayer.dispose(); // stop previous media
        }

        Media media = new Media(audioFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
    }

    @FXML
    private void switchToSignIn(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sign-in.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

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

    @FXML
    private void handleChooseAudio(ActionEvent event) {
        AudioFileHandler handler = new AudioFileHandler();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        handler.openAudioFileChooser(stage);

        File selected = handler.getSelectedAudioFile();
        if (selected != null) {
            // Save it, or set it up for playback later
        }
    }

    @FXML
    private void handlePlayPause() {
        if (mediaPlayer == null) return;

        if (isPlaying) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }

        isPlaying = !isPlaying;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        border.setStage(stage, titleBar); //  Delegate all behavior to your Border class
    }

    @FXML
    public void closeWindow() {
        stage.close();
    }

    @FXML
    private void minimizeWindow() {
        stage.setIconified(true);
    }

    @FXML
    private void toggleMaximize() {
        stage.setMaximized(!stage.isMaximized());
    }



}
