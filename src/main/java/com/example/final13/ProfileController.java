package com.example.final13;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.prefs.Preferences;

public class ProfileController {
    @FXML private ToggleButton toggleSwitch;
    @FXML private ToggleButton togglePrivateSession;
    @FXML private Button changeDefaultFolder;
    @FXML private Button changeUsername;
    @FXML private Button changePassword;
    @FXML private Button signOut;
    @FXML private Button deleteProfile;
    @FXML private Label usernameLabel;

    private int currentUserId;
    private String currentUsername;
    private Preferences prefs = Preferences.userNodeForPackage(ProfileController.class);
    private MainHomeController mainController;
    private ChartController chartController;

    public void setChartController(ChartController controller) {
        this.chartController = controller;
    }

    public void setMainController(MainHomeController controller) {
        this.mainController = controller;
        // Sync the toggle button with the current log state when controller is set
        if (mainController != null) {
            boolean isLoggingEnabled = mainController.getLog();
            togglePrivateSession.setSelected(!isLoggingEnabled); // Invert since private session means no logging
            updateToggleButtonText(togglePrivateSession);
        }
    }

    public void initialize() {
        // Load saved preferences
        toggleSwitch.setSelected(prefs.getBoolean("showNoMetadata", false));

        // Initialize private session toggle from preferences but override if mainController is set
        boolean privateSessionFromPrefs = prefs.getBoolean("privateSession", false);
        togglePrivateSession.setSelected(privateSessionFromPrefs);

        // If mainController is already set, sync with its log state
        if (mainController != null) {
            boolean isLoggingEnabled = mainController.getLog();
            togglePrivateSession.setSelected(!isLoggingEnabled);
            // Update preference to match
            prefs.putBoolean("privateSession", !isLoggingEnabled);
        }

        // Update toggle button text
        updateToggleButtonText(toggleSwitch);
        updateToggleButtonText(togglePrivateSession);

        // Add listeners
        toggleSwitch.selectedProperty().addListener((obs, oldVal, newVal) ->
                updateToggleButtonText(toggleSwitch));
        togglePrivateSession.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateToggleButtonText(togglePrivateSession);
            if (mainController != null) {
                mainController.setLog(!newVal);
            }
            prefs.putBoolean("privateSession", newVal);
        });

    }

    public void setCurrentUser(int userId, String username) {
        this.currentUserId = userId;
        this.currentUsername = username;
        usernameLabel.setText(username);
    }

    private void updateToggleButtonText(ToggleButton button) {
        button.setText(button.isSelected() ? "I" : "O");
    }

    @FXML
    private void handleToggle() {
        boolean showNoMetadata = toggleSwitch.isSelected();
        prefs.putBoolean("showNoMetadata", showNoMetadata);

        // Trigger chart reload with new filter settings
        if (chartController != null) {
            chartController.reloadFilteredData();
        }
    }


    @FXML
    private void handleTogglePrivateSession() {
        boolean privateSession = togglePrivateSession.isSelected();
        prefs.putBoolean("privateSession", privateSession);

        // Update the logging status in MainHomeController
        if (mainController != null) {
            mainController.setLoggingEnabled(!privateSession); // Invert since private session means no logging
            mainController.setLog(!privateSession); // Also update the internal log flag
        }
    }

    @FXML
    private void handleChangeDefaultFolder(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Default Music Folder");

        // Set initial directory if previously selected
        String lastDir = prefs.get("musicFolder", System.getProperty("user.home"));
        directoryChooser.setInitialDirectory(new File(lastDir));

        File selectedDir = directoryChooser.showDialog(changeDefaultFolder.getScene().getWindow());
        if (selectedDir != null) {
            prefs.put("musicFolder", selectedDir.getAbsolutePath());
            updateUserInfoFile(selectedDir.getAbsolutePath());
            showAlert("Success", "Default folder updated",
                    "Music folder set to: " + selectedDir.getAbsolutePath(), Alert.AlertType.INFORMATION);
        }
    }

    private void updateUserInfoFile(String musicFolderPath) {
        String appDataPath = System.getenv("LOCALAPPDATA");
        if (appDataPath == null) {
            System.err.println("Cannot find LOCALAPPDATA path.");
            return;
        }

        File logFile = new File(appDataPath, "SoundWave/userinfo.properties");

        try {
            // Create directories if they don't exist
            logFile.getParentFile().mkdirs();

            // Read existing properties if file exists
            Properties props = new Properties();
            if (logFile.exists()) {
                try (FileInputStream in = new FileInputStream(logFile)) {
                    props.load(in);
                }
            }

            // Update the music folder path
            props.setProperty("MusicFolder", musicFolderPath);

            // Write all properties back to file
            try (FileOutputStream out = new FileOutputStream(logFile)) {
                props.store(out, "SoundWave User Info");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChangeUsername() {
        TextInputDialog dialog = new TextInputDialog(currentUsername);
        dialog.setTitle("Change Username");
        dialog.setHeaderText("Enter new username");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newUsername -> {
            if (!newUsername.trim().isEmpty() && !newUsername.equals(currentUsername)) {
                if (updateUsernameInDatabase(newUsername)) {
                    currentUsername = newUsername;
                    usernameLabel.setText(newUsername);
                    showAlert("Success", "Username Changed",
                            "Your username has been updated successfully", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Update Failed",
                            "Could not update username", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private boolean updateUsernameInDatabase(String newUsername) {
        // Return true if successful, false otherwise
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE mat_users SET username = ? WHERE id = ?")) {

            stmt.setString(1, newUsername);
            stmt.setInt(2, currentUserId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleChangePassword() {
        PasswordChangeDialog passwordDialog = new PasswordChangeDialog();
        Optional<PasswordChangeDialog.Result> result = passwordDialog.showAndWait();

        result.ifPresent(passwordResult -> {
            if (validateCurrentPassword(passwordResult.currentPassword())) {
                if (updatePasswordInDatabase(passwordResult.newPassword())) {
                    showAlert("Success", "Password Changed",
                            "Your password has been updated successfully", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Update Failed",
                            "Could not update password", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Incorrect Password",
                        "The current password you entered is incorrect", Alert.AlertType.ERROR);
            }
        });
    }

    private boolean validateCurrentPassword(String currentPassword) {
        // Implement current password validation against database
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT password FROM mat_users WHERE id = ?")) {

            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                String inputHash = StaticVars.getMd5(currentPassword);
                return storedHash.equals(inputHash);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updatePasswordInDatabase(String newPassword) {
        // Implement password update in database
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE mat_users SET password = ? WHERE id = ?")) {

            stmt.setString(1, StaticVars.getMd5(newPassword));
            stmt.setInt(2, currentUserId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleDeleteProfile() throws IOException {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Profile");
        confirmAlert.setHeaderText("Permanently delete your profile?");
        confirmAlert.setContentText("All your data will be deleted and cannot be recovered. THIS ACTION CANNOT BE UNDONE!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (deleteProfileFromDatabase()) {
                // Clear preferences and user info
                prefs.remove("musicFolder");
                prefs.remove("showNoMetadata");
                prefs.remove("privateSession");

                // Delete user info file
                deleteUserInfoFile();

                // Return to sign in screen
                switchToSignIn(new ActionEvent(deleteProfile, null));
            } else {
                showAlert("Error", "Deletion Failed",
                        "Could not delete profile", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean deleteProfileFromDatabase() {
        // Implement profile deletion from database
        try (Connection conn = OracleConnection.getConnection()) {
            // First delete streams
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM mat_streams WHERE users_fk = ?")) {
                stmt.setInt(1, currentUserId);
                stmt.executeUpdate();
            }

            // Then delete user
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM mat_users WHERE id = ?")) {
                stmt.setInt(1, currentUserId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteUserInfoFile() {
        String appDataPath = System.getenv("LOCALAPPDATA");
        if (appDataPath != null) {
            File file = new File(appDataPath, "SoundWave/userinfo.properties");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @FXML
    private void switchToSignIn(ActionEvent event) throws IOException {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Sign Out");
        confirmAlert.setHeaderText("Sign Out");
        confirmAlert.setContentText("You will be signed out of your profile and will need to log in again");

        ButtonType result = confirmAlert.showAndWait().orElse(ButtonType.CANCEL);

        if (result == ButtonType.OK) {

            if (mainController != null) {
                mainController.disposeMediaPlayer();
            }

            // Clear user info
            String appDataPath = System.getenv("LOCALAPPDATA");
            if (appDataPath != null) {
                File file = new File(appDataPath, "SoundWave/userinfo.properties");
                if (file.exists()) file.delete();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            double width = stage.getWidth();
            double height = stage.getHeight();

            stage.setScene(new Scene(root));
            if (wasMaximized) stage.setMaximized(true);
            else {
                stage.setWidth(width);
                stage.setHeight(height);
            }

            HelloController controller = loader.getController();
            controller.setStage(stage);
        }
    }


    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper class for password change dialog
    private static class PasswordChangeDialog extends Dialog<PasswordChangeDialog.Result> {
        private final PasswordField currentPasswordField = new PasswordField();
        private final PasswordField newPasswordField = new PasswordField();
        private final PasswordField confirmPasswordField = new PasswordField();

        public PasswordChangeDialog() {
            setTitle("Change Password");
            setHeaderText("Enter your current and new password");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(new Label("Current Password:"), 0, 0);
            grid.add(currentPasswordField, 1, 0);
            grid.add(new Label("New Password:"), 0, 1);
            grid.add(newPasswordField, 1, 1);
            grid.add(new Label("Confirm New Password:"), 0, 2);
            grid.add(confirmPasswordField, 1, 2);

            getDialogPane().setContent(grid);

            ButtonType changeButton = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(changeButton, ButtonType.CANCEL);

            setResultConverter(buttonType -> {
                if (buttonType == changeButton) {
                    return new Result(
                            currentPasswordField.getText(),
                            newPasswordField.getText(),
                            confirmPasswordField.getText()
                    );
                }
                return null;
            });
        }

        record Result(String currentPassword, String newPassword, String confirmPassword) {}
    }
}