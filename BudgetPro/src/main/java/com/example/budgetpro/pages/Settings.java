/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.budgetpro.pages;

import com.example.budgetpro.dao.DepenseDAO;
import com.example.budgetpro.models.Depense;
import com.example.budgetpro.models.User;
import com.example.budgetpro.services.AuthServices;
import com.example.budgetpro.services.HistoriqueService;
import com.example.budgetpro.services.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

/**
 *
 * @author asteras
 */
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
    @FXML private CheckBox notificationsToggle;

    private HistoriqueService historiqueService;
    private List<Depense> currentDepenses;

    @FXML
    public void initialize() {
        historiqueService = new HistoriqueService();

        chargerInformationsUtilisateur();
        chargerStatistiques();
        darkModeToggle.setSelected(ThemeManager.isDarkMode());

        // Charger les dépenses de l'utilisateur connecté (pour l'export)
        User currentUser = AuthServices.getCurrentUser();
        if (currentUser != null) {
            currentDepenses = DepenseDAO.getDepensesByUserId(currentUser.getId());
        }
    }

    private void chargerInformationsUtilisateur() {
        User currentUser = AuthServices.getCurrentUser();

        if (currentUser != null) {
            String initiales = currentUser.getPrenom().substring(0, 1).toUpperCase() +
                               currentUser.getNom().substring(0, 1).toUpperCase();
            avatarLabel.setText(initiales);
            nomLabel.setText(currentUser.getFullName());
            emailLabel.setText(currentUser.getEmail());
            telephoneLabel.setText(currentUser.getTelephone());

            if (currentUser.getAge() != 0) {
                ageLabel.setText(currentUser.getAge() + " ans");
            }
        } else {
            System.err.println("Aucun utilisateur connecté");
        }
    }

    private void chargerStatistiques() {
        // TODO: Récupérer les vraies données depuis la base
        soldeLabel.setText("50 000 XOF");
        depensesLabel.setText("25 000 XOF");
        revenusLabel.setText("150 000 XOF");
    }

    @FXML
    private void handleModifierProfil() {
        showAlert("Modifier le profil", "Fonctionnalité à venir", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleDarkModeToggle() {
        ThemeManager.toggleTheme();
    }

    @FXML
    private void handleChangerMotDePasse() {
        showAlert("Changer le mot de passe", "Fonctionnalité à venir", Alert.AlertType.INFORMATION);
    }

    /**
     * Exporter les données — dialog pour choisir JSON ou CSV
     */
    @FXML
    private void handleExporter() {
        if (currentDepenses == null || currentDepenses.isEmpty()) {
            showAlert("Export", "Aucune donnée à exporter.", Alert.AlertType.WARNING);
            return;
        }

        // Choix du format
        ChoiceDialog<String> choix = new ChoiceDialog<>("CSV", "CSV", "JSON");
        choix.setTitle("Exporter les données");
        choix.setHeaderText(null);
        choix.setContentText("Choisissez le format d'export :");

        choix.showAndWait().ifPresent(format -> {
            if (format.equals("JSON")) {
                exportJSON();
            } else {
                exportCSV();
            }
        });
    }

    private void exportJSON() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en JSON");
        fileChooser.setInitialFileName("transactions.json");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers JSON", "*.json")
        );

        File file = fileChooser.showSaveDialog(nomLabel.getScene().getWindow());

        if (file != null) {
            boolean success = historiqueService.exportToJSON(currentDepenses, file.getAbsolutePath());
            if (success) {
                showAlert("Succès", "Export JSON réussi !\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Échec de l'export JSON.", Alert.AlertType.ERROR);
            }
        }
    }

    private void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en CSV");
        fileChooser.setInitialFileName("transactions.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );

        File file = fileChooser.showSaveDialog(nomLabel.getScene().getWindow());

        if (file != null) {
            boolean success = historiqueService.exportToCSV(currentDepenses, file.getAbsolutePath());
            if (success) {
                showAlert("Succès", "Export CSV réussi !\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Échec de l'export CSV.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleDeconnexion() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment vous déconnecter ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AuthServices.logout();
                System.out.println("Déconnexion...");
                // TODO: Rediriger vers la page de login
            }
        });
    }

    @FXML
    private void handleSupprimerCompte() {
        Alert confirmation = new Alert(Alert.AlertType.WARNING);
        confirmation.setTitle("Supprimer le compte");
        confirmation.setHeaderText("⚠️ Action irréversible !");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer votre compte ? Toutes vos données seront perdues.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Suppression du compte...");
                // TODO: Supprimer de la base + rediriger
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
