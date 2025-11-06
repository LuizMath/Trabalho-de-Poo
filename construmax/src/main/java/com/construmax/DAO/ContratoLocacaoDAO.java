package com.construmax.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import com.construmax.Model.ContratoLocation;
import com.construmax.Model.Equipment.Status;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Utils.Toast;

public class ContratoLocacaoDAO {
    private Connection connection;

    public ContratoLocacaoDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean insertContrato(ContratoLocation contrato) {
        String sqlContrato = "INSERT INTO Contratos (cliente_id, data_inicio, data_prevista_devolucao, valor_total, politica_renovacao, status) VALUES (?, ?, ?, ?, ?, ?)";
        boolean success = false;
        
        try {
            PreparedStatement stmtContrato = connection.prepareStatement(sqlContrato, PreparedStatement.RETURN_GENERATED_KEYS);
            stmtContrato.setInt(1, contrato.getClient().getId());
            stmtContrato.setDate(2, Date.valueOf(contrato.getStartDate()));
            stmtContrato.setDate(3, Date.valueOf(contrato.getExpectedReturnDate()));
            stmtContrato.setDouble(4, contrato.getTotalContractValue());
            stmtContrato.setString(5, contrato.getPoliticsRenewal());
            stmtContrato.setString(6, contrato.getStatus());
            stmtContrato.executeUpdate();
            ResultSet rs = stmtContrato.getGeneratedKeys();
            if (rs.next()) {
                contrato.setId(rs.getInt(1));
                EquipmentDAO equipmentDAO = new EquipmentDAO(DatabaseConnection.getConnection());
                boolean statusUpdated = equipmentDAO.updateEquipmentStatus(
                    contrato.getRentedEquipments(), 
                    Status.RENTED
                );
                if (statusUpdated) {
                    success = true;
                }
            }

            if (success) {
                Toast.showToastSucess("Contrato de Locação ID " + contrato.getId() + " Criado!");
            }
        } catch (SQLException ex) {
            Toast.showToastError("Erro ao criar Contrato!");
            System.out.println("Erro ao inserir contrato: " + ex.getMessage());
        } finally {
             DatabaseConnection.getDisconnect(); 
        }
        return success;
    }
}