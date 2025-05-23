package com.proiect;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PaginaUseriAdminController {
    private static final Logger logger = Logger.getLogger(PaginaUseriAdminController.class.getName());
    private static final String TITLU_ATENTIONARE = "Atenție";
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    private static final String URL_SERVER = "http://localhost:3000";
    private static final int TIMP_EXPIRARE_CONEXIUNE = 5000; // 5 secunde

    private static final String EROARE_STERGERE = "Eroare la ștergerea utilizatorului";
    private static final String EROARE_INCARCARE = "Eroare la încărcarea datelor";
    private static final String EROARE_SERVER = "A apărut o eroare la comunicarea cu serverul. Te rog să încerci din nou.";
    private static final String SELECTEAZA_UTILIZATOR = "Te rog să selectezi un utilizator din tabel";
    
    @FXML
    private TableView<User> tabelUtilizatori;
    @FXML
    private TableColumn<User, String> coloanaNumeUtilizator;
    @FXML
    private TableColumn<User, String> coloanaParola;
    @FXML
    private TableColumn<User, String> coloanaEmail;
    @FXML
    private TableColumn<User, String> coloanaNume;
    @FXML
    private TableColumn<User, String> coloanaPrenume;
    @FXML
    private TableColumn<User, String> coloanaDataNasterii;
    @FXML
    private TableColumn<User, Boolean> coloanaAdmin;
    @FXML
    private TableColumn<User, String> coloanaId;

    @FXML
    public void adaugareUser() throws IOException {
        App.setRoot("creareContInceputAdmin");
    }

    @FXML
    public void editareUtilizator() throws IOException {
        User utilizatorSelectat = tabelUtilizatori.getSelectionModel().getSelectedItem();
        logger.log(Level.INFO, "Încercare editare utilizator");
        
        if (utilizatorSelectat == null) {
            logger.log(Level.WARNING, "Niciun utilizator selectat pentru editare");
            afiseazaAlerta(Alert.AlertType.WARNING, 
                          TITLU_ATENTIONARE,
                          "Niciun utilizator selectat",
                          SELECTEAZA_UTILIZATOR + " pentru a-l edita.");
            return;
        }

        String idUtilizator = utilizatorSelectat.getId();
        logger.log(Level.INFO, "ID utilizator selectat pentru editare: {0}", idUtilizator);
        App.setRoot("editareUtilizator");
    }

    @FXML
    public void stergereUtilizator() {
        User utilizatorSelectat = tabelUtilizatori.getSelectionModel().getSelectedItem();
        if (utilizatorSelectat == null) {
            afiseazaAlerta(Alert.AlertType.WARNING,
                          TITLU_ATENTIONARE,
                          "Niciun utilizator selectat",
                          SELECTEAZA_UTILIZATOR + " pentru a-l șterge.");
            return;
        }

        try {
            String idUtilizator = utilizatorSelectat.getId();
            HttpURLConnection conexiune = (HttpURLConnection) URI.create(URL_SERVER + "/users/" + idUtilizator)
                                                               .toURL()
                                                               .openConnection();
            conexiune.setRequestMethod("DELETE");
            conexiune.setConnectTimeout(TIMP_EXPIRARE_CONEXIUNE);
            conexiune.setReadTimeout(TIMP_EXPIRARE_CONEXIUNE);
            
            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                afiseazaAlerta(Alert.AlertType.INFORMATION,
                              TITLU_SUCCES,
                              "Utilizator șters",
                              "Utilizatorul a fost șters cu succes.");
                tabelUtilizatori.getItems().remove(utilizatorSelectat);
                actualizeazaDate();
            } else {
                afiseazaAlerta(Alert.AlertType.ERROR,
                              TITLU_EROARE,
                              EROARE_STERGERE,
                              "Cod răspuns: " + codRaspuns);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, EROARE_STERGERE, e);
            afiseazaAlerta(Alert.AlertType.ERROR,
                          TITLU_EROARE,
                          EROARE_STERGERE,
                          EROARE_SERVER);
        }
    }

    @FXML
    public void reincarca() {
        actualizeazaDate();
    }

    @FXML
    public void inapoiLaTablouDeBord() throws IOException {
        App.setRoot("paginaPrincipalaAdmin");
    }

    @FXML
    public void initialize() {
        configurareColoane();
        actualizeazaDate();
    }

    private void configurareColoane() {
        coloanaNumeUtilizator.setCellValueFactory(new PropertyValueFactory<>("username"));
        coloanaParola.setCellValueFactory(new PropertyValueFactory<>("parola"));
        coloanaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        coloanaNume.setCellValueFactory(new PropertyValueFactory<>("nume"));
        coloanaPrenume.setCellValueFactory(new PropertyValueFactory<>("prenume"));
        coloanaDataNasterii.setCellValueFactory(new PropertyValueFactory<>("dataNasterii"));
        coloanaAdmin.setCellValueFactory(new PropertyValueFactory<>("isAdmin"));
        coloanaId.setCellValueFactory(new PropertyValueFactory<>("id"));
    }

    private void actualizeazaDate() {
        try {
            HttpURLConnection conexiune = (HttpURLConnection) URI.create(URL_SERVER + "/users/populareLista")
                                                               .toURL()
                                                               .openConnection();
            conexiune.setRequestMethod("GET");
            conexiune.setConnectTimeout(TIMP_EXPIRARE_CONEXIUNE);
            conexiune.setReadTimeout(TIMP_EXPIRARE_CONEXIUNE);

            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                try (InputStream is = conexiune.getInputStream();
                     Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                    String jsonResponse = scanner.useDelimiter("\\A").next();
                    proceseazaDateUtilizatori(jsonResponse);
                }
            } else {
                afiseazaAlerta(Alert.AlertType.ERROR,
                              TITLU_EROARE,
                              EROARE_INCARCARE,
                              "Cod răspuns: " + codRaspuns);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, EROARE_INCARCARE, e);
            afiseazaAlerta(Alert.AlertType.ERROR,
                          TITLU_EROARE,
                          EROARE_INCARCARE,
                          EROARE_SERVER);
        }
    }

    private void proceseazaDateUtilizatori(String jsonResponse) {
        JSONArray utilizatoriArray = new JSONArray(jsonResponse);
        ObservableList<User> utilizatori = FXCollections.observableArrayList();

        for (int i = 0; i < utilizatoriArray.length(); i++) {
            JSONObject utilizatorJson = utilizatoriArray.getJSONObject(i);
            User utilizator = new User(
                utilizatorJson.getString("nume"),
                utilizatorJson.getString("prenume"),
                utilizatorJson.getString("dataNasterii"),
                utilizatorJson.getString("email"),
                utilizatorJson.getString("username"),
                utilizatorJson.getString("parola"),
                utilizatorJson.getBoolean("admin"),
                utilizatorJson.getString("id"),
                utilizatorJson.getString("telefon")
            );
            utilizatori.add(utilizator);
        }

        tabelUtilizatori.setItems(utilizatori);
    }

    private void afiseazaAlerta(Alert.AlertType tip, String titlu, String antet, String continut) {
        Alert alerta = new Alert(tip);
        alerta.setTitle(titlu);
        alerta.setHeaderText(antet);
        alerta.setContentText(continut);
        alerta.showAndWait();
    }
}
