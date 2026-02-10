package com.example.budgetpro.pages;

import com.example.budgetpro.HelloApplication;
import com.example.budgetpro.services.AuthServices;
import com.example.budgetpro.services.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.io.IOException;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;



public class Login {
  @FXML
    private TextField emailTextField;
  @FXML
    private  PasswordField passwordTextField;
  @FXML
    private Button connexionButton;

    private void handleLogin() {
        System.out.println("Label cliqué !");
    }
    @FXML
     private void handleLogin (MouseEvent event){
      String email = emailTextField.getText();
      String password = passwordTextField.getText();

      if (email == null ||  email.trim().isEmpty()) {
          showAlert("Erreur", "Veuillez entrer votre email", Alert.AlertType.ERROR);
          emailTextField.requestFocus();
          return;
      }

      if (password == null || password.isEmpty()) {
          showAlert("Erreur", "Veuillez entrer votre mot de passe", Alert.AlertType.ERROR);
          passwordTextField.requestFocus();
          return;
      }

      boolean success = AuthServices.login(email, password);

      if (success) {
          String userName = AuthServices.getCurrentUser().getFullName();
          System.out.println("✅ Connexion réussie ! Bienvenue " + userName);
          SceneSwitcher.switchScene("/com/example/budgetpro/Dashboard.fxml",(Node) event.getSource());

         /* try {
              MainApplication.showDashboard();
          } catch (IOException e) {
              e.printStackTrace();
              showAlert("Erreur", "Impossible de charger le tableau de bord", Alert.AlertType.ERROR);
          }*/
      } else {

          showAlert(
                  "Erreur de connexion",
                  "Email ou mot de passe incorrect.\nVeuillez réessayer.",
                  Alert.AlertType.ERROR
          );
          passwordTextField.clear();
          emailTextField.requestFocus();
      }
  }




    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
