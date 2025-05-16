package com.proiect;

public class Zbor {
    // constructor(origine, destinatie, dataPlecare, oraPlecare, dataSosire, oraSosire, modelAvion, locuriLibere, pret)
    private String origine;
    private String destinatie;
    private String dataPlecare;
    private String oraPlecare;
    private String dataSosire;
    private String oraSosire;
    private String modelAvion;
    private int locuriLibere;
    private double pret;
    private String id;

    public Zbor(String origine, String destinatie, String dataPlecare, String oraPlecare, String dataSosire, String oraSosire, String modelAvion, int locuriLibere, double pret, String idZbor) {
    this.origine = origine;
    this.destinatie = destinatie;
    this.dataPlecare = dataPlecare;
    this.oraPlecare = oraPlecare;
    this.dataSosire = dataSosire;
    this.oraSosire = oraSosire;
    this.modelAvion = modelAvion;
    this.locuriLibere = locuriLibere;
    this.pret = pret;
}

    // Getters
    public String getOrigine() {
        return origine;
    }
    public String getDestinatie() {
        return destinatie;
    }
    public String getDataPlecare() {
        return dataPlecare;
    }
    public String getOraPlecare() {
        return oraPlecare;
    }
    public String getDataSosire() {
        return dataSosire;
    }
    public String getOraSosire() {
        return oraSosire;
    }
    public String getModelAvion() {
        return modelAvion;
    }
    public int getLocuriLibere() {
        return locuriLibere;
    }
    public double getPret() {
        return pret;
    }
    public String getId() {
        return id;
    }

}
