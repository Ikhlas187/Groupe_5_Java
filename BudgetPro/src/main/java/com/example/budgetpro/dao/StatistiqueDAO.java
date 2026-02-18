package com.example.budgetpro.dao;

import com.example.budgetpro.services.Database;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DAO pour les statistiques d'un utilisateur spécifique
 */
public class StatistiqueDAO {

    /**
     * Total des dépenses par catégorie pour un utilisateur
     */
    public Map<String, Double> getDepensesParCategorie(int userId) {
        Map<String, Double> data = new LinkedHashMap<>();

        String query = "SELECT c.nom_categorie, SUM(d.montant) as total " +
                "FROM depense d " +
                "JOIN sous_categorie sc ON d.id_sous_categorie = sc.id_sous_categorie " +
                "JOIN categorie c ON sc.id_categorie = c.id_categorie " +
                "WHERE d.id_utilisateur = ? " +  // ✅ AJOUT DU FILTRE
                "GROUP BY c.nom_categorie " +
                "ORDER BY total DESC";

        try {
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                data.put(rs.getString("nom_categorie"), rs.getDouble("total"));
            }

            System.out.println("✅ Dépenses par catégorie récupérées pour userId " + userId);

        } catch (SQLException e) {
            System.err.println("❌ Erreur dépenses par catégorie");
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Total des dépenses par jour de la semaine pour un utilisateur
     */
    public Map<String, Double> getDepensesParJour(int userId) {
        Map<String, Double> data = new LinkedHashMap<>();

        // Initialiser les jours dans l'ordre 
        data.put("Lun", 0.0);
        data.put("Mar", 0.0);
        data.put("Mer", 0.0);
        data.put("Jeu", 0.0);
        data.put("Ven", 0.0);
        data.put("Sam", 0.0);
        data.put("Dim", 0.0);

        String query = "SELECT DAYOFWEEK(date) as jour, SUM(montant) as total " +
                "FROM depense " +
                "WHERE id_utilisateur = ? " +  // ✅ AJOUT DU FILTRE
                "GROUP BY DAYOFWEEK(date) " +
                "ORDER BY jour";

        try {
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int jour = rs.getInt("jour");
                double total = rs.getDouble("total");

                // MySQL : 1=Dimanche, 2=Lundi, ..., 7=Samedi
                switch (jour) {
                    case 1 -> data.put("Dim", total);
                    case 2 -> data.put("Lun", total);
                    case 3 -> data.put("Mar", total);
                    case 4 -> data.put("Mer", total);
                    case 5 -> data.put("Jeu", total);
                    case 6 -> data.put("Ven", total);
                    case 7 -> data.put("Sam", total);
                }
            }

            System.out.println("✅ Dépenses par jour récupérées pour userId " + userId);

        } catch (SQLException e) {
            System.err.println("❌ Erreur dépenses par jour");
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Tendance mensuelle des dépenses pour un utilisateur
     */
    public Map<String, Double> getDepensesParMois(int userId) {
        Map<String, Double> data = new LinkedHashMap<>();

        // Initialiser les mois
        String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                "Jul", "Aoû", "Sept", "Oct", "Nov", "Déc"};
        for (String m : mois) {
            data.put(m, 0.0);
        }

        String query = "SELECT MONTH(date) as mois, SUM(montant) as total " +
                "FROM depense " +
                "WHERE id_utilisateur = ? " +  // ✅ AJOUT DU FILTRE
                "AND YEAR(date) = YEAR(CURDATE()) " +
                "GROUP BY MONTH(date) " +
                "ORDER BY mois";

        try {
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int moisNum = rs.getInt("mois");
                double total = rs.getDouble("total");
                data.put(mois[moisNum - 1], total);
            }

            System.out.println("✅ Dépenses par mois récupérées pour userId " + userId);

        } catch (SQLException e) {
            System.err.println("❌ Erreur dépenses par mois");
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Tendance mensuelle des revenus pour un utilisateur
     */
    public Map<String, Double> getRevenusParMois(int userId) {
        Map<String, Double> data = new LinkedHashMap<>();

        // Initialiser les mois
        String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        for (String m : mois) {
            data.put(m, 0.0);
        }

        String query = "SELECT MONTH(date) as mois, SUM(montant) as total " +
                "FROM revenu " +
                "WHERE id_utilisateur = ? " +  // ✅ AJOUT DU FILTRE
                "AND YEAR(date) = YEAR(CURDATE()) " +
                "GROUP BY MONTH(date) " +
                "ORDER BY mois";

        try {
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int moisNum = rs.getInt("mois");
                double total = rs.getDouble("total");
                data.put(mois[moisNum - 1], total);
            }

            System.out.println("✅ Revenus par mois récupérés pour userId " + userId);

        } catch (SQLException e) {
            System.err.println("❌ Erreur revenus par mois");
            e.printStackTrace();
        }

        return data;
    }

    /**
     * 🆕 BONUS : Total des dépenses pour un utilisateur
     */
    public double getTotalDepenses(int userId) {
        String query = "SELECT COALESCE(SUM(montant), 0) as total FROM depense WHERE id_utilisateur = ?";

        try {
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * 🆕 BONUS : Total des revenus pour un utilisateur
     */
    public double getTotalRevenus(int userId) {
        String query = "SELECT COALESCE(SUM(montant), 0) as total FROM revenu WHERE id_utilisateur = ?";

        try {
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * 🆕 BONUS : Solde actuel (revenus - dépenses)
     */
    public double getSolde(int userId) {
        return getTotalRevenus(userId) - getTotalDepenses(userId);
    }
}