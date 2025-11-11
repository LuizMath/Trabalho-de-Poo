package com.construmax.Controllers;

import java.io.IOException;

import com.construmax.App;
import com.construmax.Utils.Toast;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class HomeController {
  @FXML
  private VBox contentVBox;
  @FXML
  private ToggleGroup toggleGroup;
  @FXML
  private void switchView() {
    loadView("registerEquipment");
    toggleGroup.selectedToggleProperty().addListener((new ChangeListener<Toggle>() {
      @Override
      public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
        if (newValue == null) {
          oldValue.setSelected(true);
        }
        if (newValue != null) {
          ToggleButton selectedToggleButton = (ToggleButton) newValue;
          switch (selectedToggleButton.getId()) {
            case "equipment": {
              loadView("registerEquipment");
              break;
            }
            case "stock": {
              loadView("stock");
              break;
            }
            case "returnEquipment": {
              loadView("returnEquipment");
              break;
            }
            case "rentEquipment": {
              loadView("rentEquipment");
              break;
            }
            case "userContracts": {
              loadView("contracts");
              break;
            }
            default:
              break;
          }
        }
      }
    }));
  }

  @FXML
  public void initialize() {
    switchView();
  }

  private void loadView(String fxml) {
    try {
      FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
      Node node = loader.load();
      contentVBox.getChildren().setAll(node);
      if (fxml.equals("stock")) {
        contentVBox.setAlignment(Pos.TOP_CENTER);
        contentVBox.setPadding(new Insets(30, 0, 0, 0));
      } else {
        contentVBox.setAlignment(Pos.CENTER);
        contentVBox.setPadding(new Insets(0, 0, 0, 0));
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
