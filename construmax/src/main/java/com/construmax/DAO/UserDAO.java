package com.construmax.DAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import com.construmax.App;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.Session;
import com.construmax.Model.User;
import com.construmax.Utils.Toast;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserDAO {
    private Connection connection;
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static boolean execute;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }
    public User findUserByIdentifier(String identifier) throws SQLException {
        String sql = "SELECT id, name FROM Users WHERE email = ? OR cpf = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    return user; 
                }
            }
        }
        return null;
    }

    public void authenticateUser(String email, String password) {
        String sqlStatement = "select * from Users where email = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sqlStatement);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String passwordAuth = rs.getString("password");
                if (passwordEncoder.matches(password, passwordAuth)) {
                    try {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setEmail(rs.getString("email"));
                        user.setName(rs.getString("name"));
                        user.setPhone(rs.getString("phone"));
                        user.setCPF(rs.getString("cpf"));
                        Session.setUser(user);
                        DatabaseConnection.getDisconnect();
                        App.setRoot("home");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Toast.showToastError("Senha incorreta!");
                }
                return;
            }
            Toast.showToastError("Conta não encontrada!");
        } catch (SQLException ex) {
            System.out.println("Erro ao fazer select: " + ex.getMessage());
        }
    }
    public User getUserByCpf(String cpf) {
    String sql = "SELECT id, name, email, phone, cpf FROM Users WHERE cpf = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, cpf);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setCPF(rs.getString("cpf"));
                user.setVIP(false);
                return user; 
            }
        }
    } catch (SQLException ex) {
        System.out.println("Erro ao buscar usuário por CPF: " + ex.getMessage());
    }
    return null;
}

    public void insertUser(User user) {
        String sqlStatement = "insert into Users (name, password, phone, cpf, email) values (?, ?, ?, ?, ?)";

        try {
            PreparedStatement stmt = connection.prepareStatement(sqlStatement);
            stmt.setString(1, user.getName());
            stmt.setString(2, passwordEncoder.encode(user.getPassword()));
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getCPF());
            stmt.setString(5, user.getEmail());
            Toast.showToastSucess("Cadastro Realizado!");
            stmt.executeUpdate();
            execute = true;
            DatabaseConnection.getDisconnect();
        } catch (SQLException ex) {
            System.out.println("Erro ao inserir usuário: " + ex.getMessage());
            execute = false;
        }
        if (execute) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        App.setRoot("login");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }, 3000);
            return;
        }
    }
}
