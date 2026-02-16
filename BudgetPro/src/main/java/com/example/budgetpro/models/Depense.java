package com.example.budgetpro.models;

import java.time.LocalDate;

public class Depense {
    private int idDepense;
    private double montant;
    private String description;
    private LocalDate date;
    private int sousCategorieId;
    private int userId; // Référence vers SousCategorie (relation 0:n)

    // Constructeur vide
    public Depense() {}

    // Constructeur avec paramètres
    public Depense(int montant, String description, LocalDate date, int sousCategorieId, int userId) {
        this.montant = montant;
        this.description = description;
        this.date = date;
        this.userId=userId;
        this.sousCategorieId = sousCategorieId;
    }

    // Getters
    public int getIdDepense() { return idDepense; }
    public double  getMontant() { return montant; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public int getSousCategorieId() { return sousCategorieId; }
    public int getUserId() {return userId;}

    // Setters
    public void setIdDepense(int idDepense) { this.idDepense = idDepense; }
    public void setMontant(int montant) { this.montant = montant; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setUserId(int userId) {this.userId = userId;}
    public void setSousCategorieId(int sousCategorieId) { this.sousCategorieId = sousCategorieId; }

    // Méthodes du diagramme
    public void ajouterDepense() {
        System.out.println("Dépense ajoutée : " + description + " - " + montant + "€");
    }

    public void supprimerDepense() {
        System.out.println("Dépense supprimée : " + idDepense);
    }

    public void modifierDepense(int montant, String description) {
        this.montant = montant;
        this.description = description;
        System.out.println("Dépense modifiée");
    }

    @Override
    public String toString() {
        return "Depense{id=" + idDepense + ", description='" + description +
                "', montant=" + montant + "€, date=" + date + "}";
    }
}