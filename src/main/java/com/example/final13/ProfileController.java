package com.example.final13;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;

public class ProfileController {
    @FXML private ToggleButton toggleSwitch;
    @FXML private ToggleButton togglePrivateSession;

    public void initialize(){
        toggleSwitch.setText("O");
        togglePrivateSession.setText("O");
    }

    @FXML
    private void handleToggle() {
        if (toggleSwitch.isSelected()) {
            System.out.println("Switch ON");
            toggleSwitch.setText("O");
        } else {
            System.out.println("Switch OFF");
            toggleSwitch.setText("O");
        }
    }

    @FXML
    private void handleTogglePrivateSession() {
        if (togglePrivateSession.isSelected()) {
            System.out.println("Switch ON");
            togglePrivateSession.setText("O");
        } else {
            System.out.println("Switch OFF");
            togglePrivateSession.setText("O");
        }
    }

}
