package com.proiect;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class EditareUtilizatorController {
    private static final Logger logger = Logger.getLogger(EditareUtilizatorController.class.getName());
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ISO_DATE,             // yyyy-MM-dd
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };
    
    @FXML
    private TextField campNumeUtilizator;
    @FXML
    private TextField campParola;
    @FXML
    private TextField campEmail;
    @FXML
    private TextField campNume;
    @FXML
    private TextField campPrenume;
    @FXML
    private DatePicker campDataNasterii;
    @FXML
    private CheckBox campAdmin;

    private String idUtilizator;
    
    @FXML
    private void initialize() {
        idUtilizator = (String) App.getUserData().get("idUtilizator");
        logger.info("ID Utilizator pentru editare: " + idUtilizator);
        
        if (idUtilizator == null || idUtilizator.isEmpty()) {
            afiseazaEroare("Nu s-a putut găsi utilizatorul pentru editare.");
            return;
        }
        incarcaDateUtilizator();
    }

    private void incarcaDateUtilizator() {
        HttpURLConnection clientHttp = null;
        try {
            String url = "http://localhost:3000/users/" + idUtilizator;
            logger.info("Încercare încărcare utilizator de la: " + url);
            
            clientHttp = (HttpURLConnection) new URL(url).openConnection();
            clientHttp.setRequestMethod("GET");
            clientHttp.setConnectTimeout(5000);
            clientHttp.setReadTimeout(5000);

            int codRaspuns = clientHttp.getResponseCode();
            logger.info("Cod răspuns server: " + codRaspuns);

            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                try (InputStream is = clientHttp.getInputStream()) {
                    byte[] responseBytes = is.readAllBytes();
                    String raspuns = new String(responseBytes, StandardCharsets.UTF_8);
                    logger.info("Răspuns server: " + raspuns);
                    
                    JSONObject utilizator = new JSONObject(raspuns);

                    campNumeUtilizator.setText(utilizator.getString("username"));
                    campParola.setText(utilizator.getString("parola"));
                    campEmail.setText(utilizator.getString("email"));
                    campNume.setText(utilizator.getString("nume"));
                    campPrenume.setText(utilizator.getString("prenume"));
                    
                    String dataNasterii = utilizator.getString("dataNasterii");
                    LocalDate data = parseazaData(dataNasterii);
                    if (data != null) {
                        campDataNasterii.setValue(data);
                    } else {
                        logger.warning("Format dată invalid: " + dataNasterii);
                        afiseazaEroare("Format dată invalid: " + dataNasterii);
                    }
                    
                    campAdmin.setSelected(utilizator.getBoolean("admin"));
                }
            } else if (codRaspuns == HttpURLConnection.HTTP_NOT_FOUND) {
                logger.warning("Utilizatorul nu a fost găsit: " + idUtilizator);
                afiseazaEroare("Utilizatorul nu a fost găsit.");
            } else {
                logger.warning("Eroare server: " + codRaspuns);
                afiseazaEroare("Eroare la încărcarea datelor. Cod răspuns: " + codRaspuns);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Eroare la încărcarea datelor utilizatorului", e);
            afiseazaEroare("Eroare la încărcarea datelor: " + e.getMessage());
        } finally {
            if (clientHttp != null) {
                clientHttp.disconnect();
            }
        }
    }

    private LocalDate parseazaData(String data) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(data, formatter);
            } catch (DateTimeParseException e) {
                logger.warning("Eroare la parsarea datei: " + data);
            }
        }
        return null;
    }

    @FXML
    private void salveazaUtilizator() {
        if (!valideazaDate()) {
            return;
        }

        HttpURLConnection clientHttp = null;
        try {
            JSONObject dateActualizate = new JSONObject();
            dateActualizate.put("username", campNumeUtilizator.getText().trim());
            dateActualizate.put("parola", campParola.getText().trim());
            dateActualizate.put("email", campEmail.getText().trim());
            dateActualizate.put("nume", campNume.getText().trim());
            dateActualizate.put("prenume", campPrenume.getText().trim());
            dateActualizate.put("dataNasterii", campDataNasterii.getValue().format(DATE_FORMATTERS[1])); // Format dd/MM/yyyy
            dateActualizate.put("admin", campAdmin.isSelected());

            String url = "http://localhost:3000/users/" + idUtilizator;
            logger.info("Trimitere actualizare la: " + url);
            logger.info("Date actualizate: " + dateActualizate.toString());

            clientHttp = (HttpURLConnection) new URL(url).openConnection();
            clientHttp.setRequestMethod("PUT");
            clientHttp.setRequestProperty("Content-Type", "application/json");
            clientHttp.setDoOutput(true);
            clientHttp.setConnectTimeout(5000);
            clientHttp.setReadTimeout(5000);

            try (OutputStream os = clientHttp.getOutputStream()) {
                byte[] input = dateActualizate.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int codRaspuns = clientHttp.getResponseCode();
            logger.info("Cod răspuns actualizare: " + codRaspuns);

            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle(TITLU_SUCCES);
                alerta.setHeaderText("Utilizator actualizat");
                alerta.setContentText("Datele utilizatorului au fost actualizate cu succes!");
                alerta.showAndWait();
                App.setRoot("paginaUseriAdmin");
            } else {
                logger.warning("Eroare la actualizare: " + codRaspuns);
                afiseazaEroare("Nu s-au putut actualiza datele. Cod răspuns: " + codRaspuns);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Eroare la salvarea datelor", e);
            afiseazaEroare("Eroare la salvarea datelor: " + e.getMessage());
        } finally {
            if (clientHttp != null) {
                clientHttp.disconnect();
            }
        }
    }

    private boolean valideazaDate() {
        if (campNumeUtilizator.getText().trim().isEmpty() || 
            campParola.getText().trim().isEmpty() || 
            campEmail.getText().trim().isEmpty() || 
            campNume.getText().trim().isEmpty() || 
            campPrenume.getText().trim().isEmpty() || 
            campDataNasterii.getValue() == null) {
            
            afiseazaEroare("Toate câmpurile sunt obligatorii!");
            return false;
        }
        return true;
    }

    @FXML
    private void inapoi() throws Exception {
        App.setRoot("paginaUseriAdmin");
    }

    private void afiseazaEroare(String mesaj) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(TITLU_EROARE);
        alerta.setHeaderText("Eroare");
        alerta.setContentText(mesaj);
        alerta.showAndWait();
    }
}
