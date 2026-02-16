package com.example.budgetpro.services;

import com.example.budgetpro.models.Categorie;
import com.example.budgetpro.models.SousCategorie;
import java.sql.*;
import java.util.*;

public class CategorieService {

    /**
     * Initialise les catégories par défaut (sans budget ni mois)
     */
    public static void initCategoriesDefaut(int userId) {
        Map<String, List<String>> categoriesDefaut = new HashMap<>();

        categoriesDefaut.put("Alimentation", List.of("Boissons", "Courses", "Nourriture", "Restaurant"));
        categoriesDefaut.put("Logement", List.of("Eau", "Electricité", "Internet", "Loyer", "TV", "Téléphone", "Entretien", "Assurance"));
        categoriesDefaut.put("Santé", List.of("Frais d'hopitaux", "Médicaments"));
        categoriesDefaut.put("Style de vie", List.of("Animal de compagnie", "Cadeau", "Hotel", "Voyages", "Travail", "Vetements"));
        categoriesDefaut.put("Economies", List.of("Fonds d'urgence", "Epargne"));
        categoriesDefaut.put("Transport", List.of("Assurance voiture", "Essence", "Réparation", "Taxi", "Transports publics"));
        categoriesDefaut.put("Divers", List.of("Divers", "Frais bancaires", "Inconnu", "Prêt étudiant"));
        categoriesDefaut.put("Amusements", List.of("Abonnements", "Boite de nuit", "Cinéma", "Concert", "Passion", "Salle de sports", "Sports", "Vacances", "Electronique"));

        try {
            Connection conn = Database.getConnection();

            for (Map.Entry<String, List<String>> entry : categoriesDefaut.entrySet()) {
                String nomCategorie = entry.getKey();
                List<String> sousCategories = entry.getValue();

                // Créer la catégorie (SANS budget ni mois)
                String insertCatSql = "INSERT IGNORE INTO categorie (nom_categorie, id_utilisateur) VALUES (?, ?)";
                PreparedStatement catStmt = conn.prepareStatement(insertCatSql, Statement.RETURN_GENERATED_KEYS);
                catStmt.setString(1, nomCategorie);
                catStmt.setInt(2, userId);
                catStmt.executeUpdate();

                // Récupérer l'ID
                ResultSet rs = catStmt.getGeneratedKeys();
                int categorieId = 0;
                if (rs.next()) {
                    categorieId = rs.getInt(1);
                }

                // Créer les sous-catégories
                for (String nomSousCategorie : sousCategories) {
                    String insertSousCatSql = "INSERT INTO sous_categorie (nom_sous_categorie, id_categorie) VALUES (?, ?)";
                    PreparedStatement sousCatStmt = conn.prepareStatement(insertSousCatSql);
                    sousCatStmt.setString(1, nomSousCategorie);
                    sousCatStmt.setInt(2, categorieId);
                    sousCatStmt.executeUpdate();
                }
            }

            System.out.println("✅ Catégories créées pour l'utilisateur " + userId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère toutes les catégories d'un utilisateur
     */
    public static List<Categorie> getCategoriesByUser(int userId) {
        List<Categorie> categories = new ArrayList<>();

        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT * FROM categorie WHERE id_utilisateur = ? ORDER BY nom_categorie";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Categorie cat = new Categorie();
                cat.setIdCategorie(rs.getInt("id_categorie"));
                cat.setNomCategorie(rs.getString("nom_categorie"));
                cat.setUserId(rs.getInt("id_utilisateur"));
                categories.add(cat);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    /**
     * Récupère les sous-catégories d'une catégorie
     */
    public static List<SousCategorie> getSousCategoriesByCategorie(int categorieId) {
        List<SousCategorie> sousCategories = new ArrayList<>();

        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT * FROM sous_categorie WHERE id_categorie = ? ORDER BY nom_sous_categorie";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, categorieId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SousCategorie sousCat = new SousCategorie();
                sousCat.setIdSousCategorie(rs.getInt("id_sous_categorie"));
                sousCat.setNomSousCategorie(rs.getString("nom_sous_categorie"));
                sousCat.setCategorieId(rs.getInt("id_categorie"));
                sousCategories.add(sousCat);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sousCategories;
    }
}