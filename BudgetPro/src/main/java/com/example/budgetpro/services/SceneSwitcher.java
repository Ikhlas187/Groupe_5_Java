package com.example.budgetpro.services;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;

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



}
