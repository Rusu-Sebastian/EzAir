package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ControllerAutentificare {

    private static final Logger jurnal = Logger.getLogger(ControllerAutentificare.class.getName());
    
    @FXML private TextField numeUtilizator;
    @FXML private PasswordField parola;
    
    @FXML
    public void initialize() {
        // Initialization complete
    }

    // Functia de autentificare
    @SuppressWarnings("unused")
    @FXML
    private void autentificare() throws IOException, java.net.URISyntaxException {
        // Preluarea datelor introduse de utilizator
        String numeUtilizatorText = numeUtilizator.getText();
        String parolaText = parola.getText();

        // Verificarea daca toate campurile sunt completate
        if (numeUtilizatorText.isEmpty() || parolaText.isEmpty()) {
            jurnal.log(Level.WARNING, "Completati toate campurile!");
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
            String jsonCerere = String.format("{\"numeUtilizator\": \"%s\", \"parola\": \"%s\"}", numeUtilizatorText, parolaText);

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
                    jurnal.log(Level.INFO, "Răspuns server: {0}", corpRaspuns);

                    // Parsarea răspunsului JSON
                    org.json.JSONObject raspunsJson = new org.json.JSONObject(corpRaspuns);

                    // Verificarea dacă utilizatorul este admin
                    boolean esteAdmin = raspunsJson.getBoolean("esteAdmin");
                    String nume = raspunsJson.getString("nume");
                    String prenume = raspunsJson.getString("prenume");

                    // Salvarea datelor utilizatorului
                    App.getDateUtilizator().put("numeUtilizator", numeUtilizatorText);
                    App.getDateUtilizator().put("nume", nume);
                    App.getDateUtilizator().put("prenume", prenume);
                    App.getDateUtilizator().put("userId", raspunsJson.getString("id"));
                    // Adăugăm și statusul de admin ca string
                    App.getDateUtilizator().put("esteAdmin", String.valueOf(esteAdmin));

                    // Redirecționarea utilizatorului
                    if (esteAdmin) {
                        jurnal.log(Level.INFO, "Utilizatorul este administrator.");
                        App.setRoot("paginaPrincipalaAdmin");
                    } else {
                        App.setRoot("paginaPrincipalaUser");
                    }
                }
            } else {
                jurnal.log(Level.WARNING, "Datele introduse sunt greșite! Cod răspuns: {0}", codRaspuns);
            }
        }        catch (IOException | java.net.URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la conectarea la server: {0}", e.getMessage());
            App.setRoot("eroareConexiune");
        } finally {
            if (conexiuneHttp != null) {
                conexiuneHttp.disconnect();
            }
        }

        // Jurnalizarea utilizatorului (fara parola)
        jurnal.log(Level.INFO, "Nume utilizator: {0}", numeUtilizatorText);
    }

    @SuppressWarnings("unused")
    @FXML
    private void navigarePaginaCreareCont() throws IOException {
        App.setRoot("creareContInceput");
    }
}
