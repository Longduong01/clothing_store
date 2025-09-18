package com.example.demo_store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        String connectionUrl = "jdbc:sqlserver://LONGK\\SQLEXPRESS:1433;databaseName=ClothingStoreDB;encrypt=false;trustServerCertificate=true";
        String username = "sa";
        String password = "12345678";
        
        try {
            System.out.println("Testing database connection...");
            Connection connection = DriverManager.getConnection(connectionUrl, username, password);
            System.out.println("✅ Database connection successful!");
            System.out.println("Database: " + connection.getCatalog());
            System.out.println("Server: " + connection.getMetaData().getDatabaseProductName());
            connection.close();
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
