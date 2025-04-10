package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;

public class LogInController {

    private static final Logger logger = Logger.getLogger(App.class.getName());

    // functia de logare
    @FXML
    private void logIn() throws IOException, java.net.URISyntaxException {
        // Preluarea datelor introduse de utilizator
        String username = ((javafx.scene.control.TextField) App.scene.lookup("#username")).getText();
        String parola = ((javafx.scene.control.PasswordField) App.scene.lookup("#parola")).getText();

        // Verificarea daca toate campurile sunt completate
        if (username.isEmpty() || parola.isEmpty()) {
            logger.warning("Completati toate campurile!");
            return;
        }

        // URL-ul serverului
        final String url = "http://localhost:3000/users/login";
        HttpURLConnection httpClient = null;

        try {
            // Configurarea conexiunii HTTP
            httpClient = (HttpURLConnection) new java.net.URI(url).toURL().openConnection();
            httpClient.setRequestMethod("POST");
            httpClient.setRequestProperty("Content-Type", "application/json");
            httpClient.setDoOutput(true);

            // Crearea corpului cererii JSON
            String jsonInputString = String.format("{\"username\": \"%s\", \"parola\": \"%s\"}", username, parola);

            // Trimiterea cererii
            try (java.io.OutputStream os = httpClient.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Citirea răspunsului
            int responseCode = httpClient.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (java.io.InputStream is = httpClient.getInputStream();
                     java.util.Scanner scanner = new java.util.Scanner(is, java.nio.charset.StandardCharsets.UTF_8.name())) {
                    String responseBody = scanner.useDelimiter("\\A").next();
                    logger.info("Răspuns server: " + responseBody);

                    // Parsarea răspunsului JSON
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(responseBody);

                    // Verificarea dacă utilizatorul este admin
                    boolean isAdmin = jsonResponse.getBoolean("admin");
                    String nume = jsonResponse.getString("nume");
                    String prenume = jsonResponse.getString("prenume");

                    // Salvarea datelor utilizatorului
                    App.getUserData().put("username", username);
                    App.getUserData().put("nume", nume);
                    App.getUserData().put("prenume", prenume);

                    // Redirecționarea utilizatorului
                    if (isAdmin) {
                        logger.info("Utilizatorul este admin.");
                        App.setRoot("paginaPrincipalaAdmin");
                    } else {
                        App.setRoot("paginaPrincipalaUser");
                    }
                }
            } else {
                logger.warning("Datele introduse sunt gresite! Response code: " + responseCode);
            }
        } catch (IOException | java.net.URISyntaxException e) {
            logger.log(Level.SEVERE, "Eroare la conectarea la server: " + e.getMessage(), e);
            App.setRoot("eroareConexiune");
        } finally {
            if (httpClient != null) {
                httpClient.disconnect();
            }
        }

        // Logarea utilizatorului (fara parola)
        logger.info("Username: " + username);
    }

    @FXML
    private void paginaCreazaCont() throws IOException {
        App.setRoot("creareContInceput");
    }
}
