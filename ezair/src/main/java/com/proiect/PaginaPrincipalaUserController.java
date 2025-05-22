package com.proiect;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PaginaPrincipalaUserController {
    private static final Logger logger = Logger.getLogger(PaginaPrincipalaUserController.class.getName());
    private static final String SERVER_BASE_URL = "http://localhost:3000";
    
    @FXML private ComboBox<String> comboOrigine;
    @FXML private ComboBox<String> comboDestinatie;
    @FXML private DatePicker dataPlecare;
    @FXML private Label labelNumeUtilizator;
    
    @FXML private TableView<Zbor> tabelZboruri;
    @FXML private TableColumn<Zbor, String> coloanaOrigine;
    @FXML private TableColumn<Zbor, String> coloanaDestinatie;
    @FXML private TableColumn<Zbor, String> coloanaDataPlecare;
    @FXML private TableColumn<Zbor, String> coloanaOraPlecare;
    @FXML private TableColumn<Zbor, String> coloanaDataSosire;
    @FXML private TableColumn<Zbor, String> coloanaOraSosire;
    @FXML private TableColumn<Zbor, Double> coloanaPret;
    @FXML private TableColumn<Zbor, Integer> coloanaLocuriLibere;
    
    @FXML
    private void initialize() {
        configureazaTabele();
        incarcaOrase();
        afiseazaNumeUtilizator();
    }
    
    private void configureazaTabele() {
        coloanaOrigine.setCellValueFactory(new PropertyValueFactory<>("origine"));
        coloanaDestinatie.setCellValueFactory(new PropertyValueFactory<>("destinatie"));
        coloanaDataPlecare.setCellValueFactory(new PropertyValueFactory<>("dataPlecare"));
        coloanaOraPlecare.setCellValueFactory(new PropertyValueFactory<>("oraPlecare"));
        coloanaDataSosire.setCellValueFactory(new PropertyValueFactory<>("dataSosire"));
        coloanaOraSosire.setCellValueFactory(new PropertyValueFactory<>("oraSosire"));
        coloanaPret.setCellValueFactory(new PropertyValueFactory<>("pret"));
        coloanaLocuriLibere.setCellValueFactory(new PropertyValueFactory<>("locuriLibere"));
    }
    
    private void afiseazaNumeUtilizator() {
        String nume = (String) App.getUserData().get("nume");
        String prenume = (String) App.getUserData().get("prenume");
        labelNumeUtilizator.setText(nume + " " + prenume);
    }
    
    private void incarcaOrase() {
        try {
            java.net.URI uri = new java.net.URI(SERVER_BASE_URL + "/zboruri/orase");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JSONArray orase = new JSONArray(response);
                ObservableList<String> listaOrase = FXCollections.observableArrayList();
                
                for (int i = 0; i < orase.length(); i++) {
                    listaOrase.add(orase.getString(i));
                }
                
                comboOrigine.setItems(listaOrase);
                comboDestinatie.setItems(listaOrase);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Eroare la încărcarea orașelor", e);
            afiseazaEroare("Nu s-au putut încărca orașele disponibile.");
        }
    }
    
    @FXML
    private void cautaZboruri() {
        if (!validareDate()) {
            return;
        }
        
        try {
            String origine = comboOrigine.getValue();
            String destinatie = comboDestinatie.getValue();
            LocalDate dataP = dataPlecare.getValue();
            
            String url = String.format("%s/zboruri/cautare?origine=%s&destinatie=%s&data=%s", 
                SERVER_BASE_URL, origine, destinatie, dataP.toString());
            
            java.net.URI uri = new java.net.URI(url);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JSONArray zboruri = new JSONArray(response);
                ObservableList<Zbor> listaZboruri = FXCollections.observableArrayList();
                
                for (int i = 0; i < zboruri.length(); i++) {
                    JSONObject zbor = zboruri.getJSONObject(i);
                    Zbor z = new Zbor.Builder()
                        .setId(zbor.getString("id"))
                        .setOrigine(zbor.getString("origine"))
                        .setDestinatie(zbor.getString("destinatie"))
                        .setDataPlecare(zbor.getString("dataPlecare"))
                        .setOraPlecare(zbor.getString("oraPlecare"))
                        .setDataSosire(zbor.getString("dataSosire"))
                        .setOraSosire(zbor.getString("oraSosire"))
                        .setModelAvion(zbor.getString("modelAvion"))
                        .setLocuriLibere(zbor.getInt("locuriLibere"))
                        .setPret(zbor.getDouble("pret"))
                        .build();
                    listaZboruri.add(z);
                }
                
                tabelZboruri.setItems(listaZboruri);
                if (listaZboruri.isEmpty()) {
                    afiseazaInfo("Nu au fost găsite zboruri pentru criteriile selectate.");
                }
            } else {
                afiseazaEroare("A apărut o eroare la căutarea zborurilor. Te rugăm să încerci din nou.");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Eroare la căutarea zborurilor", e);
            afiseazaEroare("Nu s-au putut căuta zborurile. Te rugăm să încerci din nou.");
        }
    }
    
    private boolean validareDate() {
        if (comboOrigine.getValue() == null || comboOrigine.getValue().trim().isEmpty()) {
            afiseazaEroare("Te rugăm să selectezi orașul de origine.");
            return false;
        }
        
        if (comboDestinatie.getValue() == null || comboDestinatie.getValue().trim().isEmpty()) {
            afiseazaEroare("Te rugăm să selectezi orașul de destinație.");
            return false;
        }
        
        if (dataPlecare.getValue() == null) {
            afiseazaEroare("Te rugăm să selectezi data plecării.");
            return false;
        }

        if (comboOrigine.getValue().equals(comboDestinatie.getValue())) {
            afiseazaEroare("Orașul de origine nu poate fi același cu destinația.");
            return false;
        }

        if (dataPlecare.getValue().isBefore(LocalDate.now())) {
            afiseazaEroare("Data plecării nu poate fi în trecut.");
            return false;
        }

        return true;
    }

    @FXML
    private void rezervaBilet() {
        Zbor zborSelectat = tabelZboruri.getSelectionModel().getSelectedItem();
        if (zborSelectat == null) {
            afiseazaEroare("Te rugăm să selectezi un zbor pentru rezervare.");
            return;
        }

        if (zborSelectat.getLocuriLibere() <= 0) {
            afiseazaEroare("Ne pare rău, nu mai sunt locuri disponibile pentru acest zbor.");
            return;
        }
        
        try {
            String userId = (String) App.getUserData().get("userId");
            if (userId == null) {
                afiseazaEroare("Te rugăm să te autentifici pentru a rezerva un bilet.");
                return;
            }

            java.net.URI uri = new java.net.URI(SERVER_BASE_URL + "/bilete/cumpara");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            JSONObject bilet = new JSONObject();
            bilet.put("userId", userId);
            bilet.put("zborId", zborSelectat.getId());
            bilet.put("detaliiZbor", String.format("%s -> %s", zborSelectat.getOrigine(), zborSelectat.getDestinatie()));
            bilet.put("dataZbor", zborSelectat.getDataPlecare() + "T" + zborSelectat.getOraPlecare());
            bilet.put("pret", zborSelectat.getPret());
            
            try (var os = conn.getOutputStream()) {
                os.write(bilet.toString().getBytes(StandardCharsets.UTF_8));
            }
            
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                afiseazaSucces("Biletul a fost rezervat cu succes!");
                cautaZboruri(); // Reîmprospătează lista de zboruri
            } else {
                afiseazaEroare("Nu s-a putut rezerva biletul. Te rugăm să încerci din nou.");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Eroare la rezervarea biletului", e);
            afiseazaEroare("Nu s-a putut rezerva biletul. Te rugăm să încerci din nou.");
        }
    }
    
    @FXML
    private void contulMeu() throws Exception {
        App.setRoot("paginaContClient");
    }
    
    @FXML
    private void deconectare() throws Exception {
        App.getUserData().clear();
        App.setRoot("login");
    }
    
    private void afiseazaEroare(String mesaj) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eroare");
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
    
    private void afiseazaSucces(String mesaj) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succes");
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
    
    private void afiseazaInfo(String mesaj) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informație");
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
}
