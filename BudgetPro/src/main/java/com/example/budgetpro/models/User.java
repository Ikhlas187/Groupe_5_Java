package com.example.budgetpro.models;

public class User {
    private int id ;
    private String nom ;
    private String prenom ;
    private String email ;
    private String password;
    private String telephone;

    public User() {}

    public User(String nom,String prenom, String email, String password,String telephone) {
        this.nom = nom;
        this.prenom=prenom;
        this.email = email;
        this.password = password;
        this.telephone=telephone;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.id = id; }


    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = User.this.telephone; }

}
