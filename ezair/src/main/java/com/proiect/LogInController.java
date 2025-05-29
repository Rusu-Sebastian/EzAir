package com.proiect;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.proiect.config.ApiEndpoints;
import com.proiect.util.HttpUtil;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LogInController {
    private static final Logger jurnal = Logger.getLogger(LogInController.class.getName());
    private static final String PAGINA_ADMIN = "paginaPrincipalaAdmin";
    private static final String PAGINA_USER = "paginaPrincipalaUser";
    private static final String PAGINA_EROARE = "eroareConexiune";
    private static final String PAGINA_CREARE_CONT = "creareContInceput";
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_ATENTIONARE = "Atenție";

    @FXML private TextField campNumeUtilizator;
    @FXML private PasswordField campParola;

    @FXML
    public void initialize() {
        // Initialization complete
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void login() {
        String numeUtilizator = campNumeUtilizator.getText();
        String parola = campParola.getText();

        if (numeUtilizator.isEmpty() || parola.isEmpty()) {
            afiseazaAlerta(Alert.AlertType.WARNING,
                          TITLU_ATENTIONARE,
                          "Te rog să completezi toate câmpurile",
                          "Numele de utilizator și parola sunt obligatorii.");
            return;
        }

        try {
            autentificare(numeUtilizator, parola);
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la autentificare: {0}", e.getMessage());
            try {
                App.setRoot(PAGINA_EROARE);
            } catch (IOException ex) {
                jurnal.log(Level.SEVERE, "Nu s-a putut naviga la pagina de eroare: {0}", ex.getMessage());
            }
        }
    }

    private void autentificare(String numeUtilizator, String parola) throws IOException, URISyntaxException {
        HttpURLConnection conexiune = HttpUtil.createConnection(ApiEndpoints.LOGIN_URL, "POST");
        try {
            String dateAutentificare = String.format("{\"numeUtilizator\":\"%s\",\"parola\":\"%s\"}", 
                                                   numeUtilizator, parola);
            byte[] date = dateAutentificare.getBytes(StandardCharsets.UTF_8);
            conexiune.setRequestProperty("Content-Type", "application/json");
            conexiune.setRequestProperty("Content-Length", String.valueOf(date.length));
            conexiune.setDoOutput(true);
            conexiune.getOutputStream().write(date);

            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                proceseazaRaspunsAutentificare(conexiune);
            } else {
                gestioneazaEroareAutentificare(conexiune);
            }
        } finally {
            conexiune.disconnect();
        }
    }

    private void proceseazaRaspunsAutentificare(HttpURLConnection conexiune) throws IOException {
        try (InputStream is = conexiune.getInputStream();
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            String raspuns = scanner.useDelimiter("\\A").next();
            JSONObject dateUtilizator = new JSONObject(raspuns);
            
            Platform.runLater(() -> {
                try {
                    // Salvăm toate datele importante ale utilizatorului
                    App.getDateUtilizator().put(ApiEndpoints.USER_ID_KEY, dateUtilizator.getString("id"));
                    boolean esteAdmin = dateUtilizator.getBoolean(ApiEndpoints.IS_ADMIN_KEY);
                    App.getDateUtilizator().put(ApiEndpoints.IS_ADMIN_KEY, Boolean.toString(esteAdmin));
                    
                    // Salvăm numele și prenumele pentru afișare în interfață
                    App.getDateUtilizator().put("nume", dateUtilizator.getString("nume"));
                    App.getDateUtilizator().put("prenume", dateUtilizator.getString("prenume"));
                    App.getDateUtilizator().put("email", dateUtilizator.getString("email"));
                    App.getDateUtilizator().put("numeUtilizator", dateUtilizator.getString("numeUtilizator"));
                    
                    App.setRoot(esteAdmin ? PAGINA_ADMIN : PAGINA_USER);
                } catch (IOException e) {
                    jurnal.log(Level.SEVERE, "Eroare la navigare post-autentificare: {0}", e.getMessage());
                    afiseazaAlerta(Alert.AlertType.ERROR,
                                 TITLU_EROARE,
                                 "Nu s-a putut deschide pagina principală",
                                 "Te rog să încerci din nou.");
                }
            });
        }
    }

    private void gestioneazaEroareAutentificare(HttpURLConnection conexiune) {
        try (InputStream is = conexiune.getErrorStream()) {
            if (is != null) {
                String raspunsEroare = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                jurnal.log(Level.WARNING, "Eroare autentificare: {0}", raspunsEroare);
                Platform.runLater(() -> 
                    afiseazaAlerta(Alert.AlertType.ERROR,
                                 TITLU_EROARE,
                                 "Nu te-ai putut conecta",
                                 "Numele de utilizator sau parola sunt incorecte.")
                );
            }
        } catch (IOException e) {
            jurnal.log(Level.WARNING, "Nu s-a putut citi răspunsul de eroare: {0}", e.getMessage());
        }
    }

        @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void navigarePaginaCreareCont() {
        try {
            App.setRoot(PAGINA_CREARE_CONT);
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigare: {0}", e.getMessage());
            afiseazaAlerta(Alert.AlertType.ERROR,
                          TITLU_EROARE,
                          "Nu s-a putut deschide pagina de creare cont",
                          "Te rog să încerci din nou.");
        }
    }

    private void afiseazaAlerta(Alert.AlertType tip, String titlu, String antet, String continut) {
        Alert alerta = new Alert(tip);
        alerta.setTitle(titlu);
        alerta.setHeaderText(antet);
        alerta.setContentText(continut);
        alerta.showAndWait();
    }
}
