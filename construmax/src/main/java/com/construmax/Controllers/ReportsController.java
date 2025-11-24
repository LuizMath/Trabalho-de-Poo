package com.construmax.Controllers;

import com.construmax.DAO.ContractRentalDAO;
import com.construmax.DAO.EquipmentDAO;
import com.construmax.DAO.MaintenanceDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.ContractLocation;
import com.construmax.Model.Maintenance;
import com.construmax.Model.Session;
import com.construmax.Model.Stock;
import com.construmax.Utils.GenerateReport;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class ReportsController {

    @FXML private TabPane reportsTabPane;
    
    // Aba de Relatório de Contratos
    @FXML private TableView<ContractLocation> contractsReportTable;
    @FXML private TableColumn<ContractLocation, Integer> colContractId;
    @FXML private TableColumn<ContractLocation, LocalDate> colContractStart;
    @FXML private TableColumn<ContractLocation, LocalDate> colContractEnd;
    @FXML private TableColumn<ContractLocation, Double> colContractValue;
    @FXML private TableColumn<ContractLocation, String> colContractStatus;
    @FXML private DatePicker startDateFilter;
    @FXML private DatePicker endDateFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label totalContractsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Button exportContractsButton;

    // Aba de Relatório de Equipamentos
    @FXML private TableView<Stock> equipmentsReportTable;
    @FXML private TableColumn<Stock, String> colEquipName;
    @FXML private TableColumn<Stock, String> colEquipType;
    @FXML private TableColumn<Stock, Integer> colEquipTotal;
    @FXML private TableColumn<Stock, Integer> colEquipAvailable;
    @FXML private TableColumn<Stock, Integer> colEquipInUse;
    @FXML private TableColumn<Stock, Integer> colEquipMaintenance;
    @FXML private Label totalEquipmentsLabel;
    @FXML private Label utilizationRateLabel;

    // Aba de Relatório de Manutenções
    @FXML private TableView<Maintenance> maintenanceReportTable;
    @FXML private TableColumn<Maintenance, String> colMaintEquipName;
    @FXML private TableColumn<Maintenance, LocalDate> colMaintScheduled;
    @FXML private TableColumn<Maintenance, LocalDate> colMaintCompleted;
    @FXML private TableColumn<Maintenance, String> colMaintType;
    @FXML private TableColumn<Maintenance, String> colMaintStatus;
    @FXML private TableColumn<Maintenance, Double> colMaintCost;
    @FXML private ComboBox<String> maintenanceStatusFilter;
    @FXML private Label scheduledMaintenancesLabel;
    @FXML private Label completedMaintenancesLabel;
    @FXML private Label totalMaintenanceCostLabel;

    @FXML
    public void initialize() {
        setupContractsReport();
        setupEquipmentsReport();
        setupMaintenanceReport();
        
        loadAllReports();
    }

    private void setupContractsReport() {
        colContractId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colContractStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colContractEnd.setCellValueFactory(new PropertyValueFactory<>("expectedReturnDate"));
        colContractValue.setCellValueFactory(new PropertyValueFactory<>("totalContractValue"));
        colContractStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusFilter.getItems().addAll("Todos", "ativo", "finalizado");
        statusFilter.setValue("Todos");
    }

    private void setupEquipmentsReport() {
        colEquipName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEquipType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colEquipTotal.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colEquipAvailable.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        colEquipInUse.setCellValueFactory(new PropertyValueFactory<>("inUseQuantity"));
        colEquipMaintenance.setCellValueFactory(new PropertyValueFactory<>("maintenanceQuantity"));
    }

    private void setupMaintenanceReport() {
        colMaintEquipName.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));
        colMaintScheduled.setCellValueFactory(new PropertyValueFactory<>("scheduledDate"));
        colMaintCompleted.setCellValueFactory(new PropertyValueFactory<>("completedDate"));
        colMaintType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMaintStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colMaintCost.setCellValueFactory(new PropertyValueFactory<>("cost"));

        maintenanceStatusFilter.getItems().addAll("Todos", "agendada", "em_andamento", "concluida");
        maintenanceStatusFilter.setValue("Todos");
    }

    @FXML
    private void loadAllReports() {
        loadContractsReport();
        loadEquipmentsReport();
        loadMaintenanceReport();
    }

    @FXML
    private void loadContractsReport() {
        ContractRentalDAO contractDAO = new ContractRentalDAO(DatabaseConnection.getConnection());
        ObservableList<ContractLocation> contracts;

        if (Session.getUser() != null) {
            contracts = contractDAO.getContractsByUserId(Session.getUser().getId());
        } else {
            contracts = javafx.collections.FXCollections.observableArrayList();
        }

        // Aplica filtros
        if (!statusFilter.getValue().equals("Todos")) {
            contracts.removeIf(c -> !c.getStatus().equals(statusFilter.getValue()));
        }

        if (startDateFilter.getValue() != null) {
            contracts.removeIf(c -> c.getStartDate().isBefore(startDateFilter.getValue()));
        }

        if (endDateFilter.getValue() != null) {
            contracts.removeIf(c -> c.getExpectedReturnDate().isAfter(endDateFilter.getValue()));
        }

        contractsReportTable.setItems(contracts);

        // Calcula estatísticas
        int totalContracts = contracts.size();
        double totalRevenue = contracts.stream()
            .mapToDouble(ContractLocation::getTotalContractValue)
            .sum();

        totalContractsLabel.setText("Total de Contratos: " + totalContracts);
        totalRevenueLabel.setText(String.format("Receita Total: R$ %.2f", totalRevenue));
    }

    @FXML
    private void loadEquipmentsReport() {
        EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
        ObservableList<Stock> equipments = equipmentDAO.getAllEquipments();
        equipmentsReportTable.setItems(equipments);

        // Calcula estatísticas
        int totalEquipments = equipments.stream()
            .mapToInt(Stock::getQuantity)
            .sum();
        
        int totalInUse = equipments.stream()
            .mapToInt(Stock::getInUseQuantity)
            .sum();

        double utilizationRate = totalEquipments > 0 ? 
            (totalInUse * 100.0 / totalEquipments) : 0;

        totalEquipmentsLabel.setText("Total de Equipamentos: " + totalEquipments);
        utilizationRateLabel.setText(String.format("Taxa de Utilização: %.1f%%", utilizationRate));
    }

    @FXML
    private void loadMaintenanceReport() {
        MaintenanceDAO maintenanceDAO = new MaintenanceDAO(DatabaseConnection.getConnection());
        ObservableList<Maintenance> maintenances = maintenanceDAO.getAllMaintenances();

        // Aplica filtro de status
        if (!maintenanceStatusFilter.getValue().equals("Todos")) {
            maintenances.removeIf(m -> !m.getStatus().equals(maintenanceStatusFilter.getValue()));
        }

        maintenanceReportTable.setItems(maintenances);

        // Calcula estatísticas
        long scheduled = maintenances.stream()
            .filter(m -> m.getStatus().equals("agendada"))
            .count();
        
        long completed = maintenances.stream()
            .filter(m -> m.getStatus().equals("concluida"))
            .count();

        double totalCost = maintenances.stream()
            .filter(m -> m.getStatus().equals("concluida"))
            .mapToDouble(Maintenance::getCost)
            .sum();

        scheduledMaintenancesLabel.setText("Manutenções Agendadas: " + scheduled);
        completedMaintenancesLabel.setText("Manutenções Concluídas: " + completed);
        totalMaintenanceCostLabel.setText(String.format("Custo Total: R$ %.2f", totalCost));
    }

    @FXML
    private void exportContractsReport() {
        ObservableList<ContractLocation> contracts = contractsReportTable.getItems();
        if (contracts.isEmpty()) {
            com.construmax.Utils.Toast.showToastError("Nenhum contrato para exportar!");
            return;
        }
        GenerateReport.generateContractsReport(contracts);
    }

    @FXML
    private void exportEquipmentsReport() {
        ObservableList<Stock> equipments = equipmentsReportTable.getItems();
        if (equipments.isEmpty()) {
            com.construmax.Utils.Toast.showToastError("Nenhum equipamento para exportar!");
            return;
        }
        GenerateReport.generateEquipmentsReport(equipments);
    }

    @FXML
    private void exportMaintenanceReport() {
        ObservableList<Maintenance> maintenances = maintenanceReportTable.getItems();
        if (maintenances.isEmpty()) {
            com.construmax.Utils.Toast.showToastError("Nenhuma manutenção para exportar!");
            return;
        }
        GenerateReport.generateMaintenanceReport(maintenances);
    }

    @FXML
    private void clearFilters() {
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);
        statusFilter.setValue("Todos");
        maintenanceStatusFilter.setValue("Todos");
        loadAllReports();
    }
}