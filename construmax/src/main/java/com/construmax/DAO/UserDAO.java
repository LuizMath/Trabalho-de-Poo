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
import com.construmax.Model.User;
import com.construmax.Utils.Toast;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserDAO {
    private Connection connection;
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static boolean execute;
    public UserDAO (Connection connection) {
        this.connection = connection;
    }

    public void authenticateUser (String email) {
        String sqlStatement = "select email, password from Users where email = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sqlStatement);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String emailx = rs.getString("email");
                String password = rs.getString("password");
                System.out.println(emailx + password);
            }
            DatabaseConnection.getDisconnect();
        } catch (SQLException ex) {
            System.out.println("Erro ao fazer select: " + ex.getMessage());
        }
    }

    public void insertUser (User user) {
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
                public void run () {
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
