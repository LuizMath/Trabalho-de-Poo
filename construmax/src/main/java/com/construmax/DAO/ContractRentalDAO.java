package com.construmax.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import com.construmax.Model.ContractLocation;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Utils.Toast;

public class ContractRentalDAO {
    private Connection connection;

    public ContractRentalDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean insertContract(ContractLocation contract) throws SQLException {
        String sqlContract = "INSERT INTO Contract (id_user, start_date, end_date, total_value, status) VALUES (?, ?, ?, ?, ?)";
        boolean success = false;

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
            }
            if (success) {
                Toast.showToastSucess("Contrato de Locação Criado!");
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
