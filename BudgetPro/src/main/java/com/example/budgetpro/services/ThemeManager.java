/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.budgetpro.services;

import javafx.scene.Scene;
import java.util.prefs.Preferences;

/**
 *
 * @author asteras
 */
public class ThemeManager {
    
    private static final String THEME_PREF_KEY = "theme";
    private static final String LIGHT_THEME = "/com/example/budgetpro/styles/light.css";
    private static final String DARK_THEME = "/com/example/budgetpro/styles/dark.css";
    
    private static Scene currentScene;
    private static boolean isDarkMode = false;
    
    /**
     * Initialiser le thème pour une scène
     * @param scene
     */
    public static void initTheme(Scene scene) {
        currentScene = scene;
        
        // Charger la préférence sauvegardée
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        isDarkMode = prefs.getBoolean(THEME_PREF_KEY, false);
        
        applyTheme();
    }
    
    /**
     * Basculer entre mode clair et sombre
     */
    public static void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
        savePreference();
    }
    
    /**
     * Activer le mode sombre
     * @param dark
     */
    public static void setDarkMode(boolean dark) {
        isDarkMode = dark;
        applyTheme();
        savePreference();
    }
    
    /**
     * Appliquer le thème à la scène
     */
    private static void applyTheme() {
        if (currentScene == null) {
            System.err.println("Scene non initialisée");
            return;
        }
        
        currentScene.getStylesheets().clear();
        
        String themeFile = isDarkMode ? DARK_THEME : LIGHT_THEME;
        String themeUrl = ThemeManager.class.getResource(themeFile).toExternalForm();
        
        currentScene.getStylesheets().add(themeUrl);
        
        System.out.println(isDarkMode ? "🌙 Mode sombre activé" : "☀️ Mode clair activé");
    }
    
    /**
     * Sauvegarder la préférence
     */
    private static void savePreference() {
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        prefs.putBoolean(THEME_PREF_KEY, isDarkMode);
    }
    
    /**
     * Obtenir l'état actuel
     * @return 
     */
    public static boolean isDarkMode() {
        return isDarkMode;
    }
}