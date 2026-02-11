package com.example.budgetpro.models;

public class Categorie {
    private int idCategorie;
    private String nomCategorie;

    // Constructeur vide
    public Categorie() {}

    // Constructeur avec paramètres
    public Categorie(String nomCategorie) {
        this.nomCategorie = nomCategorie;
    }

    // Getters
    public int getIdCategorie() { return idCategorie; }
    public String getNomCategorie() { return nomCategorie; }

    // Setters
    public void setIdCategorie(int idCategorie) { this.idCategorie = idCategorie; }
    public void setNomCategorie(String nomCategorie) { this.nomCategorie = nomCategorie; }

    // Méthodes du diagramme
    public void creerCategorie() {
        System.out.println("Catégorie créée : " + nomCategorie);
    }

    public void modifierCategorie(String nouveauNom) {
        this.nomCategorie = nouveauNom;
        System.out.println("Catégorie modifiée : " + nomCategorie);
    }

    public void supprimerCategorie() {
        System.out.println("Catégorie supprimée : " + idCategorie);
    }

    @Override
    public String toString() {
        return "Categorie{id=" + idCategorie + ", nom='" + nomCategorie + "'}";
    }
}