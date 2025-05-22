package com.proiect;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;

public class SetariNotificariController {
    private static final Logger logger = Logger.getLogger(SetariNotificariController.class.getName());
    private static final String TITLU_SUCCES = "Succes";
    private static final String TITLU_EROARE = "Eroare";
    private static final String USER_ID_KEY = "userId";
    private static final String SERVER_BASE_URL = "http://localhost:3000";
    
    @FXML private CheckBox checkboxEmail;
    @FXML private CheckBox checkboxSMS;
    @FXML private CheckBox checkboxPushWeb;
    @FXML private CheckBox checkboxPromotii;
    @FXML private CheckBox checkboxAnulari;
    @FXML private CheckBox checkboxModificari;
    
    @FXML
    private void initialize() {
        incarcaSetari();
    }
    
    private void incarcaSetari() {
        try {
            String userId = (String) App.getUserData().get(USER_ID_KEY);
            java.net.URI uri = new java.net.URI(SERVER_BASE_URL + "/users/" + userId);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JSONObject user = new JSONObject(response);
                
                if (user.has("setari")) {
                    JSONObject setari = user.getJSONObject("setari");
                    checkboxEmail.setSelected(setari.optBoolean("notificariEmail", false));
                    checkboxSMS.setSelected(setari.optBoolean("notificariSMS", false));
                    checkboxPushWeb.setSelected(setari.optBoolean("notificariPushWeb", false));
                    checkboxPromotii.setSelected(setari.optBoolean("notificariPromotii", true));
                    checkboxAnulari.setSelected(setari.optBoolean("notificariAnulari", true));
                    checkboxModificari.setSelected(setari.optBoolean("notificariModificari", true));
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Eroare la încărcarea setărilor", e);
            afiseazaEroare("Nu s-au putut încărca setările.");
        }
    }
    
    @FXML
    private void salveazaSetari() {
        try {
            String userId = (String) App.getUserData().get(USER_ID_KEY);
            java.net.URI uri = new java.net.URI(SERVER_BASE_URL + "/users/" + userId + "/setari");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            JSONObject setari = new JSONObject();
            setari.put("notificariEmail", checkboxEmail.isSelected());
            setari.put("notificariSMS", checkboxSMS.isSelected());
            setari.put("notificariPushWeb", checkboxPushWeb.isSelected());
            setari.put("notificariPromotii", checkboxPromotii.isSelected());
            setari.put("notificariAnulari", checkboxAnulari.isSelected());
            setari.put("notificariModificari", checkboxModificari.isSelected());
            
            try (var os = conn.getOutputStream()) {
                os.write(setari.toString().getBytes(StandardCharsets.UTF_8));
            }
            
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(TITLU_SUCCES);
                alert.setHeaderText("Setări salvate");
                alert.setContentText("Setările au fost salvate cu succes.");
                alert.showAndWait();
            } else {
                afiseazaEroare("Nu s-au putut salva setările. Te rugăm să încerci din nou.");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Eroare la salvarea setărilor", e);
            afiseazaEroare("Nu s-au putut salva setările.");
        }
    }
    
    @FXML
    private void inapoi() throws Exception {
        App.setRoot("paginaContClient");
    }
    
    private void afiseazaEroare(String mesaj) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(TITLU_EROARE);
        alert.setHeaderText(TITLU_EROARE);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
}
