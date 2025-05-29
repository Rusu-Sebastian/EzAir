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
import javafx.scene.control.CheckBox;

public class SetariNotificariController {
    private static final Logger jurnal = Logger.getLogger(SetariNotificariController.class.getName());
    private static final String TITLU_SUCCES = "Succes";
    private static final String TITLU_EROARE = "Eroare";
    private static final String ID_UTILIZATOR = "userId";
    private static final String URL_SERVER = "http://localhost:3000";
    private static final int TIMP_EXPIRARE_CONEXIUNE = 5000; // 5 secunde
    
    private static final String MESAJ_EROARE_INCARCARE = "Nu s-au putut încărca setările de notificări.";
    private static final String MESAJ_EROARE_SALVARE = "Nu s-au putut salva setările de notificări.";
    private static final String MESAJ_SUCCES_SALVARE = "Setările de notificări au fost salvate cu succes.";
    private static final String MESAJ_ID_LIPSA = "Nu s-a putut găsi ID-ul utilizatorului.";
    
    @FXML private CheckBox checkboxEmail;
    @FXML private CheckBox checkboxSMS;
    @FXML private CheckBox checkboxPushWeb;
    @FXML private CheckBox checkboxPromotii;
    @FXML private CheckBox checkboxAnulari;
    @FXML private CheckBox checkboxModificari;
    
    @FXML
    public void initialize() {
        Platform.runLater(this::incarcaSetari);
    }
    
    private void incarcaSetari() {
        String idUtilizator = (String) App.getDateUtilizator().get(ID_UTILIZATOR);
        if (idUtilizator == null) {
            afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "ID utilizator lipsă", MESAJ_ID_LIPSA);
            return;
        }

        try {
            final HttpURLConnection conexiune = configureazaConexiune("/users/" + idUtilizator, "GET");
            try {
                final int codRaspuns = conexiune.getResponseCode();
                if (codRaspuns == HttpURLConnection.HTTP_OK) {
                    String raspuns = citesteDateRaspuns(conexiune);
                    actualizeazaSetariInterfata(new JSONObject(raspuns));
                } else {
                    jurnal.log(Level.WARNING, "Eroare la încărcarea setărilor. Cod: {0}", codRaspuns);
                    afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Eroare server", MESAJ_EROARE_INCARCARE);
                }
            } finally {
                conexiune.disconnect();
            }
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Excepție la încărcarea setărilor", e);
            afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Eroare comunicare", MESAJ_EROARE_INCARCARE);
        }
    }
    
    @FXML
    public void salveazaSetari() {
        String idUtilizator = (String) App.getDateUtilizator().get(ID_UTILIZATOR);
        if (idUtilizator == null) {
            afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "ID utilizator lipsă", MESAJ_ID_LIPSA);
            return;
        }

        try {
            final HttpURLConnection conexiune = configureazaConexiune("/users/" + idUtilizator + "/setari", "PUT");
            try {
                conexiune.setRequestProperty("Content-Type", "application/json");
                conexiune.setDoOutput(true);
                
                JSONObject setari = creeazaJsonSetari();
                try (OutputStream os = conexiune.getOutputStream()) {
                    os.write(setari.toString().getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
                
                final int codRaspuns = conexiune.getResponseCode();
                if (codRaspuns == HttpURLConnection.HTTP_OK) {
                    afiseazaAlerta(Alert.AlertType.INFORMATION, TITLU_SUCCES, "Setări salvate", MESAJ_SUCCES_SALVARE);
                } else {
                    jurnal.log(Level.WARNING, "Eroare la salvarea setărilor. Cod: {0}", codRaspuns);
                    afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Eroare server", MESAJ_EROARE_SALVARE);
                }
            } finally {
                conexiune.disconnect();
            }
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Excepție la salvarea setărilor", e);
            afiseazaAlerta(Alert.AlertType.ERROR, TITLU_EROARE, "Eroare comunicare", MESAJ_EROARE_SALVARE);
        }
    }
    
    @FXML
    public void inapoi() throws IOException {
        App.setRoot("paginaContClient");
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
    
    private String citesteDateRaspuns(HttpURLConnection conexiune) throws IOException {
        try (var is = conexiune.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    private void actualizeazaSetariInterfata(JSONObject utilizator) {
        if (utilizator.has("setari")) {
            JSONObject setari = utilizator.getJSONObject("setari");
            Platform.runLater(() -> {
                checkboxEmail.setSelected(setari.optBoolean("notificariEmail", false));
                checkboxSMS.setSelected(setari.optBoolean("notificariSMS", false));
                checkboxPushWeb.setSelected(setari.optBoolean("notificariPushWeb", false));
                checkboxPromotii.setSelected(setari.optBoolean("notificariPromotii", true));
                checkboxAnulari.setSelected(setari.optBoolean("notificariAnulari", true));
                checkboxModificari.setSelected(setari.optBoolean("notificariModificari", true));
            });
        }
    }
    
    private JSONObject creeazaJsonSetari() {
        JSONObject setari = new JSONObject();
        setari.put("notificariEmail", checkboxEmail.isSelected());
        setari.put("notificariSMS", checkboxSMS.isSelected());
        setari.put("notificariPushWeb", checkboxPushWeb.isSelected());
        setari.put("notificariPromotii", checkboxPromotii.isSelected());
        setari.put("notificariAnulari", checkboxAnulari.isSelected());
        setari.put("notificariModificari", checkboxModificari.isSelected());
        return setari;
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
