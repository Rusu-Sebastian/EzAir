package com.proiect;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class PaginaContClientController {
    private static final Logger jurnal = Logger.getLogger(PaginaContClientController.class.getName());
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes"; 
    private static final String CHEIE_ID_UTILIZATOR = "userId";
    private static final String URL_SERVER = "http://localhost:3000";
    private static final String SEPARATOR_DATA = "/";
    private static final String SEPARATOR_ORA = ":";
    private static final String SEPARATOR_RUTA = " -> ";

    @FXML private Label labelNume;
    @FXML private Label labelEmail;
    @FXML private Label labelNumeUtilizator;
    @FXML private TableView<Bilet> tabelBilete;
    @FXML private TableColumn<Bilet, String> coloanaZbor;
    @FXML private TableColumn<Bilet, String> coloanaData;
    @FXML private TableColumn<Bilet, String> coloanaStare;

    @FXML
    private void initialize() {
        configureazaTabele();
        incarcaInformatiiUtilizator();
        incarcaBilete();
    }

    private void configureazaTabele() {
        coloanaZbor.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("detaliiZbor"));
        coloanaData.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dataZbor"));
        coloanaStare.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("stare"));
    }

    private void incarcaInformatiiUtilizator() {
        try {
            String idUtilizator = (String) App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
            URI uri = new URI(URL_SERVER + "/users/" + idUtilizator);
            HttpURLConnection conexiune = (HttpURLConnection) uri.toURL().openConnection();
            conexiune.setRequestMethod("GET");
            conexiune.setConnectTimeout(5000);
            conexiune.setReadTimeout(5000);

            if (conexiune.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JSONObject utilizator = new JSONObject(new String(conexiune.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                labelNume.setText(utilizator.getString("nume") + " " + utilizator.getString("prenume"));
                labelEmail.setText(utilizator.getString("email"));
                labelNumeUtilizator.setText(utilizator.getString("username"));
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la încărcarea informațiilor utilizatorului: {0}", e.getMessage());
            afiseazaEroare("Nu s-au putut încărca informațiile contului.");
        }
    }

    private void incarcaBilete() {
        try {
            String idUtilizator = (String) App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
            URI uri = new URI(URL_SERVER + "/users/" + idUtilizator + "/bilete");
            HttpURLConnection conexiune = (HttpURLConnection) uri.toURL().openConnection();
            conexiune.setRequestMethod("GET");
            conexiune.setConnectTimeout(5000);
            conexiune.setReadTimeout(5000);

            if (conexiune.getResponseCode() == HttpURLConnection.HTTP_OK) {
                proceseazaBilete(conexiune);
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la încărcarea biletelor: {0}", e.getMessage());
            afiseazaEroare("Nu s-au putut încărca biletele.");
        }
    }

    private void proceseazaBilete(HttpURLConnection conexiune) throws Exception {
        String raspuns = new String(conexiune.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        JSONArray bilete = new JSONArray(raspuns);
        ObservableList<Bilet> listaBilete = FXCollections.observableArrayList();
        
        for (int i = 0; i < bilete.length(); i++) {
            JSONObject bilet = bilete.getJSONObject(i);
            try {
                LocalDateTime dataZbor = parseazaDataTimp(bilet.getString("dataZbor"));
                
                Bilet b = new Bilet(
                    bilet.getString("id"),
                    bilet.getString(CHEIE_ID_UTILIZATOR),
                    bilet.getString("zborId"),
                    bilet.getString("detaliiZbor"),
                    dataZbor,
                    bilet.getString("stare"),
                    bilet.getDouble("pret")
                );
                listaBilete.add(b);
            } catch (Exception e) {
                jurnal.log(Level.WARNING, "Eroare la procesarea biletului #{0}: {1}", new Object[]{i, e.getMessage()});
            }
        }
        
        tabelBilete.setItems(listaBilete);
    }

    @FXML
    private void anuleazaBilet() {
        Bilet biletSelectat = tabelBilete.getSelectionModel().getSelectedItem();
        if (biletSelectat == null) {
            afiseazaEroare("Te rog să selectezi un bilet pentru anulare.");
            return;
        }

        try {
            URI uri = new URI(URL_SERVER + "/bilete/" + biletSelectat.getId() + "/anulare");
            HttpURLConnection conexiune = (HttpURLConnection) uri.toURL().openConnection();
            configureazaConexiunePUT(conexiune);

            if (conexiune.getResponseCode() == HttpURLConnection.HTTP_OK) {
                afiseazaSucces("Bilet anulat", "Biletul a fost anulat cu succes.");
                incarcaBilete();
            } else {
                afiseazaEroare("Nu s-a putut anula biletul. Te rugăm să încerci din nou.");
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la anularea biletului: {0}", e.getMessage());
            afiseazaEroare("Nu s-a putut anula biletul.");
        }
    }

    @FXML
    private void schimbaDataZbor() {
        Bilet biletSelectat = tabelBilete.getSelectionModel().getSelectedItem();
        if (biletSelectat == null) {
            afiseazaEroare("Te rog să selectezi un bilet pentru schimbarea datei.");
            return;
        }

        String[] detaliiRuta = biletSelectat.getDetaliiZbor().split(SEPARATOR_RUTA);
        if (detaliiRuta.length != 2) {
            afiseazaEroare("Formatul detaliilor zborului este invalid.");
            return;
        }
        
        String origine = detaliiRuta[0];
        String destinatie = detaliiRuta[1];
        
        ObservableList<Zbor> zboruriDisponibile = cautaZboruriDisponibile(origine, destinatie, biletSelectat.getIdZbor());
        
        if (zboruriDisponibile.isEmpty()) {
            afiseazaEroare("Nu există alte zboruri disponibile pe ruta " + origine + SEPARATOR_RUTA + destinatie);
            return;
        }
        
        Zbor zborSelectat = afiseazaDialogSelectareZbor(origine, destinatie, zboruriDisponibile);
        
        if (zborSelectat != null) {
            modificaZborBilet(biletSelectat, zborSelectat);
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
                jurnal.warning("Nu s-au putut obține zborurile disponibile. Cod: " + conexiune.getResponseCode());
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la obținerea zborurilor disponibile: {0}", e.getMessage());
        }
        
        return zboruriDisponibile;
    }

    private void proceseazaZboruriDisponibile(HttpURLConnection conexiune, ObservableList<Zbor> zboruriDisponibile, 
                                            String idZborCurent) throws Exception {
        String raspuns = new String(conexiune.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        JSONArray zboruri = new JSONArray(raspuns);
        
        for (int i = 0; i < zboruri.length(); i++) {
            JSONObject zborObj = zboruri.getJSONObject(i);
            if (!zborObj.getString("id").equals(idZborCurent)) {
                zboruriDisponibile.add(construiesteZbor(zborObj));
            }
        }
    }

    private Zbor construiesteZbor(JSONObject zborObj) {
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
    
    private Zbor afiseazaDialogSelectareZbor(String origine, String destinatie, ObservableList<Zbor> zboruriDisponibile) {
        Dialog<Zbor> dialog = new Dialog<>();
        dialog.setTitle("Schimbă data zborului");
        dialog.setHeaderText("Alege un alt zbor disponibil pe ruta " + origine + SEPARATOR_RUTA + destinatie);

        ButtonType butonSelectare = new ButtonType("Selectează", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(butonSelectare, ButtonType.CANCEL);

        TableView<Zbor> tabelZboruri = configureazaTabelZboruri(zboruriDisponibile);
        
        dialog.getDialogPane().setMinWidth(500);
        dialog.getDialogPane().setMinHeight(300);
        dialog.getDialogPane().setContent(tabelZboruri);
        
        dialog.setResultConverter(butonDialog -> {
            if (butonDialog == butonSelectare) {
                return tabelZboruri.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        
        Optional<Zbor> rezultat = dialog.showAndWait();
        return rezultat.orElse(null);
    }

    private TableView<Zbor> configureazaTabelZboruri(ObservableList<Zbor> zboruriDisponibile) {
        TableView<Zbor> tabelZboruri = new TableView<>();
        
        TableColumn<Zbor, String> colDataPlecare = new TableColumn<>("Data plecării");
        colDataPlecare.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dataPlecare"));
        
        TableColumn<Zbor, String> colOraPlecare = new TableColumn<>("Ora plecării");
        colOraPlecare.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("oraPlecare"));
        
        TableColumn<Zbor, Integer> colLocuriLibere = new TableColumn<>("Locuri libere");
        colLocuriLibere.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("locuriLibere"));
        
        TableColumn<Zbor, Double> colPret = new TableColumn<>("Preț");
        colPret.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("pret"));
        
        tabelZboruri.getColumns().addAll(colDataPlecare, colOraPlecare, colLocuriLibere, colPret);
        tabelZboruri.setItems(zboruriDisponibile);
        
        return tabelZboruri;
    }
    
    private void modificaZborBilet(Bilet bilet, Zbor zborNou) {
        try {
            URI uri = new URI(URL_SERVER + "/bilete/" + bilet.getId() + "/modificare-zbor");
            HttpURLConnection conexiune = (HttpURLConnection) uri.toURL().openConnection();
            configureazaConexiunePUT(conexiune);
            
            JSONObject dateActualizare = new JSONObject();
            dateActualizare.put("zborId", zborNou.getId());
            dateActualizare.put("dataNoua", formateazaDataTimp(zborNou));
            
            trimiteActualizare(conexiune, dateActualizare);
            
            if (conexiune.getResponseCode() == HttpURLConnection.HTTP_OK) {
                afiseazaSucces("Zbor modificat", 
                    String.format("Biletul a fost mutat cu succes pe zborul din data %s la ora %s", 
                                zborNou.getDataPlecare(), zborNou.getOraPlecare()));
                incarcaBilete();
            } else {
                gestioneazaEroareModificare(conexiune);
            }
        } catch (Exception e) {
            jurnal.log(Level.WARNING, "Eroare la modificarea zborului: {0}", e.getMessage());
            afiseazaEroare("Nu s-a putut modifica zborul: " + e.getMessage());
        }
    }

    private void configureazaConexiunePUT(HttpURLConnection conexiune) throws Exception {
        conexiune.setRequestMethod("PUT");
        conexiune.setRequestProperty("Content-Type", "application/json");
        conexiune.setDoOutput(true);
        conexiune.setConnectTimeout(5000);
        conexiune.setReadTimeout(5000);
    }

    private String formateazaDataTimp(Zbor zbor) {
        try {
            String dataISO = Zbor.convertesteInFormatISO(zbor.getDataPlecare());
            return dataISO + "T" + zbor.getOraPlecare() + ":00";
        } catch (Exception e) {
            jurnal.warning("Eroare la conversia datei: " + e.getMessage() + ". Se folosește formatul original.");
            return zbor.getDataPlecare() + "T" + zbor.getOraPlecare();
        }
    }

    private void trimiteActualizare(HttpURLConnection conexiune, JSONObject dateActualizare) throws Exception {
        try (OutputStream os = conexiune.getOutputStream()) {
            byte[] date = dateActualizare.toString().getBytes(StandardCharsets.UTF_8);
            os.write(date, 0, date.length);
        }
    }

    private void gestioneazaEroareModificare(HttpURLConnection conexiune) {
        String raspunsEroare = "";
        try (var fluxEroare = conexiune.getErrorStream()) {
            if (fluxEroare != null) {
                raspunsEroare = new String(fluxEroare.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            jurnal.warning("Nu s-a putut citi răspunsul de eroare: " + e.getMessage());
        }
        jurnal.warning("Eroare la modificare: " + raspunsEroare);
        try {
            afiseazaEroare("Nu s-a putut modifica zborul. Te rugăm să încerci din nou. Cod: " + conexiune.getResponseCode());
        } catch (java.io.IOException ex) {
            jurnal.warning("Nu s-a putut obține codul de răspuns: " + ex.getMessage());
            afiseazaEroare("Nu s-a putut modifica zborul. Te rugăm să încerci din nou.");
        }
    }

    @FXML
    private void editeazaInformatii() throws Exception {
        App.setRoot("editareContClient");
    }

    @FXML 
    private void configureazaNotificari() throws Exception {
        App.setRoot("setariNotificari");
    }

    @FXML
    private void schimbaParola() throws Exception {
        App.setRoot("schimbaParola");
    }

    @FXML
    private void revino() throws Exception {
        App.setRoot("paginaPrincipalaUser");
    }
    
    @FXML
    private void inapoi() throws Exception {
        App.setRoot("paginaPrincipalaUser");
    }
    
    @FXML
    private void anulareBilet() {
        // Redirecționează la metoda existentă
        anuleazaBilet();
    }
    
    @FXML
    private void schimbaData() throws Exception {
        // Redirecționează la metoda existentă
        schimbaDataZbor();
    }
    
    @FXML
    private void notificari() throws Exception {
        App.setRoot("setariNotificari");
    }
    
    @FXML
    private void editareInformatii() throws Exception {
        App.setRoot("editareContClient");
    }

    private void afiseazaEroare(String mesaj) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(TITLU_EROARE);
        alerta.setHeaderText(TITLU_EROARE);
        alerta.setContentText(mesaj);
        alerta.showAndWait();
    }

    private void afiseazaSucces(String antet, String continut) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(TITLU_SUCCES);
        alerta.setHeaderText(antet);
        alerta.setContentText(continut);
        alerta.showAndWait();
    }

    private LocalDateTime parseazaDataTimp(String sirDataTimp) {
        try {
            return LocalDateTime.parse(sirDataTimp);
        } catch (Exception e) {
            return parseazaDataTimpPersonalizat(sirDataTimp);
        }
    }

    private LocalDateTime parseazaDataTimpPersonalizat(String sirDataTimp) {
        try {
            String[] parti = sirDataTimp.split("T");
            if (parti.length != 2) {
                throw new IllegalArgumentException("Format dată-timp invalid: " + sirDataTimp);
            }
            
            String[] partiData = parti[0].split(SEPARATOR_DATA);
            if (partiData.length != 3) {
                throw new IllegalArgumentException("Format dată invalid: " + parti[0]);
            }
            
            int zi = Integer.parseInt(partiData[0]);
            int luna = Integer.parseInt(partiData[1]);
            int an = Integer.parseInt(partiData[2]);
            
            String[] partiTimp = parti[1].split(SEPARATOR_ORA);
            if (partiTimp.length < 1) {
                throw new IllegalArgumentException("Format timp invalid: " + parti[1]);
            }
            
            int ora = Integer.parseInt(partiTimp[0]);
            int minut = partiTimp.length > 1 ? Integer.parseInt(partiTimp[1]) : 0;
            int secunda = partiTimp.length > 2 ? Integer.parseInt(partiTimp[2]) : 0;
            
            return LocalDateTime.of(an, luna, zi, ora, minut, secunda);
        } catch (Exception e) {
            jurnal.severe("Eroare la parsarea datei-timp: " + sirDataTimp + ", eroare: " + e.getMessage());
            throw new IllegalArgumentException("Nu se poate parsa data-timp: " + sirDataTimp, e);
        }
    }
}
