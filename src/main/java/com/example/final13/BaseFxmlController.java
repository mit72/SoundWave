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


public class BaseFxmlController {

    //spremeni stage in controler
    @FXML
    private void switchToHelloView(ActionEvent event) throws IOException {


        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Parent root = loader.load();

        // Dobi stage
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        // Spremeni controller
        HelloController controller = loader.getController();
        controller.setStage(stage);


        boolean isMaximized = stage.isMaximized();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();
        Scene newScene = new Scene(root, currentWidth, currentHeight);
        stage.setScene(newScene);
        if (isMaximized) {
            stage.setMaximized(true);
        }

        stage.show();
    }

    //window resizing and main structure
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
