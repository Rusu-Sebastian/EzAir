package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;

public class ControllerAutentificare {

    private static final Logger jurnal = Logger.getLogger(App.class.getName());

    // Functia de autentificare
    @FXML
    private void autentificare() throws IOException, java.net.URISyntaxException {
        // Preluarea datelor introduse de utilizator
        String numeUtilizator = ((javafx.scene.control.TextField) App.scena.lookup("#numeUtilizator")).getText();
        String parola = ((javafx.scene.control.PasswordField) App.scena.lookup("#parola")).getText();

        // Verificarea daca toate campurile sunt completate
        if (numeUtilizator.isEmpty() || parola.isEmpty()) {
            jurnal.warning("Completati toate campurile!");
            return;
        }

        // URL-ul serverului
        final String url = "http://localhost:3000/users/login";
        HttpURLConnection conexiuneHttp = null;

        try {
            // Configurarea conexiunii HTTP
            conexiuneHttp = (HttpURLConnection) new java.net.URI(url).toURL().openConnection();
            conexiuneHttp.setRequestMethod("POST");
            conexiuneHttp.setRequestProperty("Content-Type", "application/json");
            conexiuneHttp.setDoOutput(true);

            // Crearea corpului cererii JSON
            String jsonCerere = String.format("{\"username\": \"%s\", \"parola\": \"%s\"}", numeUtilizator, parola);

            // Trimiterea cererii
            try (java.io.OutputStream os = conexiuneHttp.getOutputStream()) {
                byte[] date = jsonCerere.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                os.write(date, 0, date.length);
            }

            // Citirea răspunsului
            int codRaspuns = conexiuneHttp.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                try (java.io.InputStream is = conexiuneHttp.getInputStream();
                     java.util.Scanner scanner = new java.util.Scanner(is, java.nio.charset.StandardCharsets.UTF_8.name())) {
                    String corpRaspuns = scanner.useDelimiter("\\A").next();
                    jurnal.info("Răspuns server: " + corpRaspuns);

                    // Parsarea răspunsului JSON
                    org.json.JSONObject raspunsJson = new org.json.JSONObject(corpRaspuns);

                    // Verificarea dacă utilizatorul este admin
                    boolean esteAdmin = raspunsJson.getBoolean("admin");
                    String nume = raspunsJson.getString("nume");
                    String prenume = raspunsJson.getString("prenume");

                    // Salvarea datelor utilizatorului
                    App.getDateUtilizator().put("numeUtilizator", numeUtilizator);
                    App.getDateUtilizator().put("nume", nume);
                    App.getDateUtilizator().put("prenume", prenume);
                    App.getDateUtilizator().put("idUtilizator", raspunsJson.getString("id"));

                    // Redirecționarea utilizatorului
                    if (esteAdmin) {
                        jurnal.info("Utilizatorul este administrator.");
                        App.setRoot("paginaPrincipalaAdmin");
                    } else {
                        App.setRoot("paginaPrincipalaUser");
                    }
                }
            } else {
                jurnal.warning("Datele introduse sunt greșite! Cod răspuns: " + codRaspuns);
            }
        } catch (IOException | java.net.URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la conectarea la server: " + e.getMessage(), e);
            App.setRoot("eroareConexiune");
        } finally {
            if (conexiuneHttp != null) {
                conexiuneHttp.disconnect();
            }
        }

        // Jurnalizarea utilizatorului (fara parola)
        jurnal.info("Nume utilizator: " + numeUtilizator);
    }

    @FXML
    private void navigarePaginaCreareCont() throws IOException {
        App.setRoot("creareContInceput");
    }
}
