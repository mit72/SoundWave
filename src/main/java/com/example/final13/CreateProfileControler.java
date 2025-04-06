package com.example.final13;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.*;
import java.io.IOException;


public class CreateProfileControler {



    @FXML private Button backButton;
    @FXML private Button CreateButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField1;
    @FXML private PasswordField passwordField2;

/*
    //run separate thread if needed in future
    @FXML
    private void createProfile() {
    new Thread(() -> {
        Connection connection = OracleConnection.getConnection();
        Statement statement = null;
        int usrID;
        String geslo;
        String stmnt;
        String usr;

        if (usernameField.getText().isEmpty() || passwordField1.getText().isEmpty() || passwordField2.getText().isEmpty()) {
            Platform.runLater(() -> showAlert("Error", "Missing information", "Please fill out every field.", AlertType.ERROR));
            return;
        }

        if (usernameField.getText().length() < 3) {
            Platform.runLater(() -> showAlert("Error", "Username length not long enough", "Username has to be longer than 3 letters.", AlertType.ERROR));
            return;
        }

        if (!passwordCheck(passwordField1.getText())) {
            Platform.runLater(() -> showAlert("Error", "Password too weak", "Please use a letter, number and a special symbol in your password with the length of 8 characters or more.", AlertType.ERROR));
            return;
        }

        if (!passwordField1.getText().equals(passwordField2.getText())) {
            Platform.runLater(() -> showAlert("Error", "Passwords do not match", "Please make sure both passwords are the same.", AlertType.ERROR));
            return;
        }

        try {
            geslo = StaticVars.getMd5(passwordField1.getText());
            usrID = getID();
            usr = usernameField.getText();

            stmnt = "INSERT INTO mat_users (id, username, password) VALUES (" + usrID + ", '" + usr + "', '" + geslo + "')";
            assert connection != null;
            statement = connection.createStatement();

            connection.setAutoCommit(false);
            statement.execute(stmnt);
            connection.commit();

            Platform.runLater(() -> showAlert("Success", "Success", "Successfully created a profile", AlertType.CONFIRMATION));

        } catch (Exception e) {
            Platform.runLater(() -> showAlert("Error", "Database Error", "There was an unexpected database error", AlertType.ERROR));
        } finally {
            try {
                assert statement != null;
                statement.close();
                connection.close();
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Very Wrong", "Very Wrong", "Very Wrong", AlertType.WARNING));
            }
            Platform.runLater(this::clearFields);
        }
    }).start();
} */

    @FXML
    private void createProfile() {

        Connection connection = OracleConnection.getConnection();
        Statement statement = null;
        int usrID;
        String geslo;
        String stmnt;
        String usr;


        if (usernameField.getText().isEmpty() || passwordField1.getText().isEmpty() || passwordField2.getText().isEmpty()) {
            showAlert("Error", "Missing information", "Please fill out every field.", AlertType.ERROR);
            return;
        }

        if(usernameField.getText().length() < 3){
            showAlert("Error", "Username length not long enough", "Username has to be longer than 3 letters.", AlertType.ERROR);
            return;
        }

        if(!passwordCheck(passwordField1.getText())){
            showAlert("Error","Password too weak", "Please use a letter, number and a special symbol in your password with the length of 8 characters or more.",AlertType.ERROR);
            return;
        }

        if (!passwordField1.getText().equals(passwordField2.getText())) {
            showAlert("Error", "Passwords do not match", "Please make sure both passwords are the same.", AlertType.ERROR);
            return;
        }

        try{
            geslo = StaticVars.getMd5(passwordField1.getText());
            usrID = getID();
            usr = usernameField.getText();

            stmnt = "INSERT INTO mat_users (id, username, password) VALUES ("+usrID+", '"+usr+"', '"+geslo+"')";
            assert connection != null;
            statement = connection.createStatement();

            connection.setAutoCommit(false);

            statement.execute(stmnt);

            connection.commit();

        } catch (Exception e){
            showAlert("Error","Database Error", "There was an unexpected database error",AlertType.ERROR);


        } finally {
            try{
                assert statement != null;
                statement.close();
                connection.close();
            } catch (Exception e){
                showAlert("very wrong","very wrong","very wrong",AlertType.WARNING);
            }
            showAlert("Success","Success","Successfully created a profile",AlertType.CONFIRMATION);
            clearFields();
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


    //dobi id
    private int getID() throws Exception {
        final Connection connection = OracleConnection.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        int id = 0;
        assert connection != null;
        stmt = connection.createStatement();
        rs = stmt.executeQuery("SELECT max(id) FROM mat_users");
        if (rs.next()) {
            id = rs.getInt(1);
        }
        connection.close();
        return id + 1;
    }

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
