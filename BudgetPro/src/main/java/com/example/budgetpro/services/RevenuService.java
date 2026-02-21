package com.example.budgetpro.services;

import com.example.budgetpro.models.Revenu;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class RevenuService {

    /**
     * Ajoute un revenu ET augmente le budgetTotal du mois concerné dans la table budget.
     * Le mois est déterminé par la date du revenu — seul ce mois est modifié.
     * La ligne budget ciblée est : id_categorie IS NULL + mois = premier jour du mois.
     */
    public static boolean ajouterRevenu(double montant, String description, LocalDate date, int userId) {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            // 1. Insérer le revenu
            String sql = "INSERT INTO revenu (montant, description, date, id_utilisateur) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, montant);
            stmt.setString(2, description);
            stmt.setDate(3, Date.valueOf(date));
            stmt.setInt(4, userId);

            if (stmt.executeUpdate() == 0) {
                conn.rollback();
                return false;
            }

            // 2. S'assurer que le budgetTotal de ce mois existe dans budget
            YearMonth moisRevenu = YearMonth.from(date);
            BudgetService.initBudgetsMois(userId, moisRevenu);

            // 3. Augmenter le budgetTotal du mois uniquement
            boolean budgetOk = BudgetService.ajouterAuBudgetTotalMois(userId, moisRevenu, montant);
            if (!budgetOk) {
                conn.rollback();
                System.err.println("❌ Impossible de mettre à jour le budget du mois " + moisRevenu);
                return false;
            }

            conn.commit();
            System.out.println("✅ Revenu ajouté : " + montant + " XOF — Budget de " + moisRevenu + " augmenté.");
            return true;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            System.err.println("❌ Erreur lors de l'ajout du revenu");
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * Récupère le total des revenus d'un utilisateur pour un mois donné.
     * (Pour l'historique et les statistiques uniquement — le budgetTotal dans budget est la source de vérité pour l'affichage.)
     */
    public static double getTotalRevenusMois(int userId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT COALESCE(SUM(montant), 0) as total FROM revenu " +
                    "WHERE id_utilisateur = ? AND DATE_FORMAT(date, '%Y-%m') = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, mois.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Récupère tous les revenus d'un utilisateur pour un mois donné.
     */
    public static List<Revenu> getRevenusMois(int userId, YearMonth mois) {
        List<Revenu> revenus = new ArrayList<>();
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT * FROM revenu WHERE id_utilisateur = ? AND DATE_FORMAT(date, '%Y-%m') = ? ORDER BY date DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, mois.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Revenu revenu = new Revenu();
                revenu.setIdRevenu(rs.getInt("id_revenu"));
                revenu.setMontant(rs.getDouble("montant"));
                revenu.setDescription(rs.getString("description"));
                revenu.setDate(rs.getDate("date").toLocalDate());
                revenu.setUserId(rs.getInt("id_utilisateur"));
                revenus.add(revenu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return revenus;
    }

    /**
     * Récupère un revenu par son ID.
     */
    public static Revenu getRevenuById(int revenuId) {
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT * FROM revenu WHERE id_revenu = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, revenuId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Revenu revenu = new Revenu();
                revenu.setIdRevenu(rs.getInt("id_revenu"));
                revenu.setMontant(rs.getDouble("montant"));
                revenu.setDescription(rs.getString("description"));
                revenu.setDate(rs.getDate("date").toLocalDate());
                revenu.setUserId(rs.getInt("id_utilisateur"));
                return revenu;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Supprime un revenu et soustrait son montant du budgetTotal du mois concerné.
     */
    public static boolean supprimerRevenu(int revenuId) {
        Connection conn = null;
        try {
            Revenu revenu = getRevenuById(revenuId);
            if (revenu == null) return false;

            conn = Database.getConnection();
            conn.setAutoCommit(false);

            String sql = "DELETE FROM revenu WHERE id_revenu = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, revenuId);
            if (stmt.executeUpdate() == 0) { conn.rollback(); return false; }

            // Soustraire le montant du budgetTotal du mois
            YearMonth mois = YearMonth.from(revenu.getDate());
            BudgetService.ajouterAuBudgetTotalMois(revenu.getUserId(), mois, -revenu.getMontant());

            conn.commit();
            System.out.println("✅ Revenu supprimé : ID " + revenuId);
            return true;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * Modifie un revenu et ajuste le budgetTotal du ou des mois concernés.
     */
    public static boolean modifierRevenu(int revenuId, double nouveauMontant,
                                         String nouvelleDescription, LocalDate nouvelleDate) {
        Connection conn = null;
        try {
            Revenu ancien = getRevenuById(revenuId);
            if (ancien == null) return false;

            conn = Database.getConnection();
            conn.setAutoCommit(false);

            String sql = "UPDATE revenu SET montant = ?, description = ?, date = ? WHERE id_revenu = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, nouveauMontant);
            stmt.setString(2, nouvelleDescription);
            stmt.setDate(3, Date.valueOf(nouvelleDate));
            stmt.setInt(4, revenuId);
            if (stmt.executeUpdate() == 0) { conn.rollback(); return false; }

            // Ajuster les budgetTotal selon que le mois a changé ou non
            YearMonth ancienMois = YearMonth.from(ancien.getDate());
            YearMonth nouveauMois = YearMonth.from(nouvelleDate);

            if (ancienMois.equals(nouveauMois)) {
                // Même mois : ajuster uniquement la différence
                double diff = nouveauMontant - ancien.getMontant();
                BudgetService.ajouterAuBudgetTotalMois(ancien.getUserId(), ancienMois, diff);
            } else {
                // Mois différent : soustraire de l'ancien, ajouter dans le nouveau
                BudgetService.ajouterAuBudgetTotalMois(ancien.getUserId(), ancienMois, -ancien.getMontant());
                BudgetService.ajouterAuBudgetTotalMois(ancien.getUserId(), nouveauMois, nouveauMontant);
            }

            conn.commit();
            System.out.println("✅ Revenu modifié : ID " + revenuId);
            return true;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    public static double getTotalRevenusGlobal(int userId) {
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT COALESCE(SUM(montant), 0) as total FROM revenu WHERE id_utilisateur = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static int getNombreRevenusMois(int userId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT COUNT(*) as total FROM revenu WHERE id_utilisateur = ? AND DATE_FORMAT(date, '%Y-%m') = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, mois.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}