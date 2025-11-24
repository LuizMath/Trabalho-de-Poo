package com.construmax.Controllers;

import com.construmax.DAO.LoyaltyDAO;
import com.construmax.DAO.PaymentsDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.LoyaltyPoints;
import com.construmax.Model.Payment;
import com.construmax.Model.Session;
import com.construmax.Utils.Toast;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;

public class PaymentsController {

    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, Integer> colContractId;
    @FXML private TableColumn<Payment, Double> colAmount;
    @FXML private TableColumn<Payment, LocalDate> colDueDate;
    @FXML private TableColumn<Payment, LocalDate> colPaymentDate;
    @FXML private TableColumn<Payment, String> colStatus;
    @FXML private TableColumn<Payment, Double> colLateFee;
    @FXML private TableColumn<Payment, Void> colActions;

    @FXML private Label totalPendingLabel;
    @FXML private Label totalPaidLabel;
    @FXML private Label totalLateFeesLabel;

    @FXML private Label loyaltyPointsLabel;
    @FXML private Label totalContractsLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label availableDiscountLabel;
    @FXML private Button usePointsButton;

    private ObservableList<Payment> payments;
    private LoyaltyPoints userLoyalty;

    @FXML
    public void initialize() {
        setupTable();
        loadPayments();
        loadLoyaltyInfo();
        updateStatistics();
    }

