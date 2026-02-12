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

        Connection conn = null;

        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); // ← IMPORTANT : Transaction

            // ========== Vérifier si l'email existe déjà ==========
            String checkSql = "SELECT id_utilisateur FROM utilisateur WHERE email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("❌ Cet email est déjà utilisé");
                conn.rollback(); // Annuler la transaction
                return false;
            }

            // ========== Vérifier si le téléphone existe déjà ==========
            String checkPhoneSql = "SELECT id_utilisateur FROM utilisateur WHERE telephone = ?";
            PreparedStatement checkPhoneStmt = conn.prepareStatement(checkPhoneSql);
            checkPhoneStmt.setString(1, telephone);
            ResultSet rsPhone = checkPhoneStmt.executeQuery();

            if (rsPhone.next()) {
                System.out.println("❌ Ce numéro de téléphone est déjà utilisé");
                conn.rollback(); // Annuler la transaction
                return false;
            }

            // ========== Insérer l'utilisateur ==========
            String insertUserSql = """
            INSERT INTO utilisateur (nom, prenom, email, password, telephone, sexe, age) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

            PreparedStatement insertUserStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS);
            insertUserStmt.setString(1, nom);
            insertUserStmt.setString(2, prenom);
            insertUserStmt.setString(3, email);
            insertUserStmt.setString(4, password); // ⚠️ En production : hasher avec BCrypt !
            insertUserStmt.setString(5, telephone);
            insertUserStmt.setString(6, sexe);
            insertUserStmt.setInt(7, age);

            int rowsAffected = insertUserStmt.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("❌ Erreur lors de l'inscription");
                conn.rollback(); // Annuler la transaction
                return false;
            }

            // ========== Récupérer l'ID de l'utilisateur créé ==========
            ResultSet generatedKeys = insertUserStmt.getGeneratedKeys();
            int userId = 0;

            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            } else {
                System.out.println("❌ Impossible de récupérer l'ID utilisateur");
                conn.rollback(); // Annuler la transaction
                return false;
            }

            // ========== Insérer le solde initial dans la table budgets ==========
            if (soldeInitial > 0) {
                String insertBudgetSql = """
                INSERT INTO budget (montant, id_utilisateur, id_categorie) 
                VALUES (?, ?, NULL)
            """;

                PreparedStatement insertBudgetStmt = conn.prepareStatement(insertBudgetSql);
                insertBudgetStmt.setDouble(1, soldeInitial);
                insertBudgetStmt.setInt(2, userId);

                int budgetRows = insertBudgetStmt.executeUpdate();

                if (budgetRows > 0) {
                    System.out.println("✅ Solde initial enregistré : " + soldeInitial + "€");
                } else {
                    System.out.println("⚠️ Erreur lors de l'enregistrement du solde initial");
                    conn.rollback(); // Annuler TOUTE la transaction (user + budget)
                    return false;
                }
            }

            // ========== VALIDER LA TRANSACTION ==========
            conn.commit(); // ← IMPORTANT : Valider toutes les modifications

            // ========== Créer la session utilisateur ==========
            currentUser = new User(nom, prenom, email, password, telephone);
            currentUser.setId(userId);
            currentUser.setSexe(sexe);
            currentUser.setAge(age);
            currentUser.setSoldeInitial(soldeInitial);
            currentUser.setCreatedAt(LocalDateTime.now());

            System.out.println("✅ Inscription réussie : " + currentUser.getFullName());
            System.out.println("   - Sexe: " + sexe);
            System.out.println("   - Âge: " + age);
            System.out.println("   - Solde initial: " + soldeInitial + "€");

            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'inscription : " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, annuler toute la transaction
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("⚠️ Transaction annulée");
                }
            } catch (SQLException rollbackEx) {
                System.err.println("❌ Erreur lors du rollback : " + rollbackEx.getMessage());
            }

            return false;

        } finally {
            // Remettre l'auto-commit à true
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la réactivation de l'auto-commit : " + e.getMessage());
            }
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

}
