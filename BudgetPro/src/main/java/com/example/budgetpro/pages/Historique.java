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
import com.example.budgetpro.dao.CategorieDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.control.DatePicker;

/**
 *
 * @author asteras
 */
public class Historique {

    @FXML
    private TableView<TransactionRow> transactionsTable;
    
    @FXML
    private TableColumn<TransactionRow, String> titreColumn;
    
    @FXML
    private TableColumn<TransactionRow, String> categorieColumn;
    
    @FXML
    private TableColumn<TransactionRow, String> prixColumn;
    
    @FXML
    private TableColumn<TransactionRow, String> dateColumn;
    
    @FXML
    private TableColumn<TransactionRow, String> descriptionColumn;
    
    @FXML
    private ComboBox<String> categorieComboBox;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private Button exportJsonButton;
    
    @FXML
    private Button exportCsvButton;
    
    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;
    
    private HistoriqueService historiqueService;
    private ObservableList<TransactionRow> transactionData;
    private List<Depense> currentDepenses;
    private CategorieDAO categorieDAO;

    /**
     * Initialisation du controller
     */
    @FXML
    public void initialize() {
        historiqueService = new HistoriqueService();
        categorieDAO = new CategorieDAO();
        transactionData = FXCollections.observableArrayList();
        
        // Configurer les colonnes du tableau
        setupTableColumns();
        
        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Configurer le ComboBox des catégories
        setupCategorieComboBox();
        
        // Charger les données
        loadAllTransactions();
    }
    
    /**
     * Configuration des colonnes du TableView
     */
    private void setupTableColumns() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Lier les données au tableau
        transactionsTable.setItems(transactionData);
    }
    
    /**
     * Configuration du ComboBox des catégories
     */
    private void setupCategorieComboBox() {
        // Récupérer les catégories depuis la base de données
        List<String> categoriesFromDB = categorieDAO.getAllCategorieName();
        ObservableList<String> categories = FXCollections.observableArrayList(categoriesFromDB);
        
        categorieComboBox.setItems(categories);
        categorieComboBox.setValue("All");
    }
    
    /**
     * Charger toutes les transactions
     */
    private void loadAllTransactions() {
        currentDepenses = DepenseDAO.getDepensesByUserId(AuthServices.getCurrentUser().getId());
        displayTransactions(currentDepenses);
    }
    
    /**
     * Afficher les transactions dans le tableau
     */
    private void displayTransactions(List<Depense> depenses) {
        transactionData.clear();
        
        for (Depense d : depenses) {
            String categorieName = historiqueService.getCategorieNameForDepense(d.getIdDepense());
            
            TransactionRow row = new TransactionRow(
                d.getNomSousCategorie(),
                categorieName,
                d.getMontant() + " XOF",
                d.getDate().toString(),
                d.getDescription()
            );
            
            transactionData.add(row);
        }
        
        // Mettre à jour le total
        updateTotal(depenses);
    }
    
    /**
     * Mettre à jour le label du total
     */
    private void updateTotal(List<Depense> depenses) {
        double total = historiqueService.calculerTotal(depenses);
        totalLabel.setText(String.format("Total : %.0f XOF", total));
    }
    
    /**
     * Gérer le filtre par catégorie
     */
    @FXML
private void handleFilterCategorie() {
    String selectedCategorie = categorieComboBox.getValue();
    
    if (selectedCategorie == null || selectedCategorie.equals("All")) {
        // Afficher toutes les transactions
        loadAllTransactions();
    } else {
        // ⭐ FILTRER PAR CATÉGORIE
        int categorieId = categorieDAO.getCategorieIdByName(selectedCategorie);
        
        if (categorieId != -1) {
            currentDepenses = historiqueService.filterByCategorie(categorieId);
            displayTransactions(currentDepenses);
            System.out.println("Filtre appliqué : " + selectedCategorie + " (ID: " + categorieId + ")");
        } else {
            System.err.println("Catégorie introuvable : " + selectedCategorie);
            loadAllTransactions();
        }
    }
}
    
    /**
     * Exporter en JSON
     */
    @FXML
    private void handleExportJSON() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en JSON");
        fileChooser.setInitialFileName("transactions.json");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers JSON", "*.json")
        );
        
        File file = fileChooser.showSaveDialog(exportJsonButton.getScene().getWindow());
        
        if (file != null) {
            boolean success = historiqueService.exportToJSON(currentDepenses, file.getAbsolutePath());
            
            if (success) {
                showAlert("Succès", "Export JSON réussi !", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Échec de l'export JSON", Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Exporter en CSV
     */
    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en CSV");
        fileChooser.setInitialFileName("transactions.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(exportCsvButton.getScene().getWindow());
        
        if (file != null) {
            boolean success = historiqueService.exportToCSV(currentDepenses, file.getAbsolutePath());
            
            if (success) {
                showAlert("Succès", "Export CSV réussi !", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Échec de l'export CSV", Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Afficher une alerte
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
    * Filtrer par période (date)
    */
   @FXML
   private void handleFilterByDate() {
       LocalDate dateDebut = dateDebutPicker.getValue();
       LocalDate dateFin = dateFinPicker.getValue();

       // Vérifier qu'au moins la date de début est sélectionnée
       if (dateDebut == null) {
           showAlert("Erreur", "Veuillez sélectionner au moins une date de début", Alert.AlertType.WARNING);
           return;
       }

       // Si pas de date de fin → utiliser la date de début (un seul jour)
       if (dateFin == null) {
           dateFin = dateDebut;
           System.out.println("✅ Filtre pour un seul jour : " + dateDebut);
       } else {
           // Vérifier l'ordre des dates
           if (dateDebut.isAfter(dateFin)) {
               showAlert("Erreur", "La date de début doit être antérieure à la date de fin", Alert.AlertType.WARNING);
               return;
           }
           System.out.println("✅ Filtre par période : " + dateDebut + " → " + dateFin);
       }

       currentDepenses = historiqueService.filterByPeriode(dateDebut, dateFin);
       displayTransactions(currentDepenses);
   }
    
    @FXML
    private void handleResetFilters() {
        // Réinitialiser les filtres
        categorieComboBox.setValue("All");
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);

        // Recharger toutes les transactions
        loadAllTransactions();

        System.out.println("Filtres réinitialisés");
    }
    
    
    /**
     * Classe interne pour représenter une ligne du tableau
     */
    public static class TransactionRow {
        private String titre;
        private String categorie;
        private String prix;
        private String date;
        private String description;
        
        public TransactionRow(String titre, String categorie, String prix, String date, String description) {
            this.titre = titre;
            this.categorie = categorie;
            this.prix = prix;
            this.date = date;
            this.description = description;
        }
        
        // Getters
        public String getTitre() { return titre; }
        public String getCategorie() { return categorie; }
        public String getPrix() { return prix; }
        public String getDate() { return date; }
        public String getDescription() { return description; }
    }
}
