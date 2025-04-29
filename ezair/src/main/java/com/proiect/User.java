package com.proiect;

public class User {
    private String nume;
    private String prenume;
    private String dataNasterii;
    private String email;
    private String username;
    private String parola;
    private boolean admin;

    public User(String nume, String prenume, String dataNasterii, String email, String username, String parola) {
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.email = email;
        this.username = username;
        this.parola = parola;
    }

    public User(String nume, String prenume, String dataNasterii, String email, String username, String parola, boolean admin) {
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.email = email;
        this.username = username;
        this.parola = parola;
        this.admin = admin;
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
}