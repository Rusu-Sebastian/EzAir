package com.proiect;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class PaginaCreareZborController {
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

        App.setRoot("paginaZboruriAdmin");

        
    }



}
