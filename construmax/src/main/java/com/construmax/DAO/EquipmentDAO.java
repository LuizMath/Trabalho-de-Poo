package com.construmax.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.Equipment;
import com.construmax.Model.Stock;
import com.construmax.Utils.Toast;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EquipmentDAO {
  private Connection connection;
  public EquipmentDAO (Connection connection) {
    this.connection = connection;
  }
  /*public boolean updateEquipmentStatus(List<Equipment> equipments, Status newStatus) {
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
}*/
  public boolean insertEquipment (Equipment equipment) {
    String sqlStatement = "insert into Equipments (name, type, description, daily_value, quantity) values (?, ?, ?, ?, ?)";
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
  public ObservableList<Stock> getAllEquipments() {
      ObservableList<Stock> equipments = FXCollections.observableArrayList();
      String sqlStatement = "SELECT Eq.name, Eq.type, Eq.quantity, Eq.description, Eq.daily_value, St.total_quantity, St.available_quantity, St.maintenance_quantity, St.in_use_quantity FROM Equipments as Eq INNER JOIN Stock as St ON Eq.id=St.id_equipment";
      try {
        PreparedStatement stmt = connection.prepareStatement(sqlStatement);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
          Stock stockItem = new Stock(rs.getString("name"), rs.getString("type"), rs.getString("description"), rs.getDouble("daily_value"), rs.getInt("total_quantity"), rs.getInt("available_quantity"), rs.getInt("maintenance_quantity"), rs.getInt("in_use_quantity"));
          equipments.add(stockItem);
        }
      } catch (SQLException ex) {
        System.out.println("Erro ao obter equipamentos: " + ex.getMessage());
      }
      return equipments;
  }
    public ObservableList<Stock> getAvailableEquipments() {
    ObservableList<Stock> equipments = FXCollections.observableArrayList();
    String sqlStatement = "SELECT Eq.name, Eq.type, Eq.quantity, Eq.description, Eq.daily_value, St.total_quantity, St.available_quantity, St.maintenance_quantity, St.in_use_quantity FROM Equipments as Eq INNER JOIN Stock as St ON Eq.id=St.id_equipment";

    try (PreparedStatement stmt = connection.prepareStatement(sqlStatement)) {
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
          if (rs.getInt("available_quantity") > 1) {
            Stock equipment = new Stock(rs.getString("name"), rs.getString("type"), rs.getString("description"), rs.getDouble("daily_value"), rs.getInt("total_quantity"), rs.getInt("available_quantity"), rs.getInt("maintenance_quantity"), rs.getInt("in_use_quantity"));
            equipments.add(equipment);
            System.out.println(rs.getString("name"));
          }
       }
    } catch (SQLException ex) {
        System.out.println("Erro ao obter equipamentos disponíveis: " + ex.getMessage());
    }
    return equipments;
  }
}
