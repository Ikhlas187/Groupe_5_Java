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
        Map<String, List<String>> categoriesDefaut = new LinkedHashMap<>();

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

                int categorieId = 0;

                // 🎯 ÉTAPE 1 : Vérifier si la catégorie existe déjà pour cet utilisateur
                String checkSql = "SELECT id_categorie FROM categorie WHERE nom_categorie = ? AND id_utilisateur = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, nomCategorie);
                checkStmt.setInt(2, userId);
                ResultSet rsCheck = checkStmt.executeQuery();

                if (rsCheck.next()) {
                    // ✅ La catégorie existe déjà, récupérer son ID
                    categorieId = rsCheck.getInt("id_categorie");
                    System.out.println("  ⚠️ Catégorie existe : " + nomCategorie + " (ID: " + categorieId + ")");
                } else {
                    // ✅ La catégorie n'existe pas, la créer
                    String insertCatSql = "INSERT INTO categorie (nom_categorie, id_utilisateur) VALUES (?, ?)";
                    PreparedStatement catStmt = conn.prepareStatement(insertCatSql, Statement.RETURN_GENERATED_KEYS);
                    catStmt.setString(1, nomCategorie);
                    catStmt.setInt(2, userId);
                    catStmt.executeUpdate();

                    ResultSet rs = catStmt.getGeneratedKeys();
                    if (rs.next()) {
                        categorieId = rs.getInt(1);
                        System.out.println("  ✅ Catégorie créée : " + nomCategorie + " (ID: " + categorieId + ")");
                    }
                }

                // 🎯 ÉTAPE 2 : Créer les sous-catégories (seulement si categorieId > 0)
                if (categorieId > 0) {
                    for (String nomSousCategorie : sousCategories) {
                        // Vérifier si la sous-catégorie existe déjà
                        String checkSousSql = "SELECT id_sous_categorie FROM sous_categorie WHERE nom_sous_categorie = ? AND id_categorie = ?";
                        PreparedStatement checkSousStmt = conn.prepareStatement(checkSousSql);
                        checkSousStmt.setString(1, nomSousCategorie);
                        checkSousStmt.setInt(2, categorieId);
                        ResultSet rsSousCheck = checkSousStmt.executeQuery();

                        if (!rsSousCheck.next()) {
                            // ✅ La sous-catégorie n'existe pas, la créer
                            String insertSousCatSql = "INSERT INTO sous_categorie (nom_sous_categorie, id_categorie) VALUES (?, ?)";
                            PreparedStatement sousCatStmt = conn.prepareStatement(insertSousCatSql);
                            sousCatStmt.setString(1, nomSousCategorie);
                            sousCatStmt.setInt(2, categorieId);
                            sousCatStmt.executeUpdate();
                        }
                    }
                }
            }

            System.out.println("✅ Catégories créées pour l'utilisateur " + userId);

        } catch (Exception e) {
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

    public static SousCategorie getSousCategorieById(int sousCategorieId) {
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT * FROM sous_categorie WHERE id_sous_categorie = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, sousCategorieId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                SousCategorie sousCat = new SousCategorie();
                sousCat.setIdSousCategorie(rs.getInt("id_sous_categorie"));
                sousCat.setNomSousCategorie(rs.getString("nom_sous_categorie"));
                sousCat.setCategorieId(rs.getInt("id_categorie"));
                return sousCat;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean ajouterSousCategorie(String nomSousCategorie, int categorieId) {
        try {
            Connection conn = Database.getConnection();

            // Vérifier si la sous-catégorie existe déjà
            String checkSql = "SELECT id_sous_categorie FROM sous_categorie WHERE nom_sous_categorie = ? AND id_categorie = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, nomSousCategorie);
            checkStmt.setInt(2, categorieId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("⚠️ Cette sous-catégorie existe déjà");
                return false;
            }

            // Créer la sous-catégorie
            String insertSql = "INSERT INTO sous_categorie (nom_sous_categorie, id_categorie) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, nomSousCategorie);
            insertStmt.setInt(2, categorieId);

            int rows = insertStmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Crée une nouvelle catégorie pour un utilisateur
     */
    public static int creerCategorie(String nomCategorie, int userId) {
        try {
            Connection conn = Database.getConnection();

            // Vérifier si la catégorie existe déjà pour cet utilisateur
            String checkSql = "SELECT id_categorie FROM categorie WHERE nom_categorie = ? AND id_utilisateur = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, nomCategorie);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("⚠️ Cette catégorie existe déjà");
                return 0;  // Échec
            }

            // Créer la catégorie
            String insertSql = "INSERT INTO categorie (nom_categorie, id_utilisateur) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, nomCategorie);
            insertStmt.setInt(2, userId);

            int rows = insertStmt.executeUpdate();

            if (rows > 0) {
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int categorieId = generatedKeys.getInt(1);
                    System.out.println("✅ Catégorie créée : " + nomCategorie + " (ID: " + categorieId + ")");
                    return categorieId;  // Retourner l'ID de la nouvelle catégorie
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;  // Échec
    }



}