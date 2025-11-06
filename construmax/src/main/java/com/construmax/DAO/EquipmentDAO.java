package com.construmax.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.Equipment;
import com.construmax.Model.Equipment.Status;
import com.construmax.Utils.Toast;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EquipmentDAO {
  private Connection connection;
  public EquipmentDAO (Connection connection) {
    this.connection = connection;
  }
  public boolean updateEquipmentStatus(List<Equipment> equipments, Status newStatus) {
    String sqlUpdateEquip = "UPDATE Equipments SET state = ? WHERE id = ?";
    try (PreparedStatement stmtUpdate = connection.prepareStatement(sqlUpdateEquip)) {
        for (Equipment equip : equipments) {
            stmtUpdate.setString(1, newStatus.getDescription()); 
            stmtUpdate.setInt(2, equip.getId()); 
            stmtUpdate.addBatch(); 
        }
        stmtUpdate.executeBatch();
        return true;
    } catch (SQLException ex) {
        System.out.println("Erro ao atualizar status do equipamento: " + ex.getMessage());
        return false;
    }
}
  public boolean insertEquipment (Equipment equipment) {
    String sqlStatement = "insert into Equipments (name, type, description, state, daily_value) values (?, ?, ?, ?, ?)";
    try {
      PreparedStatement stmt = connection.prepareStatement(sqlStatement);
      stmt.setString(1, equipment.getName());
      stmt.setString(2, equipment.getType());
      stmt.setString(3, equipment.getDescription());
      stmt.setString(4, equipment.getStatus());
      stmt.setDouble(5, equipment.getDailyValue());
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
  public ObservableList<Equipment> getAllEquipments() {
      ObservableList<Equipment> equipments = FXCollections.observableArrayList();
      String sqlStatement = "select * from Equipments";
      try {
        PreparedStatement stmt = connection.prepareStatement(sqlStatement);
        ResultSet rs = stmt.executeQuery();
        Status status;
        while (rs.next()) {
          switch (rs.getString("state")) {
            case "disponível":
              status = Status.AVAILABLE;
              break;
            case "alugado":
              status = Status.RENTED;
            default:
              status = Status.MAINTENANCE;
              break;
          }
          Equipment equipment = new Equipment(rs.getString("name"), rs.getString("type"), rs.getString("description"), status, rs.getDouble("daily_value"));
          equipments.add(equipment);
        }
      } catch (SQLException ex) {
        System.out.println("Erro ao obter equipamentos: " + ex.getMessage());
      }
      return equipments;
  }
  public ObservableList<Equipment> getAvailableEquipments() {
    ObservableList<Equipment> equipments = FXCollections.observableArrayList();
    String sqlStatement = "SELECT id, name, type, description, daily_value FROM Equipments WHERE state = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(sqlStatement)) {
        stmt.setString(1, Status.AVAILABLE.getDescription());
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Equipment equipment = new Equipment(
                rs.getInt("id"),
                rs.getString("name"), 
                rs.getString("type"), 
                rs.getString("description"), 
                Status.AVAILABLE, 
                rs.getDouble("daily_value")
            );
            equipments.add(equipment);
        }
    } catch (SQLException ex) {
        System.out.println("Erro ao obter equipamentos disponíveis: " + ex.getMessage());
    }
    return equipments;
}
}
