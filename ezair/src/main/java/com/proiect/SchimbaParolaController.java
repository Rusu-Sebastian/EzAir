package com.proiect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;

public class SchimbaParolaController {
    private static final Logger jurnal = Logger.getLogger(SchimbaParolaController.class.getName());
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    private static final String URL_SERVER = "http://localhost:3000";
    private static final String ID_UTILIZATOR = "userId";
    private static final int TIMP_EXPIRARE_CONEXIUNE = 5000; // 5 secunde
    
    private static final String EROARE_CAMPURI_OBLIGATORII = "Toate câmpurile sunt obligatorii.";
    private static final String EROARE_PAROLE_DIFERITE = "Parolele noi nu coincid.";
    private static final String EROARE_PAROLA_SCURTA = "Parola nouă trebuie să aibă cel puțin 6 caractere.";
    private static final String EROARE_PAROLA_ACTUALA = "Parola actuală este incorectă.";
    private static final String SUCCES_SCHIMBARE = "Parola a fost schimbată cu succes!";
    private static final String EROARE_COMUNICARE = "A apărut o eroare în comunicarea cu serverul.";
    private static final String EROARE_NAVIGARE = "Nu s-a putut reveni la pagina anterioară.";
    
    @FXML private PasswordField fieldParolaActuala;
    @FXML private PasswordField fieldParolaNoua;
    @FXML private PasswordField fieldConfirmareParola;
    
    @FXML
    public void schimbaParola() {
        if (!valideazaIntrari()) {
            return;
        }
        
        String idUtilizator = App.getDateUtilizator().get(ID_UTILIZATOR);
        if (idUtilizator == null) {
            afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Eroare sesiune", "Sesiunea dumneavoastră a expirat.");
            navigheazaLaLogin();
            return;
        }

        try {
            if (schimbaParolaInServer(idUtilizator)) {
                afiseazaAlerta(Alert.AlertType.INFORMATION, TITLU_SUCCES, "Parola schimbată", SUCCES_SCHIMBARE);
                Platform.runLater(this::inapoiLaPaginaContClient);
            }
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la schimbarea parolei", e);
            afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Eroare server", EROARE_COMUNICARE);
        }
    }
    
    @FXML
    public void inapoi() {
        inapoiLaPaginaContClient();
    }
    
    private boolean valideazaIntrari() {
        if (fieldParolaActuala.getText().isEmpty() || 
            fieldParolaNoua.getText().isEmpty() || 
            fieldConfirmareParola.getText().isEmpty()) {
            afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Câmpuri lipsă", EROARE_CAMPURI_OBLIGATORII);
            return false;
        }
        
        if (fieldParolaNoua.getText().length() < 6) {
            afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Parolă invalidă", EROARE_PAROLA_SCURTA);
            return false;
        }
        
        if (!fieldParolaNoua.getText().equals(fieldConfirmareParola.getText())) {
            afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Parole diferite", EROARE_PAROLE_DIFERITE);
            fieldParolaNoua.clear();
            fieldConfirmareParola.clear();
            return false;
        }
        
        return true;
    }
    
    private boolean schimbaParolaInServer(String idUtilizator) throws IOException, URISyntaxException {
        HttpURLConnection conexiune = configureazaConexiune("/users/" + idUtilizator + "/schimbaParola", "PUT");
        try {
            conexiune.setRequestProperty("Content-Type", "application/json");
            conexiune.setDoOutput(true);
            
            JSONObject dateParola = new JSONObject();
            dateParola.put("parolaActuala", fieldParolaActuala.getText());
            dateParola.put("parolaNoua", fieldParolaNoua.getText());
            
            try (OutputStream os = conexiune.getOutputStream()) {
                os.write(dateParola.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            
            int codRaspuns = conexiune.getResponseCode();
            
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                return true;
            } else if (codRaspuns == HttpURLConnection.HTTP_UNAUTHORIZED) {
                afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Parolă incorectă", EROARE_PAROLA_ACTUALA);
                fieldParolaActuala.clear();
                return false;
            } else {
                jurnal.warning(() -> "Eroare la schimbarea parolei. Cod: " + codRaspuns);
                return false;
            }
        } finally {
            conexiune.disconnect();
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
    
    private void afiseazaAlerta(Alert.AlertType tip, String titlu, String antet, String continut) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(tip);
            alerta.setTitle(titlu);
            alerta.setHeaderText(antet);
            alerta.setContentText(continut);
            alerta.showAndWait();
        });
    }
    
    private void inapoiLaPaginaContClient() {
        try {
            App.setRoot("paginaContClient");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigare", e);
            afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Eroare navigare", EROARE_NAVIGARE);
        }
    }
    
    private void navigheazaLaLogin() {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigare către login", e);
            afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Eroare navigare", "Nu s-a putut naviga către pagina de autentificare.");
        }
    }
}
