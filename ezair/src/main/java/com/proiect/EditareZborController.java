package com.proiect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class EditareZborController {
    private static final Logger jurnal = Logger.getLogger(EditareZborController.class.getName());
    private static final String URL_SERVER = "http://localhost:3000/zboruri/editareZbor/";
    private static final String SEPARATOR_DATA_PUNCT = ".";
    private static final String SEPARATOR_DATA_SLASH = "/";
    private static final String SEPARATOR_ORA = ":";
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    
    @FXML private TextField origine;
    @FXML private TextField destinatie;
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
    @FXML private TextField modelAvion;
    @FXML private TextField pret;
    @FXML private TextField locuriLibere;
    @FXML private TextField idZbor;

    @FXML
    public void initialize() {
        Map<String, String> dateUtilizator = App.getDateUtilizator();
        
        if (!dateUtilizator.containsKey("id")) {
            afiseazaEroare("Eroare la încărcarea zborului", "Nu s-au găsit datele zborului pentru editare.");
            return;
        }

        incarcaCampuri(dateUtilizator);
    }

    private void incarcaCampuri(Map<String, String> dateUtilizator) {
        idZbor.setText(dateUtilizator.get("id"));
        origine.setText(dateUtilizator.get("origine"));
        destinatie.setText(dateUtilizator.get("destinatie"));
        
        incarcaDataPlecarii(dateUtilizator.get("dataPlecare"));
        incarcaOraPlecarii(dateUtilizator.get("oraPlecare"));
        incarcaDataSosirii(dateUtilizator.get("dataSosire"));
        incarcaOraSosirii(dateUtilizator.get("oraSosire"));
        
        modelAvion.setText(dateUtilizator.get("modelAvion"));
        locuriLibere.setText(dateUtilizator.get("locuriLibere"));
        pret.setText(dateUtilizator.get("pret"));
    }

    private void incarcaDataPlecarii(String dataPlecare) {
        try {
            String[] parti = desparteData(dataPlecare);
            if (parti.length == 3) {
                dataPlecarii1.setText(parti[0]);
                dataPlecarii2.setText(parti[1]);
                dataPlecarii3.setText(parti[2]);
            } else {
                dataPlecarii1.setText(dataPlecare);
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la încărcarea datei plecării: {0}", e.getMessage());
        }
    }

    private void incarcaOraPlecarii(String oraPlecare) {
        try {
            String[] parti = oraPlecare.split(SEPARATOR_ORA);
            if (parti.length == 2) {
                oraPlecarii1.setText(parti[0]);
                oraPlecarii2.setText(parti[1]);
            } else {
                oraPlecarii1.setText(oraPlecare);
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la încărcarea orei plecării: {0}", e.getMessage());
        }
    }

    private void incarcaDataSosirii(String dataSosire) {
        try {
            String[] parti = desparteData(dataSosire);
            if (parti.length == 3) {
                dataSosirii1.setText(parti[0]);
                dataSosirii2.setText(parti[1]);
                dataSosirii3.setText(parti[2]);
            } else {
                dataSosirii1.setText(dataSosire);
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la încărcarea datei sosirii: {0}", e.getMessage());
        }
    }

    private void incarcaOraSosirii(String oraSosire) {
        try {
            String[] parti = oraSosire.split(SEPARATOR_ORA);
            if (parti.length == 2) {
                oraSosirii1.setText(parti[0]);
                oraSosirii2.setText(parti[1]);
            } else {
                oraSosirii1.setText(oraSosire);
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la încărcarea orei sosirii: {0}", e.getMessage());
        }
    }

    private String[] desparteData(String data) {
        if (data.contains(SEPARATOR_DATA_PUNCT)) {
            return data.split("\\" + SEPARATOR_DATA_PUNCT);
        } else if (data.contains(SEPARATOR_DATA_SLASH)) {
            return data.split(SEPARATOR_DATA_SLASH);
        }
        return new String[0];
    }

    @FXML
    private void finalizareEditareZbor() {
        if (!valideazaDate()) {
            return;
        }

        try {
            JSONObject dateZbor = construiesteDateZbor();
            trimiteActualizareZbor(dateZbor);
        } catch (Exception e) {
            jurnal.log(Level.SEVERE, "Eroare la actualizarea zborului: {0}", e.getMessage());
            afiseazaEroare("Eroare la actualizarea zborului", "A apărut o eroare: " + e.getMessage());
        }
    }

    private boolean valideazaDate() {
        if (dataPlecarii1.getText().isEmpty() || dataPlecarii2.getText().isEmpty() || dataPlecarii3.getText().isEmpty()) {
            afiseazaEroare("Eroare la actualizarea zborului", "Te rog să completezi data plecării în format ZZ.LL.AAAA sau ZZ/LL/AAAA");
            return false;
        }
        
        if (oraPlecarii1.getText().isEmpty() || oraPlecarii2.getText().isEmpty()) {
            afiseazaEroare("Eroare la actualizarea zborului", "Te rog să completezi ora plecării în format HH:MM");
            return false;
        }
        
        if (dataSosirii1.getText().isEmpty() || dataSosirii2.getText().isEmpty() || dataSosirii3.getText().isEmpty()) {
            afiseazaEroare("Eroare la actualizarea zborului", "Te rog să completezi data sosirii în format ZZ.LL.AAAA sau ZZ/LL/AAAA");
            return false;
        }
        
        if (oraSosirii1.getText().isEmpty() || oraSosirii2.getText().isEmpty()) {
            afiseazaEroare("Eroare la actualizarea zborului", "Te rog să completezi ora sosirii în format HH:MM");
            return false;
        }

        if (!valideazaCampuriNumerice()) {
            return false;
        }
        
        return true;
    }

    private boolean valideazaCampuriNumerice() {
        try {
            int nrLocuriLibere = Integer.parseInt(locuriLibere.getText());
            if (nrLocuriLibere < 0) {
                afiseazaEroare("Eroare la actualizarea zborului", "Numărul de locuri libere trebuie să fie un număr pozitiv.");
                return false;
            }
        } catch (NumberFormatException e) {
            afiseazaEroare("Eroare la actualizarea zborului", "Numărul de locuri libere trebuie să fie un număr valid.");
            return false;
        }
        
        try {
            double valoarePret = Double.parseDouble(pret.getText());
            if (valoarePret < 0) {
                afiseazaEroare("Eroare la actualizarea zborului", "Prețul trebuie să fie un număr pozitiv.");
                return false;
            }
        } catch (NumberFormatException e) {
            afiseazaEroare("Eroare la actualizarea zborului", "Prețul trebuie să fie un număr valid.");
            return false;
        }
        
        return true;
    }

    private JSONObject construiesteDateZbor() {
        String separatorData = determinaSeparatorData();
        String dataPlecare = String.join(separatorData, dataPlecarii1.getText(), dataPlecarii2.getText(), dataPlecarii3.getText());
        String oraPlecare = String.join(SEPARATOR_ORA, oraPlecarii1.getText(), oraPlecarii2.getText());
        String dataSosire = String.join(separatorData, dataSosirii1.getText(), dataSosirii2.getText(), dataSosirii3.getText());
        String oraSosire = String.join(SEPARATOR_ORA, oraSosirii1.getText(), oraSosirii2.getText());
        
        JSONObject dateZbor = new JSONObject();
        dateZbor.put("origine", origine.getText());
        dateZbor.put("destinatie", destinatie.getText());
        dateZbor.put("dataPlecare", dataPlecare);
        dateZbor.put("oraPlecare", oraPlecare);
        dateZbor.put("dataSosire", dataSosire);
        dateZbor.put("oraSosire", oraSosire);
        dateZbor.put("modelAvion", modelAvion.getText());
        dateZbor.put("locuriLibere", Integer.parseInt(locuriLibere.getText()));
        dateZbor.put("pret", Double.parseDouble(pret.getText()));
        
        return dateZbor;
    }

    private String determinaSeparatorData() {
        Map<String, String> dateUtilizator = App.getDateUtilizator();
        if (dateUtilizator.containsKey("dataPlecare") && 
            dateUtilizator.get("dataPlecare").contains(SEPARATOR_DATA_SLASH)) {
            return SEPARATOR_DATA_SLASH;
        }
        return SEPARATOR_DATA_PUNCT;
    }

    private void trimiteActualizareZbor(JSONObject dateZbor) throws Exception {
        String url = URL_SERVER + idZbor.getText();
        jurnal.info("Trimitere actualizare la: " + url);
        
        HttpURLConnection conexiune = (HttpURLConnection) new URI(url).toURL().openConnection();
        configureazaConexiune(conexiune);
        
        try (OutputStream os = conexiune.getOutputStream()) {
            byte[] date = dateZbor.toString().getBytes(StandardCharsets.UTF_8);
            os.write(date, 0, date.length);
        }
        
        proceseazaRaspuns(conexiune);
    }

    private void configureazaConexiune(HttpURLConnection conexiune) throws Exception {
        conexiune.setRequestMethod("PUT");
        conexiune.setRequestProperty("Content-Type", "application/json");
        conexiune.setDoOutput(true);
        conexiune.setConnectTimeout(5000);
        conexiune.setReadTimeout(5000);
    }

    private void proceseazaRaspuns(HttpURLConnection conexiune) throws Exception {
        int codRaspuns = conexiune.getResponseCode();
        jurnal.info("Cod răspuns actualizare: " + codRaspuns);
        
        if (codRaspuns == HttpURLConnection.HTTP_OK) {
            afiseazaSucces("Zbor actualizat", "Zborul a fost actualizat cu succes în sistem.");
            App.setRoot("paginaZboruriAdmin");
        } else {
            afiseazaEroare("Eroare la actualizarea zborului", 
                          "A apărut o eroare la actualizarea zborului. Te rog să încerci din nou.");
        }
    }

    @FXML
    private void revino() throws IOException {
        App.setRoot("paginaZboruriAdmin");
    }

    private void afiseazaEroare(String antet, String continut) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(TITLU_EROARE);
        alerta.setHeaderText(antet);
        alerta.setContentText(continut);
        alerta.showAndWait();
    }

    private void afiseazaSucces(String antet, String continut) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(TITLU_SUCCES);
        alerta.setHeaderText(antet);
        alerta.setContentText(continut);
        alerta.showAndWait();
    }
}
