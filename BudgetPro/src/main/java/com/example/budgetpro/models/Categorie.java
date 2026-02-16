package com.example.budgetpro.models;

/**
 * Représente une catégorie de dépenses
 * Le budget est géré séparément dans la table budgets
 */
public class Categorie {
    private int idCategorie;
    private String nomCategorie;
    private int userId;

    // Constructeur vide
    public Categorie() {}

    // Constructeur avec paramètres
    public Categorie(String nomCategorie, int userId) {
        this.nomCategorie = nomCategorie;
        this.userId = userId;
    }

    // ========== GETTERS ==========

    public int getIdCategorie() {
        return idCategorie;
    }

    public String getNomCategorie() {
        return nomCategorie;
    }

    public int getUserId() {
        return userId;
    }

    // ========== SETTERS ==========

    public void setIdCategorie(int idCategorie) {
        this.idCategorie = idCategorie;
    }

    public void setNomCategorie(String nomCategorie) {
        this.nomCategorie = nomCategorie;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Categorie{" +
                "id=" + idCategorie +
                ", nom='" + nomCategorie + '\'' +
                ", userId=" + userId +
                '}';
    }
}