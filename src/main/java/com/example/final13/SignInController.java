package com.example.final13;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.*;
import java.io.IOException;


public class SignInController {



    @FXML private Button backButton;
    @FXML private Button CreateButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField1;
    @FXML private PasswordField passwordField2;
    @FXML private CheckBox rememberMe;

    private boolean selectedState;

    public void initialize(){
        rememberMe.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            selectedState = isNowSelected;
        });
    }

    private void logUserToFile(String username, String encryptedPassword, int userId) {
        String appDataPath = System.getenv("LOCALAPPDATA");
        if (appDataPath == null) {
            System.err.println("Cannot find LOCALAPPDATA path.");
            return;
        }

        File logFile = new File(appDataPath, "SoundWave/userinfo.properties");

        try {
            // Create directories if they don't exist
            logFile.getParentFile().mkdirs();

            // Overwrite file (not append)
            FileWriter writer = new FileWriter(logFile, false); // 'false' means overwrite
            writer.write("UserID=" + userId + "\n");
            writer.write("Username=" + username + "\n");
            writer.write("Password=" + encryptedPassword + "\n");
            writer.write("Remember=" + selectedState);
            System.out.println(selectedState);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void showAlert(String title, String header, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean passwordCheck(String pass){
        boolean letr = false,numbr = false,othr = false;
        if(pass.length() < 8)
            return false;
        for (int i = 0; i < pass.length(); i++) {
            char tmp = pass.charAt(i);
            if(tmp >= 'a' && tmp <= 'z'){
                letr = true;
            }
            else if(tmp >= '0' && tmp <= '9'){
                numbr = true;
            } else {
                othr = true;
            }
        }
        return letr && othr && numbr;
    }

    private void clearFields(){
        usernameField.setText("");
        passwordField1.setText("");
        passwordField2.setText("");
    }


    //spremeni stage in controler
    @FXML
    private void switchToHelloView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        boolean isMaximized = stage.isMaximized();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();
        Scene newScene = new Scene(root, currentWidth, currentHeight);
        stage.setScene(newScene);


        HelloController controller = loader.getController();
        controller.setStage(stage);

        if (isMaximized) {
            stage.setMaximized(true);
        }

        stage.show();
    }

    @FXML
    private void signIn(ActionEvent event) throws IOException {
        // Example: You'd normally verify credentials here and get the userId
        String username = usernameField.getText();
        String password = passwordField1.getText();
        String encryptedPassword = StaticVars.getMd5(password);

        int userId = getUserIdFromDatabase(username, encryptedPassword); // <-- You implement this

        if (userId == -1) {
            showAlert("Login Failed", "Invalid credentials", "Please try again.", Alert.AlertType.ERROR);
            return;
        }
        logUserToFile(username, encryptedPassword, userId);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-home.fxml"));
        Parent root = loader.load();

        MainHomeController controller = loader.getController();
        controller.setCurrentUserId(userId); // <-- Pass userId here

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        boolean isMaximized = stage.isMaximized();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();
        Scene newScene = new Scene(root, currentWidth, currentHeight);
        stage.setScene(newScene);
        controller.setStage(stage);
        if (isMaximized) stage.setMaximized(true);
        stage.show();
    }

    private int getUserIdFromDatabase(String username, String encryptedPassword) {
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id FROM mat_users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, encryptedPassword);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Not found
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
