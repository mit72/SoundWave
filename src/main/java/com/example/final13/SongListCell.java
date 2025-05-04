package com.example.final13;

import javafx.scene.control.ListCell;
import java.io.File;

public class SongListCell extends ListCell<File> {
    @Override
    protected void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);
        if (empty || file == null) {
            setText(null);
        } else {
            // Display filename without extension
            String name = file.getName();
            setText(name.substring(0, name.lastIndexOf('.')));
        }
    }
}