    private void setupTable() {
        colContractId.setCellValueFactory(new PropertyValueFactory<>("contractId"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colPaymentDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colLateFee.setCellValueFactory(new PropertyValueFactory<>("lateFee"));

        // Coluna de ações
        colActions.setCellFactory(param -> new TableCell<Payment, Void>() {
            private final Button payButton = new Button("Pagar");

            {
                payButton.setOnAction(event -> {
                    Payment payment = getTableView().getItems().get(getIndex());
                    processPayment(payment);
                });
                
                payButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Payment payment = getTableView().getItems().get(getIndex());
                    if ("pendente".equals(payment.getStatus()) || "atrasado".equals(payment.getStatus())) {
                        setGraphic(payButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        // Estiliza linhas com base no status
        paymentsTable.setRowFactory(tv -> new TableRow<Payment>() {
            @Override
            protected void updateItem(Payment payment, boolean empty) {
                super.updateItem(payment, empty);
                if (empty || payment == null) {
                    setStyle("");
                } else if ("atrasado".equals(payment.getStatus())) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else if ("pago".equals(payment.getStatus())) {
                    setStyle("-fx-background-color: #ccffcc;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadPayments() {
        if (Session.getUser() != null) {
            PaymentsDAO paymentDAO = new PaymentsDAO(DatabaseConnection.getConnection());
            payments = paymentDAO.getPaymentsByUserId(Session.getUser().getId());
            paymentsTable.setItems(payments);
        }
    }

    private void loadLoyaltyInfo() {
        if (Session.getUser() != null) {
            LoyaltyDAO loyaltyDAO = new LoyaltyDAO(DatabaseConnection.getConnection());
            userLoyalty = loyaltyDAO.getLoyaltyByUserId(Session.getUser().getId());

            if (userLoyalty != null) {
                loyaltyPointsLabel.setText("Pontos: " + userLoyalty.getPoints());
                totalContractsLabel.setText("Total de Contratos: " + userLoyalty.getTotalContracts());
                totalSpentLabel.setText(String.format("Total Gasto: R$ %.2f", userLoyalty.getTotalSpent()));
                availableDiscountLabel.setText(String.format("Desconto Disponível: R$ %.2f", 
                    userLoyalty.calculateDiscount()));
                
                usePointsButton.setDisable(userLoyalty.getPoints() < 100);
            } else {
                loyaltyPointsLabel.setText("Pontos: 0");
                totalContractsLabel.setText("Total de Contratos: 0");
                totalSpentLabel.setText("Total Gasto: R$ 0,00");
                availableDiscountLabel.setText("Desconto Disponível: R$ 0,00");
                usePointsButton.setDisable(true);
            }
        }
    }

    private void processPayment(Payment payment) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Processar Pagamento");
        dialog.setHeaderText("Contrato #" + payment.getContractId());

        ButtonType confirmButtonType = new ButtonType("Confirmar Pagamento", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Calcula valores
        payment.setPaymentDate(LocalDate.now());
        payment.calculateLateFee();

        Label amountLabel = new Label(String.format("Valor: R$ %.2f", payment.getAmount()));
        Label lateFeeLabel = new Label(String.format("Taxa de Atraso: R$ %.2f", payment.getLateFee()));
        Label totalLabel = new Label(String.format("Total: R$ %.2f", payment.getTotalAmount()));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ComboBox<String> paymentMethodCombo = new ComboBox<>();
        paymentMethodCombo.getItems().addAll("dinheiro", "cartao", "pix", "transferencia");
        paymentMethodCombo.setValue("pix");

        CheckBox useLoyaltyCheckBox = new CheckBox("Usar pontos de fidelidade");
        Label discountLabel = new Label("Desconto: R$ 0,00");

        if (userLoyalty != null && userLoyalty.getPoints() >= 100) {
            useLoyaltyCheckBox.setDisable(false);
            useLoyaltyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    double discount = userLoyalty.calculateDiscount();
                    discountLabel.setText(String.format("Desconto: R$ %.2f", discount));
                    totalLabel.setText(String.format("Total: R$ %.2f", 
                        payment.getTotalAmount() - discount));
                } else {
                    discountLabel.setText("Desconto: R$ 0,00");
                    totalLabel.setText(String.format("Total: R$ %.2f", payment.getTotalAmount()));
                }
            });
        } else {
            useLoyaltyCheckBox.setDisable(true);
        }

        grid.add(amountLabel, 0, 0, 2, 1);
        grid.add(lateFeeLabel, 0, 1, 2, 1);
        grid.add(new Label("Método de Pagamento:"), 0, 2);
        grid.add(paymentMethodCombo, 1, 2);
        grid.add(useLoyaltyCheckBox, 0, 3, 2, 1);
        grid.add(discountLabel, 0, 4, 2, 1);
        grid.add(new javafx.scene.control.Separator(), 0, 5, 2, 1);
        grid.add(totalLabel, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == confirmButtonType) {
                payment.setPaymentMethod(paymentMethodCombo.getValue());
                
                PaymentsDAO paymentDAO = new PaymentsDAO(DatabaseConnection.getConnection());
                if (paymentDAO.updatePayment(payment)) {
                    // Usa pontos se selecionado
                    if (useLoyaltyCheckBox.isSelected() && userLoyalty != null) {
                        LoyaltyDAO loyaltyDAO = new LoyaltyDAO(DatabaseConnection.getConnection());
                        int pointsToUse = (userLoyalty.getPoints() / 100) * 100;
                        loyaltyDAO.usePoints(Session.getUser().getId(), pointsToUse);
                    }

                    Toast.showToastSucess("Pagamento realizado com sucesso!");
                    loadPayments();
                    loadLoyaltyInfo();
                    updateStatistics();
                }
            }
        });
    }

    @FXML
    private void filterPending() {
        PaymentsDAO paymentDAO = new PaymentsDAO(DatabaseConnection.getConnection());
        ObservableList<Payment> pendingPayments = paymentDAO.getPendingPayments();
        
        if (Session.getUser() != null) {
            pendingPayments.removeIf(p -> {
                // Mantém apenas pagamentos do usuário atual
                try {
                    PaymentsDAO dao = new PaymentsDAO(DatabaseConnection.getConnection());
                    Payment fullPayment = dao.getPaymentByContractId(p.getContractId());
                    return fullPayment == null;
                } catch (Exception e) {
                    return true;
                }
            });
        }
        
        paymentsTable.setItems(pendingPayments);
    }

    @FXML
    private void showAll() {
        loadPayments();
    }

    @FXML
    private void usePoints() {
        if (userLoyalty == null || userLoyalty.getPoints() < 100) {
            Toast.showToastError("Você precisa de pelo menos 100 pontos!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Usar Pontos de Fidelidade");
        alert.setHeaderText("Deseja converter seus pontos em desconto?");
        alert.setContentText(String.format("Você tem %d pontos = R$ %.2f de desconto\n" +
            "Os pontos serão usados no próximo pagamento.",
            userLoyalty.getPoints(), userLoyalty.calculateDiscount()));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Toast.showToastSucess("Pontos prontos para uso no próximo pagamento!");
            }
        });
    }

    private void updateStatistics() {
        double totalPending = payments.stream()
            .filter(p -> "pendente".equals(p.getStatus()) || "atrasado".equals(p.getStatus()))
            .mapToDouble(Payment::getTotalAmount)
            .sum();

        double totalPaid = payments.stream()
            .filter(p -> "pago".equals(p.getStatus()))
            .mapToDouble(Payment::getAmount)
            .sum();

        double totalLateFees = payments.stream()
            .filter(p -> "pago".equals(p.getStatus()))
            .mapToDouble(Payment::getLateFee)
            .sum();

        totalPendingLabel.setText(String.format("Total Pendente: R$ %.2f", totalPending));
        totalPaidLabel.setText(String.format("Total Pago: R$ %.2f", totalPaid));
        totalLateFeesLabel.setText(String.format("Total em Multas: R$ %.2f", totalLateFees));
    }
}