package com.example.budgetpro.models;

import java.time.LocalDate;

/**
 * Représente le budget alloué à une catégorie pour un mois donné
 */
public class Budget {
    private int idBudget;
    private double montant;        // Montant alloué pour cette catégorie
    private int userId;            // Utilisateur propriétaire
    private int categorieId;       // Catégorie concernée
    private LocalDate mois;        // Mois du budget (format: premier jour du mois)

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

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + idBudget +
                ", montant=" + montant + " XOF" +
                ", userId=" + userId +
                ", categorieId=" + categorieId +
                ", mois=" + mois +
                '}';
    }
}