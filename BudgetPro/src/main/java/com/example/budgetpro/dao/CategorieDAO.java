/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.budgetpro.dao;

import com.example.budgetpro.services.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author asteras
 */
public class CategorieDAO {
    
    /*
    Récupérer tous les noms de catégorie
    */
    public List<String> getAllCategorieName() {
        List<String> categories = new ArrayList<>();
        categories.add("All"); 
        
        String query = "SELECT nomCategorie FROM categorie ORDER BY nomCategorie";
        
        try {
            Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()){
                categories.add(rs.getString("nomCategorie"));
            }
            
            System.out.println((categories.size()- 1) + "catégories chargées");
            
        } catch (SQLException e){
            System.err.println("Erreur lors de la récupération des catégories");
            e.printStackTrace();
        }
        
        return categories;
    }
    
    /*
    Récupérer l'ID d'une categorie par son nom
    */
    public int getCategorieIdByName(String nomCategorie){
        String query = "SELECT id_categorie FROM categorie WHERE nomCategorie = ?";
        
        try {
            Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, nomCategorie);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()){
                return rs.getInt("id_categorie");
            }
        } catch (SQLException e){
                    System.err.println("Erreur lors de la récupération de l'ID de la catégorie");
                    e.printStackTrace();
        }
        
        return -1; // non trouvé
    }
}
