/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.budgetpro.pages;

import com.example.budgetpro.dao.DepenseDAO;
import com.example.budgetpro.models.Depense;
import com.example.budgetpro.models.User;
import com.example.budgetpro.services.AuthServices;
import com.example.budgetpro.services.Database;
import com.example.budgetpro.services.HistoriqueService;
import com.example.budgetpro.services.SceneSwitcher;
import com.example.budgetpro.services.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class Settings {

    @FXML private Label avatarLabel;
    @FXML private Label nomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label ageLabel;
    @FXML private Label soldeLabel;
    @FXML private Label depensesLabel;
    @FXML private Label revenusLabel;
    @FXML private CheckBox darkModeToggle;

    private HistoriqueService historiqueService;
    private List<Depense> currentDepenses;

    @FXML
    public void initialize() {
        historiqueService = new HistoriqueService();
        chargerInformationsUtilisateur();
        chargerStatistiques();
        darkModeToggle.setSelected(ThemeManager.isDarkMode());

        User currentUser = AuthServices.getCurrentUser();
        if (currentUser != null) {
            currentDepenses = DepenseDAO.getDepensesByUserId(currentUser.getId());
        }
    }

    // =============================================
    // CHARGEMENT DES DONNÉES
    // =============================================

    private void chargerInformationsUtilisateur() {
        User currentUser = AuthServices.getCurrentUser();
        if (currentUser == null) return;

        String initiales = currentUser.getPrenom().substring(0, 1).toUpperCase()
                         + currentUser.getNom().substring(0, 1).toUpperCase();
        avatarLabel.setText(initiales);
        nomLabel.setText(currentUser.getFullName());
        emailLabel.setText(currentUser.getEmail());
        telephoneLabel.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "-");

        if (currentUser.getAge() != 0) {
            ageLabel.setText(currentUser.getAge() + " ans");
        }
    }

    private void chargerStatistiques() {
        User user = AuthServices.getCurrentUser();
        if (user == null) return;

        int userId = user.getId();
        LocalDate now = LocalDate.now();
        String moisDebut = now.withDayOfMonth(1).toString();       // ex: 2026-02-01
        String moisFin   = now.withDayOfMonth(now.lengthOfMonth()).toString(); // ex: 2026-02-28

        try {
            Connection conn = Database.getConnection();

            // ── Solde initial (budget sans categorie) ──────────────────
            double solde = 0;
            String sqlSolde = "SELECT SUM(montant) FROM budget WHERE id_utilisateur = ? AND id_categorie IS NULL";
            PreparedStatement stmtSolde = conn.prepareStatement(sqlSolde);
            stmtSolde.setInt(1, userId);
            ResultSet rsSolde = stmtSolde.executeQuery();
            if (rsSolde.next()) solde = rsSolde.getDouble(1);

            // ── Total dépenses du mois courant ─────────────────────────
            double depenses = 0;
            String sqlDep = "SELECT SUM(montant) FROM depense WHERE id_utilisateur = ? AND date BETWEEN ? AND ?";
            PreparedStatement stmtDep = conn.prepareStatement(sqlDep);
            stmtDep.setInt(1, userId);
            stmtDep.setString(2, moisDebut);
            stmtDep.setString(3, moisFin);
            ResultSet rsDep = stmtDep.executeQuery();
            if (rsDep.next()) depenses = rsDep.getDouble(1);

            // ── Total revenus du mois courant ──────────────────────────
            double revenus = 0;
            String sqlRev = "SELECT SUM(montant) FROM revenu WHERE id_utilisateur = ? AND date BETWEEN ? AND ?";
            PreparedStatement stmtRev = conn.prepareStatement(sqlRev);
            stmtRev.setInt(1, userId);
            stmtRev.setString(2, moisDebut);
            stmtRev.setString(3, moisFin);
            ResultSet rsRev = stmtRev.executeQuery();
            if (rsRev.next()) revenus = rsRev.getDouble(1);

            // ── Solde réel = solde initial + revenus - dépenses ───────
            double soldeReel = solde + revenus - depenses;

            soldeLabel.setText(String.format("%.0f XOF", soldeReel));
            depensesLabel.setText(String.format("%.0f XOF", depenses));
            revenusLabel.setText(String.format("%.0f XOF", solde + revenus));

        } catch (SQLException e) {
            System.err.println("Erreur chargement statistiques : " + e.getMessage());
            soldeLabel.setText("- XOF");
            depensesLabel.setText("- XOF");
            revenusLabel.setText("- XOF");
        }
    }

    // =============================================
    // MODIFIER LE PROFIL
    // =============================================

    @FXML
    private void handleModifierProfil() {
        User user = AuthServices.getCurrentUser();
        if (user == null) return;

        // Dialogue Prénom
        TextInputDialog dialogPrenom = new TextInputDialog(user.getPrenom());
        dialogPrenom.setTitle("Modifier le profil");
        dialogPrenom.setHeaderText(null);
        dialogPrenom.setContentText("Prénom :");
        Optional<String> nouveauPrenom = dialogPrenom.showAndWait();
        if (nouveauPrenom.isEmpty()) return;

        // Dialogue Nom
        TextInputDialog dialogNom = new TextInputDialog(user.getNom());
        dialogNom.setTitle("Modifier le profil");
        dialogNom.setHeaderText(null);
        dialogNom.setContentText("Nom :");
        Optional<String> nouveauNom = dialogNom.showAndWait();
        if (nouveauNom.isEmpty()) return;

        // Dialogue Téléphone
        TextInputDialog dialogTel = new TextInputDialog(user.getTelephone());
        dialogTel.setTitle("Modifier le profil");
        dialogTel.setHeaderText(null);
        dialogTel.setContentText("Téléphone :");
        Optional<String> nouveauTel = dialogTel.showAndWait();
        if (nouveauTel.isEmpty()) return;

        String prenom = nouveauPrenom.get().trim();
        String nom    = nouveauNom.get().trim();
        String tel    = nouveauTel.get().trim();

        if (prenom.isEmpty() || nom.isEmpty()) {
            showAlert("Erreur", "Le nom et le prénom ne peuvent pas être vides.", Alert.AlertType.ERROR);
            return;
        }

        try {
            Connection conn = Database.getConnection();
            String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, telephone = ? WHERE id_utilisateur = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, tel);
            stmt.setInt(4, user.getId());
            stmt.executeUpdate();

            // Mettre à jour la session locale
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setTelephone(tel);

            chargerInformationsUtilisateur();
            showAlert("Succès", "Profil mis à jour avec succès.", Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de modifier le profil : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // =============================================
    // CHANGER LE MOT DE PASSE
    // =============================================

    @FXML
    private void handleChangerMotDePasse() {
        User user = AuthServices.getCurrentUser();
        if (user == null) return;

        // Ancien mot de passe
        Dialog<String> dialogAncien = creerDialogMotDePasse("Ancien mot de passe", "Saisissez votre mot de passe actuel :");
        Optional<String> ancienOpt = dialogAncien.showAndWait();
        if (ancienOpt.isEmpty()) return;

        // Vérifier l'ancien mot de passe
        if (!ancienOpt.get().equals(user.getPassword())) {
            showAlert("Erreur", "Mot de passe actuel incorrect.", Alert.AlertType.ERROR);
            return;
        }

        // Nouveau mot de passe
        Dialog<String> dialogNouveau = creerDialogMotDePasse("Nouveau mot de passe", "Saisissez votre nouveau mot de passe (min 6 caractères) :");
        Optional<String> nouveauOpt = dialogNouveau.showAndWait();
        if (nouveauOpt.isEmpty()) return;

        if (nouveauOpt.get().length() < 6) {
            showAlert("Erreur", "Le mot de passe doit contenir au moins 6 caractères.", Alert.AlertType.ERROR);
            return;
        }

        // Confirmation
        Dialog<String> dialogConfirm = creerDialogMotDePasse("Confirmation", "Confirmez votre nouveau mot de passe :");
        Optional<String> confirmOpt = dialogConfirm.showAndWait();
        if (confirmOpt.isEmpty()) return;

        if (!nouveauOpt.get().equals(confirmOpt.get())) {
            showAlert("Erreur", "Les mots de passe ne correspondent pas.", Alert.AlertType.ERROR);
            return;
        }

        // Appel AuthServices (même logique que ForgotPassword)
        boolean success = AuthServices.resetPassword(user.getEmail(), nouveauOpt.get());
        if (success) {
            user.setPassword(nouveauOpt.get()); // Mettre à jour la session locale
            showAlert("Succès", "Mot de passe modifié avec succès.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Erreur", "Impossible de modifier le mot de passe.", Alert.AlertType.ERROR);
        }
    }

    /** Crée un Dialog avec un PasswordField propre */
    private Dialog<String> creerDialogMotDePasse(String titre, String message) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(titre);
        dialog.setHeaderText(null);

        ButtonType okBtn = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        PasswordField pf = new PasswordField();
        pf.setPromptText(message);
        pf.setStyle("-fx-padding: 8; -fx-font-size: 13px;");

        dialog.getDialogPane().setContent(new javafx.scene.layout.VBox(
            new Label(message), pf
        ));

        dialog.setResultConverter(btn -> btn == okBtn ? pf.getText() : null);
        return dialog;
    }

    // =============================================
    // THÈME
    // =============================================

    @FXML
    private void handleDarkModeToggle() {
        ThemeManager.toggleTheme();
    }

    // =============================================
    // EXPORT
    // =============================================

    @FXML
    private void handleExporter() {
        if (currentDepenses == null || currentDepenses.isEmpty()) {
            showAlert("Export", "Aucune donnée à exporter.", Alert.AlertType.WARNING);
            return;
        }

        ChoiceDialog<String> choix = new ChoiceDialog<>("CSV", "CSV", "JSON");
        choix.setTitle("Exporter les données");
        choix.setHeaderText(null);
        choix.setContentText("Choisissez le format d'export :");

        choix.showAndWait().ifPresent(format -> {
            if (format.equals("JSON")) exportJSON();
            else exportCSV();
        });
    }

    private void exportJSON() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter en JSON");
        fc.setInitialFileName("transactions.json");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers JSON", "*.json"));
        File file = fc.showSaveDialog(nomLabel.getScene().getWindow());
        if (file != null) {
            boolean ok = historiqueService.exportToJSON(currentDepenses, file.getAbsolutePath());
            showAlert(ok ? "Succès" : "Erreur",
                      ok ? "Export JSON réussi !\n" + file.getAbsolutePath() : "Échec de l'export JSON.",
                      ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        }
    }

    private void exportCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter en CSV");
        fc.setInitialFileName("transactions.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fc.showSaveDialog(nomLabel.getScene().getWindow());
        if (file != null) {
            boolean ok = historiqueService.exportToCSV(currentDepenses, file.getAbsolutePath());
            showAlert(ok ? "Succès" : "Erreur",
                      ok ? "Export CSV réussi !\n" + file.getAbsolutePath() : "Échec de l'export CSV.",
                      ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        }
    }

    // =============================================
    // DÉCONNEXION
    // =============================================

    @FXML
    private void handleDeconnexion() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment vous déconnecter ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AuthServices.logout();
                SceneSwitcher.switchScene("/com/example/budgetpro/Login.fxml", nomLabel);
            }
        });
    }

    // =============================================
    // SUPPRIMER LE COMPTE
    // =============================================

    @FXML
    private void handleSupprimerCompte() {
        User user = AuthServices.getCurrentUser();
        if (user == null) return;

        // Double confirmation
        Alert confirmation = new Alert(Alert.AlertType.WARNING);
        confirmation.setTitle("Supprimer le compte");
        confirmation.setHeaderText("⚠️ Action irréversible !");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer votre compte ?\nToutes vos données seront perdues définitivement.");
        confirmation.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        confirmation.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;

            // Demander le mot de passe pour confirmer
            Dialog<String> dialogMdp = creerDialogMotDePasse("Confirmation", "Saisissez votre mot de passe pour confirmer :");
            dialogMdp.showAndWait().ifPresent(mdp -> {
                if (!mdp.equals(user.getPassword())) {
                    showAlert("Erreur", "Mot de passe incorrect. Suppression annulée.", Alert.AlertType.ERROR);
                    return;
                }

                try {
                    Connection conn = Database.getConnection();
                    int id = user.getId();

                    // Supprimer dans l'ordre (clés étrangères)
                    for (String sql : new String[]{
                        "DELETE FROM depense WHERE id_utilisateur = ?",
                        "DELETE FROM revenu WHERE id_utilisateur = ?",
                        "DELETE FROM budget WHERE id_utilisateur = ?",
                        "DELETE FROM utilisateur WHERE id_utilisateur = ?"
                    }) {
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, id);
                        stmt.executeUpdate();
                    }

                    AuthServices.logout();
                    SceneSwitcher.switchScene("/com/example/budgetpro/Login.fxml", nomLabel);

                } catch (SQLException e) {
                    showAlert("Erreur", "Impossible de supprimer le compte : " + e.getMessage(), Alert.AlertType.ERROR);
                }
            });
        });
    }

    // =============================================
    // UTILITAIRE
    // =============================================

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
