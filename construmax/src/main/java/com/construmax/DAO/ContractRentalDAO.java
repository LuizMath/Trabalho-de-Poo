package com.construmax.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.construmax.Model.ContractLocation;
import com.construmax.Model.Equipment;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Utils.Toast;

public class ContractRentalDAO {
    private Connection connection;

    public ContractRentalDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean insertContract(ContractLocation contract) throws SQLException {
        String sqlContract = "INSERT INTO Contract (id_user, start_date, end_date, total_value, status) VALUES (?, ?, ?, ?, ?)";
        
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
                
                // Insere equipamentos do contrato
                EquipmentDAO equipmentDAO = new EquipmentDAO(connection);
                for (int i = 0; i < contract.getRentedEquipments().size(); i++) {
                    equipmentDAO.insertEquipmentsInItemContract(
                        contract.getRentedEquipments().get(i).getRentedQuantity(), 
                        contract.getId(), 
                        contract.getRentedEquipments().get(i).getId(), 
                        contract.getRentedEquipments().get(i).getDailyValue()
                    );
                }
                return true;
            }
        } catch (SQLException ex) {
            Toast.showToastError("Erro ao criar Contrato!");
            throw ex;
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return false;
    }

    public ObservableList<ContractLocation> getContractsByUserId(int userId) {
        ObservableList<ContractLocation> contracts = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Contract WHERE id_user = ? ORDER BY start_date DESC";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_contract");
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

    public ObservableList<ContractLocation> getActiveContractsByUserId(int userId) {
        ObservableList<ContractLocation> contracts = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Contract WHERE id_user = ? AND status = 'ativo' ORDER BY start_date DESC";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_contract");
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                double totalValue = rs.getDouble("total_value");
                String status = rs.getString("status");
                contracts.add(new ContractLocation(id, startDate, endDate, totalValue, status));
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao buscar contratos ativos: " + ex.getMessage());
        } finally {
             DatabaseConnection.getDisconnect();
        }
        return contracts;
    }

    public ObservableList<Equipment> getEquipmentsByContractId(int id) {
        ObservableList<Equipment> equipments = FXCollections.observableArrayList();
        String sql = "SELECT Ic.quantity, Ic.id_contract, Eq.id, Eq.name FROM ItemContract as Ic " +
                     "INNER JOIN Equipments as Eq ON Ic.id_equipament = Eq.id WHERE Ic.id_contract = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Equipment equipment = new Equipment(rs.getString("name"), rs.getInt("quantity"));
                equipment.setId(rs.getInt("id"));
                equipments.add(equipment);
            }
            DatabaseConnection.getDisconnect();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return equipments;
    }

    public boolean finalizeContract(int contractId, LocalDate returnDate, double lateFee, 
                                    double damageFee, String observations) throws SQLException {
        String sql = "UPDATE Contract SET status = 'encerrado', end_date = ?, late_fee = ?, " +
                     "damage_fee = ?, observations = ? WHERE id_contract = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(returnDate));
            stmt.setDouble(2, lateFee);
            stmt.setDouble(3, damageFee);
            stmt.setString(4, observations);
            stmt.setInt(5, contractId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.out.println("Erro ao finalizar contrato: " + ex.getMessage());
            throw ex;
        } finally {
            DatabaseConnection.getDisconnect();
        }
    }

    public boolean registerDamage(int contractId, String damageDescription, double damageFee) throws SQLException {
        String sql = "INSERT INTO ContractDamage (id_contract, description, damage_fee, registered_date) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, contractId);
            stmt.setString(2, damageDescription);
            stmt.setDouble(3, damageFee);
            stmt.setDate(4, Date.valueOf(LocalDate.now()));
            
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.out.println("Erro ao registrar dano: " + ex.getMessage());
            throw ex;
        } finally {
            DatabaseConnection.getDisconnect();
        }
    }

    public boolean renewContract(int contractId, int additionalDays) throws SQLException {
        String sql = "UPDATE Contract SET end_date = DATE_ADD(end_date, INTERVAL ? DAY) WHERE id_contract = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, additionalDays);
            stmt.setInt(2, contractId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                Toast.showToastSucess("Contrato renovado com sucesso!");
                return true;
            }
        } catch (SQLException ex) {
            Toast.showToastError("Erro ao renovar contrato!");
            throw ex;
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return false;
    }
}
