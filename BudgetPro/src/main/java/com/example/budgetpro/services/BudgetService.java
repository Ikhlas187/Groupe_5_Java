package com.example.budgetpro.services;

import com.example.budgetpro.models.Budget;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class BudgetService {

    /**
     * Initialise les budgets du mois pour toutes les catégories.
     * Initialise aussi le budgetTotal du mois (id_categorie IS NULL, mois = premier jour du mois)
     * en copiant le montant du budget de référence (id_categorie IS NULL, mois IS NULL).
     * INSERT IGNORE garantit qu'on ne retouche pas un mois déjà initialisé.
     */
    public static void initBudgetsMois(int userId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();
            LocalDate premierJourMois = mois.atDay(1);

            // 1. Initialiser le budgetTotal du mois si absent
            //    Pattern : id_categorie IS NULL + mois = premier jour du mois
            String initTotalSql = """
                INSERT IGNORE INTO budget (id_utilisateur, id_categorie, montant, mois)
                SELECT ?, NULL,
                    COALESCE((SELECT montant FROM budget b2
                               WHERE b2.id_utilisateur = ? AND b2.id_categorie IS NULL AND b2.mois IS NULL
                               LIMIT 1), 0),
                    ?
                FROM dual
                WHERE NOT EXISTS (
                    SELECT 1 FROM budget b3
                    WHERE b3.id_utilisateur = ? AND b3.id_categorie IS NULL AND b3.mois = ?
                )
            """;
            PreparedStatement initTotalStmt = conn.prepareStatement(initTotalSql);
            initTotalStmt.setInt(1, userId);
            initTotalStmt.setInt(2, userId);
            initTotalStmt.setDate(3, Date.valueOf(premierJourMois));
            initTotalStmt.setInt(4, userId);
            initTotalStmt.setDate(5, Date.valueOf(premierJourMois));
            initTotalStmt.executeUpdate();

            // 2. Initialiser les budgets de catégories à 0 pour ce mois
            String getCatSql = "SELECT id_categorie FROM categorie WHERE id_utilisateur = ?";
            PreparedStatement getCatStmt = conn.prepareStatement(getCatSql);
            getCatStmt.setInt(1, userId);
            ResultSet rs = getCatStmt.executeQuery();

            while (rs.next()) {
                int categorieId = rs.getInt("id_categorie");
                String insertSql = """
                    INSERT IGNORE INTO budget (montant, id_utilisateur, id_categorie, mois)
                    VALUES (0, ?, ?, ?)
                """;
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

    /**
     * Récupère le budgetTotal du mois affiché.
     * Cherche la ligne : id_categorie IS NULL + mois = premier jour du mois.
     * Ce montant représente le budget de départ du mois, augmenté par les revenus ajoutés.
     */
    public static double getBudgetTotalMois(int userId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();
            LocalDate premierJour = mois.atDay(1);

            String sql = """
                SELECT montant FROM budget
                WHERE id_utilisateur = ? AND id_categorie IS NULL AND mois = ?
                LIMIT 1
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(premierJour));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("montant");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Met à jour le budgetTotal du mois affiché uniquement.
     * N'affecte aucun autre mois ni le budget de référence (mois IS NULL).
     */
    public static boolean updateBudgetTotalMois(int userId, YearMonth mois, double nouveauMontant) {
        try {
            Connection conn = Database.getConnection();
            LocalDate premierJour = mois.atDay(1);

            String sql = """
                UPDATE budget SET montant = ?
                WHERE id_utilisateur = ? AND id_categorie IS NULL AND mois = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, nouveauMontant);
            stmt.setInt(2, userId);
            stmt.setDate(3, Date.valueOf(premierJour));

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Ajoute un montant au budgetTotal du mois (appelé lors de l'ajout d'un revenu).
     * N'affecte que le mois de la date du revenu.
     */
    public static boolean ajouterAuBudgetTotalMois(int userId, YearMonth mois, double montantRevenu) {
        try {
            Connection conn = Database.getConnection();
            LocalDate premierJour = mois.atDay(1);

            // S'assurer que la ligne du mois existe avant d'incrémenter
            initBudgetsMois(userId, mois);

            String sql = """
                UPDATE budget SET montant = montant + ?
                WHERE id_utilisateur = ? AND id_categorie IS NULL AND mois = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, montantRevenu);
            stmt.setInt(2, userId);
            stmt.setDate(3, Date.valueOf(premierJour));

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère le budget initial de référence (entré à l'inscription, mois IS NULL).
     * Sert uniquement à initialiser les nouveaux mois.
     */
    public static double getBudgetInitial(int userId) {
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT montant FROM budget WHERE id_utilisateur = ? AND id_categorie IS NULL AND mois IS NULL";
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

    // ============================================================
    // Méthodes pour les budgets de catégories (inchangées)
    // ============================================================

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
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static double getTotalBudgetsCategoriesMois(int userId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();
            LocalDate premierJour = mois.atDay(1);

            // Exclure la ligne budgetTotal du mois (id_categorie IS NULL) - on veut seulement les catégories
            String sql = """
                SELECT COALESCE(SUM(montant), 0) as total FROM budget
                WHERE id_utilisateur = ? AND mois = ? AND id_categorie IS NOT NULL
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(premierJour));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Mise à jour du budget de référence (mois IS NULL).
     * N'affecte pas les mois déjà initialisés.
     */
    public static boolean updateBudgetInitial(int userId, double nouveauMontant) {
        try {
            Connection conn = Database.getConnection();
            String sql = "UPDATE budget SET montant = ? WHERE id_utilisateur = ? AND id_categorie IS NULL AND mois IS NULL";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, nouveauMontant);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}