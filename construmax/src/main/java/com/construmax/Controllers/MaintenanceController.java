package com.construmax.Controllers;

import com.construmax.DAO.EquipmentDAO;
import com.construmax.DAO.MaintenanceDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.Maintenance;
import com.construmax.Model.Stock;
import com.construmax.Utils.Toast;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;

public class MaintenanceController {

    @FXML private ComboBox<Stock> equipmentComboBox;
    @FXML private DatePicker scheduledDatePicker;
    @FXML private ComboBox<String> maintenanceTypeCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField costField;
    @FXML private TextField technicianField;
    @FXML private Button scheduleButton;
    
    @FXML private TableView<Maintenance> maintenanceTable;
    @FXML private TableColumn<Maintenance, String> colEquipmentName;
    @FXML private TableColumn<Maintenance, LocalDate> colScheduledDate;
    @FXML private TableColumn<Maintenance, LocalDate> colCompletedDate;
    @FXML private TableColumn<Maintenance, String> colType;
    @FXML private TableColumn<Maintenance, String> colStatus;
    @FXML private TableColumn<Maintenance, Double> colCost;
    @FXML private TableColumn<Maintenance, Void> colActions;
    
    @FXML private Label scheduledCountLabel;
    @FXML private Label overdueCountLabel;
    @FXML private Label completedCountLabel;

