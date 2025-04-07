package com.example.final13;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;



public class AudioFileHandler {

    private File selectedAudioFile;

    public void openAudioFileChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an Audio File");

        // Filter for audio file types
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac")
        );

        // Open dialog
        File file = fileChooser.showOpenDialog(stage);

        // Save the selected file
        if (file != null) {
            selectedAudioFile = file;
            System.out.println("Selected file: " + selectedAudioFile.getAbsolutePath());
        }
    }

    // Optional getter to access the file elsewhere
    public File getSelectedAudioFile() {
        return selectedAudioFile;
    }
}

