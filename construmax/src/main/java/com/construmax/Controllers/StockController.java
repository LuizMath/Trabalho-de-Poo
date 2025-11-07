package com.construmax.Controllers;

import com.construmax.DAO.EquipmentDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.Stock;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class StockController {
  @FXML
  private TableView<Stock> tableEquipments;
  @FXML
  private TableColumn<Stock, String> name;
  @FXML
  private TableColumn<Stock, String> type;
  @FXML
  private TableColumn<Stock, String> description;
  @FXML
  private TableColumn<Stock, Double> dailyValue;
  @FXML
  private TableColumn<Stock, Integer> availableQuantity;
  @FXML
  private TableColumn<Stock, Integer> maintenanceQuantity;
  @FXML
  private TableColumn<Stock, Integer> inUseQuantity;
  @FXML
  private void equipmentsInTable () {
    name.setCellValueFactory(new PropertyValueFactory<>("name"));
    type.setCellValueFactory(new PropertyValueFactory<>("type"));
    description.setCellValueFactory(new PropertyValueFactory<>("description"));
    dailyValue.setCellValueFactory(new PropertyValueFactory<>("dailyValue"));
    availableQuantity.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
    maintenanceQuantity.setCellValueFactory(new PropertyValueFactory<>("maintenanceQuantity"));
    inUseQuantity.setCellValueFactory(new PropertyValueFactory<>("inUseQuantity"));


    EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
    ObservableList<Stock> equipments = equipmentDAO.getAllEquipments();
    tableEquipments.setItems(equipments);
  }
  @FXML
  public void initialize () {
    equipmentsInTable();
  }
}
