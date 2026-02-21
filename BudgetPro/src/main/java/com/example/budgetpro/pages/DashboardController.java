package com.example.budgetpro.pages;

import com.example.budgetpro.models.*;
import com.example.budgetpro.services.*;
import com.example.budgetpro.dao.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.Locale;

public class DashboardController {

    @FXML private StackPane contentArea;
    @FXML private Label usernameLabel;
    @FXML private Button btnDashboard;
    @FXML private Button btnHistory;
    @FXML private Button btnStatistics;
    @FXML private Button btnSettings;
    @FXML private Button btnAjouterRevenu;

    // ========================================
    // VARIABLES INTERNES
    // ========================================

    private YearMonth currentMonth;
    private Label monthLabel;
    private VBox categoriesContainer;
    private Label budgetTotalLabel;      // Budget total du mois (cliquable pour modifier ce mois uniquement)
    private Label budgetRemainingLabel;  // Budget restant après dépenses
    // revenusLabel supprimé : les revenus s'ajoutent directement au budgetTotal du mois
    private Canvas budgetCanvas;

    // ========================================
    // INITIALISATION
    // ========================================

    @FXML
    public void initialize() {
        if (AuthServices.isLoggedIn()) {
            try {
                usernameLabel.setText(AuthServices.getCurrentUser().getFullName());
                if (btnAjouterRevenu != null) {
                    btnAjouterRevenu.setOnAction(e -> ajouterRevenuDialog());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        currentMonth = YearMonth.now();
        initBudgetsMoisSiNecessaire();
        loadDashboardContent();
    }

    private void initBudgetsMoisSiNecessaire() {
        int userId = AuthServices.getCurrentUser().getId();
        BudgetService.initBudgetsMois(userId, currentMonth);
    }

    private void loadDashboardContent() {
        VBox mainContainer = new VBox(12); // ← réduit de 20 à 12
        // ✅ PAS de setStyle couleur ici : la couleur vient du CSS thème via .content-area
        mainContainer.getStyleClass().add("main-content");
        mainContainer.setPadding(new Insets(15, 20, 15, 20)); // ← réduit de 20 30

        HBox header = createMonthNavigation();
        StackPane budgetCircle = createBudgetCircle();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        scrollPane.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.getStyleClass().add("categories-scroll");

        categoriesContainer = new VBox(10); // ← réduit de 20 à 10
        categoriesContainer.setPadding(new Insets(0, 0, 10, 0));
        scrollPane.setContent(categoriesContainer);
        loadCategories();

        mainContainer.getChildren().addAll(header, budgetCircle, scrollPane);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(mainContainer);
    }

    // ========================================
    // NAVIGATION PAR MOIS
    // ========================================

    private HBox createMonthNavigation() {
        HBox header = new HBox(20); // ← réduit de 30 à 20
        header.setAlignment(Pos.CENTER);

        Button btnPrev = new Button("‹"); // ← chevron simple comme la maquette
        btnPrev.setPrefSize(36, 36);      // ← réduit de 50x50 à 36x36
        btnPrev.getStyleClass().add("month-nav-btn");
        btnPrev.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            initBudgetsMoisSiNecessaire();
            updateMonth();
        });

        monthLabel = new Label();
        monthLabel.getStyleClass().add("month-title");
        // ✅ PAS de Font.font inline : taille dans le CSS
        updateMonthLabel();

        Button btnNext = new Button("›"); // ← chevron simple
        btnNext.setPrefSize(36, 36);
        btnNext.getStyleClass().add("month-nav-btn");
        btnNext.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            initBudgetsMoisSiNecessaire();
            updateMonth();
        });

