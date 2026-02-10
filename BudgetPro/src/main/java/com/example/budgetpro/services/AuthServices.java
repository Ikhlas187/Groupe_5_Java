package com.example.budgetpro.services;
import com.example.budgetpro.models.User;
import java.sql.*;
import com.example.budgetpro.services.Database;
import java.util.regex.Pattern;

public class AuthServices {

    private static User currentUser = null;

    public static boolean login(String email, String password) {
        // Validation des entrées
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email vide");
            return false;
        }

        if (password == null || password.isEmpty()) {
            System.out.println("Mot de passe vide");
            return false;
        }

        // Nettoyer l'email
        email = email.trim().toLowerCase();

        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT * FROM utilisateur WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // ✅ Identifiants corrects : créer la session
                currentUser = new User();
                currentUser.setId(rs.getInt("id"));
                currentUser.setNom(rs.getString("nom"));
                currentUser.setPrenom(rs.getString("prenom"));
                currentUser.setEmail(rs.getString("email"));
                currentUser.setTelephone(rs.getString("telephone"));

                System.out.println("✅ Connexion réussie : " + currentUser.getPrenom());
                return true;
            } else {
                // ❌ Identifiants incorrects
                System.out.println("❌ Email ou mot de passe incorrect");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la connexion : " + e.getMessage());
            return false;
        }
    }

    public static boolean register(String nom, String prenom, String email,
                                   String password, String telephone) {
        // ========== VALIDATIONS ==========

        // 1. Vérifier que les champs ne sont pas vides
        if (nom == null || nom.trim().isEmpty()) {
            System.out.println("❌ Nom vide");
            return false;
        }

        if (prenom == null || prenom.trim().isEmpty()) {
            System.out.println("❌ Prénom vide");
            return false;
        }

        if (email == null || email.trim().isEmpty()) {
            System.out.println("❌ Email vide");
            return false;
        }

        if (password == null || password.isEmpty()) {
            System.out.println("❌ Mot de passe vide");
            return false;
        }

        if (telephone == null || telephone.trim().isEmpty()) {
            System.out.println("❌ Téléphone vide");
            return false;
        }

        // 2. Valider le nom et prénom (2 caractères minimum)
        if (nom.trim().length() < 2) {
            System.out.println("❌ Nom trop court (min 2 caractères)");
            return false;
        }

        if (prenom.trim().length() < 2) {
            System.out.println("❌ Prénom trop court (min 2 caractères)");
            return false;
        }

        // 3. Valider l'email (format correct)
        /*if (!isValidEmail(email)) {
            System.out.println("❌ Format d'email invalide");
            return false;
        }*/

        // 4. Valider le mot de passe (6 caractères minimum)
        if (password.length() < 6) {
            System.out.println("❌ Mot de passe trop court (min 6 caractères)");
            return false;
        }

        // 5. Valider le téléphone (format basique)
        /*if (!isValid(telephone)) {
            System.out.println("❌ Format de téléphone invalide");
            return false;
        }*/

        // Nettoyer les données
        nom = nom.trim();
        prenom = prenom.trim();
        email = email.trim().toLowerCase();
        telephone = telephone.trim();

        try {
            Connection conn = Database.getConnection();

            // ========== Vérifier si l'email existe déjà ==========
            String checkSql = "SELECT id FROM users WHERE email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // ❌ Email déjà utilisé
                System.out.println("❌ Cet email est déjà utilisé");
                return false;
            }

            // ========== Vérifier si le téléphone existe déjà ==========
            String checkPhoneSql = "SELECT id FROM users WHERE telephone = ?";
            PreparedStatement checkPhoneStmt = conn.prepareStatement(checkPhoneSql);
            checkPhoneStmt.setString(1, telephone);
            ResultSet rsPhone = checkPhoneStmt.executeQuery();

            if (rsPhone.next()) {
                // ❌ Téléphone déjà utilisé
                System.out.println("❌ Ce numéro de téléphone est déjà utilisé");
                return false;
            }

            // ========== Insérer le nouvel utilisateur ==========
            String insertSql = "INSERT INTO users (nom, prenom, email, password, telephone) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, nom);
            insertStmt.setString(2, prenom);
            insertStmt.setString(3, email);
            insertStmt.setString(4, password); // ⚠️ En production : hasher avec BCrypt !
            insertStmt.setString(5, telephone);

            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                // ✅ Inscription réussie

                // Récupérer l'ID auto-généré
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                int userId = 0;
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1);
                }

                // Créer la session automatiquement (connexion auto après inscription)
                currentUser = new User(nom, prenom, email, password, telephone);
                currentUser.setId(userId);

                System.out.println("✅ Inscription réussie : " + currentUser.getPrenom());
                return true;
            } else {
                // ❌ Erreur lors de l'insertion
                System.out.println("❌ Erreur lors de l'inscription");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'inscription : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }




}
