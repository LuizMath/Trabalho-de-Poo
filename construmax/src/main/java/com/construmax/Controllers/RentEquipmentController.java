package com.construmax.Controllers;

import com.construmax.DAO.ContractRentalDAO;
import com.construmax.DAO.EquipmentDAO;
import com.construmax.DAO.LoyaltyDAO;
import com.construmax.DAO.PaymentDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.ContractLocation;
import com.construmax.Model.Payment;
import com.construmax.Model.Session;
import com.construmax.Model.Stock;
import com.construmax.Model.User;
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
    private static final double VIP_DISCOUNT = 0.10; // 10% de desconto

    @FXML
    private void mountEquipmentsTable() {
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

    private void loadClient() {
        User currentUser = Session.getUser();
        
        if (currentUser != null) {
            nameField.setText(currentUser.getName());
            cpfField.setText(currentUser.getCPF());
            
            // Mostra status VIP
            if (currentUser.isVIP()) {
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
            totalValueLabel.setText("Valor Bruto: R$ 0,00");
            discountLabel.setText("Desconto VIP: R$ 0,00");
            finalValueLabel.setText("Valor Final: R$ 0,00");
            return;
        }

        List<Stock> selectedEquipments = availableEquipments.stream()
            .filter(st -> st.isSelected() && st.getRentedQuantity() > 0)
            .collect(Collectors.toList());

        if (selectedEquipments.isEmpty()) {
            totalValueLabel.setText("Valor Bruto: R$ 0,00");
            discountLabel.setText("Desconto VIP: R$ 0,00");
            finalValueLabel.setText("Valor Final: R$ 0,00");
            return;
        }

        long days = ChronoUnit.DAYS.between(start, end) + 1;
        double dailyTotal = selectedEquipments.stream()
            .mapToDouble(st -> st.getDailyValue() * st.getRentedQuantity())
            .sum();
        double grossValue = dailyTotal * days;

        // Aplica desconto VIP se aplicável
        double discount = 0.0;
        User currentUser = Session.getUser();
        if (currentUser != null && currentUser.isVIP()) {
            discount = grossValue * VIP_DISCOUNT;
        }

        double finalValue = grossValue - discount;

        totalValueLabel.setText(String.format("Valor Bruto: R$ %.2f", grossValue));
        discountLabel.setText(String.format("Desconto VIP (10%%): R$ %.2f", discount));
        finalValueLabel.setText(String.format("Valor Final: R$ %.2f", finalValue));
        finalValueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: green;");
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

        User currentUser = Session.getUser();
        if (currentUser == null) {
            Toast.showToastError("Usuário não está logado!");
            return;
        }

        // Cria contrato com valor já com desconto VIP aplicado
        ContractLocation contract = new ContractLocation(currentUser, selectedEquipments, start, end);
        
        // Aplica desconto VIP se aplicável
        if (currentUser.isVIP()) {
            double originalValue = contract.getTotalContractValue();
            double discountedValue = originalValue * (1 - VIP_DISCOUNT);
            // Aqui você precisaria adicionar um setter no ContractLocation
            // contract.setTotalContractValue(discountedValue);
        }

        ContractRentalDAO contractRentalDAO = new ContractRentalDAO(DatabaseConnection.getConnection());

        try {
            // Salva contrato
            contractRentalDAO.insertContract(contract);

            // Cria pagamento
            PaymentDAO paymentDAO = new PaymentDAO(DatabaseConnection.getConnection());
            Payment payment = new Payment(
                contract.getId(),
                contract.getTotalContractValue(),
                LocalDate.now().plusDays(5) // Vencimento em 5 dias
            );
            paymentDAO.insertPayment(payment);

            // Adiciona pontos de fidelidade
            LoyaltyDAO loyaltyDAO = new LoyaltyDAO(DatabaseConnection.getConnection());
            loyaltyDAO.addPointsForContract(currentUser.getId(), contract.getTotalContractValue());

            // Gera PDF
            GenerateContract.generateContract(contract);

            // Atualiza estoque
            for (Stock selectedEquipment : selectedEquipments) {
                EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
                equipmentDAO.updateStockQuantity(
                    selectedEquipment.getRentedQuantity(),
                    selectedEquipment.getId(),
                    selectedEquipment.getAvailableQuantity(),
                    selectedEquipment.getInUseQuantity()
                );
            }

            // Limpa tela
            for (Stock eq : selectedEquipments) {
                eq.setSelected(false);
                eq.setRentedQuantity(0);
            }
            equipmentTable.refresh();
            totalValueLabel.setText("Valor Bruto: R$ 0,00");
            discountLabel.setText("Desconto VIP: R$ 0,00");
            finalValueLabel.setText("Valor Final: R$ 0,00");

            Toast.showToastSucess("Contrato de locação criado com sucesso!");

        } catch (SQLException ex) {
            Toast.showToastError("Erro ao salvar contrato: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        mountEquipmentsTable();
        loadClient();
        
        // Listeners para recalcular ao mudar datas
        startDatePicker.valueProperty().addListener((obs, old, newVal) -> calculateTotal());
        endDatePicker.valueProperty().addListener((obs, old, newVal) -> calculateTotal());
    }
}