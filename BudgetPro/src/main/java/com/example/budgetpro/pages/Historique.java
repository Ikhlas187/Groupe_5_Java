/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.budgetpro.pages;

import com.example.budgetpro.dao.DepenseDAO;
import com.example.budgetpro.models.Depense;
import com.example.budgetpro.services.AuthServices;
import com.example.budgetpro.services.HistoriqueService;
import com.example.budgetpro.dao.CategorieDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author asteras
 */
public class Historique {

    // =========================================================
    // COMPOSANTS DU TABLEAU
    // =========================================================

    @FXML private TableView<TransactionRow>          transactionsTable;
    @FXML private TableColumn<TransactionRow, String> titreColumn;
    @FXML private TableColumn<TransactionRow, String> categorieColumn;
    @FXML private TableColumn<TransactionRow, String> prixColumn;
    @FXML private TableColumn<TransactionRow, String> dateColumn;
    @FXML private TableColumn<TransactionRow, String> descriptionColumn;

    // =========================================================
    // COMPOSANTS DE FILTRAGE
    // =========================================================

    @FXML private ComboBox<String> categorieComboBox;
    @FXML private DatePicker       dateDebutPicker;
    @FXML private DatePicker       dateFinPicker;

    // =========================================================
    // PIED DE PAGE
    // =========================================================

    @FXML private Label totalLabel;

    // =========================================================
    // SERVICES ET DONNÉES
    // =========================================================

    private HistoriqueService              historiqueService;
    private CategorieDAO                   categorieDAO;
    private ObservableList<TransactionRow> transactionData;
    private List<Depense>                  currentDepenses;


    // =========================================================
    // INITIALISATION
    // =========================================================

    @FXML
    public void initialize() {
        historiqueService = new HistoriqueService();
        categorieDAO      = new CategorieDAO();
        transactionData   = FXCollections.observableArrayList();

        setupTableColumns();
        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupCategorieComboBox();
        loadAllTransactions();
    }

    private void setupTableColumns() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        transactionsTable.setItems(transactionData);
    }

    private void setupCategorieComboBox() {
        List<String> categoriesFromDB = categorieDAO.getAllCategorieName();
        ObservableList<String> categories = FXCollections.observableArrayList(categoriesFromDB);
        categorieComboBox.setItems(categories);
        categorieComboBox.setValue("All");
    }


    // =========================================================
    // CHARGEMENT DES DONNÉES
    // =========================================================

    private void loadAllTransactions() {
        currentDepenses = DepenseDAO.getDepensesByUserId(
            AuthServices.getCurrentUser().getId()
        );
        displayTransactions(currentDepenses);
    }

    private void displayTransactions(List<Depense> depenses) {
        transactionData.clear();

        for (Depense d : depenses) {
            String categorieName = historiqueService.getCategorieNameForDepense(d.getIdDepense());

            transactionData.add(new TransactionRow(
                d.getNomSousCategorie(),
                categorieName,
                d.getMontant() + " XOF",
                d.getDate().toString(),
                d.getDescription()
            ));
        }

        updateTotal(depenses);
    }

    private void updateTotal(List<Depense> depenses) {
        double total = historiqueService.calculerTotal(depenses);
        totalLabel.setText(String.format("Total : %.0f XOF", total));
    }


    // =========================================================
    // GESTIONNAIRES D'ÉVÉNEMENTS
    // =========================================================

    @FXML
    private void handleFilterCategorie() {
        String selectedCategorie = categorieComboBox.getValue();

        if (selectedCategorie == null || selectedCategorie.equals("All")) {
            loadAllTransactions();
            return;
        }

        int categorieId = categorieDAO.getCategorieIdByName(selectedCategorie);

        if (categorieId != -1) {
            currentDepenses = historiqueService.filterByCategorie(categorieId);
            displayTransactions(currentDepenses);
        } else {
            System.err.println("Catégorie introuvable : " + selectedCategorie);
            loadAllTransactions();
        }
    }

    @FXML
    private void handleFilterByDate() {
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin   = dateFinPicker.getValue();

        if (dateDebut == null) {
            showAlert("Erreur", "Veuillez sélectionner au moins une date de début.", Alert.AlertType.WARNING);
            return;
        }

        if (dateFin == null) {
            dateFin = dateDebut;
        } else if (dateDebut.isAfter(dateFin)) {
            showAlert("Erreur", "La date de début doit être antérieure à la date de fin.", Alert.AlertType.WARNING);
            return;
        }

        currentDepenses = historiqueService.filterByPeriode(dateDebut, dateFin);
        displayTransactions(currentDepenses);
    }

    @FXML
    private void handleResetFilters() {
        categorieComboBox.setValue("All");
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        loadAllTransactions();
    }


    // =========================================================
    // UTILITAIRES
    // =========================================================

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // =========================================================
    // CLASSE INTERNE : LIGNE DU TABLEAU
    // =========================================================

    public static class TransactionRow {

        private final String titre;
        private final String categorie;
        private final String prix;
        private final String date;
        private final String description;

        public TransactionRow(String titre, String categorie, String prix,
                              String date, String description) {
            this.titre       = titre;
            this.categorie   = categorie;
            this.prix        = prix;
            this.date        = date;
            this.description = description;
        }

        public String getTitre()       { return titre; }
        public String getCategorie()   { return categorie; }
        public String getPrix()        { return prix; }
        public String getDate()        { return date; }
        public String getDescription() { return description; }
    }
}
