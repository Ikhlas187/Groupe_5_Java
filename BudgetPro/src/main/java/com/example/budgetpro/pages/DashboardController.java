package com.example.budgetpro.pages;

import com.example.budgetpro.models.*;
import com.example.budgetpro.services.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import java.util.Optional;

import java.io.IOException;
import java.time.LocalDate;
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

        if (AuthServices.isLoggedIn()) {
            try {
                usernameLabel.setText(AuthServices.getCurrentUser().getFullName());
                System.out.println("Nom chargé");
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        budgetTotalLabel = new Label(BudgetService.getBudgetInitial(AuthServices.getCurrentUser().getId())+" XOF");
        budgetTotalLabel.setFont(Font.font("System Bold", 28));
        budgetTotalLabel.setOnMouseClicked(e -> editBudgetInitial());

        budgetRemainingLabel = new Label("5 XOF");
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

        // 🎯 RÉCUPÉRER LE BUDGET INITIAL (solde de départ)
        double budgetInitial = BudgetService.getBudgetInitial(userId);

        // 🎯 CALCULER LE TOTAL DES DÉPENSES
        double depensesTotal = DepenseService.getTotalDepensesMois(userId, currentMonth);

        // 🎯 CALCULER LE RESTANT
        double restant = budgetInitial - depensesTotal;

        // 🎯 AFFICHAGE
        budgetTotalLabel.setText(String.format("%.0f XOF", budgetInitial));    // Label du HAUT
        budgetRemainingLabel.setText(String.format("%.0f XOF", restant));       // Label du BAS

        // 🎯 COULEUR DU RESTANT
        if (restant < 0) {
            budgetRemainingLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 16px;"); // Rouge si négatif
        } else {
            budgetRemainingLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 16px;"); // Vert si positif
        }
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
            ajouterDepenseDialog(sousCat);
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

                // 🎯 VALIDATION 1 : Montant positif
                if (nouveauMontant < 0) {
                    showAlert("Erreur", "Le montant ne peut pas être négatif", Alert.AlertType.ERROR);
                    return;
                }

                int userId = AuthServices.getCurrentUser().getId();

                // 🎯 VALIDATION 2 : Ne pas dépasser le budget initial
                double budgetInitial = BudgetService.getBudgetInitial(userId);
                double totalAutresCategories = BudgetService.getTotalBudgetsCategoriesMois(userId, currentMonth)
                        - budget.getMontant(); // Exclure le budget actuel de cette catégorie
                double nouveauTotal = totalAutresCategories + nouveauMontant;

                if (nouveauTotal > budgetInitial) {
                    double budgetDisponible = budgetInitial - totalAutresCategories;
                    showAlert("Erreur",
                            "Budget insuffisant !\n\n" +
                                    "Budget initial : " + String.format("%.0f", budgetInitial) + " XOF\n" +
                                    "Déjà alloué aux autres catégories : " + String.format("%.0f", totalAutresCategories) + " XOF\n" +
                                    "Budget disponible : " + String.format("%.0f", budgetDisponible) + " XOF\n\n" +
                                    "Vous ne pouvez pas allouer " + String.format("%.0f", nouveauMontant) + " XOF.",
                            Alert.AlertType.ERROR);
                    return;
                }

                // 🎯 VALIDATION 3 : Vérifier les dépenses déjà effectuées
                double depensesCategorie = DepenseService.getTotalDepensesCategorie(userId, budget.getCategorieId(), currentMonth);

                if (nouveauMontant < depensesCategorie) {
                    Alert confirm = new Alert(Alert.AlertType.WARNING);
                    confirm.setTitle("Attention");
                    confirm.setHeaderText("Budget inférieur aux dépenses");
                    confirm.setContentText(
                            "Vous avez déjà dépensé " + String.format("%.0f", depensesCategorie) + " XOF dans cette catégorie.\n" +
                                    "Allouer seulement " + String.format("%.0f", nouveauMontant) + " XOF créera un déficit de " +
                                    String.format("%.0f", depensesCategorie - nouveauMontant) + " XOF.\n\n" +
                                    "Voulez-vous continuer ?"
                    );

                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        return;
                    }
                }

                // 🎯 MISE À JOUR
                boolean success = BudgetService.updateBudget(budget.getIdBudget(), nouveauMontant);

                if (success) {
                    budgetLabel.setText(String.format("%.0f XOF", nouveauMontant));
                    updateBudgetCircle();
                    loadCategories();
                    showAlert("Succès", "Budget modifié avec succès !", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Impossible de modifier le budget", Alert.AlertType.ERROR);
                }

            } catch (NumberFormatException e) {
                showAlert("Erreur", "Montant invalide !", Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Ajoute une dépense à une sous-catégorie
     */
    private void ajouterDepense(SousCategorie sousCat) {

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
    private void showHistoryContent(MouseEvent event) throws IOException {
        // TODO: Charger le contenu Historique
        System.out.println("Afficher Historique");
        setActiveButton(btnHistory);
        SceneSwitcher.switchContent("/com/example/budgetpro/Historique.fxml",contentArea);
    }

    @FXML
    private void showStatisticsContent() throws IOException {
        // TODO: Charger le contenu Statistiques
        System.out.println("Afficher Statistiques");
        setActiveButton(btnStatistics);
        SceneSwitcher.switchContent("/com/example/budgetpro/Statistique.fxml",contentArea);
    }

    @FXML
    private void showSettingsContent() throws IOException {
        // TODO: Charger le contenu Paramètres
        System.out.println("Afficher Paramètres");
        setActiveButton(btnSettings);
        SceneSwitcher.switchContent("/com/example/budgetpro/Settings.fxml",contentArea);
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
    private void ajouterDepenseDialog(SousCategorie sousCat) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une dépense");
        dialog.setHeaderText("Sous-catégorie : " + sousCat.getNomSousCategorie());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField montantField = new TextField();
        montantField.setPromptText("Montant (XOF)");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        // 🎯 BLOQUER LES DATES FUTURES
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });

        grid.add(new Label("Montant :"), 0, 0);
        grid.add(montantField, 1, 0);
        grid.add(new Label("Description :"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Date :"), 0, 2);
        grid.add(datePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    double montant = Double.parseDouble(montantField.getText());
                    String description = descriptionField.getText();
                    LocalDate date = datePicker.getValue();

                    // 🎯 VALIDATION 1 : Montant positif
                    if (montant <= 0) {
                        showAlert("Erreur", "Le montant doit être positif", Alert.AlertType.ERROR);
                        return;
                    }

                    // 🎯 VALIDATION 2 : Date dans le mois actuel
                    YearMonth dateMonth = YearMonth.from(date);
                    if (!dateMonth.equals(currentMonth)) {
                        showAlert("Erreur",
                                "La date doit être dans le mois actuel (" +
                                        currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)) + ")",
                                Alert.AlertType.ERROR);
                        return;
                    }

                    if (description == null || description.trim().isEmpty()) {
                        description = "Dépense " + sousCat.getNomSousCategorie();
                    }

                    int userId = AuthServices.getCurrentUser().getId();

                    // 🎯 VALIDATION 3 : Vérifier le budget de la catégorie
                    int categorieId = CategorieService.getSousCategorieById(sousCat.getIdSousCategorie()).getCategorieId();
                    Budget budget = BudgetService.getBudgetCategorieParMois(userId, categorieId, currentMonth);

                    if (budget != null) {
                        double budgetAlloue = budget.getMontant();
                        double depensesActuelles = DepenseService.getTotalDepensesCategorie(userId, categorieId, currentMonth);
                        double nouveauTotal = depensesActuelles + montant;

                        if (nouveauTotal > budgetAlloue) {
                            double budgetRestant = budgetAlloue - depensesActuelles;

                            Alert confirm = new Alert(Alert.AlertType.WARNING);
                            confirm.setTitle("Dépassement de budget");
                            confirm.setHeaderText("Cette dépense dépasse votre budget !");
                            confirm.setContentText(
                                    "Budget alloué : " + String.format("%.0f", budgetAlloue) + " XOF\n" +
                                            "Déjà dépensé : " + String.format("%.0f", depensesActuelles) + " XOF\n" +
                                            "Budget restant : " + String.format("%.0f", budgetRestant) + " XOF\n\n" +
                                            "Cette dépense de " + String.format("%.0f", montant) + " XOF créera un dépassement de " +
                                            String.format("%.0f", nouveauTotal - budgetAlloue) + " XOF.\n\n" +
                                            "Voulez-vous continuer quand même ?"
                            );

                            Optional<ButtonType> result = confirm.showAndWait();
                            if (result.isEmpty() || result.get() != ButtonType.OK) {
                                return;
                            }
                        }
                    }

                    // 🎯 AJOUT DE LA DÉPENSE
                    boolean success = DepenseService.ajouterDepense(
                            montant,
                            description,
                            date,
                            sousCat.getIdSousCategorie(),
                            userId
                    );

                    if (success) {
                        loadCategories();
                        updateBudgetCircle();

                        showAlert("Succès",
                                "Dépense de " + String.format("%.0f", montant) + " XOF ajoutée !",
                                Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Erreur", "Impossible d'ajouter la dépense", Alert.AlertType.ERROR);
                    }

                } catch (NumberFormatException ex) {
                    showAlert("Erreur", "Montant invalide !", Alert.AlertType.ERROR);
                }
            }
        });
    }

    /**
     * Modifier le budget initial de l'utilisateur
     */
    private void editBudgetInitial() {
        int userId = AuthServices.getCurrentUser().getId();
        double budgetActuel = BudgetService.getBudgetInitial(userId);

        TextInputDialog dialog = new TextInputDialog(String.format("%.0f", budgetActuel));
        dialog.setTitle("Modifier le budget initial");
        dialog.setHeaderText("Budget de départ mensuel");
        dialog.setContentText("Montant (XOF) :");

        dialog.showAndWait().ifPresent(montantStr -> {
            try {
                double nouveauMontant = Double.parseDouble(montantStr);

                // 🎯 VALIDATION 1 : Montant positif
                if (nouveauMontant < 0) {
                    showAlert("Erreur", "Le montant ne peut pas être négatif", Alert.AlertType.ERROR);
                    return;
                }

                // 🎯 VALIDATION 2 : Vérifier que le nouveau budget >= somme des budgets catégories
                double totalBudgetsCategories = BudgetService.getTotalBudgetsCategoriesMois(userId, currentMonth);

                if (nouveauMontant < totalBudgetsCategories) {
                    showAlert("Erreur",
                            "Le budget initial (" + String.format("%.0f", nouveauMontant) + " XOF) " +
                                    "ne peut pas être inférieur à la somme des budgets alloués aux catégories (" +
                                    String.format("%.0f", totalBudgetsCategories) + " XOF).\n\n" +
                                    "Réduisez d'abord les budgets de vos catégories.",
                            Alert.AlertType.ERROR);
                    return;
                }

                // 🎯 MISE À JOUR
                boolean success = BudgetService.updateBudgetInitial(userId, nouveauMontant);

                if (success) {
                    updateBudgetCircle();
                    showAlert("Succès",
                            "Budget initial modifié : " + String.format("%.0f", nouveauMontant) + " XOF",
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Impossible de modifier le budget initial", Alert.AlertType.ERROR);
                }

            } catch (NumberFormatException e) {
                showAlert("Erreur", "Montant invalide !", Alert.AlertType.ERROR);
            }
        });
    }

}