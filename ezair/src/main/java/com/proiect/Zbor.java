package com.proiect;

public class Zbor {
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

    // Helper method to normalize date formats - handles both DD/MM/YYYY and DD.MM.YYYY
    public static String normalizeazaFormatData(String data) {
        if (data == null) return null;
        // Replace dots with slashes for consistent format
        return data.replace(".", "/");
    }

    /**
     * Converts date strings to a standard ISO format for easier parsing
     * @param dateStr The date string to normalize (e.g., "DD/MM/YYYY", "DD.MM.YYYY")
     * @return ISO formatted date string (YYYY-MM-DD) or original string if conversion fails
     */
    public static String convertesteInFormatISO(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return dataStr;
        }
        
        try {
            // First normalize separators
            String formatNormalizat = dataStr.replace('.', '/');
            
            String[] parti = formatNormalizat.split("/");
            if (parti.length != 3) {
                return dataStr; // Return original if not in expected format
            }
            
            int zi = Integer.parseInt(parti[0]);
            int luna = Integer.parseInt(parti[1]);
            int an = Integer.parseInt(parti[2]);
            
            // Return in ISO format YYYY-MM-DD
            return String.format("%04d-%02d-%02d", an, luna, zi);
        } catch (Exception e) {
            // If parsing fails, return the original string
            return dataStr;
        }
    }

    private Zbor(Constructor constructor) {
        this.origine = constructor.origine;
        this.destinatie = constructor.destinatie;
        this.dataPlecare = constructor.dataPlecare;
        this.oraPlecare = constructor.oraPlecare;
        this.dataSosire = constructor.dataSosire;
        this.oraSosire = constructor.oraSosire;
        this.modelAvion = constructor.modelAvion;
        this.locuriLibere = constructor.locuriLibere;
        this.pret = constructor.pret;
        this.id = constructor.id;
    }

    public static class Constructor {
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

        public Constructor setOrigine(String origine) {
            this.origine = origine;
            return this;
        }

        public Constructor setDestinatie(String destinatie) {
            this.destinatie = destinatie;
            return this;
        }

        public Constructor setDataPlecare(String dataPlecare) {
            this.dataPlecare = normalizeazaFormatData(dataPlecare);
            return this;
        }

        public Constructor setOraPlecare(String oraPlecare) {
            this.oraPlecare = oraPlecare;
            return this;
        }

        public Constructor setDataSosire(String dataSosire) {
            this.dataSosire = normalizeazaFormatData(dataSosire);
            return this;
        }

        public Constructor setOraSosire(String oraSosire) {
            this.oraSosire = oraSosire;
            return this;
        }

        public Constructor setModelAvion(String modelAvion) {
            this.modelAvion = modelAvion;
            return this;
        }

        public Constructor setLocuriLibere(int locuriLibere) {
            this.locuriLibere = locuriLibere;
            return this;
        }

        public Constructor setPret(double pret) {
            this.pret = pret;
            return this;
        }

        public Constructor setId(String id) {
            this.id = id;
            return this;
        }

        public Zbor construieste() {
            return new Zbor(this);
        }
    }

    // Getteri
    public String getOrigine() { return origine; }
    public String getDestinatie() { return destinatie; }
    public String getDataPlecare() { return dataPlecare; }
    public String getOraPlecare() { return oraPlecare; }
    public String getDataSosire() { return dataSosire; }
    public String getOraSosire() { return oraSosire; }
    public String getModelAvion() { return modelAvion; }
    public int getLocuriLibere() { return locuriLibere; }
    public double getPret() { return pret; }
    public String getId() { return id; }
}
