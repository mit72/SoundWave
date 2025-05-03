package com.example.final13;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibraryController {


    @FXML Button createPlaylist;
    @FXML private ListView<String> playlistListView;


    private List<Playlist> allPlaylists;


    private MainHomeController mainController;

    public void setMainController(MainHomeController mainController) {
        this.mainController = mainController;
    }

    public void test(){
        System.out.println("Test");
    }





    @FXML
    private void playlistView() {
        Playlist newPlaylist = new Playlist("New Playlist");
        mainController.showPlaylistView(newPlaylist);
    }

}
