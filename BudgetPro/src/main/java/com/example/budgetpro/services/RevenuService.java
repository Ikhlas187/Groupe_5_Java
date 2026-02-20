package com.example.budgetpro.services;

import com.example.budgetpro.models.Revenu;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class RevenuService {

    /**
     * Ajoute un nouveau revenu
     */
    public static boolean ajouterRevenu(double montant, String description, LocalDate date, int userId) {
        try {
            Connection conn = Database.getConnection();

            String sql = "INSERT INTO revenu (montant, description, date, id_utilisateur) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, montant);
            stmt.setString(2, description);
            stmt.setDate(3, Date.valueOf(date));
            stmt.setInt(4, userId);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Revenu ajouté : " + montant + " XOF - " + description);
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ajout du revenu");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère le total des revenus d'un utilisateur pour un mois donné
     */
    public static double getTotalRevenusMois(int userId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();

            String sql = "SELECT COALESCE(SUM(montant), 0) as total " +
                    "FROM revenu " +
                    "WHERE id_utilisateur = ? " +
                    "AND DATE_FORMAT(date, '%Y-%m') = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, mois.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble("total");
                System.out.println("📊 Total revenus pour " + mois + " : " + total + " XOF");
                return total;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du calcul des revenus");
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * Récupère tous les revenus d'un utilisateur pour un mois donné
     */
    public static List<Revenu> getRevenusMois(int userId, YearMonth mois) {
        List<Revenu> revenus = new ArrayList<>();

        try {
            Connection conn = Database.getConnection();

            String sql = "SELECT * FROM revenu " +
                    "WHERE id_utilisateur = ? " +
                    "AND DATE_FORMAT(date, '%Y-%m') = ? " +
                    "ORDER BY date DESC";

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

            System.out.println("📋 " + revenus.size() + " revenu(s) récupéré(s) pour " + mois);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des revenus");
            e.printStackTrace();
        }

        return revenus;
    }

    /**
     * Récupère un revenu par son ID
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
            System.err.println("❌ Erreur lors de la récupération du revenu");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Supprime un revenu
     */
    public static boolean supprimerRevenu(int revenuId) {
        try {
            Connection conn = Database.getConnection();

            String sql = "DELETE FROM revenu WHERE id_revenu = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, revenuId);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Revenu supprimé : ID " + revenuId);
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression du revenu");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifie un revenu existant
     */
    public static boolean modifierRevenu(int revenuId, double nouveauMontant,
                                         String nouvelleDescription, LocalDate nouvelleDate) {
        try {
            Connection conn = Database.getConnection();

            String sql = "UPDATE revenu SET montant = ?, description = ?, date = ? WHERE id_revenu = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, nouveauMontant);
            stmt.setString(2, nouvelleDescription);
            stmt.setDate(3, Date.valueOf(nouvelleDate));
            stmt.setInt(4, revenuId);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Revenu modifié : ID " + revenuId);
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la modification du revenu");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère le total des revenus d'un utilisateur (tous les mois)
     */
    public static double getTotalRevenusGlobal(int userId) {
        try {
            Connection conn = Database.getConnection();

            String sql = "SELECT COALESCE(SUM(montant), 0) as total FROM revenu WHERE id_utilisateur = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

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
     * Compte le nombre de revenus pour un utilisateur dans un mois
     */
    public static int getNombreRevenusMois(int userId, YearMonth mois) {
        try {
            Connection conn = Database.getConnection();

            String sql = "SELECT COUNT(*) as total FROM revenu " +
                    "WHERE id_utilisateur = ? " +
                    "AND DATE_FORMAT(date, '%Y-%m') = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, mois.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}