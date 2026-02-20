package com.example.budgetpro.models;

import java.time.LocalDate;

public class Revenu {
    private int idRevenu;
    private String description;
    private double montant;
    private LocalDate date;
    private int categorieId;
    private int userId;// Référence vers Categorie (relation 1:n)

    // Constructeur vide
    public Revenu() {}

    // Constructeur avec paramètres
    public Revenu(String description, double montant, LocalDate date, int categorieId, int userId) {
        this.description = description;
        this.montant = montant;
        this.date = date;
        this.userId=userId;
        this.categorieId = categorieId;
    }

    // Getters
    public int getIdRevenu() { return idRevenu; }
    public String getDescription() { return description; }
    public double getMontant() { return montant; }
    public LocalDate getDate() { return date; }
    public int getCategorieId() { return categorieId; }
    public int getUserId() {return userId;}

    // Setters
    public void setIdRevenu(int idRevenu) { this.idRevenu = idRevenu; }
    public void setDescription(String description) { this.description = description; }
    public void setMontant(double montant) { this.montant = montant; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }
    public void setUserId(int userId) {this.userId = userId;}

    // Méthodes du diagramme
    public void ajouterRevenu() {
        System.out.println("Revenu ajouté : " + description + " - " + montant + "€");
    }

    public void modifierRevenu(String description, int montant) {
        this.description = description;
        this.montant = montant;
        System.out.println("Revenu modifié");
    }

    public void supprimerRevenu() {
        System.out.println("Revenu supprimé : " + idRevenu);
    }

    @Override
    public String toString() {
        return "Revenu{id=" + idRevenu + ", description='" + description +
                "', montant=" + montant + "€, date=" + date + "}";
    }
}