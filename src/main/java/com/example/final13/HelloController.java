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
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class HelloController {

    @FXML private Button createProfileButton;

    @FXML
    private void switchToCreateProfile(ActionEvent event) throws IOException {


        FXMLLoader loader = new FXMLLoader(getClass().getResource("create-profile.fxml"));
        Parent root = loader.load();

        // Dobi stage
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        // Spremeni controller
        CreateProfileControler controller = loader.getController();
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
    private double xOffset = 0;
    private double yOffset = 0;
    private Stage stage;

    private double initialWidth;
    private double initialHeight;
    private double initialX;
    private double initialY;

    private static final int RESIZE_MARGIN = 5;

    @FXML private Button exitButton;
    @FXML private Button minimizeButton;
    @FXML private Button windowedModeButton;
    @FXML private HBox titleBar;
    @FXML private VBox centerContainer;
    @FXML private AnchorPane windowButtonsAnchor;


    public void setStage(Stage stage) {
        this.stage = stage;

        enableDragging();
        enableResizing();
        if (stage.isMaximized()) {
            noResizeWhenMaximized();
        } else {
            enableDragging();
            enableResizing();
        }

        stage.maximizedProperty().addListener((obs, wasMaximized, isMaximized) -> {
            if (!isMaximized) {
                enableDragging(); // Re-enable dragging when the window is restored
                enableResizing(); // Re-enable resizing when the window is restored
            } else {
                noResizeWhenMaximized(); // Disable resizing when the window is maximized
            }
        });

    }

    private void enableResizing() {
        Scene scene = stage.getScene();

        final double RESIZE_MARGIN = 10;
        final int MIN_WIDTH = 800;
        final int MIN_HEIGHT = 450;

        scene.setOnMouseMoved(event -> {
            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();
            double width = stage.getWidth();
            double height = stage.getHeight();

            // Detect corners and sides for resizing
            if (mouseX < RESIZE_MARGIN && mouseY > height - RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.SW_RESIZE);  // Bottom-left corner
            } else if (mouseX > width - RESIZE_MARGIN && mouseY > height - RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.SE_RESIZE);  // Bottom-right corner
            } else if (mouseX < RESIZE_MARGIN && mouseY < RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.NW_RESIZE);  // Top-left corner
            } else if (mouseX > width - RESIZE_MARGIN && mouseY < RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.NE_RESIZE);  // Top-right corner
            } else if (mouseX < RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.W_RESIZE);  // Left resize
            } else if (mouseX > width - RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.E_RESIZE);  // Right resize
            } else if (mouseY < RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.N_RESIZE);  // Top resize
            } else if (mouseY > height - RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.S_RESIZE);  // Bottom resize
            } else {
                scene.setCursor(javafx.scene.Cursor.DEFAULT);  // Default cursor
            }
        });

        // Handle mouse dragging for resizing
        scene.setOnMouseDragged(event -> {
            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();
            double screenX = event.getScreenX();
            double screenY = event.getScreenY();

            // Disable dragging when resizing
            if (scene.getCursor() == javafx.scene.Cursor.DEFAULT) {
                // Allow dragging when no resizing is happening
                return;
            }

            // Left resize
            if (scene.getCursor() == javafx.scene.Cursor.W_RESIZE) {
                double deltaX = screenX - stage.getX();
                if (stage.getWidth() - deltaX > MIN_WIDTH) {  // Prevent window from being too small
                    stage.setWidth(stage.getWidth() - deltaX);
                    stage.setX(screenX);  // Adjust X position to prevent window movement
                }
            }
            // Right resize
            else if (scene.getCursor() == javafx.scene.Cursor.E_RESIZE) {
                if (mouseX > MIN_WIDTH) {
                    stage.setWidth(mouseX);
                }
            }
            // Top resize
            else if (scene.getCursor() == javafx.scene.Cursor.N_RESIZE) {
                double deltaY = screenY - stage.getY();
                if (stage.getHeight() - deltaY > MIN_HEIGHT) {
                    stage.setHeight(stage.getHeight() - deltaY);
                    stage.setY(screenY);
                }
            }
            // Bottom resize
            else if (scene.getCursor() == javafx.scene.Cursor.S_RESIZE) {
                if (mouseY > MIN_HEIGHT) {
                    stage.setHeight(mouseY);
                }
            }
            // Top-left corner resize
            else if (scene.getCursor() == javafx.scene.Cursor.NW_RESIZE) {
                double deltaX = screenX - stage.getX();
                double deltaY = screenY - stage.getY();
                if (stage.getWidth() - deltaX > MIN_WIDTH) {
                    stage.setWidth(stage.getWidth() - deltaX);
                    stage.setX(screenX);
                }
                if (stage.getHeight() - deltaY > MIN_HEIGHT) {
                    stage.setHeight(stage.getHeight() - deltaY);
                    stage.setY(screenY);
                }
            }
            // Top-right corner resize
            else if (scene.getCursor() == javafx.scene.Cursor.NE_RESIZE) {
                double deltaY = screenY - stage.getY();
                if (stage.getHeight() - deltaY > MIN_HEIGHT) {
                    stage.setHeight(stage.getHeight() - deltaY);
                    stage.setY(screenY);
                }
                if (mouseX > MIN_WIDTH) {
                    stage.setWidth(mouseX);
                }
            }
            // Bottom-left corner resize
            else if (scene.getCursor() == javafx.scene.Cursor.SW_RESIZE) {
                double deltaX = screenX - stage.getX();
                if (stage.getWidth() - deltaX > MIN_WIDTH) {
                    stage.setWidth(stage.getWidth() - deltaX);
                    stage.setX(screenX);
                }
                if (mouseY > MIN_HEIGHT) {
                    stage.setHeight(mouseY);
                }
            }
            // Bottom-right corner resize
            else if (scene.getCursor() == javafx.scene.Cursor.SE_RESIZE) {
                if (mouseX > MIN_WIDTH) {
                    stage.setWidth(mouseX);
                }
                if (mouseY > MIN_HEIGHT) {
                    stage.setHeight(mouseY);
                }
            }
        });
    }

    private void enableDragging() {
        titleBar.setOnMousePressed((MouseEvent event) -> {
            // Only enable dragging if no resizing is happening
            if (stage.getScene().getCursor() == javafx.scene.Cursor.DEFAULT) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        titleBar.setOnMouseDragged((MouseEvent event) -> {
            if (stage.getScene().getCursor() == javafx.scene.Cursor.DEFAULT) {
                // If the window is maximized, un-maximize it
                if (stage.isMaximized()) {
                    stage.setMaximized(false);
                }
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
    }

    private void noResizeWhenMaximized() {
        stage.getScene().setOnMouseMoved(null);
        stage.getScene().setOnMouseDragged(null);
        titleBar.setOnMousePressed(null);
        titleBar.setOnMouseDragged(null);
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