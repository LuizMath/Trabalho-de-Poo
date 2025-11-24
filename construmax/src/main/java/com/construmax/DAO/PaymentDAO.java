package com.construmax.DAO;

import java.sql.*;
import java.time.LocalDate;

import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.Payment;
import com.construmax.Utils.Toast;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PaymentDAO {
    private Connection connection;

    public PaymentDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean insertPayment(Payment payment) {
        String sql = "INSERT INTO Payment (id_contract, amount, due_date, status, late_fee) VALUES (?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, payment.getContractId());
            stmt.setDouble(2, payment.getAmount());
            stmt.setDate(3, Date.valueOf(payment.getDueDate()));
            stmt.setString(4, payment.getStatus());
            stmt.setDouble(5, payment.getLateFee());
            
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            
            if (rs.next()) {
                payment.setId(rs.getInt(1));
                return true;
            }
        } catch (SQLException ex) {
            Toast.showToastError("Erro ao criar pagamento!");
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return false;
    }

    public boolean updatePayment(Payment payment) {
        String sql = "UPDATE Payment SET payment_date = ?, status = ?, payment_method = ?, late_fee = ? WHERE id_payment = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            
            if (payment.getPaymentDate() != null) {
                stmt.setDate(1, Date.valueOf(payment.getPaymentDate()));
            } else {
                stmt.setNull(1, Types.DATE);
            }
            
            stmt.setString(2, "pago");
            stmt.setString(3, payment.getPaymentMethod());
            stmt.setDouble(4, payment.getLateFee());
            stmt.setInt(5, payment.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                Toast.showToastSucess("Pagamento registrado!");
                return true;
            }
        } catch (SQLException ex) {
            Toast.showToastError("Erro ao atualizar pagamento!");
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return false;
    }

    public ObservableList<Payment> getPaymentsByUserId(int userId) {
        ObservableList<Payment> payments = FXCollections.observableArrayList();
        String sql = "SELECT p.* FROM Payment p " +
                     "INNER JOIN Contract c ON p.id_contract = c.id_contract " +
                     "WHERE c.id_user = ? " +
                     "ORDER BY p.due_date DESC";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Payment payment = new Payment();
                payment.setId(rs.getInt("id_payment"));
                payment.setContractId(rs.getInt("id_contract"));
                payment.setAmount(rs.getDouble("amount"));
                payment.setDueDate(rs.getDate("due_date").toLocalDate());
                
                if (rs.getDate("payment_date") != null) {
                    payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
                }
                
                payment.setStatus(rs.getString("status"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setLateFee(rs.getDouble("late_fee"));
                
                // Atualiza status se estiver atrasado
                if ("pendente".equals(payment.getStatus()) && 
                    LocalDate.now().isAfter(payment.getDueDate())) {
                    payment.setStatus("atrasado");
                }
                
                payments.add(payment);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return payments;
    }

    public ObservableList<Payment> getPendingPayments() {
        ObservableList<Payment> payments = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Payment WHERE status IN ('pendente', 'atrasado') ORDER BY due_date ASC";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Payment payment = new Payment();
                payment.setId(rs.getInt("id_payment"));
                payment.setContractId(rs.getInt("id_contract"));
                payment.setAmount(rs.getDouble("amount"));
                payment.setDueDate(rs.getDate("due_date").toLocalDate());
                
                if (rs.getDate("payment_date") != null) {
                    payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
                }
                
                payment.setStatus(rs.getString("status"));
                payment.setLateFee(rs.getDouble("late_fee"));
                
                // Atualiza status se estiver atrasado
                if ("pendente".equals(payment.getStatus()) && 
                    LocalDate.now().isAfter(payment.getDueDate())) {
                    payment.setStatus("atrasado");
                }
                
                payments.add(payment);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return payments;
    }

    public Payment getPaymentByContractId(int contractId) {
        String sql = "SELECT * FROM Payment WHERE id_contract = ?";
        Payment payment = null;
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, contractId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                payment = new Payment();
                payment.setId(rs.getInt("id_payment"));
                payment.setContractId(rs.getInt("id_contract"));
                payment.setAmount(rs.getDouble("amount"));
                payment.setDueDate(rs.getDate("due_date").toLocalDate());
                
                if (rs.getDate("payment_date") != null) {
                    payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
                }
                
                payment.setStatus(rs.getString("status"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setLateFee(rs.getDouble("late_fee"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return payment;
    }

    public boolean deletePayment(int paymentId) {
        String sql = "DELETE FROM Payment WHERE id_payment = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, paymentId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
        return false;
    }

    /**
     * Atualiza automaticamente status de pagamentos atrasados
     */
    public void updateOverduePayments() {
        String sql = "UPDATE Payment SET status = 'atrasado' " +
                     "WHERE status = 'pendente' AND due_date < CURDATE()";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.getDisconnect();
        }
    }
}