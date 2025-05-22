package com.proiect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class EditareZborController {
    @FXML
    private TextField origine;
    @FXML
    private TextField destinatie;
    @FXML
    private TextField dataPlecarii1;
    @FXML
    private TextField dataPlecarii2;
    @FXML
    private TextField dataPlecarii3;
    @FXML
    private TextField oraPlecarii1;
    @FXML
    private TextField oraPlecarii2;
    @FXML
    private TextField dataSosirii1;
    @FXML
    private TextField dataSosirii2;
    @FXML
    private TextField dataSosirii3;
    @FXML
    private TextField oraSosirii1;
    @FXML
    private TextField oraSosirii2;
    @FXML
    private TextField modelAvion;
    @FXML
    private TextField pret;
    @FXML
    private TextField locuriLibere;
    @FXML
    private TextField idZbor;

    @FXML
    private void initialize() {
        // Get data from App.userData
        Map<String, String> userData = App.getUserData();
        
        if (userData.containsKey("id")) {
            idZbor.setText(userData.get("id"));
            origine.setText(userData.get("origine"));
            destinatie.setText(userData.get("destinatie"));
            
            // Split date components (DD.MM.YYYY or DD/MM/YYYY)
            try {
                String dataPlecare = userData.get("dataPlecare");
                // Support both dot and slash separators
                String[] dataPlecareParts;
                if (dataPlecare.contains(".")) {
                    dataPlecareParts = dataPlecare.split("\\.");
                } else if (dataPlecare.contains("/")) {
                    dataPlecareParts = dataPlecare.split("/");
                } else {
                    dataPlecareParts = new String[0]; // Empty array if no supported separator found
                }
                
                if (dataPlecareParts.length == 3) {
                    dataPlecarii1.setText(dataPlecareParts[0]);
                    dataPlecarii2.setText(dataPlecareParts[1]);
                    dataPlecarii3.setText(dataPlecareParts[2]);
                } else {
                    // If it doesn't split correctly, show the full string in the first field
                    dataPlecarii1.setText(dataPlecare);
                }
            } catch (Exception e) {
                System.out.println("Error splitting dataPlecare: " + e.getMessage());
            }
            
            // Split time components (HH:MM)
            try {
                String oraPlecare = userData.get("oraPlecare");
                String[] oraPlecariParts = oraPlecare.split(":");
                if (oraPlecariParts.length == 2) {
                    oraPlecarii1.setText(oraPlecariParts[0]);
                    oraPlecarii2.setText(oraPlecariParts[1]);
                } else {
                    // If it doesn't split correctly, show the full string in the first field
                    oraPlecarii1.setText(oraPlecare);
                }
            } catch (Exception e) {
                System.out.println("Error splitting oraPlecare: " + e.getMessage());
            }
            
            // Split date components (DD.MM.YYYY or DD/MM/YYYY)
            try {
                String dataSosire = userData.get("dataSosire");
                // Support both dot and slash separators
                String[] dataSosireParts;
                if (dataSosire.contains(".")) {
                    dataSosireParts = dataSosire.split("\\.");
                } else if (dataSosire.contains("/")) {
                    dataSosireParts = dataSosire.split("/");
                } else {
                    dataSosireParts = new String[0]; // Empty array if no supported separator found
                }
                
                if (dataSosireParts.length == 3) {
                    dataSosirii1.setText(dataSosireParts[0]);
                    dataSosirii2.setText(dataSosireParts[1]);
                    dataSosirii3.setText(dataSosireParts[2]);
                } else {
                    // If it doesn't split correctly, show the full string in the first field
                    dataSosirii1.setText(dataSosire);
                }
            } catch (Exception e) {
                System.out.println("Error splitting dataSosire: " + e.getMessage());
            }
            
            // Split time components (HH:MM)
            try {
                String oraSosire = userData.get("oraSosire");
                String[] oraSosireParts = oraSosire.split(":");
                if (oraSosireParts.length == 2) {
                    oraSosirii1.setText(oraSosireParts[0]);
                    oraSosirii2.setText(oraSosireParts[1]);
                } else {
                    // If it doesn't split correctly, show the full string in the first field
                    oraSosirii1.setText(oraSosire);
                }
            } catch (Exception e) {
                System.out.println("Error splitting oraSosire: " + e.getMessage());
            }
            
            modelAvion.setText(userData.get("modelAvion"));
            locuriLibere.setText(userData.get("locuriLibere"));
            pret.setText(userData.get("pret"));
        }
    }

    @FXML
    private void finalizareEditareZbor() throws IOException {
        try {
            // Validate input fields
            if (dataPlecarii1.getText().isEmpty() || dataPlecarii2.getText().isEmpty() || dataPlecarii3.getText().isEmpty()) {
                showErrorAlert("Eroare la actualizarea zborului", "Te rog să completezi data plecării în format ZZ.LL.AAAA sau ZZ/LL/AAAA");
                return;
            }
            
            if (oraPlecarii1.getText().isEmpty() || oraPlecarii2.getText().isEmpty()) {
                showErrorAlert("Eroare la actualizarea zborului", "Te rog să completezi ora plecării în format HH:MM");
                return;
            }
            
            if (dataSosirii1.getText().isEmpty() || dataSosirii2.getText().isEmpty() || dataSosirii3.getText().isEmpty()) {
                showErrorAlert("Eroare la actualizarea zborului", "Te rog să completezi data sosirii în format ZZ.LL.AAAA sau ZZ/LL/AAAA");
                return;
            }
            
            if (oraSosirii1.getText().isEmpty() || oraSosirii2.getText().isEmpty()) {
                showErrorAlert("Eroare la actualizarea zborului", "Te rog să completezi ora sosirii în format HH:MM");
                return;
            }
            
            // Determine which date separator to use (use the same format as input or default to dot)
            String dateSeparator = ".";  // Default to dot format
            Map<String, String> userData = App.getUserData();
            if (userData.containsKey("dataPlecare") && userData.get("dataPlecare").contains("/")) {
                dateSeparator = "/";
            }
            
            // Format dates and times with appropriate separators
            String dataPlecare = dataPlecarii1.getText() + dateSeparator + dataPlecarii2.getText() + dateSeparator + dataPlecarii3.getText();
            String oraPlecare = oraPlecarii1.getText() + ":" + oraPlecarii2.getText();
            String dataSosire = dataSosirii1.getText() + dateSeparator + dataSosirii2.getText() + dateSeparator + dataSosirii3.getText();
            String oraSosire = oraSosirii1.getText() + ":" + oraSosirii2.getText();
            
            // Validate numeric fields
            int locuriLibereVal;
            double pretVal;
            try {
                locuriLibereVal = Integer.parseInt(locuriLibere.getText());
                if (locuriLibereVal < 0) {
                    showErrorAlert("Eroare la actualizarea zborului", "Numărul de locuri libere trebuie să fie un număr pozitiv.");
                    return;
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Eroare la actualizarea zborului", "Numărul de locuri libere trebuie să fie un număr valid.");
                return;
            }
            
            try {
                pretVal = Double.parseDouble(pret.getText());
                if (pretVal < 0) {
                    showErrorAlert("Eroare la actualizarea zborului", "Prețul trebuie să fie un număr pozitiv.");
                    return;
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Eroare la actualizarea zborului", "Prețul trebuie să fie un număr valid.");
                return;
            }
            
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("origine", origine.getText());
            jsonPayload.put("destinatie", destinatie.getText());
            jsonPayload.put("dataPlecare", dataPlecare);
            jsonPayload.put("oraPlecare", oraPlecare);
            jsonPayload.put("dataSosire", dataSosire);
            jsonPayload.put("oraSosire", oraSosire);
            jsonPayload.put("modelAvion", modelAvion.getText());
            jsonPayload.put("locuriLibere", locuriLibereVal);
            jsonPayload.put("pret", pretVal);
            
            // Send PUT request to update flight
            String url = "http://localhost:3000/zboruri/editareZbor/" + idZbor.getText();
            
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            httpClient.setRequestMethod("PUT");
            httpClient.setRequestProperty("Content-Type", "application/json");
            httpClient.setDoOutput(true);
            
            try (OutputStream os = httpClient.getOutputStream()) {
                byte[] input = jsonPayload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = httpClient.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Success
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succes");
                alert.setHeaderText("Zbor actualizat cu succes");
                alert.setContentText("Zborul a fost actualizat cu succes în sistem.");
                alert.showAndWait();
                App.setRoot("paginaZboruriAdmin");
            } else {
                // Error
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Eroare");
                alert.setHeaderText("Eroare la actualizarea zborului");
                alert.setContentText("A apărut o eroare la actualizarea zborului. Te rog să încerci din nou.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            // Handle errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Eroare");
            alert.setHeaderText("Eroare la actualizarea zborului");
            alert.setContentText("A apărut o eroare: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Helper method to show error alerts
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eroare");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void backZbor() throws IOException {
        App.setRoot("paginaZboruriAdmin");
    }
}
