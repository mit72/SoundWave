package com.example.final13;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;

public class ProfileController {
    @FXML
    private ToggleButton toggleSwitch;

    @FXML
    private void handleToggle() {
        if (toggleSwitch.isSelected()) {
            System.out.println("Switch ON");
        } else {
            System.out.println("Switch OFF");
        }
    }

}
