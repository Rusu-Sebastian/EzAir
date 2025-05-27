package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class SchimbaDataZborController {
    private static final Logger jurnal = Logger.getLogger(SchimbaDataZborController.class.getName());
    private static final String URL_SERVER = "http://localhost:3000";
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    
    @FXML private Label labelDetaliiZbor;
    @FXML private TableView<Zbor> tabelZboruri;
    @FXML private TableColumn<Zbor, String> coloanaData;
    @FXML private TableColumn<Zbor, String> coloanaOra;
    @FXML private TableColumn<Zbor, Integer> coloanaLocuri;
    @FXML private TableColumn<Zbor, Double> coloanaPret;
    @FXML private Button butonConfirma;
    @FXML private Button butonAnuleaza;
    
    private Bilet biletCurent;
    
    @FXML
    private void initialize() {
        configureazaTabele();
        incarcaBiletCurent();
        incarcaZboruriDisponibile();
    }
    
    private void configureazaTabele() {
        coloanaData.setCellValueFactory(new PropertyValueFactory<>("dataPlecare"));
        coloanaOra.setCellValueFactory(new PropertyValueFactory<>("oraPlecare"));
        coloanaLocuri.setCellValueFactory(new PropertyValueFactory<>("locuriLibere"));
        coloanaPret.setCellValueFactory(new PropertyValueFactory<>("pret"));
        
        tabelZboruri.getSelectionModel().selectedItemProperty().addListener(
            (obs, vechiSelectie, nouaSelectie) -> butonConfirma.setDisable(nouaSelectie == null));
        
        butonConfirma.setDisable(true);
    }
    
    private void incarcaBiletCurent() {
        try {
            // Preluăm informațiile despre biletul care trebuie modificat din App
            String detaliiZbor = (String) App.getDateUtilizator().get("detaliiZbor");
            if (detaliiZbor != null) {
                labelDetaliiZbor.setText("Zbor: " + detaliiZbor);
            } else {
                labelDetaliiZbor.setText("Selectați zborul pe care doriți să îl modificați");
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Nu s-au putut încărca detaliile biletului", e);
            labelDetaliiZbor.setText("Informații bilet nedisponibile");
        }
    }
    
    private void incarcaZboruriDisponibile() {
        try {
            String detaliiZbor = (String) App.getDateUtilizator().get("detaliiZbor");
            String idZborCurent = (String) App.getDateUtilizator().get("zborSelectatId");
            
            if (detaliiZbor == null || idZborCurent == null) {
                jurnal.log(Level.WARNING, "Lipsesc informații despre zborul curent");
                afiseazaEroare("Nu sunt disponibile informații despre zborul curent");
                return;
            }
            
            String[] detaliiRuta = detaliiZbor.split(" -> ");
            if (detaliiRuta.length != 2) {
                jurnal.log(Level.WARNING, "Format invalid pentru detaliile zborului: {0}", detaliiZbor);
                afiseazaEroare("Formatul detaliilor zborului este invalid");
                return;
            }
            
            String origine = detaliiRuta[0];
            String destinatie = detaliiRuta[1];
            
            // Acum obținem lista de zboruri de la server
            ObservableList<Zbor> zboruriDisponibile = cautaZboruriDisponibile(origine, destinatie, idZborCurent);
            tabelZboruri.setItems(zboruriDisponibile);
            
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la încărcarea zborurilor disponibile", e);
            afiseazaEroare("Nu s-au putut încărca zborurile disponibile");
        }
    }
    
    private ObservableList<Zbor> cautaZboruriDisponibile(String origine, String destinatie, String idZborCurent) {
        ObservableList<Zbor> zboruriDisponibile = FXCollections.observableArrayList();
        
        try {
            String origineCodata = java.net.URLEncoder.encode(origine, StandardCharsets.UTF_8);
            String destinatieCodata = java.net.URLEncoder.encode(destinatie, StandardCharsets.UTF_8);
            URI uri = new URI(URL_SERVER + "/zboruri/cautareZbor?origine=" + origineCodata + "&destinatie=" + destinatieCodata);
                                
            HttpURLConnection conexiune = (HttpURLConnection) uri.toURL().openConnection();
            conexiune.setRequestMethod("GET");
            conexiune.setConnectTimeout(5000);
            conexiune.setReadTimeout(5000);

            if (conexiune.getResponseCode() == HttpURLConnection.HTTP_OK) {
                proceseazaZboruriDisponibile(conexiune, zboruriDisponibile, idZborCurent);
            } else {
                jurnal.log(Level.WARNING, "Nu s-au putut obține zborurile disponibile. Cod: {0}", conexiune.getResponseCode());
                
                // În cazul în care nu putem obține zboruri reale, adăugăm câteva zboruri de exemplu pentru testare
                adaugaZboruriDemonstrative(zboruriDisponibile, origine, destinatie);
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la obținerea zborurilor disponibile: {0}", e.getMessage());
            
            // Adăugăm zboruri demonstrative în caz de eroare pentru testare
            adaugaZboruriDemonstrative(zboruriDisponibile, origine, destinatie);
        }
        
        return zboruriDisponibile;
    }
    
    private void proceseazaZboruriDisponibile(HttpURLConnection conexiune, ObservableList<Zbor> zboruriDisponibile, 
                                            String idZborCurent) throws Exception {
        String raspuns = new String(conexiune.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        org.json.JSONArray zboruri = new org.json.JSONArray(raspuns);
        
        for (int i = 0; i < zboruri.length(); i++) {
            org.json.JSONObject zborObj = zboruri.getJSONObject(i);
            if (!zborObj.getString("id").equals(idZborCurent)) {
                zboruriDisponibile.add(construiesteZbor(zborObj));
            }
        }
    }
    
    private Zbor construiesteZbor(org.json.JSONObject zborObj) {
        return new Zbor.Constructor()
            .setId(zborObj.getString("id"))
            .setOrigine(zborObj.getString("origine"))
            .setDestinatie(zborObj.getString("destinatie"))
            .setDataPlecare(zborObj.getString("dataPlecare"))
            .setOraPlecare(zborObj.getString("oraPlecare"))
            .setDataSosire(zborObj.getString("dataSosire"))
            .setOraSosire(zborObj.getString("oraSosire"))
            .setModelAvion(zborObj.getString("modelAvion"))
            .setLocuriLibere(zborObj.getInt("locuriLibere"))
            .setPret(zborObj.getDouble("pret"))
            .construieste();
    }
    
    @FXML
    private void schimbaData() {
        Zbor zborSelectat = tabelZboruri.getSelectionModel().getSelectedItem();
        if (zborSelectat == null) {
            afiseazaEroare("Te rugăm să selectezi un zbor");
            return;
        }
        
        try {
            String biletId = (String) App.getDateUtilizator().get("biletSelectatId");
            if (biletId == null) {
                afiseazaEroare("Nu sunt disponibile informații despre biletul curent");
                return;
            }
            
            // Trimitem cererea către server pentru a modifica zborul
            URI uri = new URI(URL_SERVER + "/bilete/" + biletId + "/modificare-zbor");
            HttpURLConnection conexiune = (HttpURLConnection) uri.toURL().openConnection();
            conexiune.setRequestMethod("PUT");
            conexiune.setRequestProperty("Content-Type", "application/json");
            conexiune.setDoOutput(true);
            conexiune.setConnectTimeout(5000);
            conexiune.setReadTimeout(5000);
            
            // Pregătim datele de actualizare
            org.json.JSONObject dateActualizare = new org.json.JSONObject();
            dateActualizare.put("zborId", zborSelectat.getId());
            dateActualizare.put("dataNoua", formateazaDataTimp(zborSelectat));
            
            // Trimitem datele
            try (java.io.OutputStream os = conexiune.getOutputStream()) {
                byte[] input = dateActualizare.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Verificăm răspunsul
            if (conexiune.getResponseCode() == HttpURLConnection.HTTP_OK) {
                afiseazaSucces("Data zborului a fost schimbată cu succes!");
            } else {
                // Tratăm eroarea
                try (java.io.InputStream errorStream = conexiune.getErrorStream()) {
                    String raspunsEroare = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                    jurnal.log(Level.WARNING, "Eroare la modificarea zborului: {0}", raspunsEroare);
                }
                afiseazaEroare("Nu s-a putut schimba data zborului. Cod: " + conexiune.getResponseCode());
            }
            
            // Revenim la pagina de cont
            App.setRoot("paginaContClient");
        } catch (Exception e) {
            jurnal.log(Level.SEVERE, "Eroare la schimbarea datei zborului", e);
            afiseazaEroare("Nu s-a putut schimba data zborului: " + e.getMessage());
        }
    }
    
    private String formateazaDataTimp(Zbor zbor) {
        try {
            String dataISO = Zbor.convertesteInFormatISO(zbor.getDataPlecare());
            return dataISO + "T" + zbor.getOraPlecare() + ":00";
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la conversia datei: {0}. Se folosește formatul original.", e.getMessage());
            return zbor.getDataPlecare() + "T" + zbor.getOraPlecare();
        }
    }
    
    @FXML
    private void anuleaza() {
        try {
            App.setRoot("paginaContClient");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea înapoi", e);
            afiseazaEroare("Nu s-a putut reveni la pagina contului");
        }
    }
    
    private void afiseazaEroare(String mesaj) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(TITLU_EROARE);
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
    
    private void afiseazaSucces(String mesaj) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(TITLU_SUCCES);
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
    
    private void adaugaZboruriDemonstrative(ObservableList<Zbor> zboruriDisponibile, String origine, String destinatie) {
        // Adăugăm câteva zboruri de exemplu pentru testare
        zboruriDisponibile.add(new Zbor.Constructor()
            .setId("z1")
            .setOrigine(origine)
            .setDestinatie(destinatie)
            .setDataPlecare("28/05/2025")
            .setOraPlecare("08:30")
            .setDataSosire("28/05/2025")
            .setOraSosire("10:45")
            .setModelAvion("Boeing 737")
            .setLocuriLibere(42)
            .setPret(199.99)
            .construieste());
            
        zboruriDisponibile.add(new Zbor.Constructor()
            .setId("z2")
            .setOrigine(origine)
            .setDestinatie(destinatie)
            .setDataPlecare("29/05/2025")
            .setOraPlecare("14:15")
            .setDataSosire("29/05/2025")
            .setOraSosire("16:30")
            .setModelAvion("Airbus A320")
            .setLocuriLibere(28)
            .setPret(189.99)
            .construieste());
    }
}
