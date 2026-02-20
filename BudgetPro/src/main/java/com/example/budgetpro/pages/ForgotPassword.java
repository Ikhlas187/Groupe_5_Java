package com.example.budgetpro.pages;

import com.example.budgetpro.services.AuthServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ForgotPassword {

    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    public void handleReset(MouseEvent event) {
        String email = emailField.getText().trim().toLowerCase();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // ── Validations côté client ──────────────────────────────────────
        if (email.isEmpty()) {
            showError("Veuillez saisir votre email.");
            return;
        }

        if (newPassword.isEmpty()) {
            showError("Veuillez saisir un nouveau mot de passe.");
            return;
        }

        if (newPassword.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        // ── Appel service ────────────────────────────────────────────────
        boolean success = AuthServices.resetPassword(email, newPassword);

        if (success) {
            showSuccess("Mot de passe modifié avec succès !");
            // Redirection vers la page de connexion après 1,5 s
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::versConnexionPage0);
                } catch (InterruptedException ignored) {}
            }).start();
        } else {
            showError("Aucun compte trouvé avec cet email.");
        }
    }

    @FXML
    public void versConnexionPage(MouseEvent event) {
        versConnexionPage0();
    }

    private void versConnexionPage0() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/budgetpro/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du retour à la connexion.");
        }
    }


    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #43a047; -fx-font-size: 12px;");
    }
}