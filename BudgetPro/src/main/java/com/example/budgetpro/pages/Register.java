package com.example.budgetpro.pages;

import com.example.budgetpro.HelloApplication;
import com.example.budgetpro.services.AuthServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;

public class Register {

    @FXML private TextField nomTextField;
    @FXML private TextField prenomTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField telephoneTextField;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private Spinner<Integer> ageSpinner;
    @FXML private PasswordField passwordTextField;
    @FXML private PasswordField confirmPasswordTextField;
    @FXML private TextField soldeTextField;
    @FXML private Button inscriptionButton;

    @FXML
    public void initialize() {
        // Le Spinner et ComboBox sont déjà configurés dans le FXML
        // Mais on peut ajouter des validations en temps réel si besoin
    }

    @FXML
    private void handleRegister() {
        // 1️⃣ Récupérer les valeurs
        String nom = nomTextField.getText();
        String prenom = prenomTextField.getText();
        String email = emailTextField.getText();
        String telephone = telephoneTextField.getText();
        String sexe = sexeComboBox.getValue();
        Integer age = ageSpinner.getValue();
        String password = passwordTextField.getText();
        String confirmPassword = confirmPasswordTextField.getText();
        String soldeText = soldeTextField.getText();

        // 2️⃣ Validations basiques (interface)
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() ||
                telephone.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires", Alert.AlertType.ERROR);
            return;
        }

        if (sexe == null) {
            showAlert("Erreur", "Veuillez sélectionner votre sexe", Alert.AlertType.ERROR);
            return;
        }

        if (age == null) {
            showAlert("Erreur", "Veuillez renseigner votre âge", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Erreur", "Les mots de passe ne correspondent pas", Alert.AlertType.ERROR);
            return;
        }

        // Convertir le solde (par défaut 0 si vide ou invalide)
        double soldeInitial = 0.0;
        if (soldeText != null && !soldeText.trim().isEmpty()) {
            try {
                soldeInitial = Double.parseDouble(soldeText.trim());

                if (soldeInitial < 0) {
                    showAlert("Erreur", "Le solde initial ne peut pas être négatif", Alert.AlertType.ERROR);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Format de solde invalide (exemple : 1000.50)", Alert.AlertType.ERROR);
                return;
            }
        }

        // 3️⃣ Appeler le service
        boolean success = AuthServices.register(
                nom,
                prenom,
                email,
                password,
                telephone,
                sexe,
                age,
                soldeInitial
        );

        // 4️⃣ Afficher le résultat
        if (success) {
            showAlert("Succès", "Compte créé avec succès ! Bienvenue " + prenom + " !", Alert.AlertType.INFORMATION);
           /* try {
                Hello.showDashboard();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de charger le tableau de bord", Alert.AlertType.ERROR);
            }*/
        } else {
            showAlert("Erreur", "Inscription échouée. Vérifiez vos informations.", Alert.AlertType.ERROR);
        }
    }

    /*@FXML
    private void goToLogin() {
        try {
            HelloApplication.showLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}