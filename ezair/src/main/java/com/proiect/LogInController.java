package com.proiect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LogInController {
    private static final Logger jurnal = Logger.getLogger(LogInController.class.getName());
    private static final String URL_SERVER = "http://localhost:3000";
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_ATENTIONARE = "Atenționare";
    private static final int TIMP_EXPIRARE_CONEXIUNE = 5000; // 5 secunde
    
    // Constants for user data keys
    private static final String CHEIE_NUME_UTILIZATOR = "numeUtilizator";
    private static final String CHEIE_NUME = "nume";
    private static final String CHEIE_PRENUME = "prenume";
    private static final String CHEIE_ID_UTILIZATOR = "userId";
    private static final String CHEIE_ADMIN = "esteAdmin";
    
    // Constants for JSON fields
    private static final String CAMP_NUME_UTILIZATOR = "numeUtilizator";
    private static final String CAMP_PAROLA = "parola";
    private static final String CAMP_ID = "id";

    @FXML private TextField numeUtilizator;
    @FXML private PasswordField parola;

    @FXML
    public void autentificare() {
        if (!valideazaIntrari()) {
            return;
        }

        try {
            JSONObject dateAutentificare = new JSONObject();
            dateAutentificare.put(CAMP_NUME_UTILIZATOR, numeUtilizator.getText().trim());
            dateAutentificare.put(CAMP_PAROLA, parola.getText());

            autentificaUtilizator(dateAutentificare);
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la conectarea la server", e);
            redirectioneazaLaEroareConexiune();
        }
    }

    private boolean valideazaIntrari() {
        if (numeUtilizator.getText().trim().isEmpty() || parola.getText().isEmpty()) {
            afiseazaAlerta(Alert.AlertType.WARNING,
                          TITLU_ATENTIONARE,
                          "Câmpuri incomplete",
                          "Te rugăm să completezi toate câmpurile.");
            return false;
        }
        return true;
    }

    private void autentificaUtilizator(JSONObject dateAutentificare) throws IOException, URISyntaxException {
        HttpURLConnection conexiune = null;
        try {
            conexiune = configureazaConexiune("/users/login", "POST");
            conexiune.setRequestProperty("Content-Type", "application/json");
            conexiune.setDoOutput(true);

            try (OutputStream os = conexiune.getOutputStream()) {
                os.write(dateAutentificare.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int codRaspuns = conexiune.getResponseCode();
            switch (codRaspuns) {
                case HttpURLConnection.HTTP_OK:
                    proceseazaRaspunsAutentificare(conexiune);
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    afiseazaAlerta(Alert.AlertType.ERROR,
                                  TITLU_EROARE,
                                  "Autentificare eșuată",
                                  "Numele de utilizator sau parola sunt incorecte.");
                    parola.clear();
                    break;
                default:
                    jurnal.warning(() -> String.format("Server a returnat codul: %d", codRaspuns));
                    afiseazaAlerta(Alert.AlertType.ERROR,
                                  TITLU_EROARE,
                                  "Eroare server",
                                  "A apărut o eroare la procesarea cererii. Te rugăm să încerci din nou.");
                    break;
            }
        } finally {
            if (conexiune != null) {
                conexiune.disconnect();
            }
        }
    }

    private void proceseazaRaspunsAutentificare(HttpURLConnection conexiune) throws IOException {
        try (InputStream is = conexiune.getInputStream();
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            String raspuns = scanner.useDelimiter("\\A").next();
            JSONObject dateUtilizator = new JSONObject(raspuns);

            salveazaDateUtilizator(dateUtilizator);
            redirectioneazaUtilizator(dateUtilizator.getBoolean(CHEIE_ADMIN));
        }
    }

    private void salveazaDateUtilizator(JSONObject dateUtilizator) {
        App.getDateUtilizator().put(CHEIE_NUME_UTILIZATOR, dateUtilizator.getString(CAMP_NUME_UTILIZATOR));
        App.getDateUtilizator().put(CHEIE_NUME, dateUtilizator.getString(CHEIE_NUME));
        App.getDateUtilizator().put(CHEIE_PRENUME, dateUtilizator.getString(CHEIE_PRENUME));
        App.getDateUtilizator().put(CHEIE_ID_UTILIZATOR, dateUtilizator.getString(CAMP_ID));
        App.getDateUtilizator().put(CHEIE_ADMIN, String.valueOf(dateUtilizator.getBoolean(CHEIE_ADMIN)));
    }

    private void redirectioneazaUtilizator(boolean esteAdmin) {
        try {
            if (esteAdmin) {
                jurnal.info("Autentificare reușită ca administrator");
                App.setRoot("paginaPrincipalaAdmin");
            } else {
                jurnal.info("Autentificare reușită ca utilizator normal");
                App.setRoot("paginaPrincipalaUser");
            }
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la redirecționare după autentificare", e);
            afiseazaAlerta(Alert.AlertType.ERROR,
                          TITLU_EROARE,
                          "Eroare de navigare",
                          "Nu s-a putut încărca pagina principală. Te rugăm să încerci din nou.");
        }
    }

    @FXML
    public void navigheazaLaCreareCont() {
        try {
            App.setRoot("creareContInceput");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea către pagina de creare cont", e);
            afiseazaAlerta(Alert.AlertType.ERROR,
                          TITLU_EROARE,
                          "Eroare de navigare",
                          "Nu s-a putut deschide pagina de creare cont. Te rugăm să încerci din nou.");
        }
    }

    private HttpURLConnection configureazaConexiune(String caleApi, String metodaHttp) 
            throws IOException, URISyntaxException {
        URI uri = new URI(URL_SERVER + caleApi);
        HttpURLConnection conexiune = (HttpURLConnection) uri.toURL().openConnection();
        conexiune.setRequestMethod(metodaHttp);
        conexiune.setConnectTimeout(TIMP_EXPIRARE_CONEXIUNE);
        conexiune.setReadTimeout(TIMP_EXPIRARE_CONEXIUNE);
        return conexiune;
    }

    private void redirectioneazaLaEroareConexiune() {
        try {
            App.setRoot("eroareConexiune");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la redirecționarea către pagina de eroare", e);
            afiseazaAlerta(Alert.AlertType.ERROR,
                          TITLU_EROARE,
                          "Eroare critică",
                          "Nu s-a putut afișa pagina de eroare. Vă rugăm să reporniți aplicația.");
        }
    }

    private void afiseazaAlerta(Alert.AlertType tip, String titlu, String antet, String continut) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(tip);
            alerta.setTitle(titlu);
            alerta.setHeaderText(antet);
            alerta.setContentText(continut);
            alerta.showAndWait();
        });
    }
}
