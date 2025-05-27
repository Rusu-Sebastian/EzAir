package com.proiect;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PaginaZboruriAdminController {
    private static final Logger jurnal = Logger.getLogger(PaginaZboruriAdminController.class.getName());
    private static final String URL_SERVER = "http://localhost:3000";
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_ATENTIONARE = "Atenție";
    private static final String TITLU_SUCCES = "Succes";
    private static final String TIMP_EXPIRARE = "30000";
    private static final String CHEIE_ID_UTILIZATOR = "userId";
    private static final String CHEIE_ADMIN = "esteAdmin";
    
    @FXML private TableView<Zbor> tabelZboruri;
    @FXML private TableColumn<Zbor, String> coloanaOrigine;
    @FXML private TableColumn<Zbor, String> coloanaDestinatie;
    @FXML private TableColumn<Zbor, String> coloanaModelAvion;
    @FXML private TableColumn<Zbor, Integer> coloanaLocuriLibere;
    @FXML private TableColumn<Zbor, Double> coloanaPret;
    @FXML private TableColumn<Zbor, String> coloanaId;
    @FXML private TableColumn<Zbor, String> coloanaDataPlecare;
    @FXML private TableColumn<Zbor, String> coloanaOraPlecare;
    @FXML private TableColumn<Zbor, String> coloanaDataSosire;
    @FXML private TableColumn<Zbor, String> coloanaOraSosire;

    private static final String PAGINA_LOGIN = "login";

    private static final String LOG_FORMAT_PROCES_ZBOR = "Se procesează zborul {0}: {1}";
    private static final String LOG_FORMAT_EROARE_ZBOR = "Eroare la construirea zborului {0}: {1}";
    private static final String LOG_FORMAT_ZBOR_ID_MISSING = "Se omite zborul cu ID lipsă la indexul {0}";
    private static final String LOG_FORMAT_ZBOR_INVALID = "Zborul de la indexul {0} are origine sau destinație invalidă: {1}";
    private static final String LOG_FORMAT_ZBOR_SUCCES = "Zbor procesat cu succes, ID: {0}";
    private static final String LOG_FORMAT_ZBORURI_INCARCATE = "S-au încărcat cu succes {0} zboruri";
    private static final String LOG_FORMAT_EROARE_JSON = "Eroare la parsarea JSON: {0}";
    private static final String LOG_FORMAT_EROARE_SERVER = "Eroare server: {0}, Răspuns: {1}";
    private static final String LOG_FORMAT_EROARE_FLUX = "Nu s-a putut citi fluxul de eroare: {0}";
    
    // Property names used in JSON and TableView
    private static final String PROP_ORIGINE = "origine";
    private static final String PROP_DESTINATIE = "destinatie";
    private static final String PROP_MODEL_AVION = "modelAvion";
    private static final String PROP_LOCURI_LIBERE = "locuriLibere";
    private static final String PROP_PRET = "pret";
    private static final String PROP_ID = "id";
    private static final String PROP_DATA_PLECARE = "dataPlecare";
    private static final String PROP_ORA_PLECARE = "oraPlecare";
    private static final String PROP_DATA_SOSIRE = "dataSosire";
    private static final String PROP_ORA_SOSIRE = "oraSosire";

    // Logging methods
    private void logEraoareConstructieZbor(int index, String mesaj) {
        jurnal.warning(MessageFormat.format(LOG_FORMAT_EROARE_ZBOR, index, mesaj));
    }
    
    private void logZborFaraId(int index) {
        jurnal.warning(MessageFormat.format(LOG_FORMAT_ZBOR_ID_MISSING, index));
    }
    
    private void logZborInvalid(int index, JSONObject obiectZbor) {
        jurnal.warning(MessageFormat.format(LOG_FORMAT_ZBOR_INVALID, index, obiectZbor));
    }
    
    private void logProcesareZbor(int index, JSONObject obiectZbor) {
        jurnal.info(MessageFormat.format(LOG_FORMAT_PROCES_ZBOR, index, obiectZbor.toString(2)));
    }
    
    private void logZborProcesat(String idZbor) {
        jurnal.info(MessageFormat.format(LOG_FORMAT_ZBOR_SUCCES, idZbor));
    }
    
    private void logTotalZboruriIncarcate(int numarZboruri) {
        jurnal.info(MessageFormat.format(LOG_FORMAT_ZBORURI_INCARCATE, numarZboruri));
    }
    
    private void logEroareJSON(String mesaj) {
        jurnal.severe(MessageFormat.format(LOG_FORMAT_EROARE_JSON, mesaj));
    }
    


    @FXML
    public void initialize() {
        if (!verificaPermisiuniAdmin()) {
            return;
        }
        configureazaColoaneTabel();
        incarcaZboruri();
    }

    private boolean verificaPermisiuniAdmin() {
        if (!esteUtilizatorAdmin()) {
            jurnal.severe("Încercare de acces neautorizat la panoul de administrare zboruri");
            Platform.runLater(() -> {
                afiseazaEroare("Nu aveți permisiunile necesare pentru accesarea acestei pagini");
                try {
                    App.setRoot(PAGINA_LOGIN);
                } catch (IOException e) {
                    jurnal.log(Level.SEVERE, "Eroare la redirecționare către login", e);
                }
            });
            return false;
        }
        return true;
    }

    private boolean esteUtilizatorAdmin() {
        String idUtilizator = App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
        String esteAdmin = App.getDateUtilizator().get(CHEIE_ADMIN);
        return idUtilizator != null && "true".equals(esteAdmin);
    }

    private void configureazaColoaneTabel() {
        coloanaOrigine.setCellValueFactory(new PropertyValueFactory<>(PROP_ORIGINE));
        coloanaDestinatie.setCellValueFactory(new PropertyValueFactory<>(PROP_DESTINATIE));
        coloanaModelAvion.setCellValueFactory(new PropertyValueFactory<>(PROP_MODEL_AVION));
        coloanaLocuriLibere.setCellValueFactory(new PropertyValueFactory<>(PROP_LOCURI_LIBERE));
        coloanaPret.setCellValueFactory(new PropertyValueFactory<>(PROP_PRET));
        coloanaId.setCellValueFactory(new PropertyValueFactory<>(PROP_ID));
        coloanaDataPlecare.setCellValueFactory(new PropertyValueFactory<>(PROP_DATA_PLECARE));
        coloanaOraPlecare.setCellValueFactory(new PropertyValueFactory<>(PROP_ORA_PLECARE));
        coloanaDataSosire.setCellValueFactory(new PropertyValueFactory<>(PROP_DATA_SOSIRE));
        coloanaOraSosire.setCellValueFactory(new PropertyValueFactory<>(PROP_ORA_SOSIRE));
    }

    @FXML
    public void adaugaZbor() throws Exception {
        App.setRoot("creareZbor");
    }

    @FXML
    public void editeazaZbor() throws IOException {
        Zbor zborSelectat = tabelZboruri.getSelectionModel().getSelectedItem();
        if (zborSelectat == null) {
            afiseazaAtentionare("Niciun zbor selectat", 
                              "Te rog să selectezi un zbor din tabel pentru a-l edita.");
            return;
        }
        
        salveazaDateZborPentruEditare(zborSelectat);
        App.setRoot("editareZbor");
    }

    private void salveazaDateZborPentruEditare(Zbor zbor) {
        Map<String, String> dateUtilizator = App.getDateUtilizator();
        dateUtilizator.clear();
        dateUtilizator.put(PROP_ID, zbor.getId());
        dateUtilizator.put(PROP_ORIGINE, zbor.getOrigine());
        dateUtilizator.put(PROP_DESTINATIE, zbor.getDestinatie());
        dateUtilizator.put(PROP_DATA_PLECARE, zbor.getDataPlecare());
        dateUtilizator.put(PROP_ORA_PLECARE, zbor.getOraPlecare());
        dateUtilizator.put(PROP_DATA_SOSIRE, zbor.getDataSosire());
        dateUtilizator.put(PROP_ORA_SOSIRE, zbor.getOraSosire());
        dateUtilizator.put(PROP_MODEL_AVION, zbor.getModelAvion());
        dateUtilizator.put(PROP_LOCURI_LIBERE, String.valueOf(zbor.getLocuriLibere()));
        dateUtilizator.put(PROP_PRET, String.valueOf(zbor.getPret()));
    }

    @FXML
    public void stergeZbor() throws Exception {
        Zbor zborSelectat = tabelZboruri.getSelectionModel().getSelectedItem();
        if (zborSelectat == null) {
            afiseazaAtentionare("Niciun zbor selectat", 
                              "Te rog să selectezi un zbor din tabel pentru a-l șterge.");
            return;
        }
        
        try {
            String url = URL_SERVER + "/zboruri/stergereZbor/" + zborSelectat.getId();
            URI uri = new URI(url);
            HttpURLConnection conexiune = (HttpURLConnection) uri.toURL().openConnection();
            conexiune.setRequestMethod("DELETE");
            
            if (conexiune.getResponseCode() == HttpURLConnection.HTTP_OK) {
                afiseazaSucces("Zbor șters cu succes", 
                             "Zborul a fost șters cu succes din sistem.");
            } else {
                afiseazaEroare("Eroare la ștergerea zborului",
                             "A apărut o eroare la ștergerea zborului. Te rog să încerci din nou.");
            }
            incarcaZboruri();
        } catch (Exception e) {
            jurnal.log(Level.SEVERE, "Eroare la ștergerea zborului: {0}", e.getMessage());
            afiseazaEroare("Eroare la ștergerea zborului",
                         "A apărut o eroare la ștergerea zborului: " + e.getMessage());
        }
    }

    @FXML
    public void reincarcaZboruri() {
        incarcaZboruri();
    }

    @FXML
    public void revinoPaginaPrincipala() throws Exception {
        App.setRoot("paginaPrincipalaAdmin");
    }
    
    private void incarcaZboruri() {
        HttpURLConnection conexiune = null;
        try {
            jurnal.info("Se încearcă încărcarea zborurilor de la server...");
            URI uri = new URI(URL_SERVER + "/zboruri/populareLista");
            conexiune = (HttpURLConnection) uri.toURL().openConnection();
            configureazaConexiuneGET(conexiune);

            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns != HttpURLConnection.HTTP_OK) {
                gestioneazaEroareIncarcareDate(conexiune, codRaspuns);
                return;
            }

            String raspunsJSON = citesteDateRaspuns(conexiune);
            if (raspunsJSON.trim().equals("[]")) {
                jurnal.info("Serverul a returnat o listă goală de zboruri");
                tabelZboruri.setItems(FXCollections.observableArrayList());
                return;
            }
            
            proceseazaDateZboruri(raspunsJSON);
            
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare de conexiune la server: {0}", e.getMessage());
            afiseazaEroare("Eroare de conexiune", 
                         "Nu s-a putut stabili conexiunea cu serverul. Te rog să verifici conexiunea și să încerci din nou.");
        } catch (Exception e) {
            jurnal.log(Level.SEVERE, "Eroare la încărcarea zborurilor: {0}", e.getMessage());
            afiseazaEroare("Eroare la încărcarea datelor", 
                         "A apărut o eroare la încărcarea datelor. Te rog să încerci din nou.");
        } finally {
            if (conexiune != null) {
                conexiune.disconnect();
            }
        }
    }

    private void configureazaConexiuneGET(HttpURLConnection conexiune) throws Exception {
        conexiune.setRequestMethod("GET");
        conexiune.setConnectTimeout(Integer.parseInt(TIMP_EXPIRARE));
        conexiune.setReadTimeout(Integer.parseInt(TIMP_EXPIRARE));
        conexiune.setRequestProperty("Accept", "application/json");
        conexiune.setRequestProperty("User-Agent", "JavaFX Client");
    }

    private void gestioneazaEroareIncarcareDate(HttpURLConnection conexiune, int codRaspuns) {
        String raspunsEroare = null;
        try (InputStream fluxEroare = conexiune.getErrorStream()) {
            if (fluxEroare != null) {
                raspunsEroare = new String(fluxEroare.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            jurnal.severe("Nu s-a putut citi fluxul de eroare: " + e.getMessage());
        }
        
        jurnal.severe("Eroare server: " + codRaspuns + ", Răspuns: " + raspunsEroare);
        String mesajEroare = raspunsEroare != null ? raspunsEroare : "Nu s-au putut încărca datele de la server.";
        afiseazaEroare("Eroare la încărcarea datelor", 
                     mesajEroare + " (Cod: " + codRaspuns + ")");
    }

    private String citesteDateRaspuns(HttpURLConnection conexiune) throws IOException {
        try (InputStream fluxIntrare = conexiune.getInputStream();
             Scanner scanner = new Scanner(fluxIntrare, StandardCharsets.UTF_8)) {
            if (!scanner.hasNext()) {
                throw new IOException("Răspuns gol de la server");
            }
            String raspuns = scanner.useDelimiter("\\A").next();
            if (raspuns.isEmpty()) {
                throw new IOException("Răspuns JSON gol");
            }
            return raspuns;
        }
    }

    private void proceseazaDateZboruri(String dateJSON) {
        try {
            JSONArray arrayZboruri = new JSONArray(dateJSON);
            ObservableList<Zbor> listaZboruri = FXCollections.observableArrayList();

            for (int i = 0; i < arrayZboruri.length(); i++) {
                try {
                    JSONObject obiectZbor = arrayZboruri.getJSONObject(i);
                    logProcesareZbor(i, obiectZbor);
                    
                    Zbor zbor = construiesteZborDinJSON(obiectZbor, i);
                    if (zbor != null) {
                        listaZboruri.add(zbor);
                        logZborProcesat(zbor.getId());
                    }
                } catch (Exception e) {
                    jurnal.warning("Eroare la procesarea zborului " + (i + 1) + ": " + e.getMessage());
                }
            }

            actualizeazaInterfataUtilizator(listaZboruri);
            logTotalZboruriIncarcate(listaZboruri.size());
            
        } catch (Exception e) {
            logEroareJSON(e.getMessage());
            throw new RuntimeException("Eroare la procesarea datelor zborurilor: " + e.getMessage());
        }
    }

    private Zbor construiesteZborDinJSON(JSONObject obiectZbor, int index) {
        if (!obiectZbor.has("id") || obiectZbor.isNull("id")) {
            logZborFaraId(index);
            return null;
        }
        
        try {
            Zbor zbor = new Zbor.Constructor()
                .setId(obiectZbor.optString(PROP_ID, ""))
                .setOrigine(obiectZbor.optString(PROP_ORIGINE, ""))
                .setDestinatie(obiectZbor.optString(PROP_DESTINATIE, ""))
                .setDataPlecare(obiectZbor.optString(PROP_DATA_PLECARE, ""))
                .setOraPlecare(obiectZbor.optString(PROP_ORA_PLECARE, ""))
                .setDataSosire(obiectZbor.optString(PROP_DATA_SOSIRE, ""))
                .setOraSosire(obiectZbor.optString(PROP_ORA_SOSIRE, ""))
                .setModelAvion(obiectZbor.optString(PROP_MODEL_AVION, ""))
                .setLocuriLibere(obiectZbor.optInt(PROP_LOCURI_LIBERE, 0))
                .setPret(obiectZbor.optDouble(PROP_PRET, 0.0))
                .construieste();

            if (zbor.getOrigine() == null || zbor.getOrigine().isEmpty() || 
                zbor.getDestinatie() == null || zbor.getDestinatie().isEmpty()) {
                logZborInvalid(index, obiectZbor);
                return null;
            }

            return zbor;
        } catch (Exception e) {
            logEraoareConstructieZbor(index, e.getMessage());
            return null;
        }
    }

    private void actualizeazaInterfataUtilizator(ObservableList<Zbor> listaZboruri) {
        if (listaZboruri.isEmpty()) {
            jurnal.warning("Nu s-au găsit zboruri valide în răspuns");
        } else {
            jurnal.info("S-au încărcat cu succes " + listaZboruri.size() + " zboruri");
        }
        
        Platform.runLater(() -> tabelZboruri.setItems(listaZboruri));
    }

    private void afiseazaEroare(String mesaj) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle(TITLU_EROARE);
            alerta.setHeaderText(TITLU_EROARE);
            alerta.setContentText(mesaj);
            alerta.showAndWait();
        });
    }

    private void afiseazaEroare(String antet, String mesaj) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle(TITLU_EROARE);
            alerta.setHeaderText(antet);
            alerta.setContentText(mesaj);
            alerta.showAndWait();
        });
    }

    private void afiseazaSucces(String antet, String continut) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(TITLU_SUCCES);
        alerta.setHeaderText(antet);
        alerta.setContentText(continut);
        alerta.showAndWait();
    }

    private void afiseazaAtentionare(String antet, String continut) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(TITLU_ATENTIONARE);
        alerta.setHeaderText(antet);
        alerta.setContentText(continut);
        alerta.showAndWait();
    }
}
