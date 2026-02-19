package com.example.budgetpro.pages;

import java.util.HashMap;
import java.util.Map;

public class CategorieIcon {

    private static final Map<String, String> COULEURS = new HashMap<>();
    private static final Map<String, String> ICONES = new HashMap<>();

    static {
        // 🎨 COULEURS PAR CATÉGORIE
        COULEURS.put("Alimentation", "#FF6B6B");
        COULEURS.put("Logement", "#4ECDC4");
        COULEURS.put("Santé", "#95E1D3");
        COULEURS.put("Style de vie", "#F38181");
        COULEURS.put("Economies", "#FFA07A");
        COULEURS.put("Transport", "#6C5CE7");
        COULEURS.put("Divers", "#A8E6CF");
        COULEURS.put("Amusements", "#FFD93D");

        // 🎯 ICÔNES - Alimentation
        ICONES.put("Boissons", "🥤");
        ICONES.put("Courses", "🛒");
        ICONES.put("Nourriture", "🍽️");
        ICONES.put("Restaurant", "🍴");

        // Logement
        ICONES.put("Eau", "💧");
        ICONES.put("Electricité", "⚡");
        ICONES.put("Internet", "🌐");
        ICONES.put("Loyer", "🏠");
        ICONES.put("TV", "📺");
        ICONES.put("Téléphone", "📱");
        ICONES.put("Entretien", "🔧");
        ICONES.put("Assurance", "🛡️");

        // Santé
        ICONES.put("Frais d'hopitaux", "🏥");
        ICONES.put("Médicaments", "💊");

        // Style de vie
        ICONES.put("Animal de compagnie", "🐾");
        ICONES.put("Cadeau", "🎁");
        ICONES.put("Hotel", "🏨");
        ICONES.put("Voyages", "✈️");
        ICONES.put("Travail", "💼");
        ICONES.put("Vetements", "👔");

        // Economies
        ICONES.put("Fonds d'urgence", "🆘");
        ICONES.put("Epargne", "💰");

        // Transport
        ICONES.put("Assurance voiture", "🚗");
        ICONES.put("Essence", "⛽");
        ICONES.put("Réparation", "🔩");
        ICONES.put("Taxi", "🚕");
        ICONES.put("Transports publics", "🚌");

        // Divers
        ICONES.put("Divers", "📦");
        ICONES.put("Frais bancaires", "🏦");
        ICONES.put("Inconnu", "❓");
        ICONES.put("Prêt étudiant", "🎓");

        // Amusements
        ICONES.put("Abonnements", "📋");
        ICONES.put("Boite de nuit", "🎉");
        ICONES.put("Cinéma", "🎬");
        ICONES.put("Concert", "🎵");
        ICONES.put("Passion", "❤️");
        ICONES.put("Salle de sports", "🏋️");
        ICONES.put("Sports", "⚽");
        ICONES.put("Vacances", "🏖️");
        ICONES.put("Electronique", "💻");
    }

    public static String getIcone(String nomSousCategorie) {
        return ICONES.getOrDefault(nomSousCategorie, "📌");
    }

    public static String getCouleur(String nomCategorie) {
        return COULEURS.getOrDefault(nomCategorie, "#95A5A6");
    }

    public static void ajouterCouleur(String nomCategorie, String couleur) {
        COULEURS.put(nomCategorie, couleur);
    }

    public static void ajouterIcone(String nomSousCategorie, String icone) {
        ICONES.put(nomSousCategorie, icone);
    }
}