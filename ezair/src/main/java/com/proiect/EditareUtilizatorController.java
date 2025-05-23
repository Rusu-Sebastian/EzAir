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
    private static final Logger jurnal = Logger.getLogger(EditareUtilizatorController.class.getName());
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    private static final String URL_SERVER = "http://localhost:3000/users/";
    private static final String FORMAT_DATA_AFISARE = "dd/MM/yyyy";
    private static final DateTimeFormatter[] FORMATE_DATA = {
        DateTimeFormatter.ISO_DATE,             // yyyy-MM-dd
        DateTimeFormatter.ofPattern(FORMAT_DATA_AFISARE),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };
    
    @FXML private TextField campNumeUtilizator;
    @FXML private TextField campParola;
    @FXML private TextField campEmail;
    @FXML private TextField campNume;
    @FXML private TextField campPrenume;
    @FXML private DatePicker campDataNasterii;
    @FXML private CheckBox campEsteAdmin;

    private String idUtilizator;
    
    @FXML
    private void initializeaza() {
        idUtilizator = (String) App.getDateUtilizator().get("idUtilizator");
        jurnal.info("ID Utilizator pentru editare: " + idUtilizator);
        
        if (idUtilizator == null || idUtilizator.isEmpty()) {
            afiseazaEroare("Nu s-a putut găsi utilizatorul pentru editare.");
            return;
        }
        incarcaDateUtilizator();
    }

    private void incarcaDateUtilizator() {
        HttpURLConnection clientHttp = null;
        try {
            String url = URL_SERVER + idUtilizator;
            jurnal.info("Încercare încărcare utilizator de la: " + url);
            
            clientHttp = (HttpURLConnection) new URL(url).openConnection();
            clientHttp.setRequestMethod("GET");
            clientHttp.setConnectTimeout(5000);
            clientHttp.setReadTimeout(5000);

            int codRaspuns = clientHttp.getResponseCode();
            jurnal.info("Cod răspuns server: " + codRaspuns);

            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                proceseazaRaspunsServer(clientHttp);
            } else if (codRaspuns == HttpURLConnection.HTTP_NOT_FOUND) {
                jurnal.warning("Utilizatorul nu a fost găsit: " + idUtilizator);
                afiseazaEroare("Utilizatorul nu a fost găsit.");
            } else {
                jurnal.warning("Eroare server: " + codRaspuns);
                afiseazaEroare("Eroare la încărcarea datelor. Cod răspuns: " + codRaspuns);
            }
        } catch (Exception e) {
            jurnal.log(Level.SEVERE, "Eroare la încărcarea datelor utilizatorului", e);
            afiseazaEroare("Eroare la încărcarea datelor: " + e.getMessage());
        } finally {
            if (clientHttp != null) {
                clientHttp.disconnect();
            }
        }
    }

    private void proceseazaRaspunsServer(HttpURLConnection clientHttp) throws Exception {
        try (InputStream is = clientHttp.getInputStream()) {
            byte[] dateRaspuns = is.readAllBytes();
            String raspuns = new String(dateRaspuns, StandardCharsets.UTF_8);
            jurnal.info("Răspuns server: " + raspuns);
            
            JSONObject utilizator = new JSONObject(raspuns);
            actualizeazaCampuriFormular(utilizator);
        }
    }

    private void actualizeazaCampuriFormular(JSONObject utilizator) {
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
            jurnal.warning("Format dată invalid: " + dataNasterii);
            afiseazaEroare("Format dată invalid: " + dataNasterii);
        }
        
        campEsteAdmin.setSelected(utilizator.getBoolean("admin"));
    }

    private LocalDate parseazaData(String data) {
        for (DateTimeFormatter format : FORMATE_DATA) {
            try {
                return LocalDate.parse(data, format);
            } catch (DateTimeParseException e) {
                jurnal.warning("Eroare la parsarea datei: " + data);
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
            JSONObject dateActualizate = construiesteDateActualizate();
            String url = URL_SERVER + idUtilizator;
            jurnal.info("Trimitere actualizare la: " + url);
            jurnal.info("Date actualizate: " + dateActualizate);

            clientHttp = (HttpURLConnection) new URL(url).openConnection();
            configureazaConexiune(clientHttp);
            trimiteDateActualizate(clientHttp, dateActualizate);
            proceseazaRaspunsActualizare(clientHttp);

        } catch (Exception e) {
            jurnal.log(Level.SEVERE, "Eroare la salvarea datelor", e);
            afiseazaEroare("Eroare la salvarea datelor: " + e.getMessage());
        } finally {
            if (clientHttp != null) {
                clientHttp.disconnect();
            }
        }
    }

    private JSONObject construiesteDateActualizate() {
        JSONObject dateActualizate = new JSONObject();
        dateActualizate.put("username", campNumeUtilizator.getText().trim());
        dateActualizate.put("parola", campParola.getText().trim());
        dateActualizate.put("email", campEmail.getText().trim());
        dateActualizate.put("nume", campNume.getText().trim());
        dateActualizate.put("prenume", campPrenume.getText().trim());
        dateActualizate.put("dataNasterii", campDataNasterii.getValue().format(FORMATE_DATA[1]));
        dateActualizate.put("admin", campEsteAdmin.isSelected());
        return dateActualizate;
    }

    private void configureazaConexiune(HttpURLConnection clientHttp) throws Exception {
        clientHttp.setRequestMethod("PUT");
        clientHttp.setRequestProperty("Content-Type", "application/json");
        clientHttp.setDoOutput(true);
        clientHttp.setConnectTimeout(5000);
        clientHttp.setReadTimeout(5000);
    }

    private void trimiteDateActualizate(HttpURLConnection clientHttp, JSONObject dateActualizate) throws Exception {
        try (OutputStream os = clientHttp.getOutputStream()) {
            byte[] date = dateActualizate.toString().getBytes(StandardCharsets.UTF_8);
            os.write(date, 0, date.length);
        }
    }

    private void proceseazaRaspunsActualizare(HttpURLConnection clientHttp) throws Exception {
        int codRaspuns = clientHttp.getResponseCode();
        jurnal.info("Cod răspuns actualizare: " + codRaspuns);

        if (codRaspuns == HttpURLConnection.HTTP_OK) {
            afiseazaSucces("Datele utilizatorului au fost actualizate cu succes!");
            App.setRoot("paginaUseriAdmin");
        } else {
            jurnal.warning("Eroare la actualizare: " + codRaspuns);
            afiseazaEroare("Nu s-au putut actualiza datele. Cod răspuns: " + codRaspuns);
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
    private void revino() throws Exception {
        App.setRoot("paginaUseriAdmin");
    }

    private void afiseazaEroare(String mesaj) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(TITLU_EROARE);
        alerta.setHeaderText(TITLU_EROARE);
        alerta.setContentText(mesaj);
        alerta.showAndWait();
    }

    private void afiseazaSucces(String mesaj) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(TITLU_SUCCES);
        alerta.setHeaderText(TITLU_SUCCES);
        alerta.setContentText(mesaj);
        alerta.showAndWait();
    }
}
