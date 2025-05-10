package com.example.final13;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.Properties;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Properties userProps = loadUserInfo();

        FXMLLoader fxmlLoader;
        BorderPane root;

        if (userProps != null && "true".equalsIgnoreCase(userProps.getProperty("Remember"))) {
            // Load main view
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-home.fxml"));
            root = fxmlLoader.load();

            MainHomeController mainController = fxmlLoader.getController();
            int userId = Integer.parseInt(userProps.getProperty("UserID"));
            mainController.setCurrentUserId(userId);

            // ✅ Setup custom resizing & dragging
            HBox titleBar = (HBox) fxmlLoader.getNamespace().get("titleBar");
            Border border = new Border();
            border.setStage(stage, titleBar);

        } else {
            // Load login view
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            root = fxmlLoader.load();

            HelloController controller = fxmlLoader.getController();
            controller.setStage(stage);  // Already sets up Border
        }

        Scene scene = new Scene(root, 900, 500);
        stage.setMinWidth(900);
        stage.setMinHeight(500);
        stage.setScene(scene);
        stage.setTitle("SoundWave");
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        stage.show();
    }



    private Properties loadUserInfo() {
        String appDataPath = System.getenv("LOCALAPPDATA");
        if (appDataPath == null) return null;

        File file = new File(appDataPath, "SoundWave/userinfo.properties");
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file)) {
            Properties props = new Properties();
            props.load(reader);
            return props;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void clearUserInfo() {
        String appDataPath = System.getenv("LOCALAPPDATA");
        if (appDataPath == null) return;

        File file = new File(appDataPath, "SoundWave/userinfo.properties");
        if (file.exists()) {
            file.delete();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
