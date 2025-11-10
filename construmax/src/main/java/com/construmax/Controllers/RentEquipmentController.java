package com.construmax.Controllers;

import com.construmax.DAO.ContractRentalDAO;
import com.construmax.DAO.EquipmentDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.ContractLocation;
import com.construmax.Model.Equipment;
import com.construmax.Model.Session;
import com.construmax.Model.Stock;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.construmax.Utils.GenerateContract;
import com.construmax.Utils.Toast;
import javafx.util.converter.IntegerStringConverter;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
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
    @FXML private TableView<Stock> equipmentTable;
    @FXML private TableColumn<Stock, Boolean> colSelect;
    @FXML private TableColumn<Stock, Integer> colQtd;
    @FXML private TableColumn<Stock, String> colName;
    @FXML private TableColumn<Stock, String> colType;
    @FXML private TableColumn<Stock, Integer> availableQuantity;
    @FXML private TableColumn<Stock, Double> colDailyValue;

    private ObservableList<Stock> availableEquipments;

    @FXML
    private void mountEquipmentsTable () {
        colSelect.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        availableQuantity.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        colDailyValue.setCellValueFactory(new PropertyValueFactory<>("dailyValue"));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("rentedQuantity"));
        colQtd.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colQtd.setOnEditCommit(event -> {
            Stock stock = event.getRowValue();
            int newValue = event.getNewValue();
            if (newValue < 1) {
                Toast.showToastError("A quantidade deve ser pelo menos 1!");
                equipmentTable.refresh();
                return;
            }
            if (newValue > stock.getAvailableQuantity()) {
                Toast.showToastError("Quantidade indisponível no estoque!");
                equipmentTable.refresh();
                return;
            }
            stock.setRentedQuantity(newValue);
            stock.setSelected(newValue > 0);
            calculateTotal();
        });
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

        List<Stock> selectedEquipments = availableEquipments.stream()
            .filter(st -> st.isSelected() && st.getRentedQuantity() > 0)
            .collect(Collectors.toList());

        if (selectedEquipments.isEmpty()) {
            totalValueLabel.setText("Valor Total (Bruto/Líquido): R$ 0,00");
            return;
        }

        long days = ChronoUnit.DAYS.between(start, end) + 1;
        double dailyTotal = selectedEquipments.stream().mapToDouble(st -> st.getDailyValue() * st.getRentedQuantity()).sum();
        double grossValue = dailyTotal * days;

        totalValueLabel.setText(String.format("Valor Total Bruto: R$ %.2f", grossValue));
    }
    @FXML
    private void createContract() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        List<Stock> selectedEquipments = availableEquipments.stream()
            .filter(Stock::isSelected)
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
        GenerateContract.generateContract(contract);
        Toast.showToastSucess("Contrato de locação criado com sucesso!");
        for (int i = 0; i < selectedEquipments.size(); i++) {
            EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
            equipmentDAO.updateStockQuantity(selectedEquipments.get(i).getRentedQuantity(), selectedEquipments.get(i).getId(), selectedEquipments.get(i).getAvailableQuantity(), selectedEquipments.get(i).getInUseQuantity());
        }
        for (Stock eq : selectedEquipments) {
            eq.setSelected(false);
            eq.setRentedQuantity(0);

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
