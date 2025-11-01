package com.construmax.Database;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.DriverManager;
import java.sql.Connection;

public class DatabaseConnection {
    private static Dotenv dotenv;
    private static Connection connection;
    public static void init (Dotenv d) {
        dotenv = d;
    }
    public static Connection getConnection() {
        try {
            String url = dotenv.get("DB_URL");
            String user = dotenv.get("DB_USER");
            String password = dotenv.get("DB_PASSWORD");
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Conectado ao banco!");
            return connection;
        } catch (SQLException e) {
            System.out.println("Erro ao conectar: " + e.getMessage());
            return null;
        }
    }
    public static void getDisconnect () {
        try {
            connection.close();
            connection = null;
            System.out.println("Conexão encerrada com sucesso!");
        } catch (SQLException ex) {
            System.out.println("Erro ao fechar conexão: " + ex.getMessage());
        }
    }
}
