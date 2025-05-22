package com.proiect;

import java.time.LocalDateTime;

public class Bilet {
    private String id;
    private String userId;
    private String zborId;
    private String detaliiZbor;
    private LocalDateTime dataZbor;
    private String stare; // "ACTIV", "ANULAT", "MODIFICAT"
    private double pret;

    public Bilet(String id, String userId, String zborId, String detaliiZbor, 
                 LocalDateTime dataZbor, String stare, double pret) {
        this.id = id;
        this.userId = userId;
        this.zborId = zborId;
        this.detaliiZbor = detaliiZbor;
        this.dataZbor = dataZbor;
        this.stare = stare;
        this.pret = pret;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getZborId() { return zborId; }
    public String getDetaliiZbor() { return detaliiZbor; }
    public LocalDateTime getDataZbor() { return dataZbor; }
    public String getStare() { return stare; }
    public double getPret() { return pret; }

    // Setters
    public void setStare(String stare) { this.stare = stare; }
    public void setDataZbor(LocalDateTime dataZbor) { this.dataZbor = dataZbor; }
}
