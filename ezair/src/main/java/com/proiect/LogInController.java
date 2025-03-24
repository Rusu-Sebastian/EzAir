package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javafx.fxml.FXML;

public class LogInController {

    //functia de logare
    @FXML
    private void logIn() throws IOException {
        //preluarea datelor introduse de utilizator
        String username = ((javafx.scene.control.TextField) App.scene.lookup("#username")).getText();
        String password = ((javafx.scene.control.PasswordField) App.scene.lookup("#parola")).getText();

        //verificarea daca toate campurile sunt completate
        if(username.equals("") || password.equals("")) {
            System.out.println("Completati toate campurile!");
            return;
        } else {
            //conectarea la server si trimiterea datelor de logare in format json
            String url = "http://localhost:3000/login?username=" + username + "&password=" + password;
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            httpClient.setRequestMethod("GET");
            httpClient.setRequestProperty("Content-Type", "application/json");

            int responseCode = httpClient.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                App.setRoot("paginaPrincipala");
            } else {
                System.out.println("Datele introduse sunt gresite!");
                System.out.println("Response code: " + responseCode);
            }
        }
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
    }

    @FXML
    private void paginaCreazaCont() throws IOException {
        App.setRoot("creareContNume");
    }
}