/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.budgetpro.dao;

import com.example.budgetpro.services.Database;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author asteras
 */
public class StatistiqueDAO {
    
    /*
    Total des dépenses par catégorie 
    */
    public Map<String, Double> getDepensesParCategorie(){
        Map<String, Double> data = new LinkedHashMap<>();
        String query = "SELECT c.nomCategorie, SUM(d.montant) as total " +
                "FROM depense d " +
                "JOIN sous_categorie sc ON d.id_sous_categorie = sc.id_sous_categorie " +
                "JOIN categorie c ON sc.id_categorie = c.id_categorie " +
                "GROUP BY c.nomCategorie " +
                "ORDER BY total DESC";
        
        try {
            Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()){
                data.put(rs.getString("nomCategorie"), rs.getDouble("total"));
            }
            
            System.out.println("Dépense par catégorie récupérées");
        
        } catch (SQLException e) {
            System.err.println("Erreur dépenses par catégorie");
            e.printStackTrace();
        }
        
        return data;
                
    }
    
    /*
    Total des dépenses par jour de la semaine
    */
    public Map<String, Double> getDepensesParJour(){
        Map<String, Double> data = new LinkedHashMap<>();
        
        // Initialiser les jours dans l'ordre 
        data.put("Lun", 0.0);
        data.put("Mar", 0.0);
        data.put("Mer", 0.0);
        data.put("Jeu", 0.0);
        data.put("Ven", 0.0);
        data.put("Sam", 0.0);
        data.put("Dim", 0.0);
        
        String query = "SELECT DAYOFWEEK(date) as jour, SUM(montant) as total " +
                "FROM depense " +
                "GROUP BY DAYOFWEEK(date) " +
                "ORDER BY jour";
        
        
        try {
            Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()){
                int jour = rs.getInt("jour");
                double total = rs.getDouble("total");
                
                // MySQL : 1=Dimanche, 2=Lundi, ..., 7=Samedi
                switch (jour){
                    case 1 -> data.put("Dim", total);
                    case 2 -> data.put("Lun", total);
                    case 3 -> data.put("Mar", total);
                    case 4 -> data.put("Mer", total);
                    case 5 -> data.put("Jeu", total);
                    case 6 -> data.put("Ven", total);
                    case 7 -> data.put("Sam", total);
                }
            }
            
            System.out.println("Dépenses par jour récupérées");
            
        } catch (SQLException e){
            System.err.println("Erreur dépenses par jour");
            e.printStackTrace();
        }
        
        return data;
        
    }
    
    /*
    Tendance mensuelle des dépenses
    */
    public Map<String, Double> getDepensesParMois(){
        Map<String, Double> data = new LinkedHashMap<>();
        
        // Initialiser les mois
        String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun", 
            "Jul", "Aoû", "Sept", "Oct", "Nov", "Déc"};
        for (String m : mois){
            data.put(m, 0.0);
        }
        
        String query = "SELECT MONTH(date) as mois, SUM(montant) as total " +
                "FROM depense " +
                "WHERE YEAR(date) = YEAR(CURDATE()) "+
                "GROUP BY MONTH(date) " +
                "ORDER BY mois";
        
        try {
            Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()){
                int moisNum = rs.getInt("mois");
                double total = rs.getDouble("total");
                data.put(mois[moisNum - 1], total);
            }
            
            System.out.println("Dépenses par mois récupérées");
            
        } catch (SQLException e){
            System.err.println("Erreur dépenses par mois");
            e.printStackTrace();
        }
        
        return data;
    }
    
    /*
    Tendance mensuelle des revenus
    */
    public Map<String, Double> getRevenusParMois() {
        Map<String, Double> data = new LinkedHashMap<>();
        
        // Initialiser les mois
        String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun", 
                         "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        for (String m : mois) {
            data.put(m, 0.0);
        }
        
        String query = "SELECT MONTH(date) as mois, SUM(montant) as total " +
                      "FROM revenu " +
                      "WHERE YEAR(date) = YEAR(CURDATE()) " +
                      "GROUP BY MONTH(date) " +
                      "ORDER BY mois";
        
        try {
            Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int moisNum = rs.getInt("mois");
                double total = rs.getDouble("total");
                data.put(mois[moisNum - 1], total);
            }
            
            System.out.println("Revenus par mois récupérés");
            
        } catch (SQLException e) {
            System.err.println("Erreur revenus par mois");
            e.printStackTrace();
        }
        
        return data;
    }
}
