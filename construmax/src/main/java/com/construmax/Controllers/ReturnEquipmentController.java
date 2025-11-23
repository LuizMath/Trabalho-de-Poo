package com.construmax.Controllers;

import java.time.LocalDate;

import com.construmax.DAO.ContractRentalDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.ContractLocation;
import com.construmax.Model.Equipment;
import com.construmax.Model.Session;
import com.construmax.Model.User;

import javafx.scene.control.Label;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ReturnEquipmentController {
    @FXML
    private TableView<ContractLocation> contractsTable;
    @FXML
    private TableColumn<ContractLocation, Integer> colId;
    @FXML
    private TableColumn<ContractLocation, LocalDate> colStartDate;
    @FXML
    private TableColumn<ContractLocation, LocalDate> colEndDate;
    @FXML
    private TableColumn<ContractLocation, Double> colTotalValue;
    @FXML
    private TableColumn<ContractLocation, String> colStatus;
    @FXML 
    private TableColumn<ContractLocation, Boolean> colSelect;
    @FXML
    private ListView<Equipment> listOfEquipments;

    @FXML private void mountTableViewAndListView () {
        colSelect.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colSelect.setCellFactory(column -> new TableCell<ContractLocation, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            public TableCell<ContractLocation, Boolean> init() {
                checkBox.setOnAction(event -> {
                    ContractLocation current = getTableRow().getItem();
                    if (current == null) return;
                    if (checkBox.isSelected()) {
                        for (ContractLocation c : getTableView().getItems()) {
                            if (c != current) {
                                c.setSelected(false);
                            }
                        }
                    }
                    current.setSelected(checkBox.isSelected());
                    ContractRentalDAO contractRentalDAO = new ContractRentalDAO(DatabaseConnection.getConnection());
                    listOfEquipments.setItems(contractRentalDAO.getEquipmentsByContractId(current.getId()));
                });
                return this;
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    checkBox.setSelected(false);
                    return;
                }
                ContractLocation contract = getTableRow().getItem();
                checkBox.selectedProperty().unbind();
                checkBox.setSelected(contract.isSelected());
                setGraphic(checkBox);
            }
        }.init());
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("expectedReturnDate"));
        colTotalValue.setCellValueFactory(new PropertyValueFactory<>("totalContractValue"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        listOfEquipments.setPrefHeight(200);
        listOfEquipments.setMaxHeight(200);
        listOfEquipments.setMinHeight(200);
        listOfEquipments.setPlaceholder(new Label("Nenhum equipamento encontrado."));
        listOfEquipments.setCellFactory(param -> new ListCell<Equipment>() {
        @Override
        protected void updateItem(Equipment item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getName() + " (Qtd: " + item.getQuantity() + ")");
            }
          }
      });
    }
 
    @FXML
    public void initialize () {
        mountTableViewAndListView();
        loadUserContracts();
    }

    private void loadUserContracts() {
        User currentUser = Session.getUser();
        if (currentUser != null) {
            ContractRentalDAO contractDAO = new ContractRentalDAO(DatabaseConnection.getConnection());
            ObservableList<ContractLocation> contracts = contractDAO.getContractsByUserId(currentUser.getId());
            contractsTable.setItems(contracts);
        } else {
            System.err.println("Erro: Nenhum usuário logado para carregar contratos.");
        }
    }
}
