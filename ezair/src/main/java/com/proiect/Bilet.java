package com.proiect;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Bilet {
    private final StringProperty zbor;
    private final StringProperty data;
    private final StringProperty stare;
    private String id;
    private String zborId;

    public Bilet(String zbor, String data, String stare, String id) {
        this.zbor = new SimpleStringProperty(zbor);
        this.data = new SimpleStringProperty(data);
        this.stare = new SimpleStringProperty(stare);
        this.id = id;
    }

    public String getZbor() {
        return zbor.get();
    }

    public StringProperty zborProperty() {
        return zbor;
    }

    public String getData() {
        return data.get();
    }

    public StringProperty dataProperty() {
        return data;
    }

    public String getStare() {
        return stare.get();
    }

    public StringProperty stareProperty() {
        return stare;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getZborId() {
        return zborId;
    }

    public void setZborId(String zborId) {
        this.zborId = zborId;
    }
}
