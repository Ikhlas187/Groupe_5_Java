/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.budgetpro.dao;

import com.example.budgetpro.models.Depense;
import com.example.budgetpro.services.Database;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DepenseDAO {
    
    /**
     * Récupérer toutes les dépenses
     * @return 
     */
    public List<Depense> getAllDepenses() {
        List<Depense> depenses = new ArrayList<>();
        String query = "SELECT id_depense, montant, description, date, " +
                      "id_sous_categorie, id_utilisateur " +
                      "FROM depense ORDER BY date DESC";
        
        try {
            Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Depense depense = new Depense();
                depense.setIdDepense(rs.getInt("id_depense"));
                depense.setMontant(rs.getInt("montant"));
                depense.setDescription(rs.getString("description"));
                depense.setDate(rs.getDate("date").toLocalDate());
                depense.setSousCategorieId(rs.getInt("id_sous_categorie"));
                depense.setUserId(rs.getInt("id_utilisateur"));
                
                depenses.add(depense);
            }
            
            System.out.println(depenses.size() + " dépenses récupérées");
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des dépenses");
            e.printStackTrace();
        }
        
        return depenses;
    }
    
    /**
     * Récupérer les dépenses d'un utilisateur spécifique
     * @return 
     */
    public List<Depense> getDepensesByUserId(int userId) {
        List<Depense> depenses = new ArrayList<>();
        String query = "SELECT id_depense, montant, description, date, " +
                      "id_sous_categorie, id_utilisateur " +
                      "FROM depense WHERE id_utilisateur = ? ORDER BY date DESC";
        
        try {
            Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Depense depense = new Depense();
                depense.setIdDepense(rs.getInt("id_depense"));
                depense.setMontant(rs.getInt("montant"));
                depense.setDescription(rs.getString("description"));
                depense.setDate(rs.getDate("date").toLocalDate());
                depense.setSousCategorieId(rs.getInt("id_sous_categorie"));
                depense.setUserId(rs.getInt("id_utilisateur"));
                
                depenses.add(depense);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des dépenses par utilisateur");
            e.printStackTrace();
        }
        
        return depenses;
    }
    
    /**
     * Filtrer les dépenses par catégorie (via sous_categorie)
     * @return 
     */
    public List<Depense> getDepensesByCategorie(int categorieId) {
        List<Depense> depenses = new ArrayList<>();
        String query = "SELECT d.id_depense, d.montant, d.description, d.date, " +
                      "d.id_sous_categorie, d.id_utilisateur " +
                      "FROM depense d " +
                      "JOIN sous_categorie sc ON d.id_sous_categorie = sc.id_sous_categorie " +
                      "WHERE sc.id_categorie = ? " +
                      "ORDER BY d.date DESC";
        
        try {
            Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            
            pstmt.setInt(1, categorieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Depense depense = new Depense();
                depense.setIdDepense(rs.getInt("id_depense"));
                depense.setMontant(rs.getInt("montant"));
                depense.setDescription(rs.getString("description"));
                depense.setDate(rs.getDate("date").toLocalDate());
                depense.setSousCategorieId(rs.getInt("id_sous_categorie"));
                depense.setUserId(rs.getInt("id_utilisateur"));
                
                depenses.add(depense);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du filtrage par catégorie");
            e.printStackTrace();
        }
        
        return depenses;
    }
    
    /**
     * Filtrer les dépenses par période
     * @return 
     */
    public List<Depense> getDepensesByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        List<Depense> depenses = new ArrayList<>();
        String query = "SELECT id_depense, montant, description, date, " +
                      "id_sous_categorie, id_utilisateur " +
                      "FROM depense " +
                      "WHERE date BETWEEN ? AND ? " +
                      "ORDER BY date DESC";
        
        try {
            Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            
            pstmt.setDate(1, Date.valueOf(dateDebut));
            pstmt.setDate(2, Date.valueOf(dateFin));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Depense depense = new Depense();
                depense.setIdDepense(rs.getInt("id_depense"));
                depense.setMontant(rs.getInt("montant"));
                depense.setDescription(rs.getString("description"));
                depense.setDate(rs.getDate("date").toLocalDate());
                depense.setSousCategorieId(rs.getInt("id_sous_categorie"));
                depense.setUserId(rs.getInt("id_utilisateur"));
                
                depenses.add(depense);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du filtrage par période");
            e.printStackTrace();
        }
        
        return depenses;
    }
    
    /**
     * Récupérer le nom de la catégorie d'une dépense
     * @return 
     */
    public String getCategorieNameByDepense(int idDepense) {
        String query = "SELECT c.nomCategorie " +
                      "FROM depense d " +
                      "JOIN sous_categorie sc ON d.id_sous_categorie = sc.id_sous_categorie " +
                      "JOIN categorie c ON sc.id_categorie = c.id_categorie " +
                      "WHERE d.id_depense = ?";
        
        try {
            Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, idDepense);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("nomCategorie");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom de catégorie");
            e.printStackTrace();
        }
        
        return "Inconnu";
    }
}
