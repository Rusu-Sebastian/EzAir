package com.proiect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Platform;
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
    
    // Field constants to avoid duplication
    private static final String CAMP_NUME_UTILIZATOR = "numeUtilizator";
    private static final String CAMP_PAROLA = "parola";
    private static final String CAMP_EMAIL = "email";
    private static final String CAMP_NUME = "nume";
    private static final String CAMP_PRENUME = "prenume";
    private static final String CAMP_DATA_NASTERII = "dataNasterii";
    private static final String CAMP_ESTE_ADMIN = "esteAdmin";
    private static final String CHEIE_ID_UTILIZATOR = "userId";
    private static final String CHEIE_ADMIN = "esteAdmin";
    
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
    @FXML private CheckBox campAdmin;
    
    private String idUtilizator;
    private App app;
    
    @FXML
    public void initialize() {
        app = App.getInstance();
        idUtilizator = (String) App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
        jurnal.log(Level.INFO, "ID Utilizator pentru editare: {0}", idUtilizator);
        
        if (idUtilizator == null || idUtilizator.isEmpty()) {
            afiseazaEroare("Nu s-a putut găsi utilizatorul pentru editare.");
            return;
        }

        // Verifică dacă checkbox-ul a fost injectat corect
        if (campAdmin == null) {
            jurnal.warning("Checkbox-ul campAdmin nu a fost inițializat prin FXML");
            afiseazaEroare("Eroare la inițializarea formularului.");
            return;
        }

        incarcaDateUtilizator();
    }

    private void incarcaDateUtilizator() {
        HttpURLConnection clientHttp = null;
        try {
            String url = URL_SERVER + idUtilizator;
            jurnal.log(Level.INFO, "Încercare încărcare utilizator de la: {0}", url);
            
            clientHttp = (HttpURLConnection) URI.create(url).toURL().openConnection();
            clientHttp.setRequestMethod("GET");
            clientHttp.setConnectTimeout(5000);
            clientHttp.setReadTimeout(5000);

            int codRaspuns = clientHttp.getResponseCode();
            jurnal.log(Level.INFO, "Cod răspuns server: {0}", codRaspuns);

            switch (codRaspuns) {
                case HttpURLConnection.HTTP_OK:
                    proceseazaRaspunsServer(clientHttp);
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    jurnal.log(Level.WARNING, "Utilizatorul nu a fost găsit: {0}", idUtilizator);
                    afiseazaEroare("Utilizatorul nu a fost găsit.");
                    break;
                default:
                    jurnal.log(Level.WARNING, "Eroare server: {0}", codRaspuns);
                    afiseazaEroare("Eroare la încărcarea datelor. Cod răspuns: " + codRaspuns);
                    break;
            }
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la încărcarea datelor utilizatorului", e);
            afiseazaEroare("Eroare la încărcarea datelor: " + e.getMessage());
        } catch (SecurityException e) {
            jurnal.log(Level.SEVERE, "Eroare de securitate la încărcarea datelor", e);
            afiseazaEroare("Eroare de securitate: " + e.getMessage());
        } finally {
            if (clientHttp != null) {
                clientHttp.disconnect();
            }
        }
    }

    private void proceseazaRaspunsServer(HttpURLConnection clientHttp) throws IOException {
        try (InputStream is = clientHttp.getInputStream()) {
            byte[] dateRaspuns = is.readAllBytes();
            String raspuns = new String(dateRaspuns, StandardCharsets.UTF_8);
            jurnal.log(Level.INFO, "Răspuns server: {0}", raspuns);
            
            JSONObject utilizator = new JSONObject(raspuns);
            actualizeazaCampuriFormular(utilizator);
        }
    }

    private void actualizeazaCampuriFormular(JSONObject utilizator) {
        try {
            // Verify required fields are present
            String[] campuriObligatorii = {CAMP_NUME_UTILIZATOR, CAMP_PAROLA, CAMP_EMAIL, CAMP_NUME, CAMP_PRENUME, CAMP_DATA_NASTERII};
            for (String camp : campuriObligatorii) {
                if (!utilizator.has(camp)) {
                    jurnal.log(Level.WARNING, "Câmp obligatoriu lipsă: {0}", camp);
                    Platform.runLater(() -> afiseazaEroare("Date incomplete pentru utilizator. Lipsește: " + camp));
                    return;
                }
            }

            // Handle date parsing before UI updates
            String dataNasterii = utilizator.getString(CAMP_DATA_NASTERII);
            LocalDate data = parseazaData(dataNasterii);
            if (data == null) {
                jurnal.log(Level.WARNING, "Format dată invalid: {0}", dataNasterii);
                Platform.runLater(() -> afiseazaEroare("Format dată naștere invalid: " + dataNasterii));
                return;
            }

            // Perform all UI updates on the JavaFX thread
            Platform.runLater(() -> {
                try {
                    campNumeUtilizator.setText(utilizator.getString(CAMP_NUME_UTILIZATOR));
                    campParola.setText(utilizator.getString(CAMP_PAROLA));
                    campEmail.setText(utilizator.getString(CAMP_EMAIL));
                    campNume.setText(utilizator.getString(CAMP_NUME));
                    campPrenume.setText(utilizator.getString(CAMP_PRENUME));
                    campDataNasterii.setValue(data);
                    if (campAdmin != null) {
                        campAdmin.setSelected(utilizator.optBoolean(CAMP_ESTE_ADMIN, false));
                    } else {
                        jurnal.warning("campAdmin nu a fost inițializat corect");
                    }
                } catch (JSONException e) {
                    jurnal.log(Level.SEVERE, "Eroare la citirea datelor JSON", e);
                    afiseazaEroare("Eroare la citirea datelor: " + e.getMessage());
                } catch (NullPointerException e) {
                    jurnal.log(Level.SEVERE, "Câmp UI neinițializat", e);
                    afiseazaEroare("Eroare la inițializarea formularului: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    jurnal.log(Level.SEVERE, "Valoare invalidă în formular", e);
                    afiseazaEroare("Eroare validare date: " + e.getMessage());
                }
            });
        } catch (JSONException e) {
            jurnal.log(Level.WARNING, "Eroare la citirea datelor din JSON", e);
            Platform.runLater(() -> afiseazaEroare("Eroare la citirea datelor: " + e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            jurnal.log(Level.WARNING, "Eroare la actualizarea câmpurilor formularului", e);
            Platform.runLater(() -> afiseazaEroare("Eroare la completarea formularului: " + e.getMessage()));
        }
    }

    private LocalDate parseazaData(String data) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }

        for (DateTimeFormatter format : FORMATE_DATA) {
            try {
                return LocalDate.parse(data, format);
            } catch (DateTimeParseException e) {
                jurnal.log(Level.FINE, "Încercare eșuată pentru formatul: {0}", format);
            }
        }
        jurnal.log(Level.WARNING, "Nu s-a putut parsa data: {0}", data);
        return null;
    }

    @SuppressWarnings("unused")
    @FXML
    private void salveazaUtilizator() {
        if (!valideazaDate()) {
            return;
        }

        HttpURLConnection clientHttp = null;
        try {
            JSONObject dateActualizate = construiesteDateActualizate();
            String url = URL_SERVER + idUtilizator;
            jurnal.log(Level.INFO, "Trimitere actualizare la: {0}", new Object[]{url});
            jurnal.log(Level.INFO, "Date actualizate: {0}", dateActualizate);

            clientHttp = (HttpURLConnection) URI.create(url).toURL().openConnection();
            configureazaConexiune(clientHttp);
            trimiteDateActualizate(clientHttp, dateActualizate);
            proceseazaRaspunsActualizare(clientHttp);

        } catch (IOException | RuntimeException e) {
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
        dateActualizate.put(CAMP_NUME_UTILIZATOR, campNumeUtilizator.getText().trim());
        dateActualizate.put(CAMP_PAROLA, campParola.getText().trim());
        dateActualizate.put(CAMP_EMAIL, campEmail.getText().trim());
        dateActualizate.put(CAMP_NUME, campNume.getText().trim());
        dateActualizate.put(CAMP_PRENUME, campPrenume.getText().trim());
        dateActualizate.put(CAMP_DATA_NASTERII, campDataNasterii.getValue().format(DateTimeFormatter.ofPattern(FORMAT_DATA_AFISARE)));
        dateActualizate.put(CAMP_ESTE_ADMIN, campAdmin.isSelected());
        return dateActualizate;
    }

    private void configureazaConexiune(HttpURLConnection clientHttp) throws IOException {
        clientHttp.setRequestMethod("PUT");
        clientHttp.setRequestProperty("Content-Type", "application/json");
        clientHttp.setDoOutput(true);
        clientHttp.setConnectTimeout(5000);
        clientHttp.setReadTimeout(5000);
    }

    private void trimiteDateActualizate(HttpURLConnection clientHttp, JSONObject dateActualizate) throws IOException {
        try (OutputStream os = clientHttp.getOutputStream()) {
            byte[] date = dateActualizate.toString().getBytes(StandardCharsets.UTF_8);
            os.write(date, 0, date.length);
        }
    }

    private void proceseazaRaspunsActualizare(HttpURLConnection clientHttp) throws IOException {
        int codRaspuns = clientHttp.getResponseCode();
        jurnal.log(Level.INFO, "Cod răspuns actualizare: {0}", codRaspuns);

        if (codRaspuns == HttpURLConnection.HTTP_OK) {
            afiseazaSucces("Datele utilizatorului au fost actualizate cu succes!");
            
            // Păstrăm credențialele de admin înainte de a naviga înapoi
            String userId = (String) App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
            String esteAdmin = (String) App.getDateUtilizator().get(CHEIE_ADMIN);
            
            // Curățăm datele vechi dar păstrăm datele de autentificare
            App.getDateUtilizator().clear();
            
            // Restaurăm credențialele
            if (userId != null) {
                App.getDateUtilizator().put(CHEIE_ID_UTILIZATOR, userId);
            }
            if (esteAdmin != null) {
                App.getDateUtilizator().put(CHEIE_ADMIN, esteAdmin);
            }
            
            app.setRoot("paginaUseriAdmin");
        } else {
            jurnal.log(Level.WARNING, "Eroare la actualizare: {0}", codRaspuns);
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

    @SuppressWarnings("unused")
    @FXML
    private void revino() {
        try {
            // Păstrăm credențialele de admin înainte de a naviga înapoi
            String userId = (String) App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
            String esteAdmin = (String) App.getDateUtilizator().get(CHEIE_ADMIN);
            
            // Curățăm datele vechi dar păstrăm datele de autentificare
            App.getDateUtilizator().clear();
            
            // Restaurăm credențialele
            if (userId != null) {
                App.getDateUtilizator().put(CHEIE_ID_UTILIZATOR, userId);
            }
            if (esteAdmin != null) {
                App.getDateUtilizator().put(CHEIE_ADMIN, esteAdmin);
            }
            
            app.setRoot("paginaUseriAdmin");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la revenirea la pagina anterioară", e);
            afiseazaEroare("Nu s-a putut reveni la pagina anterioară");
        }
    }
}
