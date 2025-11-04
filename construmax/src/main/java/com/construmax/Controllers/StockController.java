package com.construmax.Controllers;

import com.construmax.Model.Equipment;
import com.construmax.Model.Equipment.Status;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class StockController {
  @FXML
  private TableView<Equipment> tableEquipments;
  @FXML
  private TableColumn<Equipment, String> name;
  @FXML
  private TableColumn<Equipment, String> type;
  @FXML
  public void initialize () {
    name.setCellValueFactory(new PropertyValueFactory<>("name"));
    type.setCellValueFactory(new PropertyValueFactory<>("type"));
    ObservableList<Equipment> equipments = FXCollections.observableArrayList(
      new Equipment("Chave de fenda", "Ferramenta", "Vonder Imantada Magnética", Status.AVAILABLE, 5.0)
    );
    tableEquipments.setItems(equipments);
  }
}
