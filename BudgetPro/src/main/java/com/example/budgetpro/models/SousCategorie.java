package com.example.budgetpro.models;

public class SousCategorie {
    private int idSousCategorie;
    private String nomSousCategorie;
    private String description;
    private int categorieId; // Référence vers Categorie (relation 1:n)

    // Constructeur vide
    public SousCategorie() {}

    // Constructeur avec paramètres
    public SousCategorie(String nomSousCategorie, String description, int categorieId) {
        this.nomSousCategorie = nomSousCategorie;
        this.description = description;
        this.categorieId = categorieId;
    }

    // Getters
    public int getIdSousCategorie() { return idSousCategorie; }
    public String getNomSousCategorie() { return nomSousCategorie; }
    public String getDescription() { return description; }
    public int getCategorieId() { return categorieId; }

    // Setters
    public void setIdSousCategorie(int idSousCategorie) { this.idSousCategorie = idSousCategorie; }
    public void setNomSousCategorie(String nomSousCategorie) { this.nomSousCategorie = nomSousCategorie; }
    public void setDescription(String description) { this.description = description; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }

    // Méthodes du diagramme
    public void creerSousCategorie() {
        System.out.println("Sous-catégorie créée : " + nomSousCategorie);
    }

    public void modifierSousCategorie(String nouveauNom, String nouvelleDescription) {
        this.nomSousCategorie = nouveauNom;
        this.description = nouvelleDescription;
        System.out.println("Sous-catégorie modifiée");
    }

    public void supprimerSousCategorie() {
        System.out.println("Sous-catégorie supprimée : " + idSousCategorie);
    }

    @Override
    public String toString() {
        return "SousCategorie{id=" + idSousCategorie + ", nom='" + nomSousCategorie +
                "', description='" + description + "'}";
    }
}