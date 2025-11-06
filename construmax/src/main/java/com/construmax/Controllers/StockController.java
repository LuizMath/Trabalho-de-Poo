package com.construmax.Controllers;

import com.construmax.DAO.EquipmentDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.Equipment;

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
  private TableColumn<Equipment, String> description;
  @FXML
  private TableColumn<Equipment, Double> dailyValue;
  @FXML
  private TableColumn<Equipment, Integer> availableQuantity;
  @FXML
  private TableColumn<Equipment, Integer> maintenanceQuantity;
  @FXML
  private void equipmentsInTable () {
    name.setCellValueFactory(new PropertyValueFactory<>("name"));
    type.setCellValueFactory(new PropertyValueFactory<>("type"));
    description.setCellValueFactory(new PropertyValueFactory<>("description"));
    dailyValue.setCellValueFactory(new PropertyValueFactory<>("dailyValue"));
    availableQuantity.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
    maintenanceQuantity.setCellValueFactory(new PropertyValueFactory<>("maintenanceQuantity"));

    EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
    ObservableList<Equipment> equipments = equipmentDAO.getAllEquipments();
    tableEquipments.setItems(equipments);
  }
  @FXML
  public void initialize () {
    equipmentsInTable();
  }
}
