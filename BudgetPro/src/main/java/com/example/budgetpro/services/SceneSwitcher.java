package com.example.budgetpro.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import javafx.scene.layout.StackPane;

public class SceneSwitcher {
    
    public static void switchScene(String fxml, Node node) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) node.getScene().getWindow();
            Scene newScene = new Scene(root);
            
            boolean isPagePublique = fxml.contains("Login")
                    || fxml.contains("Register")
                    || fxml.contains("ForgotPassword");
            
            if (!isPagePublique){
                ThemeManager.initTheme(newScene);
            }
            
            stage.setScene(newScene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void switchContent(String fxml, StackPane container) throws IOException {
        Parent root = FXMLLoader.load(SceneSwitcher.class.getResource(fxml));

        container.getChildren().clear();
        container.getChildren().add(root);
        
        // APPLIQUER LE THÈME AU NOUVEAU CONTENU 
        if (container.getScene() != null) {
            ThemeManager.initTheme(container.getScene());
        }
    }
}