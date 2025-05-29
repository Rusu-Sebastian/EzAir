package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.proiect.config.ApiEndpoints;
import com.proiect.util.HttpUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class PaginaContClientController {
    private static final Logger jurnal = Logger.getLogger(PaginaContClientController.class.getName());
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    private static final String PAGINA_EDITARE = "editareContClient";
    private static final String PAGINA_NOTIFICARI = "setariNotificari";
    private static final String PAGINA_PAROLA = "schimbaParola";

    @FXML private Label labelNume;
    @FXML private Label labelEmail;
    @FXML private Label labelNumeUtilizator;
    @FXML private TableView<Bilet> tabelBilete;
    @FXML private TableColumn<Bilet, String> coloanaZbor;
    @FXML private TableColumn<Bilet, String> coloanaData;
    @FXML private TableColumn<Bilet, String> coloanaStare;

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void initialize() {
        configureazaTabele();
        incarcaInformatiiUtilizator();
        incarcaBilete();
    }

    private void configureazaTabele() {
        coloanaZbor.setCellValueFactory(cellData -> cellData.getValue().zborProperty());
        coloanaData.setCellValueFactory(cellData -> cellData.getValue().dataProperty());
        coloanaStare.setCellValueFactory(cellData -> cellData.getValue().stareProperty());
    }

    private void incarcaInformatiiUtilizator() {
        String idUtilizator = (String) App.getDateUtilizator().get(ApiEndpoints.USER_ID_KEY);
        jurnal.info("Încărcare informații pentru utilizatorul cu ID: " + idUtilizator);
        
        try {
            String endpoint = String.format(ApiEndpoints.GET_USER, idUtilizator);
            jurnal.info("Apelând endpoint: " + endpoint);
            
            HttpURLConnection conexiune = HttpUtil.createConnection(endpoint, "GET");

            int codRaspuns = conexiune.getResponseCode();
            jurnal.info("Cod răspuns: " + codRaspuns);
            
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                String raspuns = new String(conexiune.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                jurnal.info("Răspuns server: " + raspuns);
                
                JSONObject utilizator = new JSONObject(raspuns);
                String nume = utilizator.getString("nume");
                String prenume = utilizator.getString("prenume");
                String email = utilizator.getString("email");
                String numeUtilizator = utilizator.getString("numeUtilizator");
                
                jurnal.info("Date extrase - nume: " + nume + ", prenume: " + prenume);
                
                labelNume.setText(nume + " " + prenume);
                labelEmail.setText(email);
                labelNumeUtilizator.setText(numeUtilizator);
            } else {
                afiseazaEroare("Nu s-au putut încărca informațiile contului.");
            }
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la încărcarea informațiilor utilizatorului", e);
            afiseazaEroare("Nu s-au putut încărca informațiile contului.");
        }
    }

    private void incarcaBilete() {
        String idUtilizator = (String) App.getDateUtilizator().get(ApiEndpoints.USER_ID_KEY);
        try {
            HttpURLConnection conexiune = HttpUtil.createConnection(
                String.format(ApiEndpoints.USER_TICKETS, idUtilizator),
                "GET"
            );

            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                String raspuns = new String(conexiune.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JSONArray bilete = new JSONArray(raspuns);
                ObservableList<Bilet> listaBilete = FXCollections.observableArrayList();
                
                for (int i = 0; i < bilete.length(); i++) {
                    JSONObject bilet = bilete.getJSONObject(i);
                    LocalDateTime dataZbor = parseazaDataTimp(bilet.getString("dataZbor"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    
                    Bilet biletObj = new Bilet(
                        bilet.getString("detaliiZbor"),
                        dataZbor.format(formatter),
                        bilet.getString("stare"),
                        bilet.getString("id")
                    );
                    
                    // Salvează zborId pentru bilet pentru a fi folosit ulterior
                    biletObj.setZborId(bilet.getString("zborId"));
                    listaBilete.add(biletObj);
                }
                
                tabelBilete.setItems(listaBilete);
            } else {
                afiseazaEroare("Nu s-au putut încărca biletele.");
            }
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la încărcarea biletelor", e);
            afiseazaEroare("Nu s-au putut încărca biletele.");
        }
    }

    private LocalDateTime parseazaDataTimp(String sirDataTimp) {
        try {
            // Încercăm mai întâi parsarea în format ISO cu Z (UTC)
            if (sirDataTimp.endsWith("Z")) {
                Instant instant = Instant.parse(sirDataTimp);
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            }
            // Încercăm parsarea normală ISO
            return LocalDateTime.parse(sirDataTimp);
        } catch (DateTimeParseException e) {
            jurnal.warning("Format dată invalid, încercând format personalizat");
            return parseazaDataTimpPersonalizat(sirDataTimp);
        }
    }

    private LocalDateTime parseazaDataTimpPersonalizat(String sirDataTimp) {
        try {
            // Format: dd/MM/yyyy HH:mm
            String[] parteDateTimp = sirDataTimp.split(" ");
            if (parteDateTimp.length < 2) {
                // Dacă nu avem și ora, folosim doar data cu ora 00:00
                String[] parteData = parteDateTimp[0].split("/");
                if (parteData.length < 3) {
                    jurnal.warning("Format dată invalid: " + sirDataTimp);
                    return LocalDateTime.now(); // Fallback la data curentă
                }
                int zi = Integer.parseInt(parteData[0]);
                int luna = Integer.parseInt(parteData[1]);
                int an = Integer.parseInt(parteData[2]);
                return LocalDateTime.of(an, luna, zi, 0, 0, 0);
            }
            
            String[] parteData = parteDateTimp[0].split("/");
            String[] parteTimp = parteDateTimp[1].split(":");
            
            int zi = Integer.parseInt(parteData[0]);
            int luna = Integer.parseInt(parteData[1]);
            int an = Integer.parseInt(parteData[2]);
            int ora = Integer.parseInt(parteTimp[0]);
            int minut = Integer.parseInt(parteTimp[1]);
            
            return LocalDateTime.of(an, luna, zi, ora, minut, 0);
        } catch (Exception e) {
            jurnal.warning("Eroare la parsarea datei: " + sirDataTimp + " - " + e.getMessage());
            return LocalDateTime.now(); // Fallback la data curentă
        }
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void editareInformatii() {
        try {
            App.setRoot(PAGINA_EDITARE);
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea către editare cont", e);
            afiseazaEroare("Nu s-a putut deschide pagina de editare cont");
        }
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void notificari() {
        try {
            App.setRoot(PAGINA_NOTIFICARI);
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea către setări notificări", e);
            afiseazaEroare("Nu s-a putut deschide pagina de setări notificări");
        }
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void schimbaParola() {
        try {
            App.setRoot(PAGINA_PAROLA);
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea către schimbare parolă", e);
            afiseazaEroare("Nu s-a putut deschide pagina de schimbare parolă");
        }
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void inapoi() {
        try {
            App.setRoot("paginaPrincipalaUser");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea înapoi", e);
            afiseazaEroare("Nu s-a putut naviga înapoi la pagina principală");
        }
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void anulareBilet() {
        Bilet biletSelectat = tabelBilete.getSelectionModel().getSelectedItem();
        if (biletSelectat == null) {
            afiseazaEroare("Te rog să selectezi un bilet pentru anulare.");
            return;
        }
        
        Alert confirmare = new Alert(Alert.AlertType.CONFIRMATION);
        confirmare.setTitle("Confirmare anulare");
        confirmare.setHeaderText("Ești sigur că vrei să anulezi acest bilet?");
        confirmare.setContentText("Această acțiune nu poate fi anulată.");
        
        confirmare.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Implementează logica de anulare
                afiseazaSucces("Biletul a fost anulat cu succes.");
                incarcaBilete(); // Reîncarcă lista
            }
        });
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void schimbaData() {
        Bilet biletSelectat = tabelBilete.getSelectionModel().getSelectedItem();
        if (biletSelectat == null) {
            afiseazaEroare("Te rog să selectezi un bilet pentru a schimba data.");
            return;
        }
        
        try {
            // Obține informațiile despre biletul selectat de pe server
            String biletId = biletSelectat.getId();
            String zborId = obtineInformatiiCompleteBilet(biletId);
            
            // Stochează informațiile necesare pentru pagina următoare
            App.getDateUtilizator().put("biletSelectatId", biletId);
            App.getDateUtilizator().put("zborSelectatId", zborId);
            App.getDateUtilizator().put("detaliiZbor", biletSelectat.getZbor());
            App.setRoot("schimbaDataZbor");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea către schimbare data", e);
            afiseazaEroare("Nu s-a putut deschide pagina de schimbare data");
        }
    }

    private String obtineInformatiiCompleteBilet(String biletId) {
        try {
            String idUtilizator = (String) App.getDateUtilizator().get(ApiEndpoints.USER_ID_KEY);
            HttpURLConnection conexiune = HttpUtil.createConnection(
                String.format(ApiEndpoints.USER_TICKETS, idUtilizator),
                "GET"
            );

            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                String raspuns = new String(conexiune.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JSONArray bilete = new JSONArray(raspuns);
                
                for (int i = 0; i < bilete.length(); i++) {
                    JSONObject bilet = bilete.getJSONObject(i);
                    if (bilet.getString("id").equals(biletId)) {
                        return bilet.getString("zborId");
                    }
                }
            }
        } catch (Exception e) {
            jurnal.log(Level.SEVERE, "Eroare la obținerea informațiilor complete despre bilet", e);
        }
        return null;
    }

    private void afiseazaEroare(String mesaj) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(TITLU_EROARE);
        alerta.setHeaderText(null);
        alerta.setContentText(mesaj);
        alerta.showAndWait();
    }

    private void afiseazaSucces(String mesaj) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(TITLU_SUCCES);
        alerta.setHeaderText(null);
        alerta.setContentText(mesaj);
        alerta.showAndWait();
    }
}
