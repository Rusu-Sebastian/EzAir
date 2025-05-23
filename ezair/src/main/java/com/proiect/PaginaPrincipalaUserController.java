package com.proiect;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.application.Platform;
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
    private static final Logger jurnal = Logger.getLogger(PaginaPrincipalaUserController.class.getName());
    private static final String URL_SERVER = "http://localhost:3000";
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    private static final String TITLU_INFO = "Informație";
    private static final String CHEIE_ID_UTILIZATOR = "userId";
    private static final String SEPARATOR_RUTA = " -> ";
    
    @FXML private ComboBox<String> comboOrigine;
    @FXML private ComboBox<String> comboDestinatie;
    @FXML private DatePicker dataPlecare;
    @FXML private Label labelNumeUtilizator;
    @FXML private Label labelDataOptionala;
    
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
    private void initializeaza() {
        configureazaTabele();
        incarcaOrase();
        afiseazaNumeUtilizator();
        configureazaEticheteAuxiliare();
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
        String nume = (String) App.getDateUtilizator().get("nume");
        String prenume = (String) App.getDateUtilizator().get("prenume");
        labelNumeUtilizator.setText(nume + " " + prenume);
    }
    
    private void configureazaEticheteAuxiliare() {
        if (labelDataOptionala != null) {
            labelDataOptionala.setText("(Opțional)");
            labelDataOptionala.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
        }
    }
    
    private void incarcaOrase() {
        try {
            URI uri = new URI(URL_SERVER + "/zboruri/orase");
            HttpURLConnection conexiune = (HttpURLConnection) uri.toURL().openConnection();
            conexiune.setRequestMethod("GET");
            conexiune.setConnectTimeout(5000);
            conexiune.setReadTimeout(5000);
            
            if (conexiune.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String raspuns = new String(conexiune.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JSONArray orase = new JSONArray(raspuns);
                ObservableList<String> listaOrase = FXCollections.observableArrayList();
                
                for (int i = 0; i < orase.length(); i++) {
                    listaOrase.add(orase.getString(i));
                }
                
                comboOrigine.setItems(listaOrase);
                comboDestinatie.setItems(listaOrase);
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la încărcarea orașelor: {0}", e.getMessage());
            afiseazaEroare("Nu s-au putut încărca orașele disponibile.");
        }
    }
    
    @FXML
    private void cautaZboruri() {
        if (!valideazaFormularCautare()) {
            return;
        }
        
        try {
            String origine = comboOrigine.getValue();
            String destinatie = comboDestinatie.getValue();
            String url = construiesteURLCautare(origine, destinatie);
            
            URI uri = new URI(url);
            HttpURLConnection conexiune = (HttpURLConnection) uri.toURL().openConnection();
            conexiune.setRequestMethod("GET");
            conexiune.setConnectTimeout(5000);
            conexiune.setReadTimeout(5000);
            
            if (conexiune.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String raspuns = new String(conexiune.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                proceseazaRaspunsZboruri(new JSONArray(raspuns));
            } else {
                afiseazaEroare("A apărut o eroare la căutarea zborurilor. Te rugăm să încerci din nou.");
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la căutarea zborurilor: {0}", e.getMessage());
            afiseazaEroare("Nu s-au putut căuta zborurile. Te rugăm să încerci din nou.");
        }
    }

    private String construiesteURLCautare(String origine, String destinatie) {
        StringBuilder constructorURL = new StringBuilder();
        constructorURL.append(String.format("%s/zboruri/cautareZbor?origine=%s&destinatie=%s", 
            URL_SERVER, origine, destinatie));
        
        if (dataPlecare.getValue() != null) {
            constructorURL.append("&data=").append(dataPlecare.getValue().toString());
        }
        
        return constructorURL.toString();
    }

    private void proceseazaRaspunsZboruri(JSONArray zboruri) {
        jurnal.info("S-au primit " + zboruri.length() + " zboruri de la căutare");
        ObservableList<Zbor> listaZboruri = FXCollections.observableArrayList();
        
        for (int i = 0; i < zboruri.length(); i++) {
            try {
                Zbor zbor = construiesteZborDinJSON(zboruri.getJSONObject(i), i);
                if (zbor != null) {
                    listaZboruri.add(zbor);
                }
            } catch (Exception e) {
                jurnal.warning("Eroare la procesarea zborului " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        actualizeazaInterfataUtilizator(listaZboruri);
    }

    private Zbor construiesteZborDinJSON(JSONObject zbor, int index) {
        if (!zbor.has("id") || zbor.isNull("id")) {
            jurnal.warning("Se omite zborul cu ID lipsă la indexul " + index);
            return null;
        }
        
        try {
            return new Zbor.Constructor()
                .setId(zbor.optString("id", ""))
                .setOrigine(zbor.optString("origine", ""))
                .setDestinatie(zbor.optString("destinatie", ""))
                .setDataPlecare(zbor.optString("dataPlecare", ""))
                .setOraPlecare(zbor.optString("oraPlecare", ""))
                .setDataSosire(zbor.optString("dataSosire", ""))
                .setOraSosire(zbor.optString("oraSosire", ""))
                .setModelAvion(zbor.optString("modelAvion", ""))
                .setLocuriLibere(zbor.optInt("locuriLibere", 0))
                .setPret(zbor.optDouble("pret", 0.0))
                .construieste();
        } catch (Exception e) {
            jurnal.warning("Eroare la construirea zborului " + index + ": " + e.getMessage());
            return null;
        }
    }

    private void actualizeazaInterfataUtilizator(ObservableList<Zbor> listaZboruri) {
        Platform.runLater(() -> {
            tabelZboruri.setItems(listaZboruri);
            if (listaZboruri.isEmpty()) {
                afiseazaInfo("Nu au fost găsite zboruri pentru criteriile selectate.");
            } else {
                String mesaj = dataPlecare.getValue() == null ? 
                    "S-au găsit toate zborurile disponibile pentru ruta selectată" : 
                    "S-au găsit zborurile disponibile pentru data și ruta selectată";
                afiseazaInfo(mesaj);
            }
        });
    }

    private boolean valideazaFormularCautare() {
        if (comboOrigine.getValue() == null || comboOrigine.getValue().trim().isEmpty()) {
            afiseazaEroare("Te rugăm să selectezi orașul de origine.");
            return false;
        }
        
        if (comboDestinatie.getValue() == null || comboDestinatie.getValue().trim().isEmpty()) {
            afiseazaEroare("Te rugăm să selectezi orașul de destinație.");
            return false;
        }
        
        if (comboOrigine.getValue().equals(comboDestinatie.getValue())) {
            afiseazaEroare("Orașul de origine nu poate fi același cu destinația.");
            return false;
        }

        if (dataPlecare.getValue() != null && dataPlecare.getValue().isBefore(LocalDate.now())) {
            afiseazaEroare("Data plecării nu poate fi în trecut.");
            return false;
        }

        return true;
    }

    @FXML
    private void rezervaBilet() {
        jurnal.info("Încercare de rezervare bilet...");
        Zbor zborSelectat = tabelZboruri.getSelectionModel().getSelectedItem();
        
        if (!valideazaRezervareBilet(zborSelectat)) {
            return;
        }
        
        try {
            String idUtilizator = (String) App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
            jurnal.info("Se trimite rezervare pentru utilizatorul: " + idUtilizator);
            
            JSONObject dateBilet = construiesteDateBilet(zborSelectat, idUtilizator);
            trimiteRezervareBilet(dateBilet);
            
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la rezervarea biletului: {0}", e.getMessage());
            afiseazaEroare("Nu s-a putut rezerva biletul. Te rugăm să încerci din nou.");
        }
    }

    private boolean valideazaRezervareBilet(Zbor zborSelectat) {
        if (zborSelectat == null) {
            jurnal.warning("Niciun zbor selectat pentru rezervare");
            afiseazaEroare("Te rugăm să selectezi un zbor pentru rezervare.");
            return false;
        }
        
        jurnal.info("Zbor selectat pentru rezervare: " + zborSelectat.getId() + 
                   " (" + zborSelectat.getOrigine() + SEPARATOR_RUTA + zborSelectat.getDestinatie() + ")");

        if (zborSelectat.getLocuriLibere() <= 0) {
            jurnal.warning("Încercare de rezervare a unui zbor fără locuri disponibile");
            afiseazaEroare("Ne pare rău, nu mai sunt locuri disponibile pentru acest zbor.");
            return false;
        }

        String idUtilizator = (String) App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
        if (idUtilizator == null) {
            jurnal.warning("ID Utilizator lipsă - utilizator neautentificat");
            afiseazaEroare("Te rugăm să te autentifici pentru a rezerva un bilet.");
            return false;
        }

        return true;
    }

    private JSONObject construiesteDateBilet(Zbor zbor, String idUtilizator) {
        JSONObject dateBilet = new JSONObject();
        dateBilet.put(CHEIE_ID_UTILIZATOR, idUtilizator);
        dateBilet.put("zborId", zbor.getId());
        dateBilet.put("detaliiZbor", String.format("%s%s%s", 
            zbor.getOrigine(), SEPARATOR_RUTA, zbor.getDestinatie()));
        dateBilet.put("dataZbor", formateazaDataZbor(zbor));
        dateBilet.put("pret", zbor.getPret());
        
        return dateBilet;
    }

    private String formateazaDataZbor(Zbor zbor) {
        try {
            String dataISO = Zbor.convertesteInFormatISO(zbor.getDataPlecare());
            return dataISO + "T" + zbor.getOraPlecare() + ":00";
        } catch (Exception e) {
            jurnal.warning("Eroare la conversia datei în format ISO: " + e.getMessage());
            return zbor.getDataPlecare() + "T" + zbor.getOraPlecare();
        }
    }

    private void trimiteRezervareBilet(JSONObject dateBilet) throws Exception {
        URI uri = new URI(URL_SERVER + "/bilete/cumpara");
        HttpURLConnection conexiune = (HttpURLConnection) uri.toURL().openConnection();
        configureazaConexiunePOST(conexiune);
        
        try (var os = conexiune.getOutputStream()) {
            os.write(dateBilet.toString().getBytes(StandardCharsets.UTF_8));
        }
        
        proceseazaRaspunsRezervare(conexiune);
    }

    private void configureazaConexiunePOST(HttpURLConnection conexiune) throws Exception {
        conexiune.setRequestMethod("POST");
        conexiune.setRequestProperty("Content-Type", "application/json");
        conexiune.setDoOutput(true);
        conexiune.setConnectTimeout(5000);
        conexiune.setReadTimeout(5000);
    }

    private void proceseazaRaspunsRezervare(HttpURLConnection conexiune) throws Exception {
        int codRaspuns = conexiune.getResponseCode();
        jurnal.info("Cod răspuns server: " + codRaspuns);
        
        if (codRaspuns == HttpURLConnection.HTTP_OK) {
            Platform.runLater(() -> {
                afiseazaSucces("Biletul a fost rezervat cu succes!");
                cautaZboruri();
            });
        } else {
            String mesajEroare = extrageEroareDinRaspuns(conexiune);
            Platform.runLater(() -> afiseazaEroare(mesajEroare));
        }
    }

    private String extrageEroareDinRaspuns(HttpURLConnection conexiune) {
        String mesajEroare = "Nu s-a putut rezerva biletul. Te rugăm să încerci din nou.";
        try (var fluxEroare = conexiune.getErrorStream()) {
            if (fluxEroare != null) {
                String eroareBruta = new String(fluxEroare.readAllBytes(), StandardCharsets.UTF_8);
                jurnal.warning("Eroare de la server: " + eroareBruta);
                
                try {
                    JSONObject eroare = new JSONObject(eroareBruta);
                    if (eroare.has("error")) {
                        mesajEroare = eroare.getString("error");
                    }
                } catch (Exception e) {
                    jurnal.warning("Răspunsul nu este în format JSON: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            jurnal.warning("Nu s-a putut citi răspunsul de eroare: " + e.getMessage());
        }
        return mesajEroare;
    }
    
    @FXML
    private void navigheazaContulMeu() throws Exception {
        App.setRoot("paginaContClient");
    }
    
    @FXML
    private void deconectare() throws Exception {
        App.getDateUtilizator().clear();
        App.setRoot("login");
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
    
    private void afiseazaInfo(String mesaj) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(TITLU_INFO);
        alerta.setHeaderText(null);
        alerta.setContentText(mesaj);
        alerta.showAndWait();
    }
}
