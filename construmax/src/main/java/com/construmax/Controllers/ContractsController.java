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
    public void initialize() {
        // 1. Configurar as colunas da tabela
        // Os nomes ("id", "startDate", etc.) devem corresponder
        // aos getters do seu modelo ContractLocation (ex: getId(), getStartDate())
        
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("expectedReturnDate"));
        colTotalValue.setCellValueFactory(new PropertyValueFactory<>("totalContractValue"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Carregar os dados
        loadUserContracts();
    }

    private void loadUserContracts() {
        // 3. Obter o usuário da sessão
        User currentUser = Session.getUser();
        
        if (currentUser != null) {
            // 4. Chamar o DAO para buscar os contratos
            ContractRentalDAO contractDAO = new ContractRentalDAO(DatabaseConnection.getConnection());
            ObservableList<ContractLocation> contracts = contractDAO.getContractsByUserId(currentUser.getId());
            
            // 5. Popular a tabela
            contractsTable.setItems(contracts);
        } else {
            System.err.println("Erro: Nenhum usuário logado para carregar contratos.");
            // Opcional: mostrar uma mensagem na tela
        }
    }
}

