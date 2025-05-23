package com.proiect;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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

    @FXML
    public void initialize() {
        configureazaColoaneTabel();
        incarcaZboruri();
    }

    private void configureazaColoaneTabel() {
        coloanaOrigine.setCellValueFactory(new PropertyValueFactory<>("origine"));
        coloanaDestinatie.setCellValueFactory(new PropertyValueFactory<>("destinatie"));
        coloanaModelAvion.setCellValueFactory(new PropertyValueFactory<>("modelAvion"));
        coloanaLocuriLibere.setCellValueFactory(new PropertyValueFactory<>("locuriLibere"));
        coloanaPret.setCellValueFactory(new PropertyValueFactory<>("pret"));
        coloanaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        coloanaDataPlecare.setCellValueFactory(new PropertyValueFactory<>("dataPlecare"));
        coloanaOraPlecare.setCellValueFactory(new PropertyValueFactory<>("oraPlecare"));
        coloanaDataSosire.setCellValueFactory(new PropertyValueFactory<>("dataSosire"));
        coloanaOraSosire.setCellValueFactory(new PropertyValueFactory<>("oraSosire"));
    }

    @FXML
    private void adaugaZbor() throws Exception {
        App.setRoot("creareZbor");
    }

    @FXML
    private void editeazaZbor() throws IOException {
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
        dateUtilizator.put("id", zbor.getId());
        dateUtilizator.put("origine", zbor.getOrigine());
        dateUtilizator.put("destinatie", zbor.getDestinatie());
        dateUtilizator.put("dataPlecare", zbor.getDataPlecare());
        dateUtilizator.put("oraPlecare", zbor.getOraPlecare());
        dateUtilizator.put("dataSosire", zbor.getDataSosire());
        dateUtilizator.put("oraSosire", zbor.getOraSosire());
        dateUtilizator.put("modelAvion", zbor.getModelAvion());
        dateUtilizator.put("locuriLibere", String.valueOf(zbor.getLocuriLibere()));
        dateUtilizator.put("pret", String.valueOf(zbor.getPret()));
    }

    @FXML
    private void stergeZbor() throws Exception {
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
    public void incarcaZboruri() {
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
                    
                    Zbor zbor = construiesteZborDinJSON(obiectZbor, i);
                    if (zbor != null) {
                        listaZboruri.add(zbor);
                    }
                } catch (Exception e) {
                    jurnal.warning("Eroare la procesarea zborului " + (i + 1) + ": " + e.getMessage());
                }
            }

            actualizeazaInterfataUtilizator(listaZboruri);
            
        } catch (Exception e) {
            jurnal.severe("Eroare la parsarea JSON: " + e.getMessage());
            throw new RuntimeException("Eroare la procesarea datelor zborurilor: " + e.getMessage());
        }
    }

    private Zbor construiesteZborDinJSON(JSONObject obiectZbor, int index) {
        if (!obiectZbor.has("id") || obiectZbor.isNull("id")) {
            jurnal.warning("Se omite zborul cu ID lipsă la indexul " + index);
            return null;
        }
        
        try {
            Zbor zbor = new Zbor.Constructor()
                .setId(obiectZbor.optString("id", ""))
                .setOrigine(obiectZbor.optString("origine", ""))
                .setDestinatie(obiectZbor.optString("destinatie", ""))
                .setDataPlecare(obiectZbor.optString("dataPlecare", ""))
                .setOraPlecare(obiectZbor.optString("oraPlecare", ""))
                .setDataSosire(obiectZbor.optString("dataSosire", ""))
                .setOraSosire(obiectZbor.optString("oraSosire", ""))
                .setModelAvion(obiectZbor.optString("modelAvion", ""))
                .setLocuriLibere(obiectZbor.optInt("locuriLibere", 0))
                .setPret(obiectZbor.optDouble("pret", 0.0))
                .construieste();

            if (zbor.getOrigine() == null || zbor.getOrigine().isEmpty() || 
                zbor.getDestinatie() == null || zbor.getDestinatie().isEmpty()) {
                jurnal.warning("Zborul de la indexul " + index + " are origine sau destinație invalidă: " + obiectZbor);
                return null;
            }

            return zbor;
        } catch (Exception e) {
            jurnal.warning("Eroare la construirea zborului " + index + ": " + e.getMessage());
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

    @FXML
    private void revinoPaginaPrincipala() throws Exception {
        App.setRoot("paginaPrincipalaAdmin");
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

    private void afiseazaAtentionare(String antet, String continut) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(TITLU_ATENTIONARE);
        alerta.setHeaderText(antet);
        alerta.setContentText(continut);
        alerta.showAndWait();
    }
}
