package com.example.final13;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    @FXML private Button createProfileButton;
    @FXML private Button signInButton;
    @FXML private Button exitButton;
    @FXML private Button minimizeButton;
    @FXML private Button windowedModeButton;
    @FXML private HBox titleBar;
    @FXML private VBox centerContainer;
    @FXML private AnchorPane windowButtonsAnchor;

    private Stage stage;

    // Border utility for dragging & resizing
    private final Border border = new Border();

    @FXML
    private void switchToCreateProfile(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("create-profile.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = stage.getScene();

        scene.setRoot(root);

        CreateProfileControler controller = loader.getController();
        controller.setStage(stage); // This controller should also use Border

        stage.show();
    }

    @FXML
    private void switchToSignIn(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sign-in.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = stage.getScene();

        scene.setRoot(root);

        SignInController controller = loader.getController();
        controller.setStage(stage); // This controller should also use Border

        stage.show();
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
