package com.example.final13;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Properties userProps = loadUserInfo();

        FXMLLoader fxmlLoader;
        BorderPane root;

        if (userProps != null && "true".equalsIgnoreCase(userProps.getProperty("Remember"))) {
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-home.fxml"));
            root = fxmlLoader.load();

            MainHomeController mainController = fxmlLoader.getController();
            int userId = Integer.parseInt(userProps.getProperty("UserID"));
            mainController.setCurrentUserId(userId);
            mainController.setStage(stage);  // Set stage reference

            Scene scene = new Scene(root, 900, 500);
            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/icon.ico")));
            // Now initialize the stage components
            mainController.initializeStage();

        } else {
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            root = fxmlLoader.load();

            HelloController controller = fxmlLoader.getController();
            controller.setStage(stage);

            Scene scene = new Scene(root, 900, 500);
            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/icon.ico")));
        }

        stage.getIcons().add(
                new Image(Objects.requireNonNull(
                        HelloApplication.class.getResourceAsStream("/com/example/final13/img/icon.png"))
                )
        );


        stage.setMinWidth(900);
        stage.setMinHeight(500);
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
