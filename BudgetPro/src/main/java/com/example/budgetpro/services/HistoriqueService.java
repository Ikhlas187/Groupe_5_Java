/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.budgetpro.services;

import com.example.budgetpro.dao.DepenseDAO;
import com.example.budgetpro.models.Depense;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author asteras
 */
public class HistoriqueService {
    
    private DepenseDAO depenseDAO;
    
    public HistoriqueService() {
        this.depenseDAO = new DepenseDAO();
    }
    
    /*
    Récupérer toutes les dépenses
    */
    public List<Depense> getAllDepenses(){
        return depenseDAO.getAllDepenses();
    }
    
    /*
    Filtrer par catégorie
    */
    public List<Depense> filterByCategorie(int categorieId){
        return depenseDAO.getDepensesByCategorie(categorieId);
    }
    
    /*
    Filtrer par période 
    */
    public List<Depense> filterByPeriode(LocalDate debut, LocalDate fin){
        return depenseDAO.getDepensesByPeriode(debut, fin);
    }
    
    /*
    Exporter les dépenses en JSON
    */
    public boolean exportToJSON(List<Depense> depenses, String filepath) {
        try (FileWriter writer = new FileWriter(filepath)) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .create();
            
            gson.toJson(depenses, writer);
            System.out.println("Export JSON réussi : " + filepath);
            return true;
            
        } catch (IOException e) {
            System.err.println("Erreur lors de l'export JSON");
            e.printStackTrace();
            return false;
        }
    }
    
    /*
    Exporter les dépenses en CSV
    */
    public boolean exportToCSV(List<Depense> depenses, String filepath){
        try (FileWriter writer = new FileWriter(filepath)){
            
            // En-tête CSV 
            writer.append("ID, Titre, Catégorie, Prix, Date, Description\n");
            
            // Données
            for (Depense d : depenses){
                String categorie = depenseDAO.getCategorieNameByDepense(d.getIdDepense());
                
                writer.append(String.valueOf(d.getIdDepense())).append(",");
                writer.append(escapeCSV(d.getDescription())).append(",");
                writer.append(String.valueOf(d.getMontant())).append(",");
                writer.append(d.getDate().toString()).append(",");
                writer.append(escapeCSV(d.getDescription())).append("\n");
            }
            
            System.out.println("Export CSV réussi : " + filepath);
            return true;
        } catch (IOException e){
            System.err.println("Erreur lors de l'export CSV");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Échapper les caractères spéciaux pour CSV
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        // Si la valeur contient des virgules, guillemets ou retours à la ligne
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // Échapper les guillemets en les doublant
            value = value.replace("\"", "\"\"");
            // Entourer de guillemets
            return "\"" + value + "\"";
        }
        
        return value;
    }
    
    /**
     * Calculer le total des dépenses
     * @param depenses
     * @return 
     */
    public double calculerTotal(List<Depense> depenses) {
        return depenses.stream()
                .mapToDouble(Depense::getMontant)
                .sum();
    }
    
    /**
      Obtenir le nom de la catégorie pour une dépense
     */
    public String getCategorieNameForDepense(int idDepense) {
        return depenseDAO.getCategorieNameByDepense(idDepense);
    }
}

