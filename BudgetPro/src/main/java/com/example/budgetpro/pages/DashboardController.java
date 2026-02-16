package com.example.budgetpro.pages;

import com.example.budgetpro.models.*;
import com.example.budgetpro.services.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    // ========================================
    // ÉLÉMENTS FXML (Sidebar)
    // ========================================

    @FXML private StackPane contentArea;
    @FXML private Label usernameLabel;
    @FXML private Button btnDashboard;
    @FXML private Button btnHistory;
    @FXML private Button btnStatistics;
    @FXML private Button btnSettings;

    // ========================================
    // VARIABLES INTERNES
    // ========================================

    private YearMonth currentMonth;
    private Label monthLabel;
    private VBox categoriesContainer;
    private Label budgetTotalLabel;
    private Label budgetRemainingLabel;

    // ========================================
    // INITIALISATION
    // ========================================

    @FXML
    public void initialize() {
        // Vérifier si un utilisateur est connecté
        if (!AuthServices.isLoggedIn()) {
            try {
                usernameLabel.setText(AuthServices.getCurrentUser().getFullName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        currentMonth = YearMonth.now();
        initBudgetsMoisSiNecessaire();
        loadDashboardContent();
    }

    /**
     * Initialise les budgets du mois actuel si pas encore fait
     */
    private void initBudgetsMoisSiNecessaire() {
        int userId = AuthServices.getCurrentUser().getId();
        BudgetService.initBudgetsMois(userId, currentMonth);
    }

    // ========================================
    // CHARGEMENT DU CONTENU PRINCIPAL
    // ========================================

    /**
     * Charge le contenu principal du dashboard
     */
    private void loadDashboardContent() {
        // Container principal
        VBox mainContainer = new VBox(30);
        mainContainer.setStyle("-fx-background-color: #F5F5F5; -fx-padding: 30 40;");

        // Header avec navigation mois
        HBox header = createMonthNavigation();

        // Cercle de budget
        VBox budgetCircle = createBudgetCircle();

        // ScrollPane pour les catégories
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Container des catégories
        categoriesContainer = new VBox(20);
        scrollPane.setContent(categoriesContainer);

        // Charger les catégories
        loadCategories();

        // Assembler
        mainContainer.getChildren().addAll(header, budgetCircle, scrollPane);

        // Afficher dans la zone de contenu
        contentArea.getChildren().clear();
        contentArea.getChildren().add(mainContainer);
    }

    // ========================================
    // NAVIGATION PAR MOIS
    // ========================================

    /**
     * Crée le header avec navigation de mois
     */
    private HBox createMonthNavigation() {
        HBox header = new HBox(30);
        header.setAlignment(Pos.CENTER);

        // Bouton mois précédent
        Button btnPrev = new Button("◀");
        btnPrev.setPrefSize(50, 50);
        btnPrev.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand;");
        btnPrev.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            initBudgetsMoisSiNecessaire();
            updateMonth();
        });

        // Label du mois
        monthLabel = new Label();
        monthLabel.setFont(Font.font("System Bold", 24));
        updateMonthLabel();

        // Bouton mois suivant
        Button btnNext = new Button("▶");
        btnNext.setPrefSize(50, 50);
        btnNext.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand;");
        btnNext.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            initBudgetsMoisSiNecessaire();
            updateMonth();
        });

        header.getChildren().addAll(btnPrev, monthLabel, btnNext);
        return header;
    }

    /**
     * Met à jour l'affichage du mois
     */
    private void updateMonth() {
        updateMonthLabel();
        updateBudgetCircle();
        loadCategories();
    }

    /**
     * Met à jour uniquement le label du mois
     */
    private void updateMonthLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        String moisFormate = currentMonth.format(formatter);
        // Mettre la première lettre en majuscule
        moisFormate = moisFormate.substring(0, 1).toUpperCase() + moisFormate.substring(1);
        monthLabel.setText(moisFormate);
    }

    // ========================================
    // CERCLE DE BUDGET
    // ========================================

    /**
     * Crée le cercle de budget global
     */
    private VBox createBudgetCircle() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);

        // Labels qui seront mis à jour
        budgetTotalLabel = new Label("0 XOF");
        budgetTotalLabel.setFont(Font.font("System Bold", 28));

        budgetRemainingLabel = new Label("0 XOF");
        budgetRemainingLabel.setStyle("-fx-text-fill: #666;");
        budgetRemainingLabel.setFont(Font.font(16));

        // Calculer les totaux
        updateBudgetCircle();

        container.getChildren().addAll(budgetTotalLabel, budgetRemainingLabel);
        return container;
    }

    /**
     * Met à jour les valeurs du cercle de budget
     */
    private void updateBudgetCircle() {
        if (budgetTotalLabel == null || budgetRemainingLabel == null) return;

        int userId = AuthServices.getCurrentUser().getId();

        // Calculer le budget total de tous les budgets du mois
        double budgetTotal = BudgetService.getTotalBudgetsMois(userId, currentMonth);

        // Calculer le total des dépenses du mois
        double depensesTotal = DepenseService.getTotalDepensesMois(userId, currentMonth);

        // Calculer le restant
        double restant = budgetTotal - depensesTotal;

        // Mettre à jour les labels
        budgetTotalLabel.setText(String.format("%.0f XOF", budgetTotal));
        budgetRemainingLabel.setText(String.format("%.0f XOF", restant));
    }

    // ========================================
    // CHARGEMENT DES CATÉGORIES
    // ========================================

    /**
     * Charge toutes les catégories avec leurs budgets
     */
    private void loadCategories() {
        categoriesContainer.getChildren().clear();

        int userId = AuthServices.getCurrentUser().getId();

        // Récupérer toutes les catégories de l'utilisateur
        List<Categorie> categories = CategorieService.getCategoriesByUser(userId);

        // Créer une carte pour chaque catégorie
        for (Categorie categorie : categories) {
            VBox card = createCategorieCard(categorie);
            categoriesContainer.getChildren().add(card);
        }
    }

    /**
     * Crée une carte de catégorie
     */
    private VBox createCategorieCard(Categorie categorie) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // ========== HEADER : Nom + Budget + Restant ==========
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Nom de la catégorie
        Label nomLabel = new Label(categorie.getNomCategorie());
        nomLabel.setFont(Font.font("System Bold", 18));
        nomLabel.setStyle("-fx-text-fill: #2196F3;");

        // Spacer pour pousser les montants à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Récupérer le budget de cette catégorie pour le mois actuel
        int userId = AuthServices.getCurrentUser().getId();
        Budget budget = BudgetService.getBudgetCategorieParMois(userId, categorie.getIdCategorie(), currentMonth);

        double budgetAlloue = (budget != null) ? budget.getMontant() : 0.0;

        // Calculer les dépenses totales de cette catégorie
        double totalDepenses = DepenseService.getTotalDepensesCategorie(userId, categorie.getIdCategorie(), currentMonth);

        // Calculer le restant
        double budgetRestant = budgetAlloue - totalDepenses;

        // Label budget alloué
        Label budgetLabel = new Label(String.format("%.0f XOF", budgetAlloue));
        budgetLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        budgetLabel.setOnMouseClicked(e -> editBudget(budget, budgetLabel));
        budgetLabel.setStyle(budgetLabel.getStyle() + "-fx-cursor: hand;");

        // Label budget restant
        Label restantLabel = new Label(String.format("%.0f XOF", budgetRestant));
        restantLabel.setStyle("-fx-font-size: 14px;");

        // Colorer en rouge si dépassé
        if (budgetRestant < 0) {
            restantLabel.setStyle(restantLabel.getStyle() + "-fx-text-fill: #F44336;");
        } else {
            restantLabel.setStyle(restantLabel.getStyle() + "-fx-text-fill: #4CAF50;");
        }

        header.getChildren().addAll(nomLabel, spacer, budgetLabel, restantLabel);
        card.getChildren().add(header);

        // ========== SOUS-CATÉGORIES ==========
        List<SousCategorie> sousCategories = CategorieService.getSousCategoriesByCategorie(categorie.getIdCategorie());

        for (SousCategorie sousCat : sousCategories) {
            HBox ligne = createSousCategorieRow(sousCat);
            card.getChildren().add(ligne);
        }

        // ========== BOUTON AJOUTER SOUS-CATÉGORIE ==========
        Button btnAddSousCat = new Button("➕ Ajouter une sous-catégorie");
        btnAddSousCat.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-cursor: hand;");
        btnAddSousCat.setOnAction(e -> {
            System.out.println("Ajouter sous-catégorie à : " + categorie.getNomCategorie());
            // TODO: Implémenter dialogue d'ajout
        });

        card.getChildren().add(btnAddSousCat);

        return card;
    }

    /**
     * Crée une ligne de sous-catégorie
     */
    private HBox createSousCategorieRow(SousCategorie sousCat) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5 0;");

        // Icône
        Label iconLabel = new Label("🍴");
        iconLabel.setStyle("-fx-font-size: 18px;");

        // Nom de la sous-catégorie
        Label nomLabel = new Label(sousCat.getNomSousCategorie());
        nomLabel.setStyle("-fx-font-size: 14px;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Calculer le total des dépenses pour cette sous-catégorie
        int userId = AuthServices.getCurrentUser().getId();
        double totalDepenses = DepenseService.getTotalDepensesSousCategorie(
                userId,
                sousCat.getIdSousCategorie(),
                currentMonth
        );

        // Label du montant
        Label montantLabel = new Label(String.format("%.0f XOF", totalDepenses));
        montantLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Bouton + pour ajouter une dépense
        Button btnPlus = new Button("+");
        btnPlus.setPrefSize(30, 30);
        btnPlus.setStyle("-fx-background-color: #333; -fx-text-fill: white; " +
                "-fx-background-radius: 50%; -fx-cursor: hand;");
        btnPlus.setOnAction(e -> {
            ajouterDepense(sousCat);
        });

        row.getChildren().addAll(iconLabel, nomLabel, spacer, montantLabel, btnPlus);

        return row;
    }

    // ========================================
    // ACTIONS
    // ========================================

    /**
     * Édite le budget d'une catégorie
     */
    private void editBudget(Budget budget, Label budgetLabel) {
        TextInputDialog dialog = new TextInputDialog(String.format("%.0f", budget.getMontant()));
        dialog.setTitle("Modifier le budget");
        dialog.setHeaderText("Budget pour " + currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)));
        dialog.setContentText("Montant (XOF) :");

        dialog.showAndWait().ifPresent(montantStr -> {
            try {
                double nouveauMontant = Double.parseDouble(montantStr);

                if (nouveauMontant < 0) {
                    showAlert("Erreur", "Le montant ne peut pas être négatif", Alert.AlertType.ERROR);
                    return;
                }

                // Mettre à jour en base de données
                boolean success = BudgetService.updateBudget(budget.getIdBudget(), nouveauMontant);

                if (success) {
                    // Mettre à jour l'affichage
                    budgetLabel.setText(String.format("%.0f XOF", nouveauMontant));
                    updateBudgetCircle();
                    loadCategories(); // Recharger pour mettre à jour les restants
                } else {
                    showAlert("Erreur", "Impossible de mettre à jour le budget", Alert.AlertType.ERROR);
                }

            } catch (NumberFormatException e) {
                showAlert("Erreur", "Montant invalide", Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Ajoute une dépense à une sous-catégorie
     */
    private void ajouterDepense(SousCategorie sousCat) {
        // TODO: Ouvrir un dialogue pour ajouter une dépense
        System.out.println("Ajouter dépense pour : " + sousCat.getNomSousCategorie());

        // Pour l'instant, juste un message
        showAlert("Info",
                "Fonctionnalité en cours de développement\n" +
                        "Sous-catégorie : " + sousCat.getNomSousCategorie(),
                Alert.AlertType.INFORMATION);
    }

    /**
     * Affiche une boîte de dialogue
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void showDashboardContent() {
        loadDashboardContent();
        setActiveButton(btnDashboard);
    }

    @FXML
    private void showHistoryContent() {
        // TODO: Charger le contenu Historique
        System.out.println("Afficher Historique");
        setActiveButton(btnHistory);
    }

    @FXML
    private void showStatisticsContent() {
        // TODO: Charger le contenu Statistiques
        System.out.println("Afficher Statistiques");
        setActiveButton(btnStatistics);
    }

    @FXML
    private void showSettingsContent() {
        // TODO: Charger le contenu Paramètres
        System.out.println("Afficher Paramètres");
        setActiveButton(btnSettings);
    }

    @FXML
    private void addTransaction() {
        System.out.println("Ajouter une transaction");
        // TODO: Ouvrir dialogue
    }

    @FXML
    private void addCategory() {
        System.out.println("Ajouter une catégorie");
        // TODO: Ouvrir dialogue
    }

   /* @FXML
    private void handleLogout() {
        AuthServices.logout();
        try {
            com.example.budgetpro.MainApplication.showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private void setActiveButton(Button activeButton) {
        String inactiveStyle = "-fx-background-color: #FFB84D; -fx-text-fill: #333; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-radius: 10; -fx-alignment: CENTER_LEFT; " +
                "-fx-padding: 15 20; -fx-cursor: hand;";

        String activeStyle = "-fx-background-color: #FF9800; -fx-text-fill: #333; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-radius: 10; -fx-alignment: CENTER_LEFT; " +
                "-fx-padding: 15 20; -fx-cursor: hand;";

        btnDashboard.setStyle(inactiveStyle);
        btnHistory.setStyle(inactiveStyle);
        btnStatistics.setStyle(inactiveStyle);
        btnSettings.setStyle(inactiveStyle);

        activeButton.setStyle(activeStyle);
    }
}