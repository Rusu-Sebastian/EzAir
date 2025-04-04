package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


import javafx.fxml.FXML;

public class LogInController {

    //functia de logare
    @FXML
    private void logIn() throws IOException, java.net.URISyntaxException {
        //preluarea datelor introduse de utilizator
        String username = ((javafx.scene.control.TextField) App.scene.lookup("#username")).getText();
        String parola = ((javafx.scene.control.PasswordField) App.scene.lookup("#parola")).getText();

        //verificarea daca toate campurile sunt completate
        if(username.isEmpty()|| parola.isEmpty()){
            System.out.println("Completati toate campurile!");
            return;
        } else {
            //conectarea la server si trimiterea datelor de logare in format json
            String url = "http://localhost:3000/users/login";
            HttpURLConnection httpClient = (HttpURLConnection) new java.net.URI(url).toURL().openConnection();
            httpClient.setRequestMethod("POST");
            httpClient.setRequestProperty("Content-Type", "application/json");
            httpClient.setDoOutput(true);
            String jsonInputString = String.format("{\"username\": \"%s\", \"parola\": \"%s\"}", username, parola);
            try (java.io.OutputStream os = httpClient.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            httpClient.connect();

            int responseCode = httpClient.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                App.setRoot("paginaPrincipala");
            } else {
                System.out.println("Datele introduse sunt gresite! Response code: " + responseCode);
            }
        }
        System.out.println("Username: " + username);
        System.out.println("parola: " + parola);
    }

    @FXML
    private void paginaCreazaCont() throws IOException {
        App.setRoot("creareContInceput");
    }
}