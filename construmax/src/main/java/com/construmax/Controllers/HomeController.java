package com.construmax.Controllers;

import java.io.IOException;
import java.io.InputStream;

import com.construmax.App;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class HomeController {
  @FXML
private VBox contentVBox;

@FXML
private ToggleGroup toggleGroup;

@FXML
public void initialize() {
  loadView("registerEquipment");
}

private void loadView(String fxml) {
    try {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Node node = loader.load();
        contentVBox.getChildren().addAll(node);
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
}
