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


public class MainHomeController {



    //window resizing and main structure
    @FXML private Button LogOut;
    @FXML private Button exitButton;
    @FXML private Button minimizeButton;
    @FXML private Button windowedModeButton;
    @FXML private HBox titleBar;
    @FXML private VBox centerContainer;
    @FXML private AnchorPane windowButtonsAnchor;

    private Stage stage;

    private final Border border = new Border();

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
