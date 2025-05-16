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
    private TableColumn<Zbor, String> coloanaDataPlecare;
    @FXML
    private TableColumn<Zbor, String> coloanaOraPlecare;
    @FXML
    private TableColumn<Zbor, String> coloanaDataSosire;
    @FXML
    private TableColumn<Zbor, String> coloanaOraSosire;
    @FXML
    private TableColumn<Zbor, String> coloanaModelAvion;
    @FXML
    private TableColumn<Zbor, Integer> coloanaLocuriLibere;
    @FXML
    private TableColumn<Zbor, Double> coloanaPret;
    @FXML
    private TableColumn<Zbor, String> coloanaId;
    @FXML
    private void adaugareZbor() throws Exception {
        App.setRoot("creareZbor");
    }

    @FXML
    private void backZbor() throws Exception {
        App.setRoot("paginaZboruriAdmin");
    }

    @FXML
    private void finalizareAdaugareZbor() throws Exception {
        String origine = ((javafx.scene.control.TextField) App.scene.lookup("#origine")).getText();
        String destinatie = ((javafx.scene.control.TextField) App.scene.lookup("#destinatie")).getText();
        String modelAvion = ((javafx.scene.control.TextField) App.scene.lookup("#modelAvion")).getText();
        String pret = ((javafx.scene.control.TextField) App.scene.lookup("#pret")).getText();

        if(origine.isEmpty() || destinatie.isEmpty() || modelAvion.isEmpty() || pret.isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#dataPlecare1")).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#dataPlecare2")).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#dataPlecare3")).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#oraPlecare1")).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#oraPlecare2")).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#dataSosire1")).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#dataSosire2")).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#dataSosire3")).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#oraSosire1")).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#oraSosire2")).getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenție");
            alert.setHeaderText("Toate câmpurile sunt obligatorii");
            alert.setContentText("Te rog să completezi toate câmpurile.");
            alert.showAndWait();
            return;
        }
        int locuriLibere = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#locuriLibere")).getText());
        int dataPlecare1 = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#dataPlecare1")).getText());
        int dataPlecare2 = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#dataPlecare2")).getText());
        int dataPlecare3 = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#dataPlecare3")).getText());
        int oraPlecare1 = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#oraPlecare1")).getText());
        int oraPlecare2 = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#oraPlecare2")).getText());
        int dataSosire1 = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#dataSosire1")).getText());
        int dataSosire2 = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#dataSosire2")).getText());
        int dataSosire3 = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#dataSosire3")).getText());
        int oraSosire1 = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#oraSosire1")).getText());
        int oraSosire2 = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#oraSosire2")).getText());

        int currentYear = java.time.Year.now().getValue();
        int currentMonth = java.time.Month.from(java.time.LocalDate.now()).getValue();
        int currentDay = java.time.LocalDate.now().getDayOfMonth();
        if (dataPlecare1 < currentDay && dataPlecare2 == currentMonth && dataPlecare3 == currentYear) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenție");
            alert.setHeaderText("Data plecării nu poate fi în trecut");
            alert.setContentText("Te rog să alegi o dată de plecare validă.");
            alert.showAndWait();
            return;
        }
        if (dataSosire1 < currentDay && dataSosire2 == currentMonth && dataSosire3 == currentYear) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenție");
            alert.setHeaderText("Data sosirii nu poate fi în trecut");
            alert.setContentText("Te rog să alegi o dată de sosire validă.");
            alert.showAndWait();
            return;
        }
        if (locuriLibere < 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenție");
            alert.setHeaderText("Numărul de locuri libere nu poate fi negativ");
            alert.setContentText("Te rog să introduci un număr valid de locuri libere.");
            alert.showAndWait();
            return;
        }
        if (Double.parseDouble(pret) < 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenție");
            alert.setHeaderText("Prețul nu poate fi negativ");
            alert.setContentText("Te rog să introduci un preț valid.");
            alert.showAndWait();
            return;
        }

        String dataPlecare = dataPlecare1 + "/" + dataPlecare2 + "/" + dataPlecare3;
        String oraPlecare = oraPlecare1 + ":" + oraPlecare2;
        String dataSosire = dataSosire1 + "/" + dataSosire2 + "/" + dataSosire3;
        String oraSosire = oraSosire1 + ":" + oraSosire2;

        String url = "http://localhost:3000/zboruri/adaugareZbor";
        String jsonInputString = String.format(
                "{\"origine\": \"%s\", \"destinatie\": \"%s\", \"dataPlecare\": \"%s\", \"oraPlecare\": \"%s\", \"dataSosire\": \"%s\", \"oraSosire\": \"%s\", \"modelAvion\": \"%s\", \"locuriLibere\": %d, \"pret\": %.2f}",
                origine, destinatie, dataPlecare, oraPlecare, dataSosire, oraSosire, modelAvion, locuriLibere, Double.parseDouble(pret));
        HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
        httpClient.setRequestMethod("POST");
        httpClient.setRequestProperty("Content-Type", "application/json");
        httpClient.setDoOutput(true);
        httpClient.getOutputStream().write(jsonInputString.getBytes(StandardCharsets.UTF_8));
        int responseCode = httpClient.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Afișează un pop-up pentru confirmarea adăugării
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succes");
            alert.setHeaderText("Zbor adăugat cu succes");
            alert.setContentText("Zborul a fost adăugat cu succes în sistem.");
            alert.showAndWait();
        } else {
            // Afișează un pop-up pentru eroare
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Eroare");
            alert.setHeaderText("Eroare la adăugarea zborului");
            alert.setContentText("A apărut o eroare la adăugarea zborului. Te rog să încerci din nou.");
            alert.showAndWait();
        }
        // Reîncarcă datele din tabel
        populeazaTabelZboruri();
        App.setRoot("paginaZboruriAdmin");

        
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
        String url = "http://localhost:3000/zboruri/stergeZbor/" + idZbor;
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
        coloanaDataPlecare.setCellValueFactory(new PropertyValueFactory<>("dataPlecare"));
        coloanaOraPlecare.setCellValueFactory(new PropertyValueFactory<>("oraPlecare"));
        coloanaDataSosire.setCellValueFactory(new PropertyValueFactory<>("dataSosire"));
        coloanaOraSosire.setCellValueFactory(new PropertyValueFactory<>("oraSosire"));
        coloanaModelAvion.setCellValueFactory(new PropertyValueFactory<>("modelAvion"));
        coloanaLocuriLibere.setCellValueFactory(new PropertyValueFactory<>("locuriLibere"));
        coloanaPret.setCellValueFactory(new PropertyValueFactory<>("pret"));
        coloanaId.setCellValueFactory(new PropertyValueFactory<>("idZbor"));

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
                Zbor zbor = new Zbor(
                        jsonObject.getString("origine"),
                        jsonObject.getString("destinatie"),
                        jsonObject.getString("dataPlecare"),
                        jsonObject.getString("oraPlecare"),
                        jsonObject.getString("dataSosire"),
                        jsonObject.getString("oraSosire"),
                        jsonObject.getString("modelAvion"),
                        jsonObject.getInt("locuriLibere"),
                        jsonObject.getDouble("pret"),
                        jsonObject.getString("idZbor")
                );
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
        App.setRoot("paginaZboruriAdmin");
    }

}
