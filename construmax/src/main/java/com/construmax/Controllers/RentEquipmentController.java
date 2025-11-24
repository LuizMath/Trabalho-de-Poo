package com.construmax.Controllers;

import com.construmax.DAO.ContractRentalDAO;
import com.construmax.DAO.EquipmentDAO;
import com.construmax.DAO.LoyaltyDAO;
import com.construmax.DAO.PaymentsDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.ContractLocation;
import com.construmax.Model.Payment;
import com.construmax.Model.Session;
import com.construmax.Model.Stock;
import com.construmax.Model.User;
import com.construmax.Utils.GenerateContract;
import com.construmax.Utils.Toast;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class RentEquipmentController {

    @FXML private TextField cpfField;
    @FXML private TextField nameField;
    @FXML private Label clientInfoLabel;
    @FXML private Label vipStatusLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label totalValueLabel;
    @FXML private Label discountLabel;
    @FXML private Label finalValueLabel;
    @FXML private TableView<Stock> equipmentTable;
    @FXML private TableColumn<Stock, Boolean> colSelect;
    @FXML private TableColumn<Stock, Integer> colQtd;
    @FXML private TableColumn<Stock, String> colName;
    @FXML private TableColumn<Stock, String> colType;
    @FXML private TableColumn<Stock, Integer> availableQuantity;
    @FXML private TableColumn<Stock, Double> colDailyValue;

    private ObservableList<Stock> availableEquipments;
    private static final double VIP_DISCOUNT = 0.10;

    @FXML
    public void initialize() {
        mountEquipmentsTable();
        loadClient();
        startDatePicker.valueProperty().addListener((obs, old, n) -> calculateTotal());
        endDatePicker.valueProperty().addListener((obs, old, n) -> calculateTotal());
    }

    private void mountEquipmentsTable() {
        colSelect.setCellValueFactory(cd -> cd.getValue().selectedProperty());
        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        availableQuantity.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        colDailyValue.setCellValueFactory(new PropertyValueFactory<>("dailyValue"));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("rentedQuantity"));
        colQtd.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        colQtd.setOnEditCommit(event -> {
            Stock stock = event.getRowValue();
            Integer newValue = event.getNewValue();
            if (newValue == null || newValue < 1) {
                Toast.showToastError("A quantidade deve ser pelo menos 1!");
                stock.setRentedQuantity(event.getOldValue());
                equipmentTable.refresh();
                return;
            }
            if (newValue > stock.getAvailableQuantity()) {
                Toast.showToastError("Quantidade indisponível no estoque!");
                stock.setRentedQuantity(event.getOldValue());
                equipmentTable.refresh();
                return;
            }
            stock.setRentedQuantity(newValue);
            stock.setSelected(newValue > 0);
            calculateTotal();
        });

        loadAvailableEquipments();

        if (availableEquipments != null) {
            availableEquipments.forEach(eq ->
                eq.selectedProperty().addListener((o, ov, nv) -> calculateTotal()));
        }
    }

    private void loadAvailableEquipments() {
    try {
        EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
        availableEquipments = equipmentDAO.getAvailableEquipments();
        equipmentTable.setItems(availableEquipments);
    } catch (Exception e) {
        Toast.showToastError("Erro ao carregar equipamentos: " + e.getMessage());
        e.printStackTrace();
    }
}


    private void loadClient() {
        User u = Session.getUser();
        if (u != null) {
            nameField.setText(u.getName());
            cpfField.setText(u.getCPF());
            if (u.isVIP()) {
                vipStatusLabel.setText("⭐ CLIENTE VIP - 10% DE DESCONTO");
                vipStatusLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
            } else {
                vipStatusLabel.setText("Cliente Regular");
                vipStatusLabel.setStyle("-fx-text-fill: gray;");
            }
        } else {
            nameField.setText("N/A - Faça login");
            cpfField.setText("N/A - Faça login");
            vipStatusLabel.setText("");
        }
    }

    @FXML
    private void calculateTotal() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null || start.isAfter(end)) {
            resetTotalLabels();
            return;
        }
        List<Stock> selected = availableEquipments.stream()
                .filter(st -> st.isSelected() && st.getRentedQuantity() > 0)
                .collect(Collectors.toList());
        if (selected.isEmpty()) {
            resetTotalLabels();
            return;
        }
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        double dailyTotal = selected.stream()
                .mapToDouble(st -> st.getDailyValue() * st.getRentedQuantity())
                .sum();
        double gross = dailyTotal * days;
        double discount = 0.0;
        User u = Session.getUser();
        if (u != null && u.isVIP()) discount = gross * VIP_DISCOUNT;
        double total = gross - discount;

        totalValueLabel.setText(String.format("Valor Bruto: R$ %.2f", gross));
        discountLabel.setText(String.format("Desconto VIP (10%%): R$ %.2f", discount));
        finalValueLabel.setText(String.format("Valor Final: R$ %.2f", total));
        finalValueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: green;");
    }

    private void resetTotalLabels() {
        totalValueLabel.setText("Valor Bruto: R$ 0,00");
        discountLabel.setText("Desconto VIP (10%): R$ 0,00");
        finalValueLabel.setText("Valor Final: R$ 0,00");
    }

    @FXML
    private void createContract() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null || start.isAfter(end)) {
            Toast.showToastError("Verifique as datas de locação.");
            return;
        }
        List<Stock> selected = availableEquipments.stream()
                .filter(st -> st.isSelected() && st.getRentedQuantity() > 0)
                .collect(Collectors.toList());
        if (selected.isEmpty()) {
            Toast.showToastError("Selecione pelo menos um equipamento.");
            return;
        }
        User u = Session.getUser();
        if (u == null) {
            Toast.showToastError("Usuário não está logado!");
            return;
        }

        try {
            ContractLocation contract = new ContractLocation(u, selected, start, end);
            double finalValue = contract.getTotalContractValue();
            if (u.isVIP()) finalValue = finalValue * (1 - VIP_DISCOUNT);

            ContractRentalDAO contractDAO = new ContractRentalDAO(DatabaseConnection.getConnection());
            contractDAO.insertContract(contract);

            PaymentsDAO paymentDAO = new PaymentsDAO(DatabaseConnection.getConnection());
            Payment payment = new Payment(contract.getId(), finalValue, LocalDate.now().plusDays(5));
            paymentDAO.insertPayment(payment);

            LoyaltyDAO loyaltyDAO = new LoyaltyDAO(DatabaseConnection.getConnection());
            loyaltyDAO.addPointsForContract(u.getId(), finalValue);

            GenerateContract.generateContract(contract);

            EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
            for (Stock eq : selected) {
                equipmentDAO.updateStockQuantity(
                        eq.getRentedQuantity(),
                        eq.getId(),
                        eq.getAvailableQuantity(),
                        eq.getInUseQuantity()
                );
            }

            clearForm();
            Toast.showToastSucess("Contrato de locação criado com sucesso!");

        } catch (SQLException e) {
            Toast.showToastError("Erro ao salvar contrato: " + e.getMessage());
        } finally {
            DatabaseConnection.getDisconnect();
        }
    }

    private void clearForm() {
        availableEquipments.forEach(eq -> {
            eq.setSelected(false);
            eq.setRentedQuantity(0);
        });
        equipmentTable.refresh();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        resetTotalLabels();
    }
}
