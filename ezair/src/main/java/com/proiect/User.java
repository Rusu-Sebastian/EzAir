package com.proiect;

public class User {
    private String nume;
    private String prenume;
    private String dataNasterii;
    private String email;
    private String numeUtilizator;
    private String parola;
    private boolean esteAdmin;
    private String id;
    private String telefon;

    public User(String nume, String prenume, String dataNasterii, String email, String numeUtilizator, String parola, String id, String telefon) {
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.email = email;
        this.numeUtilizator = numeUtilizator;
        this.parola = parola;
        this.id = id;
        this.telefon = telefon;
        this.esteAdmin = false;
    }

    public User(String nume, String prenume, String dataNasterii, String email, String numeUtilizator, String parola, boolean esteAdmin, String id, String telefon) {
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.email = email;
        this.numeUtilizator = numeUtilizator;
        this.parola = parola;
        this.esteAdmin = esteAdmin;
        this.id = id;
        this.telefon = telefon;
    }

    // Getteri
    public String getNume() {
        return nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public String getDataNasterii() {
        return dataNasterii;
    }

    public String getEmail() {
        return email;
    }

    public String getNumeUtilizator() {
        return numeUtilizator;
    }

    public String getParola() {
        return parola;
    }

    public boolean esteAdmin() {
        return esteAdmin;
    }

    public String getId() {
        return id;
    }

    public String getTelefon() {
        return telefon;
    }
}