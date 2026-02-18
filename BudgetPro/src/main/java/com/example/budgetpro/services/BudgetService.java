package com.example.budgetpro.services;

import com.example.budgetpro.models.Budget;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class BudgetService {

    public static void initBudgetsMois(int userId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();

            String getCatSql = "SELECT id_categorie FROM categorie WHERE id_utilisateur = ?";
            PreparedStatement getCatStmt = conn.prepareStatement(getCatSql);
            getCatStmt.setInt(1, userId);
            ResultSet rs = getCatStmt.executeQuery();

            LocalDate premierJourMois = mois.atDay(1);

            while (rs.next()) {
                int categorieId = rs.getInt("id_categorie");

                String insertSql = "INSERT IGNORE INTO budget (montant, id_utilisateur, id_categorie, mois) VALUES (0, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, categorieId);
                insertStmt.setDate(3, Date.valueOf(premierJourMois));
                insertStmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Budget getBudgetCategorieParMois(int userId, int categorieId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();
            LocalDate premierJour = mois.atDay(1);

            String sql = "SELECT * FROM budget WHERE id_utilisateur = ? AND id_categorie = ? AND mois = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, categorieId);
            stmt.setDate(3, Date.valueOf(premierJour));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Budget budget = new Budget();
                budget.setIdBudget(rs.getInt("id_budget"));
                budget.setMontant(rs.getDouble("montant"));
                budget.setUserId(rs.getInt("id_utilisateur"));
                budget.setCategorieId(rs.getInt("id_categorie"));
                budget.setMois(rs.getDate("mois").toLocalDate());
                return budget;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean updateBudget(int budgetId, double nouveauMontant) {
        try {
            Connection conn = Database.getConnection();
            String sql = "UPDATE budget SET montant = ? WHERE id_budget = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, nouveauMontant);
            stmt.setInt(2, budgetId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static double getTotalBudgetsMois(int userId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();
            LocalDate premierJour = mois.atDay(1);

            String sql = "SELECT COALESCE(SUM(montant), 0) as total FROM budget WHERE id_utilisateur = ? AND mois = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(premierJour));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }
        public static double getBudgetInitial(int userId) {
            try {
                Connection conn = Database.getConnection();

                String sql = "SELECT montant FROM budget WHERE id_utilisateur = ? AND id_categorie IS NULL";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);


                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getDouble("montant");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0.0;
        }
}