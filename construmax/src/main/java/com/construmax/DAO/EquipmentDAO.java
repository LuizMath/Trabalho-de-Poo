package com.construmax.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.Equipment;
import com.construmax.Model.Stock;
import com.construmax.Utils.Toast;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EquipmentDAO {
    private Connection connection;
    
    public EquipmentDAO(Connection connection) {
        this.connection = connection;
    }
    
    public boolean insertEquipment(Equipment equipment) {
        String sqlStatement = "INSERT INTO Equipments (name, type, description, daily_value, quantity) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sqlStatement);
            stmt.setString(1, equipment.getName());
            stmt.setString(2, equipment.getType());
            stmt.setString(3, equipment.getDescription());
            stmt.setDouble(4, equipment.getDailyValue());
            stmt.setInt(5, equipment.getQuantity());
            stmt.executeUpdate();
            Toast.showToastSucess("Equipamento Cadastrado!");
            DatabaseConnection.getDisconnect();
            return true;
        } catch (SQLException ex) {
            Toast.showToastError("Erro ao Cadastrar Equipamento!");
            System.out.println("Erro ao inserir equipamento: " + ex.getMessage());
            return false;
        }
    }
    
    public void updateStockQuantity(int quantityRented, int id, int available_quantity, int in_use_quantity) throws SQLException {
        String sqlStatement = "UPDATE Stock SET available_quantity = ?, in_use_quantity = ? WHERE id_equipment = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sqlStatement);
            stmt.setInt(1, available_quantity - quantityRented);
            stmt.setInt(2, in_use_quantity + quantityRented);
            stmt.setInt(3, id);
            stmt.executeUpdate();
            DatabaseConnection.getDisconnect();
        } catch (SQLException ex) {
            System.out.println("Erro ao fazer update: " + ex.getMessage());
            throw ex;
        }
    }

    public void returnEquipmentToStock(int equipmentId, int quantity, boolean hasDamage) throws SQLException {
        String sqlStatement;
        
        if (hasDamage) {
            // Se tem dano, vai para manutenção
            sqlStatement = "UPDATE Stock SET in_use_quantity = in_use_quantity - ?, maintenance_quantity = maintenance_quantity + ? WHERE id_equipment = ?";
        } else {
            // Se não tem dano, retorna para disponível
            sqlStatement = "UPDATE Stock SET in_use_quantity = in_use_quantity - ?, available_quantity = available_quantity + ? WHERE id_equipment = ?";
        }
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sqlStatement);
            stmt.setInt(1, quantity);
            stmt.setInt(2, quantity);
            stmt.setInt(3, equipmentId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Erro ao retornar equipamento ao estoque: " + ex.getMessage());
            throw ex;
        } finally {
            DatabaseConnection.getDisconnect();
        }
    }

    public ObservableList<Stock> getAllEquipments() {
        ObservableList<Stock> equipments = FXCollections.observableArrayList();
        String sqlStatement = "SELECT Eq.id, Eq.name, Eq.type, Eq.quantity, Eq.description, Eq.daily_value, " +
                             "St.total_quantity, St.available_quantity, St.maintenance_quantity, St.in_use_quantity " +
                             "FROM Equipments as Eq INNER JOIN Stock as St ON Eq.id=St.id_equipment";
        try {
            PreparedStatement stmt = connection.prepareStatement(sqlStatement);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Stock stockItem = new Stock(
                    rs.getString("name"), 
                    rs.getString("type"), 
                    rs.getString("description"), 
                    rs.getDouble("daily_value"), 
                    rs.getInt("total_quantity"), 
                    rs.getInt("available_quantity"), 
                    rs.getInt("maintenance_quantity"), 
                    rs.getInt("in_use_quantity")
                );
                stockItem.setId(rs.getInt("id"));
                equipments.add(stockItem);
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao obter equipamentos: " + ex.getMessage());
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return equipments;
    }
    
    public void insertEquipmentsInItemContract(int quantity, int idContract, int idEquipment, Double unitaryValue) throws SQLException {
        String sqlStatement = "INSERT INTO ItemContract (quantity, id_contract, id_equipament, unitary_value) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sqlStatement);
            stmt.setInt(1, quantity);
            stmt.setInt(2, idContract);
            stmt.setInt(3, idEquipment);
            stmt.setDouble(4, unitaryValue);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Erro ao inserir item no contrato: " + ex.getMessage());
            throw ex; 
        }
    }
    
    public ObservableList<Stock> getAvailableEquipments() {
        ObservableList<Stock> equipments = FXCollections.observableArrayList();
        String sqlStatement = "SELECT Eq.id, Eq.name, Eq.type, Eq.quantity, Eq.description, Eq.daily_value, " +
                             "St.total_quantity, St.available_quantity, St.maintenance_quantity, St.in_use_quantity " +
                             "FROM Equipments as Eq INNER JOIN Stock as St ON Eq.id=St.id_equipment";
        try (PreparedStatement stmt = connection.prepareStatement(sqlStatement)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt("available_quantity") > 0) {
                    Stock equipment = new Stock(
                        rs.getString("name"), 
                        rs.getString("type"), 
                        rs.getString("description"), 
                        rs.getDouble("daily_value"), 
                        rs.getInt("total_quantity"), 
                        rs.getInt("available_quantity"), 
                        rs.getInt("maintenance_quantity"), 
                        rs.getInt("in_use_quantity")
                    );
                    equipment.setId(rs.getInt("id"));
                    equipments.add(equipment);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao obter equipamentos disponíveis: " + ex.getMessage());
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return equipments;
    }

    public Equipment getEquipmentById(int id) {
        String sql = "SELECT * FROM Equipments WHERE id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Equipment equipment = new Equipment(
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getString("description"),
                    rs.getDouble("daily_value"),
                    rs.getInt("quantity")
                );
                equipment.setId(id);
                return equipment;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return null;
    }
}