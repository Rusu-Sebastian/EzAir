package com.proiect;

import java.time.LocalDateTime;

public class Bilet {
    private String id;
    private String idUtilizator;
    private String idZbor;
    private String detaliiZbor;
    private LocalDateTime dataZbor;
    private String stare; // "ACTIV", "ANULAT", "MODIFICAT"
    private double pret;

    public Bilet(String id, String idUtilizator, String idZbor, String detaliiZbor, 
                 LocalDateTime dataZbor, String stare, double pret) {
        this.id = id;
        this.idUtilizator = idUtilizator;
        this.idZbor = idZbor;
        this.detaliiZbor = detaliiZbor;
        this.dataZbor = dataZbor;
        this.stare = stare;
        this.pret = pret;
    }

    // Getteri
    public String getId() { return id; }
    public String getIdUtilizator() { return idUtilizator; }
    public String getIdZbor() { return idZbor; }
    public String getDetaliiZbor() { return detaliiZbor; }
    public LocalDateTime getDataZbor() { return dataZbor; }
    public String getStare() { return stare; }
    public double getPret() { return pret; }

    // Setteri
    public void setStare(String stare) { this.stare = stare; }
    public void setDataZbor(LocalDateTime dataZbor) { this.dataZbor = dataZbor; }
}
