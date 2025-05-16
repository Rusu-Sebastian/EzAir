package com.proiect;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PaginaZboruriAdminController {
    
    @FXML
    private TableView<Zbor> tabelZboruri;
    @FXML
    private TableColumn<Zbor, String> coloanaOrigine;
    @FXML
    private TableColumn<Zbor, String> coloanaDestinatie;
    @FXML
    private TableColumn<Zbor, String> coloanaModelAvion;
    @FXML
    private TableColumn<Zbor, Integer> coloanaLocuriLibere;
    @FXML
    private TableColumn<Zbor, Double> coloanaPret;
    @FXML
    private TableColumn<Zbor, String> coloanaId;
    @FXML
    private TableColumn<Zbor, String> coloanaDataPlecarii;
    @FXML
    private TableColumn<Zbor, String> coloanaOraPlecarii;
    @FXML
    private TableColumn<Zbor, String> coloanaDataSosirii;
    @FXML
    private TableColumn<Zbor, String> coloanaOraSosirii;

    @FXML
    private void adaugareZbor() throws Exception {
        App.setRoot("creareZbor");
    }

    @FXML
    private void editareZbor() throws Exception {
        // Zbor zborSelectat = tabelZboruri.getSelectionModel().getSelectedItem();
        // if (zborSelectat == null) {
        //     // Afișează un pop-up pentru utilizatorul neales
        //     Alert alert = new Alert(Alert.AlertType.WARNING);
        //     alert.setTitle("Atenție");
        //     alert.setHeaderText("Niciun zbor selectat");
        //     alert.setContentText("Te rog să selectezi un zbor din tabel pentru a-l edita.");
        //     alert.showAndWait();
        //     return;
        // }
        // App.setRoot("editareZbor");
    }
    @FXML
    private void deleteZbor() throws Exception {
        Zbor zborSelectat = tabelZboruri.getSelectionModel().getSelectedItem();
        if (zborSelectat == null) {
            // Afișează un pop-up pentru utilizatorul neales
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenție");
            alert.setHeaderText("Niciun zbor selectat");
            alert.setContentText("Te rog să selectezi un zbor din tabel pentru a-l șterge.");
            alert.showAndWait();
            return;
        }
        
        String idZbor = zborSelectat.getId();
        String url = "http://localhost:3000/zboruri/stergereZbor/" + idZbor;
        HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
        httpClient.setRequestMethod("DELETE");
        int responseCode = httpClient.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Afișează un pop-up pentru confirmarea ștergerii
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succes");
            alert.setHeaderText("Zbor șters cu succes");
            alert.setContentText("Zborul a fost șters cu succes din sistem.");
            alert.showAndWait();
        } else {
            // Afișează un pop-up pentru eroare
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Eroare");
            alert.setHeaderText("Eroare la ștergerea zborului");
            alert.setContentText("A apărut o eroare la ștergerea zborului. Te rog să încerci din nou.");
            alert.showAndWait();
        }
        // Reîncarcă datele din tabel
        populeazaTabelZboruri();
        
    }
    @FXML
    private void initialize() {
        coloanaOrigine.setCellValueFactory(new PropertyValueFactory<>("origine"));
        coloanaDestinatie.setCellValueFactory(new PropertyValueFactory<>("destinatie"));
        coloanaModelAvion.setCellValueFactory(new PropertyValueFactory<>("modelAvion"));
        coloanaLocuriLibere.setCellValueFactory(new PropertyValueFactory<>("locuriLibere"));
        coloanaPret.setCellValueFactory(new PropertyValueFactory<>("pret"));
        coloanaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        coloanaDataPlecarii.setCellValueFactory(new PropertyValueFactory<>("dataPlecare"));
        coloanaOraPlecarii.setCellValueFactory(new PropertyValueFactory<>("oraPlecare"));
        coloanaDataSosirii.setCellValueFactory(new PropertyValueFactory<>("dataSosire"));
        coloanaOraSosirii.setCellValueFactory(new PropertyValueFactory<>("oraSosire"));

        // Populează tabelul cu datele din API
        populeazaTabelZboruri();
    }
    private void populeazaTabelZboruri() {
        try {
            String url = "http://localhost:3000/zboruri/populareLista";
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            httpClient.setRequestMethod("GET");
            httpClient.setRequestProperty("Accept", "application/json");

            if (httpClient.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + httpClient.getResponseCode());
            }

            InputStream inputStream = httpClient.getInputStream();
            String jsonString = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
            JSONArray jsonArray = new JSONArray(jsonString);

            ObservableList<Zbor> zboruriList = FXCollections.observableArrayList();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Zbor zbor = new Zbor.Builder()
                        .setOrigine(jsonObject.getString("origine"))
                        .setDestinatie(jsonObject.getString("destinatie"))
                        .setDataPlecare(jsonObject.getString("dataPlecare"))
                        .setOraPlecare(jsonObject.getString("oraPlecare"))
                        .setDataSosire(jsonObject.getString("dataSosire"))
                        .setOraSosire(jsonObject.getString("oraSosire"))
                        .setModelAvion(jsonObject.getString("modelAvion"))
                        .setLocuriLibere(jsonObject.getInt("locuriLibere"))
                        .setPret(jsonObject.getDouble("pret"))
                        .setId(jsonObject.getString("id"))
                        .build();
                zboruriList.add(zbor);
            }

            tabelZboruri.setItems(zboruriList);
            httpClient.disconnect();
        } catch (Exception e) {
                    // Log the exception or show an alert to the user
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Eroare");
                    alert.setHeaderText("Eroare la încărcarea datelor");
                    alert.setContentText("A apărut o eroare la încărcarea datelor. Te rog să încerci din nou.");
                    alert.showAndWait();
            }
        }
    @FXML
    private void reload() throws Exception {
        initialize();
    }

    @FXML
    private void backToDashboard() throws Exception {
        App.setRoot("paginaPrincipalaAdmin");
    }
}
