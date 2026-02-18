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
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void switchContent(String fxml, StackPane container) throws IOException {
        Parent root = FXMLLoader.load(SceneSwitcher.class.getResource(fxml));

        container.getChildren().clear();      // On vide le panneau
        container.getChildren().add(root);    // On ajoute le nouveau contenu
    }




}