        header.getChildren().addAll(btnPrev, monthLabel, btnNext);
        return header;
    }

    private void updateMonth() {
        updateMonthLabel();
        updateBudgetCircle();
        loadCategories();
    }

    private void updateMonthLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        String moisFormate = currentMonth.format(formatter);
        moisFormate = moisFormate.substring(0, 1).toUpperCase() + moisFormate.substring(1);
        monthLabel.setText(moisFormate);
    }

    // ========================================
    // CERCLE DE BUDGET
    // ========================================

    private StackPane createBudgetCircle() {
        StackPane container = new StackPane();
        double size = 250;
        Canvas canvas = new Canvas(size, size);

        VBox labelsContainer = new VBox(5);
        labelsContainer.setAlignment(Pos.CENTER);

        // Budget total du mois (cliquable — modifie uniquement ce mois)
        budgetTotalLabel = new Label("0 XOF");
        budgetTotalLabel.setFont(Font.font("System Bold", 20));
        budgetTotalLabel.setStyle("-fx-cursor: hand;");
        budgetTotalLabel.setOnMouseClicked(e -> editBudgetTotalMois());

        // Budget restant = budgetTotalMois - dépenses du mois
        budgetRemainingLabel = new Label("0 XOF");
        budgetRemainingLabel.setFont(Font.font(14));
        budgetRemainingLabel.getStyleClass().add("budget-remaining");

        labelsContainer.getChildren().addAll(budgetTotalLabel, budgetRemainingLabel);

        budgetCanvas = canvas;
        container.getChildren().addAll(canvas, labelsContainer);
        updateBudgetCircle();
        return container;
    }

    /**
     * Met à jour les valeurs du cercle.
     * budgetTotalMois = ligne budget (id_categorie IS NULL, mois = ce mois).
     * Il commence au budget de référence et augmente à chaque revenu ajouté dans ce mois.
     */
    private void updateBudgetCircle() {
        if (budgetTotalLabel == null || budgetRemainingLabel == null || budgetCanvas == null) return;

        int userId = AuthServices.getCurrentUser().getId();

        double budgetTotalMois = BudgetService.getBudgetTotalMois(userId, currentMonth);
        double depensesTotal   = DepenseService.getTotalDepensesMois(userId, currentMonth);
        double restant         = budgetTotalMois - depensesTotal;

        budgetTotalLabel.setText(String.format("%.0f XOF", budgetTotalMois));
        budgetRemainingLabel.setText(String.format("%.0f XOF", restant));

        budgetRemainingLabel.setStyle(restant < 0
                ? "-fx-text-fill: #F44336;"
                : "-fx-text-fill: #4CAF50;");

        drawProgressCircle(userId, depensesTotal);
    }

    // ========================================
    // CHARGEMENT DES CATÉGORIES
    // ========================================

    private void loadCategories() {
        categoriesContainer.getChildren().clear();
        int userId = AuthServices.getCurrentUser().getId();
        List<Categorie> categories = CategorieService.getCategoriesByUser(userId);
        for (Categorie categorie : categories) {
            categoriesContainer.getChildren().add(createCategorieCard(categorie));
        }
    }

    private VBox createCategorieCard(Categorie categorie) {
        VBox card = new VBox(6);
        card.getStyleClass().add("category-card");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        String couleur = CategorieIcon.getCouleur(categorie.getNomCategorie());

        Label nomLabel = new Label(categorie.getNomCategorie());
        nomLabel.getStyleClass().add("category-title");
        nomLabel.setStyle("-fx-text-fill: " + couleur + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int userId = AuthServices.getCurrentUser().getId();
        Budget budget = BudgetService.getBudgetCategorieParMois(userId, categorie.getIdCategorie(), currentMonth);

        double budgetAlloue = (budget != null) ? budget.getMontant() : 0.0;
        double totalDepenses = DepenseService.getTotalDepensesCategorie(userId, categorie.getIdCategorie(), currentMonth);
        double budgetRestant = budgetAlloue - totalDepenses;

        Label budgetLabel = new Label(String.format("%.0f XOF", budgetAlloue));
        budgetLabel.getStyleClass().add("subcategory-amount");
        budgetLabel.setStyle("-fx-cursor: hand; -fx-font-weight: bold;");
        budgetLabel.setOnMouseClicked(e -> editBudget(budget, budgetLabel));

        Label restantLabel = new Label(String.format("%.0f XOF", budgetRestant));
        restantLabel.setStyle(budgetRestant < 0
                ? "-fx-font-size: 13px; -fx-text-fill: #F44336;"
                : "-fx-font-size: 13px; -fx-text-fill: #4CAF50;");

        header.getChildren().addAll(nomLabel, spacer, budgetLabel, new Label("  "), restantLabel);
        card.getChildren().add(header);

        // Calcul du pourcentage de progression
        double progress = (budgetAlloue > 0) ? Math.min(totalDepenses / budgetAlloue, 1.0) : 0;


        StackPane progressContainer = new StackPane();
        progressContainer.setPrefHeight(8); // Hauteur augmentée à 8px
        progressContainer.setMaxWidth(Double.MAX_VALUE);
        progressContainer.setStyle("-fx-background-radius: 4;");

        // Barre de progression réelle
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setPrefHeight(10);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: " + couleur + "; -fx-control-inner-background: transparent;");

        progressBar.prefWidthProperty().bind(progressContainer.widthProperty());
        progressContainer.getChildren().add(progressBar);
        card.getChildren().add(progressContainer);

        List<SousCategorie> sousCategories = CategorieService.getSousCategoriesByCategorie(categorie.getIdCategorie());
        for (SousCategorie sousCat : sousCategories) {
            card.getChildren().add(createSousCategorieRow(sousCat, categorie));
        }

        Button btnAddSousCat = new Button("+ Ajouter une sous-catégorie");
        btnAddSousCat.setStyle("-fx-background-color: transparent; -fx-font-size: 11px; -fx-padding: 3 0; -fx-cursor: hand;");
        btnAddSousCat.getStyleClass().add("subcategory-name");
        btnAddSousCat.setOnAction(e -> ajouterSousCategorieDialog(categorie));
        card.getChildren().add(btnAddSousCat);

        return card;
    }

    private HBox createSousCategorieRow(SousCategorie sousCat, Categorie categorie) {
        HBox row = new HBox(10); // ← réduit de 12 à 10
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 2, 0)); // ← réduit de 3 0 à 2 0

        String icone  = CategorieIcon.getIcone(sousCat.getNomSousCategorie());
        String couleur = CategorieIcon.getCouleur(categorie.getNomCategorie());

        Label iconLabel = new Label(icone);
        iconLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: " + couleur + ";"); // ← réduit de 18px à 15px

        Label nomLabel = new Label(sousCat.getNomSousCategorie());
        // ✅ styleClass : couleur texte vient du thème CSS
        nomLabel.getStyleClass().add("subcategory-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int userId = AuthServices.getCurrentUser().getId();
        double totalDepenses = DepenseService.getTotalDepensesSousCategorie(
                userId, sousCat.getIdSousCategorie(), currentMonth);

        Label montantLabel = new Label(String.format("%.0f XOF", totalDepenses));
        // ✅ styleClass : couleur texte vient du thème CSS
        montantLabel.getStyleClass().add("subcategory-amount");

        // Bouton + rond — fond couleur catégorie conservé (sémantique, pas thème)
        Button btnPlus = new Button("+");
        btnPlus.setPrefSize(24, 24); // ← réduit de 26 à 24
        btnPlus.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; " +
                "-fx-background-radius: 50%; -fx-cursor: hand; -fx-font-size: 13px; -fx-font-weight: bold;");
        btnPlus.setOnAction(e -> ajouterDepenseDialog(sousCat));

        row.getChildren().addAll(iconLabel, nomLabel, spacer, montantLabel, btnPlus);
        return row;
    }


    // ========================================
    // ÉDITION DU BUDGET TOTAL DU MOIS
    // ========================================

    /**
     * Modifie le budgetTotal du mois affiché uniquement.
     * Les autres mois et le budget de référence (inscription) restent intacts.
     */
    private void editBudgetTotalMois() {
        int userId = AuthServices.getCurrentUser().getId();
        double budgetActuel = BudgetService.getBudgetTotalMois(userId, currentMonth);
        String moisFormate = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH));

        TextInputDialog dialog = new TextInputDialog(String.format("%.0f", budgetActuel));
        dialog.setTitle("Modifier le budget de " + moisFormate);
        dialog.setHeaderText("Budget total pour " + moisFormate + " uniquement");
        dialog.setContentText("Montant (XOF) :");

        dialog.showAndWait().ifPresent(montantStr -> {
            try {
                double nouveauMontant = Double.parseDouble(montantStr);

                if (nouveauMontant < 0) {
                    showAlert("Erreur", "Le montant ne peut pas être négatif", Alert.AlertType.ERROR);
                    return;
                }

                double totalCats = BudgetService.getTotalBudgetsCategoriesMois(userId, currentMonth);
                if (nouveauMontant < totalCats) {
                    showAlert("Erreur",
                            "Le budget du mois (" + String.format("%.0f", nouveauMontant) + " XOF) " +
                                    "ne peut pas être inférieur à la somme des budgets catégories (" +
                                    String.format("%.0f", totalCats) + " XOF).\n\n" +
                                    "Réduisez d'abord les budgets de vos catégories.",
                            Alert.AlertType.ERROR);
                    return;
                }

                boolean success = BudgetService.updateBudgetTotalMois(userId, currentMonth, nouveauMontant);
                if (success) {
                    updateBudgetCircle();
                    showAlert("Succès",
                            "Budget de " + moisFormate + " modifié : " + String.format("%.0f", nouveauMontant) + " XOF\n" +
                                    "(Les autres mois ne sont pas affectés)",
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Impossible de modifier le budget du mois", Alert.AlertType.ERROR);
                }

            } catch (NumberFormatException e) {
                showAlert("Erreur", "Montant invalide !", Alert.AlertType.ERROR);
            }
        });
    }

    // ========================================
    // ÉDITION DU BUDGET D'UNE CATÉGORIE
    // ========================================

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

                int userId = AuthServices.getCurrentUser().getId();
                double budgetTotalMois = BudgetService.getBudgetTotalMois(userId, currentMonth);
                double totalAutres = BudgetService.getTotalBudgetsCategoriesMois(userId, currentMonth) - budget.getMontant();

                if (totalAutres + nouveauMontant > budgetTotalMois) {
                    double dispo = budgetTotalMois - totalAutres;
                    showAlert("Erreur",
                            "Budget insuffisant !\n\n" +
                                    "Budget du mois : " + String.format("%.0f", budgetTotalMois) + " XOF\n" +
                                    "Déjà alloué aux autres catégories : " + String.format("%.0f", totalAutres) + " XOF\n" +
                                    "Disponible : " + String.format("%.0f", dispo) + " XOF",
                            Alert.AlertType.ERROR);
                    return;
                }

                double depensesCat = DepenseService.getTotalDepensesCategorie(userId, budget.getCategorieId(), currentMonth);
                if (nouveauMontant < depensesCat) {
                    Alert confirm = new Alert(Alert.AlertType.WARNING);
                    confirm.setTitle("Attention");
                    confirm.setHeaderText("Budget inférieur aux dépenses");
                    confirm.setContentText("Vous avez déjà dépensé " + String.format("%.0f", depensesCat) + " XOF.\n" +
                            "Cela créera un déficit de " + String.format("%.0f", depensesCat - nouveauMontant) + " XOF.\n\nContinuer ?");
                    Optional<ButtonType> r = confirm.showAndWait();
                    if (r.isEmpty() || r.get() != ButtonType.OK) return;
                }

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

    // ========================================
    // AJOUTER UN REVENU
    // ========================================

    /**
     * Ajoute un revenu : insère dans la table revenu ET augmente le budgetTotal
     * du mois de la date choisie dans la table budget (id_categorie IS NULL, mois = ce mois).
     */
    @FXML
    public void ajouterRevenuDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un revenu");
        dialog.setHeaderText("💰 Nouveau revenu");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField montantField = new TextField();
        montantField.setPromptText("Montant (XOF)");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Ex: Salaire, Prime, Freelance...");

        DatePicker datePicker = new DatePicker(LocalDate.now());
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

        grid.add(new Label("Montant * :"), 0, 0);
        grid.add(montantField, 1, 0);
        grid.add(new Label("Description * :"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Date :"), 0, 2);
        grid.add(datePicker, 1, 2);

        Label infoLabel = new Label("ℹ️ Le revenu sera ajouté au budget du mois de la date choisie.");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        grid.add(infoLabel, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Platform.runLater(() -> montantField.requestFocus());

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String montantStr = montantField.getText();
                    String description = descriptionField.getText();
                    LocalDate date = datePicker.getValue();

                    if (montantStr == null || montantStr.trim().isEmpty()) {
                        showAlert("Erreur", "Le montant est obligatoire", Alert.AlertType.ERROR);
                        return;
                    }

                    double montant = Double.parseDouble(montantStr);

                    if (montant <= 0) {
                        showAlert("Erreur", "Le montant doit être positif", Alert.AlertType.ERROR);
                        return;
                    }

                    if (description == null || description.trim().isEmpty()) {
                        showAlert("Erreur", "La description est obligatoire", Alert.AlertType.ERROR);
                        return;
                    }

                    // Confirmation si le revenu est dans un autre mois que celui affiché
                    YearMonth dateMonth = YearMonth.from(date);
                    if (!dateMonth.equals(currentMonth)) {
                        String moisRevenu = dateMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH));
                        Alert confirm = new Alert(Alert.AlertType.WARNING);
                        confirm.setTitle("Revenu dans un autre mois");
                        confirm.setHeaderText("Confirmation requise");
                        confirm.setContentText(
                                "Ce revenu sera ajouté au budget de " + moisRevenu + ".\n\n" +
                                        "Le budget du mois actuel ne sera pas modifié.\n\nContinuer ?"
                        );
                        Optional<ButtonType> result = confirm.showAndWait();
                        if (result.isEmpty() || result.get() != ButtonType.OK) return;
                    }

                    int userId = AuthServices.getCurrentUser().getId();
                    boolean success = RevenuService.ajouterRevenu(montant, description.trim(), date, userId);

                    if (success) {
                        updateBudgetCircle();
                        String moisLabel = YearMonth.from(date).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH));
                        showAlert("Succès",
                                "Revenu de " + String.format("%.0f", montant) + " XOF ajouté !\n" +
                                        "Description : " + description.trim() + "\n" +
                                        "Budget de " + moisLabel + " augmenté.",
                                Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Erreur", "Impossible d'ajouter le revenu", Alert.AlertType.ERROR);
                    }

                } catch (NumberFormatException ex) {
                    showAlert("Erreur", "Montant invalide !", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ========================================
    // CERCLE DE PROGRESSION
    // ========================================

    private void drawProgressCircle(int userId, double depensesTotal) {
        GraphicsContext gc = budgetCanvas.getGraphicsContext2D();
        double size    = budgetCanvas.getWidth();
        double centerX = size / 2;
        double centerY = size / 2;
        double radius  = (size - 40) / 2; // ← réduit le margin de 60 à 40
        double lineWidth = 10; // ← réduit de 15 à 10

        gc.clearRect(0, 0, size, size);
        gc.setLineWidth(lineWidth);
        // ✅ Couleur du cercle vide selon le thème
        gc.setStroke(ThemeManager.isDarkMode() ? Color.web("#4a5568") : Color.web("#E0E0E0"));
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        if (depensesTotal == 0) return;

        StatistiqueDAO statistiqueDAO = new StatistiqueDAO();
        Map<String, Double> depensesParCategorie = statistiqueDAO.getDepensesParCategorie(userId);
        double startAngle = -90;

        for (Map.Entry<String, Double> entry : depensesParCategorie.entrySet()) {
            double montant    = entry.getValue();
            double angle      = (montant / depensesTotal) * 360;
            String couleurHex = CategorieIcon.getCouleur(entry.getKey());

            gc.setStroke(Color.web(couleurHex));
            gc.setLineWidth(lineWidth);
            gc.strokeArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                    startAngle, angle, javafx.scene.shape.ArcType.OPEN);
            startAngle += angle;
        }
    }


    // ========================================
    // DIALOGUES SECONDAIRES
    // ========================================

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

                    if (montant <= 0) {
                        showAlert("Erreur", "Le montant doit être positif", Alert.AlertType.ERROR);
                        return;
                    }

                    if (!YearMonth.from(date).equals(currentMonth)) {
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
                    int categorieId = CategorieService.getSousCategorieById(sousCat.getIdSousCategorie()).getCategorieId();
                    Budget budget = BudgetService.getBudgetCategorieParMois(userId, categorieId, currentMonth);

                    if (budget != null) {
                        double budgetAlloue = budget.getMontant();
                        double depensesActuelles = DepenseService.getTotalDepensesCategorie(userId, categorieId, currentMonth);
                        double nouveauTotal = depensesActuelles + montant;

                        if (nouveauTotal > budgetAlloue) {
                            double restant = budgetAlloue - depensesActuelles;
                            Alert confirm = new Alert(Alert.AlertType.WARNING);
                            confirm.setTitle("Dépassement de budget");
                            confirm.setHeaderText("Cette dépense dépasse votre budget !");
                            confirm.setContentText(
                                    "Budget alloué : " + String.format("%.0f", budgetAlloue) + " XOF\n" +
                                            "Déjà dépensé : " + String.format("%.0f", depensesActuelles) + " XOF\n" +
                                            "Restant : " + String.format("%.0f", restant) + " XOF\n\n" +
                                            "Dépassement : " + String.format("%.0f", nouveauTotal - budgetAlloue) + " XOF\n\nContinuer ?");
                            Optional<ButtonType> r = confirm.showAndWait();
                            if (r.isEmpty() || r.get() != ButtonType.OK) return;
                        }
                    }

                    boolean success = DepenseService.ajouterDepense(montant, description, date,
                            sousCat.getIdSousCategorie(), userId);

                    if (success) {
                        loadCategories();
                        updateBudgetCircle();
                        showAlert("Succès", "Dépense de " + String.format("%.0f", montant) + " XOF ajoutée !",
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

    private void ajouterSousCategorieDialog(Categorie categorie) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ajouter une sous-catégorie");
        dialog.setHeaderText("Catégorie : " + categorie.getNomCategorie());
        dialog.setContentText("Nom de la sous-catégorie :");

        dialog.showAndWait().ifPresent(nom -> {
            if (nom == null || nom.trim().isEmpty()) {
                showAlert("Erreur", "Le nom ne peut pas être vide", Alert.AlertType.ERROR);
                return;
            }
            if (nom.trim().length() < 2) {
                showAlert("Erreur", "Le nom doit contenir au moins 2 caractères", Alert.AlertType.ERROR);
                return;
            }
            boolean success = CategorieService.ajouterSousCategorie(nom.trim(), categorie.getIdCategorie());
            if (success) {
                loadCategories();
                showAlert("Succès", "Sous-catégorie \"" + nom.trim() + "\" ajoutée !", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Cette sous-catégorie existe déjà ou une erreur s'est produite", Alert.AlertType.ERROR);
            }
        });
    }

    public void creerCategorieDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Créer une catégorie");
        dialog.setHeaderText("Nouvelle catégorie de budget");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nomField = new TextField();
        nomField.setPromptText("Ex: Loisirs, Education, Santé...");
        TextField sousCategoriesField = new TextField();
        sousCategoriesField.setPromptText("Ex: Cinéma, Livres (séparées par des virgules)");
        Label infoLabel = new Label("Les sous-catégories sont optionnelles.");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        grid.add(new Label("Nom de la catégorie * :"), 0, 0);
        grid.add(nomField, 0, 1);
        grid.add(new Label("Sous-catégories (optionnel) :"), 0, 2);
        grid.add(sousCategoriesField, 0, 3);
        grid.add(infoLabel, 0, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Platform.runLater(() -> nomField.requestFocus());

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String nomCategorie = nomField.getText();
                String sousCategoriesStr = sousCategoriesField.getText();

                if (nomCategorie == null || nomCategorie.trim().isEmpty()) {
                    showAlert("Erreur", "Le nom est obligatoire", Alert.AlertType.ERROR);
                    return;
                }
                if (nomCategorie.trim().length() < 2 || nomCategorie.trim().length() > 50) {
                    showAlert("Erreur", "Le nom doit contenir entre 2 et 50 caractères", Alert.AlertType.ERROR);
                    return;
                }

                int userId = AuthServices.getCurrentUser().getId();
                int categorieId = CategorieService.creerCategorie(nomCategorie.trim(), userId);

                if (categorieId == 0) {
                    showAlert("Erreur", "Impossible de créer la catégorie.\nElle existe peut-être déjà.", Alert.AlertType.ERROR);
                    return;
                }

                if (sousCategoriesStr != null && !sousCategoriesStr.trim().isEmpty()) {
                    for (String sousCat : sousCategoriesStr.split(",")) {
                        String nom = sousCat.trim();
                        if (!nom.isEmpty() && nom.length() >= 2) {
                            CategorieService.ajouterSousCategorie(nom, categorieId);
                        }
                    }
                }

                BudgetService.initBudgetsMois(userId, currentMonth);
                loadCategories();
                showAlert("Succès", "Catégorie \"" + nomCategorie.trim() + "\" créée avec succès !", Alert.AlertType.INFORMATION);
            }
        });
    }

    // ========================================
    // UTILITAIRES
    // ========================================

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML private void showDashboardContent() { loadDashboardContent(); setActiveButton(btnDashboard); }

    @FXML
    private void showHistoryContent(MouseEvent event) throws IOException {
        setActiveButton(btnHistory);
        SceneSwitcher.switchContent("/com/example/budgetpro/Historique.fxml", contentArea);
    }

    @FXML
    private void showStatisticsContent() throws IOException {
        setActiveButton(btnStatistics);
        SceneSwitcher.switchContent("/com/example/budgetpro/Statistique.fxml", contentArea);
    }

    @FXML
    private void showSettingsContent() throws IOException {
        setActiveButton(btnSettings);
        SceneSwitcher.switchContent("/com/example/budgetpro/Settings.fxml", contentArea);
    }

    @FXML private void addTransaction() {}
    @FXML private void addCategory() {}

    private void setActiveButton(Button activeButton) {
        for (Button b : List.of(btnDashboard, btnHistory, btnStatistics, btnSettings)) {
            b.getStyleClass().removeAll("sidebar-btn-active");
            if (!b.getStyleClass().contains("sidebar-btn")) {
                b.getStyleClass().add("sidebar-btn");
            }
        }
        activeButton.getStyleClass().remove("sidebar-btn");
        if (!activeButton.getStyleClass().contains("sidebar-btn-active")) {
            activeButton.getStyleClass().add("sidebar-btn-active");
        }
    }
}