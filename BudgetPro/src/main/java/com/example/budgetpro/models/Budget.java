package com.example.budgetpro.models;

import java.time.LocalDate;

public class Budget {
    private int idBudget;
    private double montant;        // Montant alloué pour cette catégorie
    private int userId;            // Utilisateur propriétaire
    private int categorieId;       // Catégorie concernée (ex: Alimentation)
    private LocalDate mois;        // Mois du budget (ex: 2026-01-01)

    // Constructeur vide
    public Budget() {}

    // Constructeur avec paramètres
    public Budget(double montant, int userId, int categorieId, LocalDate mois) {
        this.montant = montant;
        this.userId = userId;
        this.categorieId = categorieId;
        this.mois = mois;
    }

    // ========== GETTERS ==========

    public int getIdBudget() {
        return idBudget;
    }

    public double getMontant() {
        return montant;
    }

    public int getUserId() {
        return userId;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public LocalDate getMois() {
        return mois;
    }

    // ========== SETTERS ==========

    public void setIdBudget(int idBudget) {
        this.idBudget = idBudget;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }

    public void setMois(LocalDate mois) {
        this.mois = mois;
    }

    // ========== MÉTHODES MÉTIER (à déplacer dans BudgetService) ==========

    /**
     * Vérifie si le budget est dépassé
     * @param totalDepenses Total des dépenses pour cette catégorie
     * @return true si dépassé, false sinon
     */
    public boolean estDepasse(double totalDepenses) {
        return totalDepenses > montant;
    }

    /**
     * Calcule le solde restant
     * @param totalDepenses Total des dépenses pour cette catégorie
     * @return Montant restant (peut être négatif si dépassé)
     */
    public double getSoldeRestant(double totalDepenses) {
        return montant - totalDepenses;
    }

    /**
     * Calcule le pourcentage utilisé
     * @param totalDepenses Total des dépenses pour cette catégorie
     * @return Pourcentage (0-100+)
     */
    public double getPourcentageUtilise(double totalDepenses) {
        if (montant == 0) return 0;
        return (totalDepenses / montant) * 100;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + idBudget +
                ", montant=" + montant + "€" +
                ", userId=" + userId +
                ", categorieId=" + categorieId +
                ", mois=" + mois +
                '}';
    }
}