package com.example.budgetpro.models;

/**
 * Représente une sous-catégorie liée à une catégorie
 */
public class SousCategorie {
    private int idSousCategorie;
    private String nomSousCategorie;
    private int categorieId;

    public SousCategorie() {}

    public SousCategorie(String nomSousCategorie, int categorieId) {
        this.nomSousCategorie = nomSousCategorie;
        this.categorieId = categorieId;
    }

    // Getters
    public int getIdSousCategorie() { return idSousCategorie; }
    public String getNomSousCategorie() { return nomSousCategorie; }
    public int getCategorieId() { return categorieId; }

    // Setters
    public void setIdSousCategorie(int idSousCategorie) { this.idSousCategorie = idSousCategorie; }
    public void setNomSousCategorie(String nomSousCategorie) { this.nomSousCategorie = nomSousCategorie; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }
}