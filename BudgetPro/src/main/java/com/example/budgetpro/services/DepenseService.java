package com.example.budgetpro.services;

import java.sql.*;
import java.time.LocalDate;
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

    public static boolean ajouterDepense(double montant, String description, LocalDate date,
                                         int sousCategorieId, int userId) {
        try {
            Connection conn = Database.getConnection();

            // Récupérer l'id_categorie depuis la sous-catégorie
            String getCatSql = "SELECT id_categorie FROM sous_categorie WHERE id_sous_categorie = ?";
            PreparedStatement getCatStmt = conn.prepareStatement(getCatSql);
            getCatStmt.setInt(1, sousCategorieId);
            ResultSet rsCat = getCatStmt.executeQuery();

            int categorieId = 0;
            if (rsCat.next()) {
                categorieId = rsCat.getInt("id_categorie");
            }

            // Insérer la dépense
            String sql = """
                INSERT INTO depense (montant, description, date, id_sous_categorie, id_utilisateur, id_categorie) 
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, montant);
            stmt.setString(2, description);
            stmt.setDate(3, Date.valueOf(date));
            stmt.setInt(4, sousCategorieId);
            stmt.setInt(5, userId);
            stmt.setInt(6, categorieId);

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}