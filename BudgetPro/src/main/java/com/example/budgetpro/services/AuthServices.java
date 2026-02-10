package com.example.budgetpro.services;
import com.example.budgetpro.models.User;
import java.sql.*;
import java.time.LocalDateTime;
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
                currentUser.setId(rs.getInt("id_utilisateur"));
                currentUser.setNom(rs.getString("nom"));
                currentUser.setPrenom(rs.getString("prenom"));
                currentUser.setEmail(rs.getString("email"));
                currentUser.setTelephone(rs.getString("telephone"));

                System.out.println("✅ Connexion réussie : " + currentUser.getPrenom());
                return true;
            } else {
                System.out.println(" Email ou mot de passe incorrect");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion : " + e.getMessage());
            return false;
        }
    }

    public static boolean register(String nom, String prenom, String email,
                                   String password, String telephone,
                                   String sexe, Integer age, Double soldeInitial) {
        // ========== VALIDATIONS ==========

        if (nom == null || nom.trim().isEmpty()) {
            System.out.println("Nom vide");
            return false;
        }

        if (prenom == null || prenom.trim().isEmpty()) {
            System.out.println(" Prénom vide");
            return false;
        }

        if (email == null || email.trim().isEmpty()) {
            System.out.println(" Email vide");
            return false;
        }

        if (password == null || password.isEmpty()) {
            System.out.println(" Mot de passe vide");
            return false;
        }

        if (telephone == null || telephone.trim().isEmpty()) {
            System.out.println(" Téléphone vide");
            return false;
        }

        if (sexe == null || sexe.isEmpty()) {
            System.out.println(" Sexe non renseigné");
            return false;
        }

        if (age == null) {
            System.out.println(" Âge non renseigné");
            return false;
        }

        if (soldeInitial == null) {
            soldeInitial = 0.0;
        }

        // Validations métier
        if (nom.trim().length() < 2) {
            System.out.println("❌ Nom trop court (min 2 caractères)");
            return false;
        }

        if (prenom.trim().length() < 2) {
            System.out.println("❌ Prénom trop court (min 2 caractères)");
            return false;
        }

       /* if (!isValidEmail(email)) {
            System.out.println("❌ Format d'email invalide");
            return false;
        }*/

        if (password.length() < 6) {
            System.out.println("❌ Mot de passe trop court (min 6 caractères)");
            return false;
        }

        /*if (!isValidPhone(telephone)) {
            System.out.println("❌ Format de téléphone invalide");
            return false;
        }*/

        if (age < 13 || age > 120) {
            System.out.println("Âge invalide (doit être entre 13 et 120)");
            return false;
        }

        if (!sexe.equals("Homme") && !sexe.equals("Femme") && !sexe.equals("Autre")) {
            System.out.println(" Sexe invalide");
            return false;
        }

        if (soldeInitial < 0) {
            System.out.println("Solde initial ne peut pas être négatif");
            return false;
        }

        // Nettoyer les données
        nom = nom.trim();
        prenom = prenom.trim();
        email = email.trim().toLowerCase();
        telephone = telephone.trim();

        try {
            Connection conn = Database.getConnection();
            String checkSql = "SELECT id FROM utilisateur WHERE email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println(" Cet email est déjà utilisé");
                return false;
            }

            // ========== Vérifier si le téléphone existe déjà ==========
            String checkPhoneSql = "SELECT id FROM utilisateur WHERE telephone = ?";
            PreparedStatement checkPhoneStmt = conn.prepareStatement(checkPhoneSql);
            checkPhoneStmt.setString(1, telephone);
            ResultSet rsPhone = checkPhoneStmt.executeQuery();

            if (rsPhone.next()) {
                System.out.println("Ce numéro de téléphone est déjà utilisé");
                return false;
            }

            String insertSql = """
            INSERT INTO utilisateur (nom, prenom, email, password, telephone, sexe, age) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, nom);
            insertStmt.setString(2, prenom);
            insertStmt.setString(3, email);
            insertStmt.setString(4, password);
            insertStmt.setString(5, telephone);
            insertStmt.setString(6, sexe);
            insertStmt.setInt(7, age);

            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                int userId = 0;
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1);
                }

                // Créer la session automatiquement
                currentUser = new User(nom, prenom, email, password, telephone);
                currentUser.setId(userId);
                currentUser.setSexe(sexe);
                currentUser.setAge(age);
                currentUser.setSoldeInitial(soldeInitial);
                currentUser.setCreatedAt(LocalDateTime.now());

                System.out.println("✅ Inscription réussie : " + currentUser.getFullName());
                System.out.println("   - Sexe: " + sexe);
                System.out.println("   - Âge: " + age);


                return true;
            } else {
                System.out.println("❌ Erreur lors de l'inscription");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'inscription : " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }

    public static User getCurrentUser() {
        return currentUser;
    }

}
