package com.construmax.Database;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConnection {
    

    // Método que cria e devolve uma conexão pronta
    public static Connection getConnection() {
        try {
            String url = dotenv.get("DB_URL");
            String user = dotenv.get("DB_USER");
            String password = dotenv.get("DB_PASSWORD");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Conectado ao banco!");
            return conn;
        } catch (SQLException e) {
            System.out.println("❌ Erro ao conectar: " + e.getMessage());
            return null;
        }
    }
}
