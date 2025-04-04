package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.zone.ZoneOffsetTransitionRule;

import javafx.fxml.FXML;

class User {
    String nume;
    String prenume;
    String dataNasterii;
    String email;
    String username;
    String parola;
    public User(String nume, String prenume,String dataNasterii, String email, String username, String parola) {
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.email = email;
        this.username = username;
        this.parola = parola;
    }
}

public class CreareContController {

    // butonu de inapoi/cancel care duce inapoi la pagina de pornire adica la login
    @FXML
    private void backCreareContInceput() throws IOException {
        App.setRoot("login");
    }


    @FXML
    private void backCreareContFinal() throws IOException {
        App.setRoot("creareContInceput");
    }

    @FXML
    private void cancelCreareCont() throws IOException {
        App.setRoot("login");
    }



    // butonu de next care salveaza datele despre nume si email introduse si merge la pagina urmatoare care este cea ce preia data nasterii
     @FXML
    private void creareContInceput() throws IOException {
        String nume = ((javafx.scene.control.TextField) App.scene.lookup("#nume")).getText();
        String prenume = ((javafx.scene.control.TextField) App.scene.lookup("#prenume")).getText();
        if (nume.isEmpty() || prenume.isEmpty() || 
            ((javafx.scene.control.TextField) App.scene.lookup("#ziuaNasterii")).getText().isEmpty() || 
            ((javafx.scene.control.TextField) App.scene.lookup("#lunaNasterii")).getText().isEmpty() || 
            ((javafx.scene.control.TextField) App.scene.lookup("#anNasterii")).getText().isEmpty()) {
            System.out.println("Toate campurile sunt obligatorii");
            return;
        } else if (((javafx.scene.control.TextField) App.scene.lookup("#ziuaNasterii")).getText().length() > 2 || 
                ((javafx.scene.control.TextField) App.scene.lookup("#lunaNasterii")).getText().length() > 2 || 
                ((javafx.scene.control.TextField) App.scene.lookup("#anNasterii")).getText().length() > 4) {
                System.out.println("Data nasterii este invalida");
            return;
        }
        int ziNastere = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#ziuaNasterii")).getText());
        int lunaNastere = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#lunaNasterii")).getText());
        int anNastere = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#anNasterii")).getText());
        int currentYear = java.time.Year.now().getValue();
        if (ziNastere > 31 || lunaNastere > 12 || anNastere < 1900 || anNastere > currentYear) {
            System.out.println("Data nasterii este invalida");
            return;
        }
        String dataNasterii = ((javafx.scene.control.TextField) App.scene.lookup("#ziuaNasterii")).getText() + "/" +
                ((javafx.scene.control.TextField) App.scene.lookup("#lunaNasterii")).getText() + "/" +
                ((javafx.scene.control.TextField) App.scene.lookup("#anNasterii")).getText();
        creareUserPartial(nume, prenume, dataNasterii);
        App.setRoot("creareContFinal");
        App.getUserData().put("nume", nume);
        App.getUserData().put("prenume", prenume);
        App.getUserData().put("dataNasterii", dataNasterii);
    }
    private User creareUserPartial(String nume, String prenume, String dataNasterii) {
        User utilizatorPartial = new User(nume, prenume, dataNasterii, null, null, null);
        return utilizatorPartial;
    }

    
    @FXML
    private void creareContFinal() throws IOException {
        String username = ((javafx.scene.control.TextField) App.scene.lookup("#username")).getText();
        String parola = ((javafx.scene.control.TextField) App.scene.lookup("#parola")).getText();
        String confirmareParola = ((javafx.scene.control.TextField) App.scene.lookup("#confirmareParola")).getText();
        String email = ((javafx.scene.control.TextField) App.scene.lookup("#email")).getText();
        if(username.isEmpty() || parola.isEmpty() || confirmareParola.isEmpty() || email.isEmpty()) {
            System.out.println("Toate campurile sunt obligatorii");
            return;
        } else if (parola.length() < 8) {
            System.out.println("Parola trebuie sa aiba minim 8 caractere");
            return;
        } else if (!parola.equals(confirmareParola)) {
            System.out.println("Parolele nu se potrivesc");
            return;
        } else if (!email.contains("@")) {
            System.out.println("Emailul este invalid");
            return;
        }
        User utilizatorNou = new User(
            (String) App.getUserData().get("nume"),
            (String) App.getUserData().get("prenume"),
            (String) App.getUserData().get("dataNasterii"),
            email,
            username,
            parola

        );
        String url = "http://localhost:3000/users/creareCont";
        HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
        httpClient.setRequestMethod("POST");
        httpClient.setRequestProperty("Content-Type", "application/json");
        httpClient.setDoOutput(true);
        String jsonInputString = String.format(
            "{\"nume\": \"%s\", \"prenume\": \"%s\", \"dataNasterii\": \"%s\", \"email\": \"%s\", \"username\": \"%s\", \"parola\": \"%s\"}",
            utilizatorNou.nume, utilizatorNou.prenume, utilizatorNou.dataNasterii, utilizatorNou.email, utilizatorNou.username, utilizatorNou.parola
        );
        try (java.io.OutputStream os = httpClient.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        int responseCode = httpClient.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Cont creat cu succes");
            App.setRoot("login");
        } else {
            System.out.println("Eroare la crearea contului");
        }
    }
}
