package com.construmax.Controllers;

import com.construmax.DAO.ContractRentalDAO;
import com.construmax.DAO.EquipmentDAO;
import com.construmax.DAO.UserDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.ContractLocation;
import com.construmax.Model.Equipment;
import com.construmax.Model.Session;
import com.construmax.Model.User;
import com.construmax.Utils.Toast;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class RentEquipmentController {

    @FXML private TextField cpfField;
    @FXML private TextField nameField;
    @FXML private Label clientInfoLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label totalValueLabel;
    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, Boolean> colSelect;
    @FXML private TableColumn<Equipment, String> colName;
    @FXML private TableColumn<Equipment, String> colType;
    @FXML private TableColumn<Equipment, Double> colDailyValue;

    private ObservableList<Equipment> availableEquipments;

    @FXML
    private void mountEquipmentsTable () {
        colSelect.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDailyValue.setCellValueFactory(new PropertyValueFactory<>("dailyValue"));
        loadAvailableEquipments();
        availableEquipments.forEach(eq ->
            eq.selectedProperty().addListener((obs, oldV, newV) -> calculateTotal())
        );
    }
    @FXML
    private void loadAvailableEquipments() {
        EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
        availableEquipments = equipmentDAO.getAvailableEquipments();
        equipmentTable.setItems(availableEquipments);
        DatabaseConnection.getDisconnect();
    }
    private void loadClient () {
        nameField.setText(Session.getUser().getName());
        cpfField.setText(Session.getUser().getCPF());
    }
    @FXML
    private void calculateTotal() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null || start.isAfter(end)) {
            totalValueLabel.setText("Valor Total (Bruto/Líquido): R$ 0,00");
            return;
        }

        List<Equipment> selectedEquipments = availableEquipments.stream()
            .filter(Equipment::isSelected)
            .collect(Collectors.toList());

        if (selectedEquipments.isEmpty()) {
            totalValueLabel.setText("Valor Total (Bruto/Líquido): R$ 0,00");
            return;
        }

        long days = ChronoUnit.DAYS.between(start, end) + 1;
        double dailyTotal = selectedEquipments.stream().mapToDouble(Equipment::getDailyValue).sum();
        double grossValue = dailyTotal * days;

        totalValueLabel.setText(String.format("Valor Total Bruto: R$ %.2f", grossValue));
    }
    @FXML
    private void createContract() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        List<Equipment> selectedEquipments = availableEquipments.stream()
            .filter(Equipment::isSelected)
            .collect(Collectors.toList());
        if (selectedEquipments.isEmpty()) {
            Toast.showToastError("Selecione pelo menos um equipamento.");
            return;
        }
        if (start == null || end == null || start.isAfter(end)) {
            Toast.showToastError("Verifique as datas de locação.");
            return;
        }
        ContractLocation contract = new ContractLocation(Session.getUser(), selectedEquipments, start, end);
        ContractRentalDAO contractRentalDAO = new ContractRentalDAO(DatabaseConnection.getConnection());
        try {
            contractRentalDAO.insertContract(contract);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        Toast.showToastSucess("Contrato de locação criado com sucesso!");
        for (Equipment eq : selectedEquipments) {
            eq.setSelected(false);
        }
        equipmentTable.refresh();
        totalValueLabel.setText("Valor Total (Bruto/Líquido): R$ 0,00");
    }

    @FXML
    public void initialize() {
        mountEquipmentsTable();
        loadClient();
    }

}
