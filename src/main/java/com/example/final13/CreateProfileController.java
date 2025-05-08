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

import java.sql.*;
import java.io.IOException;

public class CreateProfileController {
    @FXML private Button backButton;
    @FXML private Button CreateButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField1;
    @FXML private PasswordField passwordField2;
    @FXML private HBox titleBar;
    @FXML private VBox centerContainer;
    @FXML private AnchorPane windowButtonsAnchor;

    private Stage stage;
    private final Border border = new Border();

    @FXML
    private void createProfile() {
        // Validate inputs first
        if (!validateInputs()) {
            return;
        }

        // Use try-with-resources for automatic resource management
        try (Connection connection = OracleConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO mat_users (id, username, password) VALUES (?, ?, ?)")) {

            connection.setAutoCommit(false);

            // Set parameters safely using PreparedStatement
            statement.setInt(1, getNextId());
            statement.setString(2, usernameField.getText().trim());
            statement.setString(3, StaticVars.getMd5(passwordField1.getText()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 1) {
                connection.commit();
                showAlert("Success", "Profile Created",
                        "Your profile was created successfully!", Alert.AlertType.INFORMATION);
                clearFields();
                switchToSignIn();
            } else {
                connection.rollback();
                showAlert("Error", "Database Error",
                        "Failed to create profile", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Error", "Database Error",
                    "Username might already exist or database error occurred: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Unexpected Error",
                    "An unexpected error occurred: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private boolean validateInputs() {
        // Check for empty fields
        if (usernameField.getText().trim().isEmpty() ||
                passwordField1.getText().isEmpty() ||
                passwordField2.getText().isEmpty()) {
            showAlert("Error", "Missing Information",
                    "Please fill out all fields", Alert.AlertType.ERROR);
            return false;
        }

        // Check username length
        if (usernameField.getText().trim().length() < 3) {
            showAlert("Error", "Invalid Username",
                    "Username must be at least 3 characters long", Alert.AlertType.ERROR);
            return false;
        }

        // Check password strength
        if (!isPasswordValid(passwordField1.getText())) {
            showAlert("Error", "Weak Password",
                    "Password must contain at least 8 characters with letters, numbers and special characters",
                    Alert.AlertType.ERROR);
            return false;
        }

        // Check password match
        if (!passwordField1.getText().equals(passwordField2.getText())) {
            showAlert("Error", "Password Mismatch",
                    "Passwords do not match", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private boolean isPasswordValid(String password) {
        if (password.length() < 8) return false;

        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;

            if (hasLetter && hasDigit && hasSpecial) return true;
        }
        return false;
    }

    private int getNextId() throws SQLException {
        try (Connection connection = OracleConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT NVL(MAX(id), 0) + 1 FROM mat_users")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        usernameField.clear();
        passwordField1.clear();
        passwordField2.clear();
    }

    private void switchToSignIn() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sign-in.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) CreateButton.getScene().getWindow();
        stage.getScene().setRoot(root);

        SignInController controller = loader.getController();
        controller.setStage(stage);
    }

    @FXML
    private void switchToHelloView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
        stage.setScene(newScene);

        HelloController controller = loader.getController();
        controller.setStage(stage);

        if (stage.isMaximized()) {
            stage.setMaximized(true);
        }
    }

    // Window control methods
    public void setStage(Stage stage) {
        this.stage = stage;
        border.setStage(stage, titleBar);
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