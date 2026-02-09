package com.example.budgetpro.services;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/gestion_finance";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion réussie !");
            return connection;
        } catch (SQLException e) {
            System.out.println("Erreur de connexion !");
            return null;
        }
    }
}
