package com.construmax.Controllers;

import javafx.event.EventHandler;

import com.construmax.DAO.EquipmentDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.Equipment;
import com.construmax.Model.Equipment.Status;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class RegisterEquipmentController {
  @FXML
  private TextField name;
  @FXML
  private TextField type;
  @FXML
  private TextField description;
  @FXML
  private TextField dailyValue;
  @FXML
  private Button submit;
  @FXML
  private void clickSubmitButton () {
    submit.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
        Equipment equipment = new Equipment(name.getText(), type.getText(), description.getText(), Status.AVAILABLE, Double.parseDouble(dailyValue.getText()));
        boolean clearInputs = equipmentDAO.insertEquipment(equipment);
        if (clearInputs) {
          name.setText("");
          type.setText("");
          description.setText("");
          dailyValue.setText("");
        }
      }
    });
  }
  @FXML
  public void initialize () {
    clickSubmitButton();
  }
}
