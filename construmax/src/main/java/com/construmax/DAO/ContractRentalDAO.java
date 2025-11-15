package com.construmax.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.construmax.Model.ContractLocation;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Utils.Toast;

public class ContractRentalDAO {
    private Connection connection;

    public ContractRentalDAO(Connection connection) {
        this.connection = connection;
    }

    // A assinatura original 'throws SQLException' e retorna 'boolean' é confusa.
    // Vamos simplificar: ele retorna 'boolean' (sucesso/falha) e não lança exceção (ele a trata).
    // A assinatura DEVE ter 'throws SQLException'
public boolean insertContract(ContractLocation contract) throws SQLException {
    String sqlContract = "INSERT INTO Contract (id_user, start_date, end_date, total_value, status) VALUES (?, ?, ?, ?, ?)";
    boolean success = false; // Este boolean é na verdade desnecessário, mas vamos manter

    try {
        PreparedStatement stmtContract = connection.prepareStatement(sqlContract, PreparedStatement.RETURN_GENERATED_KEYS);
        stmtContract.setInt(1, contract.getClient().getId());
        stmtContract.setDate(2, Date.valueOf(contract.getStartDate()));
        stmtContract.setDate(3, Date.valueOf(contract.getExpectedReturnDate()));
        stmtContract.setDouble(4, contract.getTotalContractValue());
        stmtContract.setString(5, contract.getStatus());
        stmtContract.executeUpdate();
        ResultSet rs = stmtContract.getGeneratedKeys();
        
        if (rs.next()) {
            contract.setId(rs.getInt(1));
            EquipmentDAO equipmentDAO = new EquipmentDAO(connection);
            for (int i = 0; i < contract.getRentedEquipments().size(); i++) {
                equipmentDAO.insertEquipmentsInItemContract(contract.getRentedEquipments().get(i).getRentedQuantity(), contract.getId(), contract.getRentedEquipments().get(i).getId());
            };
            success = true; 
        }
        if (success) { // 2. 'success' AINDA É 'false', então este Toast NUNCA roda
            Toast.showToastSucess("Contrato de Locação Criado!");
        }
    } catch (SQLException ex) {
        Toast.showToastError("Erro ao criar Contrato!");        
    } finally {
         DatabaseConnection.getDisconnect();
    }
    return success;
}
    public ObservableList<ContractLocation> getContractsByUserId(int userId) {
        ObservableList<ContractLocation> contracts = FXCollections.observableArrayList();
        String sql = "SELECT id, start_date, end_date, total_value, status FROM Contract WHERE id_user = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                double totalValue = rs.getDouble("total_value");
                String status = rs.getString("status");

                
                contracts.add(new ContractLocation(id, startDate, endDate, totalValue, status));
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao buscar contratos: " + ex.getMessage());
        } finally {
             DatabaseConnection.getDisconnect(); 
        }
        return contracts;
    }
}

