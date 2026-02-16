package com.example.budgetpro.services;

import java.sql.*;
import java.time.YearMonth;

public class DepenseService {

    public static double getTotalDepensesSousCategorie(int userId, int sousCategorieId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();

            String sql = """
                SELECT COALESCE(SUM(montant), 0) as total 
                FROM depense 
                WHERE id_utilisateur = ? 
                AND id_sous_categorie = ? 
                AND DATE_FORMAT(date, '%Y-%m') = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, sousCategorieId);
            stmt.setString(3, mois.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public static double getTotalDepensesCategorie(int userId, int categorieId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();

            String sql = """
                SELECT COALESCE(SUM(d.montant), 0) as total 
                FROM depense d
                JOIN sous_categorie sc ON d.id_sous_categorie = sc.id_sous_categorie
                WHERE d.id_utilisateur = ? 
                AND sc.id_categorie = ? 
                AND DATE_FORMAT(d.date,'%Y-%m') = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, categorieId);
            stmt.setString(3, mois.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public static double getTotalDepensesMois(int userId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();

            String sql = """
                SELECT COALESCE(SUM(montant), 0) as total 
                FROM depense 
                WHERE id_utilisateur = ? 
                AND DATE_FORMAT(date,'%Y-%m') = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, mois.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }
}