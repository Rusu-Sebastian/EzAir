package com.proiect;

public class User {
    private String nume;
    private String prenume;
    private String dataNasterii;
    private String email;
    private String username;
    private String parola;
    private boolean admin;
    private String id;
    private String telefon;

    public User(String nume, String prenume, String dataNasterii, String email, String username, String parola, String id, String telefon) {
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.email = email;
        this.username = username;
        this.parola = parola;
        this.id = id;
        this.telefon = telefon;
    }

    public User(String nume, String prenume, String dataNasterii, String email, String username, String parola, boolean admin, String id, String telefon) {
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.email = email;
        this.username = username;
        this.parola = parola;
        this.admin = admin;
        this.id = id;
        this.telefon = telefon;
    }

    // Getters
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

    public String getUsername() {
        return username;
    }

    public String getParola() {
        return parola;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getId() {
        return id;
    }
    public String getTelefon(){
        return telefon;
    }
}