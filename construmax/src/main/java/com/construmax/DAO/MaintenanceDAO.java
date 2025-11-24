package com.construmax.DAO;

import java.sql.*;
import java.time.LocalDate;

import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.Maintenance;
import com.construmax.Utils.Toast;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MaintenanceDAO {
    private Connection connection;

    public MaintenanceDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean insertMaintenance(Maintenance maintenance) {
        String sql = "INSERT INTO Maintenance (id_equipment, scheduled_date, type, description, status) VALUES (?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, maintenance.getEquipmentId());
            stmt.setDate(2, Date.valueOf(maintenance.getScheduledDate()));
            stmt.setString(3, maintenance.getType());
            stmt.setString(4, maintenance.getDescription());
            stmt.setString(5, maintenance.getStatus());
            
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            
            if (rs.next()) {
                maintenance.setId(rs.getInt(1));
                Toast.showToastSucess("Manutenção agendada com sucesso!");
                return true;
            }
        } catch (SQLException ex) {
            Toast.showToastError("Erro ao agendar manutenção!");
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return false;
    }

    public boolean updateMaintenance(Maintenance maintenance) throws SQLException {
        String sql = "UPDATE Maintenance SET status = ?, completed_date = ?, cost = ?, technician = ? WHERE id_maintenance = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, maintenance.getStatus());
            
            if (maintenance.getCompletedDate() != null) {
                stmt.setDate(2, Date.valueOf(maintenance.getCompletedDate()));
            } else {
                stmt.setNull(2, Types.DATE);
            }
            
            stmt.setDouble(3, maintenance.getCost());
            stmt.setString(4, maintenance.getTechnician());
            stmt.setInt(5, maintenance.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            DatabaseConnection.getDisconnect();
        }
    }

    public ObservableList<Maintenance> getAllMaintenances() {
        ObservableList<Maintenance> maintenances = FXCollections.observableArrayList();
        String sql = "SELECT m.*, e.name as equipment_name FROM Maintenance m " +
                     "INNER JOIN Equipments e ON m.id_equipment = e.id " +
                     "ORDER BY m.scheduled_date DESC";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Maintenance maintenance = new Maintenance();
                maintenance.setId(rs.getInt("id_maintenance"));
                maintenance.setEquipmentId(rs.getInt("id_equipment"));
                maintenance.setEquipmentName(rs.getString("equipment_name"));
                maintenance.setScheduledDate(rs.getDate("scheduled_date").toLocalDate());
                
                if (rs.getDate("completed_date") != null) {
                    maintenance.setCompletedDate(rs.getDate("completed_date").toLocalDate());
                }
                
                maintenance.setType(rs.getString("type"));
                maintenance.setStatus(rs.getString("status"));
                maintenance.setDescription(rs.getString("description"));
                maintenance.setCost(rs.getDouble("cost"));
                maintenance.setTechnician(rs.getString("technician"));
                
                maintenances.add(maintenance);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return maintenances;
    }

    public ObservableList<Maintenance> getScheduledMaintenances() {
        ObservableList<Maintenance> maintenances = FXCollections.observableArrayList();
        String sql = "SELECT m.*, e.name as equipment_name FROM Maintenance m " +
                     "INNER JOIN Equipments e ON m.id_equipment = e.id " +
                     "WHERE m.status = 'agendada' OR m.status = 'em_andamento' " +
                     "ORDER BY m.scheduled_date ASC";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Maintenance maintenance = new Maintenance();
                maintenance.setId(rs.getInt("id_maintenance"));
                maintenance.setEquipmentId(rs.getInt("id_equipment"));
                maintenance.setEquipmentName(rs.getString("equipment_name"));
                maintenance.setScheduledDate(rs.getDate("scheduled_date").toLocalDate());
                maintenance.setType(rs.getString("type"));
                maintenance.setStatus(rs.getString("status"));
                maintenance.setDescription(rs.getString("description"));
                
                maintenances.add(maintenance);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return maintenances;
    }

    public ObservableList<Maintenance> getMaintenancesByEquipmentId(int equipmentId) {
        ObservableList<Maintenance> maintenances = FXCollections.observableArrayList();
        String sql = "SELECT m.*, e.name as equipment_name FROM Maintenance m " +
                     "INNER JOIN Equipments e ON m.id_equipment = e.id " +
                     "WHERE m.id_equipment = ? " +
                     "ORDER BY m.scheduled_date DESC";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, equipmentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Maintenance maintenance = new Maintenance();
                maintenance.setId(rs.getInt("id_maintenance"));
                maintenance.setEquipmentId(rs.getInt("id_equipment"));
                maintenance.setEquipmentName(rs.getString("equipment_name"));
                maintenance.setScheduledDate(rs.getDate("scheduled_date").toLocalDate());
                
                if (rs.getDate("completed_date") != null) {
                    maintenance.setCompletedDate(rs.getDate("completed_date").toLocalDate());
                }
                
                maintenance.setType(rs.getString("type"));
                maintenance.setStatus(rs.getString("status"));
                maintenance.setDescription(rs.getString("description"));
                maintenance.setCost(rs.getDouble("cost"));
                maintenance.setTechnician(rs.getString("technician"));
                
                maintenances.add(maintenance);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return maintenances;
    }

    /**
     * Atualiza o status de manutenção do equipamento no estoque
     * @param equipmentId ID do equipamento
     * @param inMaintenance true para mover para manutenção, false para retornar
     */
    public void updateEquipmentMaintenanceStatus(int equipmentId, boolean inMaintenance) throws SQLException {
        String sql;
        
        if (inMaintenance) {
            // Move equipamento disponível para manutenção
            sql = "UPDATE Stock SET available_quantity = available_quantity - 1, " +
                  "maintenance_quantity = maintenance_quantity + 1 WHERE id_equipment = ?";
        } else {
            // Retorna equipamento de manutenção para disponível
            sql = "UPDATE Stock SET available_quantity = available_quantity + 1, " +
                  "maintenance_quantity = maintenance_quantity - 1 WHERE id_equipment = ?";
        }
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, equipmentId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            DatabaseConnection.getDisconnect();
        }
    }

    public boolean deleteMaintenance(int maintenanceId) {
        String sql = "DELETE FROM Maintenance WHERE id_maintenance = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, maintenanceId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                Toast.showToastSucess("Manutenção excluída!");
                return true;
            }
        } catch (SQLException ex) {
            Toast.showToastError("Erro ao excluir manutenção!");
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return false;
    }
}