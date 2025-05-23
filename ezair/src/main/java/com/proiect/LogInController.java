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
    static final String URL_SERVER = "http://localhost:3000";
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_ATENTIONARE = "Atenționare";
    private static final int TIMP_EXPIRARE_CONEXIUNE = 5000;
    
    public static final String CHEIE_NUME_UTILIZATOR = "username";
    public static final String CHEIE_NUME = "nume";
    public static final String CHEIE_PRENUME = "prenume";
    public static final String CHEIE_ID_UTILIZATOR = "userId";
    public static final String CHEIE_ADMIN = "admin";
    
    public static final String CAMP_USERNAME = "username";
    public static final String CAMP_PAROLA = "parola";
    public static final String CAMP_ID = "id";

    @FXML public TextField numeUtilizator;
    @FXML public PasswordField parola;
    
    public final HttpConnectionFactory connectionFactory;

    public LogInController() {
        this(new DefaultHttpConnectionFactory());
    }
    
    // Constructor for testing
    LogInController(HttpConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @FXML
    public void autentificare() {
        if (!valideazaIntrari()) {
            return;
        }

        try {
            JSONObject dateAutentificare = new JSONObject();
            dateAutentificare.put(CAMP_USERNAME, numeUtilizator.getText().trim());
            dateAutentificare.put(CAMP_PAROLA, parola.getText());

            autentificaUtilizator(dateAutentificare);
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la conectarea la server", e);
            redirectioneazaLaEroareConexiune();
        }
    }

    public boolean valideazaIntrari() {
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
            URI uri = new URI(URL_SERVER + "/users/login");
            conexiune = connectionFactory.createConnection(uri);
            conexiune.setRequestMethod("POST");
            conexiune.setRequestProperty("Content-Type", "application/json");
            conexiune.setDoOutput(true);
            conexiune.setConnectTimeout(TIMP_EXPIRARE_CONEXIUNE);
            conexiune.setReadTimeout(TIMP_EXPIRARE_CONEXIUNE);

            try (OutputStream os = conexiune.getOutputStream()) {
                os.write(dateAutentificare.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                proceseazaRaspunsAutentificare(conexiune);
            } else if (codRaspuns == HttpURLConnection.HTTP_UNAUTHORIZED) {
                afiseazaAlerta(Alert.AlertType.ERROR,
                              TITLU_EROARE,
                              "Autentificare eșuată",
                              "Numele de utilizator sau parola sunt incorecte.");
                parola.clear();
            } else {
                jurnal.warning(() -> String.format("Server a returnat codul: %d", codRaspuns));
                afiseazaAlerta(Alert.AlertType.ERROR,
                              TITLU_EROARE,
                              "Eroare server",
                              "A apărut o eroare la procesarea cererii. Te rugăm să încerci din nou.");
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
            jurnal.info("Răspuns autentificare: " + raspuns);
            JSONObject dateUtilizator = new JSONObject(raspuns);
            
            salveazaDateUtilizator(dateUtilizator);
            boolean esteAdmin = dateUtilizator.getBoolean(CHEIE_ADMIN);
            redirectioneazaUtilizator(esteAdmin);
        }
    }

    private void salveazaDateUtilizator(JSONObject dateUtilizator) {
        App.getDateUtilizator().put(CHEIE_NUME_UTILIZATOR, dateUtilizator.getString(CAMP_USERNAME));
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
                          "Nu s-a putut încărca pagina de creare cont. Te rugăm să încerci din nou.");
        }
    }

    private void redirectioneazaLaEroareConexiune() {
        try {
            App.setRoot("eroareConexiune");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la redirecționarea către pagina de eroare", e);
            Platform.exit();
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
