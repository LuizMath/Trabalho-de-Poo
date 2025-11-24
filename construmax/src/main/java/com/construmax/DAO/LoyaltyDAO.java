package com.construmax.DAO;

import java.sql.*;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.LoyaltyPoints;
import com.construmax.Utils.Toast;

public class LoyaltyDAO {
    private Connection connection;

    public LoyaltyDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean insertLoyalty(LoyaltyPoints loyalty) {
        String sql = "INSERT INTO LoyaltyPoints (id_user, points, total_contracts, total_spent) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, loyalty.getUserId());
            stmt.setInt(2, loyalty.getPoints());
            stmt.setInt(3, loyalty.getTotalContracts());
            stmt.setDouble(4, loyalty.getTotalSpent());
            
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            
            if (rs.next()) {
                loyalty.setId(rs.getInt(1));
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return false;
    }

    public boolean updateLoyalty(LoyaltyPoints loyalty) {
        String sql = "UPDATE LoyaltyPoints SET points = ?, total_contracts = ?, total_spent = ? WHERE id_user = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, loyalty.getPoints());
            stmt.setInt(2, loyalty.getTotalContracts());
            stmt.setDouble(3, loyalty.getTotalSpent());
            stmt.setInt(4, loyalty.getUserId());
            
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return false;
    }

    public LoyaltyPoints getLoyaltyByUserId(int userId) {
        String sql = "SELECT * FROM LoyaltyPoints WHERE id_user = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                LoyaltyPoints loyalty = new LoyaltyPoints();
                loyalty.setId(rs.getInt("id_loyalty"));
                loyalty.setUserId(rs.getInt("id_user"));
                loyalty.setPoints(rs.getInt("points"));
                loyalty.setTotalContracts(rs.getInt("total_contracts"));
                loyalty.setTotalSpent(rs.getDouble("total_spent"));
                return loyalty;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return null;
    }

    public void addPointsForContract(int userId, double contractValue) {
        LoyaltyPoints loyalty = getLoyaltyByUserId(userId);
        
        if (loyalty == null) {
            loyalty = new LoyaltyPoints(userId);
            loyalty.addPoints(contractValue);
            insertLoyalty(loyalty);
        } else {
            loyalty.addPoints(contractValue);
            updateLoyalty(loyalty);
        }

        // Verifica se deve promover a VIP
        if (loyalty.shouldBeVIP()) {
            promoteToVIP(userId);
        }
    }

    private void promoteToVIP(int userId) {
        String sql = "UPDATE Users SET is_vip = TRUE WHERE id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            Toast.showToastSucess("Parabéns! Você foi promovido a cliente VIP!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
    }

    public boolean usePoints(int userId, int pointsToUse) {
        LoyaltyPoints loyalty = getLoyaltyByUserId(userId);
        
        if (loyalty != null && loyalty.usePoints(pointsToUse)) {
            return updateLoyalty(loyalty);
        }
        return false;
    }
}