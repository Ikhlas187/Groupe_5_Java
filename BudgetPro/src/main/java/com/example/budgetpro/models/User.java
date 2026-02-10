package com.example.budgetpro.models;
import java.time.LocalDateTime;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String telephone;
    private String sexe;           // ← NOUVEAU
    private int age;               // ← NOUVEAU
    private double soldeInitial;   // ← NOUVEAU
    private LocalDateTime createdAt; // ← NOUVEAU (date de création)

    // Constructeur vide
    public User() {}

    // Constructeur avec paramètres (SANS createdAt car auto-généré)
    public User(String nom, String prenom, String email, String password, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
    }

    // ========== GETTERS ==========

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getSexe() {
        return sexe;
    }

    public int getAge() {
        return age;
    }

    public double getSoldeInitial() {
        return soldeInitial;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ========== SETTERS ==========

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setSoldeInitial(double soldeInitial) {
        this.soldeInitial = soldeInitial;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", sexe='" + sexe + '\'' +
                ", age=" + age +
                ", soldeInitial=" + soldeInitial +
                ", createdAt=" + createdAt +
                '}';
    }

    public String getFullName() {
        return prenom + " " + nom;
    }
}