package com.construmax.Controllers;

import com.construmax.DAO.ContractRentalDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.ContractLocation;
import com.construmax.Model.Session;
import com.construmax.Model.User;
import java.time.LocalDate;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ContractsController {

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
    public void initialize () {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("expectedReturnDate"));
        colTotalValue.setCellValueFactory(new PropertyValueFactory<>("totalContractValue"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
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