    private ObservableList<Stock> availableEquipments;
    private ObservableList<Maintenance> maintenances;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTable();
        loadEquipments();
        loadMaintenances();
        updateStatistics();
    }

    private void setupComboBoxes() {
        maintenanceTypeCombo.getItems().addAll("preventiva", "corretiva");
        maintenanceTypeCombo.setValue("preventiva");
        
        equipmentComboBox.setCellFactory(param -> new ListCell<Stock>() {
            @Override
            protected void updateItem(Stock item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getType() + ")");
                }
            }
        });
        
        equipmentComboBox.setButtonCell(new ListCell<Stock>() {
            @Override
            protected void updateItem(Stock item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getType() + ")");
                }
            }
        });
    }

    private void setupTable() {
        colEquipmentName.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));
        colScheduledDate.setCellValueFactory(new PropertyValueFactory<>("scheduledDate"));
        colCompletedDate.setCellValueFactory(new PropertyValueFactory<>("completedDate"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        
        // Coluna de ações
        colActions.setCellFactory(param -> new TableCell<Maintenance, Void>() {
            private final Button completeButton = new Button("Concluir");
            private final Button startButton = new Button("Iniciar");
            
            {
                completeButton.setOnAction(event -> {
                    Maintenance maintenance = getTableView().getItems().get(getIndex());
                    completeMaintenance(maintenance);
                });
                
                startButton.setOnAction(event -> {
                    Maintenance maintenance = getTableView().getItems().get(getIndex());
                    startMaintenance(maintenance);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Maintenance maintenance = getTableView().getItems().get(getIndex());
                    if ("agendada".equals(maintenance.getStatus())) {
                        setGraphic(startButton);
                    } else if ("em_andamento".equals(maintenance.getStatus())) {
                        setGraphic(completeButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadEquipments() {
        EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
        availableEquipments = equipmentDAO.getAllEquipments();
        equipmentComboBox.setItems(availableEquipments);
    }

    private void loadMaintenances() {
        MaintenanceDAO maintenanceDAO = new MaintenanceDAO(DatabaseConnection.getConnection());
        maintenances = maintenanceDAO.getAllMaintenances();
        maintenanceTable.setItems(maintenances);
    }

    @FXML
    private void scheduleMaintenance() {
        Stock selectedEquipment = equipmentComboBox.getValue();
        LocalDate scheduledDate = scheduledDatePicker.getValue();
        String type = maintenanceTypeCombo.getValue();
        String description = descriptionArea.getText();

        if (selectedEquipment == null) {
            Toast.showToastError("Selecione um equipamento!");
            return;
        }

        if (scheduledDate == null) {
            Toast.showToastError("Selecione a data de agendamento!");
            return;
        }

        if (description == null || description.trim().isEmpty()) {
            Toast.showToastError("Descreva a manutenção!");
            return;
        }

        Maintenance maintenance = new Maintenance(
            selectedEquipment.getId(),
            selectedEquipment.getName(),
            scheduledDate,
            type,
            description
        );

        MaintenanceDAO maintenanceDAO = new MaintenanceDAO(DatabaseConnection.getConnection());
        if (maintenanceDAO.insertMaintenance(maintenance)) {
            clearForm();
            loadMaintenances();
            updateStatistics();
        }
    }

    private void startMaintenance(Maintenance maintenance) {
        maintenance.setStatus("em_andamento");
        
        MaintenanceDAO maintenanceDAO = new MaintenanceDAO(DatabaseConnection.getConnection());
        
        try {
            // Atualiza status da manutenção
            maintenanceDAO.updateMaintenance(maintenance);
            
            // Move equipamento para manutenção no estoque
            maintenanceDAO.updateEquipmentMaintenanceStatus(maintenance.getEquipmentId(), true);
            
            Toast.showToastSucess("Manutenção iniciada!");
            loadMaintenances();
            updateStatistics();
            
        } catch (SQLException ex) {
            Toast.showToastError("Erro ao iniciar manutenção!");
            ex.printStackTrace();
        }
    }

    private void completeMaintenance(Maintenance maintenance) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Concluir Manutenção");
        dialog.setHeaderText("Informe os detalhes da conclusão");

        ButtonType confirmButtonType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField costField = new TextField();
        costField.setPromptText("Custo da manutenção");
        TextField technicianField = new TextField();
        technicianField.setPromptText("Nome do técnico");

        grid.add(new Label("Custo:"), 0, 0);
        grid.add(costField, 1, 0);
        grid.add(new Label("Técnico:"), 0, 1);
        grid.add(technicianField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == confirmButtonType) {
                try {
                    double cost = Double.parseDouble(costField.getText());
                    String technician = technicianField.getText();

                    maintenance.setCompletedDate(LocalDate.now());
                    maintenance.setCost(cost);
                    maintenance.setTechnician(technician);

                    MaintenanceDAO maintenanceDAO = new MaintenanceDAO(DatabaseConnection.getConnection());
                    
                    // Atualiza manutenção
                    maintenanceDAO.updateMaintenance(maintenance);
                    
                    // Retorna equipamento do estoque de manutenção
                    maintenanceDAO.updateEquipmentMaintenanceStatus(maintenance.getEquipmentId(), false);

                    Toast.showToastSucess("Manutenção concluída!");
                    loadMaintenances();
                    updateStatistics();

                } catch (NumberFormatException ex) {
                    Toast.showToastError("Custo inválido!");
                } catch (SQLException ex) {
                    Toast.showToastError("Erro ao concluir manutenção!");
                    ex.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void filterScheduled() {
        MaintenanceDAO maintenanceDAO = new MaintenanceDAO(DatabaseConnection.getConnection());
        maintenances = maintenanceDAO.getScheduledMaintenances();
        maintenanceTable.setItems(maintenances);
    }

    @FXML
    private void showAll() {
        loadMaintenances();
    }

    private void updateStatistics() {
        long scheduled = maintenances.stream()
            .filter(m -> "agendada".equals(m.getStatus()))
            .count();
        
        long overdue = maintenances.stream()
            .filter(Maintenance::isOverdue)
            .count();
        
        long completed = maintenances.stream()
            .filter(m -> "concluida".equals(m.getStatus()))
            .count();

        scheduledCountLabel.setText("Agendadas: " + scheduled);
        overdueCountLabel.setText("Atrasadas: " + overdue);
        completedCountLabel.setText("Concluídas: " + completed);
    }

    private void clearForm() {
        equipmentComboBox.setValue(null);
        scheduledDatePicker.setValue(null);
        maintenanceTypeCombo.setValue("preventiva");
        descriptionArea.clear();
        costField.clear();
        technicianField.clear();
    }
}