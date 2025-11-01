package com.construmax.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.construmax.Model.User;

public class UserDAO {
    private Connection connection;
    public UserDAO (Connection connection) {
        this.connection = connection;
    }

    public void insertUser (User user) {
        System.out.println(user.getCPF());
        System.out.println(user.getPassword());
        System.out.println(user.getEmail());
        System.out.println(user.getPhone());
        String sqlStatement = "insert into Users (name, password, phone, cpf, email) values (?, ?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sqlStatement);
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getCPF());
            stmt.setString(5, user.getEmail());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Erro ao inserir usuário: " + ex.getMessage());
        }
    } 
}
