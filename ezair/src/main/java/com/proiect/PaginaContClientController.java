package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.proiect.config.ApiEndpoints;
import com.proiect.util.HttpUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class PaginaContClientController {
    private static final Logger jurnal = Logger.getLogger(PaginaContClientController.class.getName());
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    private static final String PAGINA_EDITARE = "editareContClient";
    private static final String PAGINA_NOTIFICARI = "setariNotificari";
    private static final String PAGINA_PAROLA = "schimbareParola";

    @FXML private Label labelNume;
    @FXML private Label labelEmail;
    @FXML private Label labelNumeUtilizator;
    @FXML private TableView<Bilet> tabelBilete;
    @FXML private TableColumn<Bilet, String> coloanaZbor;
    @FXML private TableColumn<Bilet, String> coloanaData;
    @FXML private TableColumn<Bilet, String> coloanaStare;
    private App app;

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void initialize() {
        app = App.getInstance();
        configureazaTabele();
        incarcaInformatiiUtilizator();
        incarcaBilete();
    }

    private void configureazaTabele() {
        coloanaZbor.setCellValueFactory(cellData -> cellData.getValue().zborProperty());
        coloanaData.setCellValueFactory(cellData -> cellData.getValue().dataProperty());
        coloanaStare.setCellValueFactory(cellData -> cellData.getValue().stareProperty());
    }

    private void incarcaInformatiiUtilizator() {
        String idUtilizator = (String) App.getDateUtilizator().get(ApiEndpoints.USER_ID_KEY);
        try {
            HttpURLConnection conexiune = HttpUtil.createConnection(
                String.format(ApiEndpoints.GET_USER, idUtilizator), 
                "GET"
            );

            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                String raspuns = new String(conexiune.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JSONObject utilizator = new JSONObject(raspuns);
                labelNume.setText(utilizator.getString("nume") + " " + utilizator.getString("prenume"));
                labelEmail.setText(utilizator.getString("email"));
                labelNumeUtilizator.setText(utilizator.getString("numeUtilizator"));
            } else {
                afiseazaEroare("Nu s-au putut încărca informațiile contului.");
            }
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la încărcarea informațiilor utilizatorului", e);
            afiseazaEroare("Nu s-au putut încărca informațiile contului.");
        }
    }

    private void incarcaBilete() {
        String idUtilizator = (String) App.getDateUtilizator().get(ApiEndpoints.USER_ID_KEY);
        try {
            HttpURLConnection conexiune = HttpUtil.createConnection(
                String.format(ApiEndpoints.USER_TICKETS, idUtilizator),
                "GET"
            );

            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                String raspuns = new String(conexiune.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JSONArray bilete = new JSONArray(raspuns);
                ObservableList<Bilet> listaBilete = FXCollections.observableArrayList();
                
                for (int i = 0; i < bilete.length(); i++) {
                    JSONObject bilet = bilete.getJSONObject(i);
                    LocalDateTime dataZbor = parseazaDataTimp(bilet.getString("dataZbor"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    
                    listaBilete.add(new Bilet(
                        bilet.getString("ruta"),
                        dataZbor.format(formatter),
                        bilet.getString("stare"),
                        bilet.getString("id")
                    ));
                }
                
                tabelBilete.setItems(listaBilete);
            } else {
                afiseazaEroare("Nu s-au putut încărca biletele.");
            }
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la încărcarea biletelor", e);
            afiseazaEroare("Nu s-au putut încărca biletele.");
        }
    }

    private LocalDateTime parseazaDataTimp(String sirDataTimp) {
        try {
            return LocalDateTime.parse(sirDataTimp);
        } catch (DateTimeParseException e) {
            jurnal.warning("Format dată invalid, încercând format personalizat");
            return parseazaDataTimpPersonalizat(sirDataTimp);
        }
    }

    private LocalDateTime parseazaDataTimpPersonalizat(String sirDataTimp) {
        // Format: dd/MM/yyyy HH:mm
        String[] parteDateTimp = sirDataTimp.split(" ");
        String[] parteData = parteDateTimp[0].split("/");
        String[] parteTimp = parteDateTimp[1].split(":");
        
        int zi = Integer.parseInt(parteData[0]);
        int luna = Integer.parseInt(parteData[1]);
        int an = Integer.parseInt(parteData[2]);
        int ora = Integer.parseInt(parteTimp[0]);
        int minut = Integer.parseInt(parteTimp[1]);
        
        return LocalDateTime.of(an, luna, zi, ora, minut, 0);
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void editareInformatii() {
        try {
            app.setRoot(PAGINA_EDITARE);
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea către editare cont", e);
            afiseazaEroare("Nu s-a putut deschide pagina de editare cont");
        }
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void notificari() {
        try {
            app.setRoot(PAGINA_NOTIFICARI);
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea către setări notificări", e);
            afiseazaEroare("Nu s-a putut deschide pagina de setări notificări");
        }
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void schimbaParola() {
        try {
            app.setRoot(PAGINA_PAROLA);
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea către schimbare parolă", e);
            afiseazaEroare("Nu s-a putut deschide pagina de schimbare parolă");
        }
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void inapoi() {
        try {
            app.setRoot("paginaPrincipalaUser");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea înapoi", e);
            afiseazaEroare("Nu s-a putut naviga înapoi la pagina principală");
        }
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void anulareBilet() {
        Bilet biletSelectat = tabelBilete.getSelectionModel().getSelectedItem();
        if (biletSelectat == null) {
            afiseazaEroare("Te rog să selectezi un bilet pentru anulare.");
            return;
        }
        
        Alert confirmare = new Alert(Alert.AlertType.CONFIRMATION);
        confirmare.setTitle("Confirmare anulare");
        confirmare.setHeaderText("Ești sigur că vrei să anulezi acest bilet?");
        confirmare.setContentText("Această acțiune nu poate fi anulată.");
        
        confirmare.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Implementează logica de anulare
                afiseazaSucces("Biletul a fost anulat cu succes.");
                incarcaBilete(); // Reîncarcă lista
            }
        });
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void schimbaData() {
        Bilet biletSelectat = tabelBilete.getSelectionModel().getSelectedItem();
        if (biletSelectat == null) {
            afiseazaEroare("Te rog să selectezi un bilet pentru a schimba data.");
            return;
        }
        
        try {
            // Stochează ID-ul biletului pentru pagina următoare
            App.getDateUtilizator().put("biletId", biletSelectat.getId());
            app.setRoot("schimbaDataZbor");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea către schimbare data", e);
            afiseazaEroare("Nu s-a putut deschide pagina de schimbare data");
        }
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
}
