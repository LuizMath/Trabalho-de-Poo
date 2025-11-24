package com.construmax.Controllers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.sql.SQLException;

import com.construmax.DAO.ContractRentalDAO;
import com.construmax.DAO.EquipmentDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.ContractLocation;
import com.construmax.Model.Equipment;
import com.construmax.Model.Session;
import com.construmax.Model.Stock;
import com.construmax.Model.User;
import com.construmax.Utils.Toast;

import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

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
    @FXML
    private VBox inspectionPanel;
    @FXML
    private Label lateFeesLabel;
    @FXML
    private Label damageFeesLabel;
    @FXML
    private Label totalFeesLabel;
    @FXML
    private Button confirmReturnButton;
    @FXML
    private DatePicker returnDatePicker;
    @FXML
    private TextArea observationsArea;
    @FXML
    private CheckBox hasDamageCheckBox;
    @FXML
    private TextField damageDescriptionField;

    private ContractLocation selectedContract;
    private double calculatedLateFee = 0.0;
    private double calculatedDamageFee = 0.0;

    @FXML 
    private void mountTableViewAndListView() {
        colSelect.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colSelect.setCellFactory(column -> new TableCell<ContractLocation, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            
            public TableCell<ContractLocation, Boolean> init() {
                checkBox.setOnAction(event -> {
                    ContractLocation current = getTableRow().getItem();
                    if (current == null) return;
                    
                    if (checkBox.isSelected()) {
                        // Desseleciona todos os outros
                        for (ContractLocation c : getTableView().getItems()) {
                            if (c != current) {
                                c.setSelected(false);
                            }
                        }
                        selectedContract = current;
                        loadContractDetails(current);
                    } else {
                        selectedContract = null;
                        clearInspectionPanel();
                    }
                    current.setSelected(checkBox.isSelected());
                    getTableView().refresh();
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
        
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colStartDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("expectedReturnDate"));
        colTotalValue.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalContractValue"));
        colStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        
        listOfEquipments.setPrefHeight(200);
        listOfEquipments.setMaxHeight(200);
        listOfEquipments.setMinHeight(200);
        listOfEquipments.setPlaceholder(new Label("Selecione um contrato para ver os equipamentos."));
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

    private void loadContractDetails(ContractLocation contract) {
        ContractRentalDAO contractDAO = new ContractRentalDAO(DatabaseConnection.getConnection());
        ObservableList<Equipment> equipments = contractDAO.getEquipmentsByContractId(contract.getId());
        listOfEquipments.setItems(equipments);
        
        // Calcula taxas
        calculateFees(contract);
        
        // Mostra painel de inspeção
        inspectionPanel.setVisible(true);
        inspectionPanel.setManaged(true);
        confirmReturnButton.setDisable(false);
    }

    private void calculateFees(ContractLocation contract) {
        LocalDate returnDate = returnDatePicker.getValue() != null ? 
            returnDatePicker.getValue() : LocalDate.now();
        
        // Calcula taxa de atraso
        calculatedLateFee = 0.0;
        if (returnDate.isAfter(contract.getExpectedReturnDate())) {
            long daysLate = ChronoUnit.DAYS.between(contract.getExpectedReturnDate(), returnDate);
            double dailyRate = contract.getTotalContractValue() / 
                ChronoUnit.DAYS.between(contract.getStartDate(), contract.getExpectedReturnDate());
            calculatedLateFee = dailyRate * daysLate * 1.5; // 150% do valor da diária
        }
        
        // Taxa de dano (se houver)
        calculatedDamageFee = 0.0;
        if (hasDamageCheckBox.isSelected()) {
            // Busca a taxa de dano dos equipamentos
            ContractRentalDAO contractDAO = new ContractRentalDAO(DatabaseConnection.getConnection());
            ObservableList<Equipment> equipments = contractDAO.getEquipmentsByContractId(contract.getId());
            
            for (Equipment eq : equipments) {
                if (eq.getDamageFee() != null) {
                    calculatedDamageFee += eq.getDamageFee() * eq.getQuantity();
                }
            }
        }
        
        // Atualiza labels
        lateFeesLabel.setText(String.format("Taxa de Atraso: R$ %.2f", calculatedLateFee));
        damageFeesLabel.setText(String.format("Taxa de Danos: R$ %.2f", calculatedDamageFee));
        totalFeesLabel.setText(String.format("Total de Taxas: R$ %.2f", calculatedLateFee + calculatedDamageFee));
    }

    @FXML
    private void onReturnDateChanged() {
        if (selectedContract != null) {
            calculateFees(selectedContract);
        }
    }

    @FXML
    private void onDamageCheckChanged() {
        damageDescriptionField.setDisable(!hasDamageCheckBox.isSelected());
        if (selectedContract != null) {
            calculateFees(selectedContract);
        }
    }

    @FXML
    private void confirmReturn() {
        if (selectedContract == null) {
            Toast.showToastError("Selecione um contrato para devolver!");
            return;
        }

        if (returnDatePicker.getValue() == null) {
            Toast.showToastError("Selecione a data de devolução!");
            return;
        }

        if (hasDamageCheckBox.isSelected() && 
            (damageDescriptionField.getText() == null || damageDescriptionField.getText().trim().isEmpty())) {
            Toast.showToastError("Descreva os danos encontrados!");
            return;
        }

        try {
            // 1. Atualiza o contrato para "finalizado"
            ContractRentalDAO contractDAO = new ContractRentalDAO(DatabaseConnection.getConnection());
            contractDAO.finalizeContract(selectedContract.getId(), returnDatePicker.getValue(), 
                calculatedLateFee, calculatedDamageFee, observationsArea.getText());

            // 2. Retorna equipamentos ao estoque
            ObservableList<Equipment> equipments = contractDAO.getEquipmentsByContractId(selectedContract.getId());
            EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
            
            for (Equipment eq : equipments) {
                equipmentDAO.returnEquipmentToStock(eq.getId(), eq.getQuantity(), 
                    hasDamageCheckBox.isSelected());
            }

            // 3. Registra observações e danos se houver
            if (hasDamageCheckBox.isSelected()) {
                contractDAO.registerDamage(selectedContract.getId(), 
                    damageDescriptionField.getText(), calculatedDamageFee);
            }

            Toast.showToastSucess("Equipamento devolvido com sucesso!");
            
            // Limpa seleção e recarrega
            clearInspectionPanel();
            loadUserContracts();
            
        } catch (SQLException ex) {
            Toast.showToastError("Erro ao processar devolução: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void clearInspectionPanel() {
        inspectionPanel.setVisible(false);
        inspectionPanel.setManaged(false);
        listOfEquipments.getItems().clear();
        returnDatePicker.setValue(LocalDate.now());
        observationsArea.clear();
        hasDamageCheckBox.setSelected(false);
        damageDescriptionField.clear();
        damageDescriptionField.setDisable(true);
        calculatedLateFee = 0.0;
        calculatedDamageFee = 0.0;
        lateFeesLabel.setText("Taxa de Atraso: R$ 0,00");
        damageFeesLabel.setText("Taxa de Danos: R$ 0,00");
        totalFeesLabel.setText("Total de Taxas: R$ 0,00");
        confirmReturnButton.setDisable(true);
    }

    @FXML
    public void initialize() {
        mountTableViewAndListView();
        loadUserContracts();
        
        // Inicializa painel de inspeção escondido
        inspectionPanel.setVisible(false);
        inspectionPanel.setManaged(false);
        
        // Configura data padrão
        returnDatePicker.setValue(LocalDate.now());
        
        // Configura listeners
        hasDamageCheckBox.selectedProperty().addListener((obs, old, newVal) -> onDamageCheckChanged());
        returnDatePicker.valueProperty().addListener((obs, old, newVal) -> onReturnDateChanged());
        
        confirmReturnButton.setDisable(true);
    }

    private void loadUserContracts() {
        User currentUser = Session.getUser();
        if (currentUser != null) {
            ContractRentalDAO contractDAO = new ContractRentalDAO(DatabaseConnection.getConnection());
            ObservableList<ContractLocation> contracts = contractDAO.getActiveContractsByUserId(currentUser.getId());
            contractsTable.setItems(contracts);
        } else {
            System.err.println("Erro: Nenhum usuário logado para carregar contratos.");
        }
    }
}
