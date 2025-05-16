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

    private Zbor(Builder builder) {
        this.origine = builder.origine;
        this.destinatie = builder.destinatie;
        this.dataPlecare = builder.dataPlecare;
        this.oraPlecare = builder.oraPlecare;
        this.dataSosire = builder.dataSosire;
        this.oraSosire = builder.oraSosire;
        this.modelAvion = builder.modelAvion;
        this.locuriLibere = builder.locuriLibere;
        this.pret = builder.pret;
        this.id = builder.id;
    }

    public static class Builder {
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

        public Builder setOrigine(String origine) {
            this.origine = origine;
            return this;
        }

        public Builder setDestinatie(String destinatie) {
            this.destinatie = destinatie;
            return this;
        }

        public Builder setDataPlecare(String dataPlecare) {
            this.dataPlecare = dataPlecare;
            return this;
        }

        public Builder setOraPlecare(String oraPlecare) {
            this.oraPlecare = oraPlecare;
            return this;
        }

        public Builder setDataSosire(String dataSosire) {
            this.dataSosire = dataSosire;
            return this;
        }

        public Builder setOraSosire(String oraSosire) {
            this.oraSosire = oraSosire;
            return this;
        }

        public Builder setModelAvion(String modelAvion) {
            this.modelAvion = modelAvion;
            return this;
        }

        public Builder setLocuriLibere(int locuriLibere) {
            this.locuriLibere = locuriLibere;
            return this;
        }

        public Builder setPret(double pret) {
            this.pret = pret;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Zbor build() {
            return new Zbor(this);
        }
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
