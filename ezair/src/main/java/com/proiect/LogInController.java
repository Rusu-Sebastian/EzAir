package com.proiect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javafx.fxml.FXML;

public class LogInController {

    @FXML
    private void logIn() throws IOException {
        String username = ((javafx.scene.control.TextField) App.scene.lookup("#username")).getText();
        String password = ((javafx.scene.control.PasswordField) App.scene.lookup("#parola")).getText();

        if(username.equals("") || password.equals("")) {
            System.out.println("Completati toate campurile!");
            return;
        } else {
            String url = "http://localhost:3000/login";
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            httpClient.setRequestMethod("POST");
            httpClient.setRequestProperty("Content-Type", "application/json");
            httpClient.setDoOutput(true);

            String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

            try(OutputStream os = httpClient.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);           
            }

            int responseCode = httpClient.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
            App.setRoot("paginaPrincipala");
            } else {
            System.out.println("Datele introduse sunt gresite!");
            }
        }
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
    }

    @FXML
    private void paginaCreazaCont() throws IOException {
        App.setRoot("creareCont");
    }
    
    
}
 