package com.example.final13;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;



public class HelloApplication extends Application {


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        BorderPane root = fxmlLoader.load();
        Scene scene = new Scene(root, 900, 500);
        stage.setMinWidth(900);
        stage.setMinHeight(500);

        stage.setScene(scene);
        stage.setTitle("Music Player");
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        //stage.setMaximized(true);

        HelloController controller = fxmlLoader.getController();
        controller.setStage(stage);


        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}