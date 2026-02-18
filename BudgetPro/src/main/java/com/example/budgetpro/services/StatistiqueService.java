/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.budgetpro.services;

import com.example.budgetpro.dao.StatistiqueDAO;
import java.util.Map;

/**
 *
 * @author asteras
 */
public class StatistiqueService {
    
    private StatistiqueDAO statistiqueDAO;
    
    public StatistiqueService() {
        this.statistiqueDAO = new StatistiqueDAO();
    }
    
    /*
    Données pour le Pie Chart
    */
    public Map<String, Double> getDepensesParCategorie(){
        return statistiqueDAO.getDepensesParCategorie(AuthServices.getCurrentUser().getId());
    }
    
    /*
    Données revenus pour le Bar Chart
    */
    public Map<String, Double> getDepensesParJour(){
        return statistiqueDAO.getDepensesParJour(AuthServices.getCurrentUser().getId());
    }
    
    /*
    Données dépenses pour le line chart
    */
    public Map<String, Double> getDepensesParMois(){
        return statistiqueDAO.getDepensesParMois(AuthServices.getCurrentUser().getId());
    }
    
    /*
    Données revenus pour le line chart
    */
    public Map<String, Double> getRevenusParMois(){
        return statistiqueDAO.getRevenusParMois(AuthServices.getCurrentUser().getId());
    }
}
