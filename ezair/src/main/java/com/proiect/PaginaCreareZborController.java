package com.proiect;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class PaginaCreareZborController {
    private static final String TITLU_ATENTIONARE = "Atenție";
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    private static final String MESAJ_EROARE_ADAUGARE = "A apărut o eroare la adăugarea zborului. Te rog să încerci din nou.";
    private static final String CHEIE_ID_UTILIZATOR = "userId";
    private static final String CHEIE_ADMIN = "esteAdmin";

    @FXML private TextField origine;
    @FXML private TextField destinatie;
    @FXML private TextField modelAvion;
    @FXML private TextField locuriLibere;
    @FXML private TextField pret;
    @FXML private TextField dataPlecarii1;
    @FXML private TextField dataPlecarii2;
    @FXML private TextField dataPlecarii3;
    @FXML private TextField oraPlecarii1;
    @FXML private TextField oraPlecarii2;
    @FXML private TextField dataSosirii1;
    @FXML private TextField dataSosirii2;
    @FXML private TextField dataSosirii3;
    @FXML private TextField oraSosirii1;
    @FXML private TextField oraSosirii2;
    
    public void initialize() {
        // Initialization complete
    }
    
    @SuppressWarnings("unused")
    @FXML
    private void backZbor() throws Exception {
        // Păstrăm credențialele de admin înainte de a naviga înapoi
        Map<String, Object> dateUtilizator = App.getDateUtilizator();
        String idUtilizator = (String) dateUtilizator.get(CHEIE_ID_UTILIZATOR);
        String esteAdmin = (String) dateUtilizator.get(CHEIE_ADMIN);
        
        // Curățăm datele vechi dar păstrăm datele de autentificare
        dateUtilizator.clear();
        
        // Restaurăm credențialele
        if (idUtilizator != null) {
            dateUtilizator.put(CHEIE_ID_UTILIZATOR, idUtilizator);
        }
        if (esteAdmin != null) {
            dateUtilizator.put(CHEIE_ADMIN, esteAdmin);
        }
        
        App.setRoot("paginaZboruriAdmin");
    }

    @SuppressWarnings("unused")
    @FXML
    private void finalizareAdaugareZbor() throws Exception {
        // Obține valorile din câmpurile de text
        String valoareOrigine = origine.getText();
        String valoareDestinatie = destinatie.getText();
        String valoareModelAvion = modelAvion.getText();

        // Check if any field is empty
        if(valoareOrigine.isEmpty() || valoareDestinatie.isEmpty() || valoareModelAvion.isEmpty() || 
                pret.getText().isEmpty() ||
                dataPlecarii1.getText().isEmpty() ||
                dataPlecarii2.getText().isEmpty() ||
                dataPlecarii3.getText().isEmpty() ||
                oraPlecarii1.getText().isEmpty() ||
                oraPlecarii2.getText().isEmpty() ||
                dataSosirii1.getText().isEmpty() ||
                dataSosirii2.getText().isEmpty() ||
                dataSosirii3.getText().isEmpty() ||
                oraSosirii1.getText().isEmpty() ||
                oraSosirii2.getText().isEmpty() ||
                locuriLibere.getText().isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle(TITLU_ATENTIONARE);
            alerta.setHeaderText("Toate câmpurile sunt obligatorii");
            alerta.setContentText("Te rog să completezi toate câmpurile.");
            alerta.showAndWait();
            return;
        }
        
        // Procesează valorile numerice
        int valoareLocuriLibere = Integer.parseInt(locuriLibere.getText());
        int valoareDataPlecare1 = Integer.parseInt(dataPlecarii1.getText());
        int valoareDataPlecare2 = Integer.parseInt(dataPlecarii2.getText());
        int valoareDataPlecare3 = Integer.parseInt(dataPlecarii3.getText());
        int valoareOraPlecare1 = Integer.parseInt(oraPlecarii1.getText());
        int valoareOraPlecare2 = Integer.parseInt(oraPlecarii2.getText());
        int valoareDataSosire1 = Integer.parseInt(dataSosirii1.getText());
        int valoareDataSosire2 = Integer.parseInt(dataSosirii2.getText());
        int valoareDataSosire3 = Integer.parseInt(dataSosirii3.getText());
        int valoareOraSosire1 = Integer.parseInt(oraSosirii1.getText());
        int valoareOraSosire2 = Integer.parseInt(oraSosirii2.getText());
        int valoarePret = Integer.parseInt(pret.getText());

        int anCurent = java.time.Year.now().getValue();
        int lunaCurenta = java.time.Month.from(java.time.LocalDate.now()).getValue();
        int ziuaCurenta = java.time.LocalDate.now().getDayOfMonth();
        if (valoareDataPlecare1 < ziuaCurenta && valoareDataPlecare2 == lunaCurenta && valoareDataPlecare3 == anCurent) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle(TITLU_ATENTIONARE);
            alerta.setHeaderText("Data plecării nu poate fi în trecut");
            alerta.setContentText("Te rog să alegi o dată de plecare validă.");
            alerta.showAndWait();
            return;
        }
        if (valoareDataSosire1 < ziuaCurenta && valoareDataSosire2 == lunaCurenta && valoareDataSosire3 == anCurent) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle(TITLU_ATENTIONARE);
            alerta.setHeaderText("Data sosirii nu poate fi în trecut");
            alerta.setContentText("Te rog să alegi o dată de sosire validă.");
            alerta.showAndWait();
            return;
        }
        if (valoareLocuriLibere < 0) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Atenție");
            alerta.setHeaderText("Numărul de locuri libere nu poate fi negativ");
            alerta.setContentText("Te rog să introduci un număr valid de locuri libere.");
            alerta.showAndWait();
            return;
        }
        if (valoarePret < 0) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle(TITLU_ATENTIONARE);
            alerta.setHeaderText("Prețul nu poate fi negativ");
            alerta.setContentText("Te rog să introduci un preț valid.");
            alerta.showAndWait();
            return;
        }

        String dataPlecare = valoareDataPlecare1 + "/" + valoareDataPlecare2 + "/" + valoareDataPlecare3;
        String oraPlecare = valoareOraPlecare1 + ":" + valoareOraPlecare2;
        String dataSosire = valoareDataSosire1 + "/" + valoareDataSosire2 + "/" + valoareDataSosire3;
        String oraSosire = valoareOraSosire1 + ":" + valoareOraSosire2;

        try {
            String url = "http://localhost:3000/zboruri/adaugareZbor";
            String sirDateJson = String.format(
                    "{\"origine\": \"%s\", \"destinatie\": \"%s\", \"dataPlecare\": \"%s\", \"oraPlecare\": \"%s\", \"dataSosire\": \"%s\", \"oraSosire\": \"%s\", \"modelAvion\": \"%s\", \"locuriLibere\": %d, \"pret\": %d}",
                    valoareOrigine, valoareDestinatie, dataPlecare, oraPlecare, dataSosire, oraSosire, valoareModelAvion, valoareLocuriLibere, valoarePret);
            
            // Creează URL-ul și conexiunea
            URL adresaServer = new URL(url);
            HttpURLConnection clientHttp = (HttpURLConnection) adresaServer.openConnection();
            clientHttp.setRequestMethod("POST");
            clientHttp.setRequestProperty("Content-Type", "application/json");
            clientHttp.setDoOutput(true);
            clientHttp.getOutputStream().write(sirDateJson.getBytes(StandardCharsets.UTF_8));
            int codRaspuns = clientHttp.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                // Afișează un pop-up pentru confirmarea adăugării
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Succes");
                alerta.setHeaderText("Zbor adăugat cu succes");
                alerta.setContentText("Zborul a fost adăugat cu succes în sistem.");
                alerta.showAndWait();
            } else {
                // Afișează un pop-up pentru eroare
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle(TITLU_EROARE);
                alerta.setHeaderText("Eroare la adăugarea zborului");
                alerta.setContentText(MESAJ_EROARE_ADAUGARE);
                alerta.showAndWait();
            }
        } catch (Exception e) {
            // Afișează un pop-up pentru eroare generală
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle(TITLU_EROARE);
            alerta.setHeaderText("Eroare la adăugarea zborului");
            alerta.setContentText(MESAJ_EROARE_ADAUGARE);
            alerta.showAndWait();
            e.printStackTrace();
        }

        App.setRoot("paginaZboruriAdmin");
    }
}
