package com.proiect;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
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
    private void adaugareUser() throws Exception {
        App.setRoot("creareContInceputAdmin");
    }

    @FXML
    private void editareUtilizator() throws Exception {
        User utilizatorSelectat = tabelUtilizatori.getSelectionModel().getSelectedItem();
        logger.info("Încercare editare utilizator");
        
        if (utilizatorSelectat == null) {
            logger.warning("Niciun utilizator selectat pentru editare");
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle(TITLU_ATENTIONARE);
            alerta.setHeaderText("Niciun utilizator selectat");
            alerta.setContentText("Te rog să selectezi un utilizator din tabel pentru a-l edita.");
            alerta.showAndWait();
            return;
        }

        String idUtilizator = utilizatorSelectat.getId();
        logger.info("ID utilizator selectat pentru editare: " + idUtilizator);
        logger.info("Date utilizator: username=" + utilizatorSelectat.getUsername() + 
                   ", email=" + utilizatorSelectat.getEmail() +
                   ", nume=" + utilizatorSelectat.getNume());

        // Salvăm utilizatorul în userData pentru a-l folosi în controllerul de editare
        App.getUserData().put("idUtilizator", idUtilizator);
        
        // Verifică dacă ID-ul a fost salvat corect
        String idSalvat = (String) App.getUserData().get("idUtilizator");
        logger.info("ID utilizator salvat în userData: " + idSalvat);

        App.setRoot("editareUtilizator");
    }

    @FXML
    private void stergereUtilizator() throws Exception {
        User utilizatorSelectat = tabelUtilizatori.getSelectionModel().getSelectedItem();
        if (utilizatorSelectat == null) {
            // Afișează un pop-up pentru utilizatorul neales
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle(TITLU_ATENTIONARE);
            alerta.setHeaderText("Niciun utilizator selectat");
            alerta.setContentText("Te rog să selectezi un utilizator din tabel pentru a-l șterge.");
            alerta.showAndWait();
            return;
        }

        String idUtilizator = utilizatorSelectat.getId();
        String url = "http://localhost:3000/users/" + idUtilizator;
        HttpURLConnection clientHttp = (HttpURLConnection) new URL(url).openConnection();
        clientHttp.setRequestMethod("DELETE");
        int codRaspuns = clientHttp.getResponseCode();

        if (codRaspuns == HttpURLConnection.HTTP_OK) {
            // Afișează un pop-up pentru ștergere reușită
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle(TITLU_SUCCES);
            alerta.setHeaderText("Utilizator șters");
            alerta.setContentText("Utilizatorul a fost șters cu succes.");
            alerta.showAndWait();

            tabelUtilizatori.getItems().remove(utilizatorSelectat); // Elimină utilizatorul din tabel
            incarcaDate(); // Reîncarcă datele
        } else {
            // Afișează un pop-up pentru eroare
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle(TITLU_EROARE);
            alerta.setHeaderText("Eroare la ștergerea utilizatorului");
            alerta.setContentText("Cod răspuns: " + codRaspuns);
            alerta.showAndWait();
        }
    }

    @FXML
    private void reincarca() throws Exception {
        incarcaDate();
    }

    @FXML
    private void inapoiLaTablouDeBord() throws Exception {
        App.setRoot("paginaPrincipalaAdmin");
    }

    @FXML
    private void initialize() {
        // Configurează coloanele
        coloanaNumeUtilizator.setCellValueFactory(new PropertyValueFactory<>("username"));
        coloanaParola.setCellValueFactory(new PropertyValueFactory<>("parola"));
        coloanaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        coloanaNume.setCellValueFactory(new PropertyValueFactory<>("nume"));
        coloanaPrenume.setCellValueFactory(new PropertyValueFactory<>("prenume"));
        coloanaDataNasterii.setCellValueFactory(new PropertyValueFactory<>("dataNasterii"));
        coloanaAdmin.setCellValueFactory(new PropertyValueFactory<>("isAdmin"));
        coloanaId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Încarcă datele în tabel
        incarcaDate();
    }

    private void incarcaDate() {
        try {
            String url = "http://localhost:3000/users/populareLista";
            HttpURLConnection clientHttp = (HttpURLConnection) new URL(url).openConnection();
            clientHttp.setRequestMethod("GET");

            int codRaspuns = clientHttp.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                try (InputStream is = clientHttp.getInputStream();
                     Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                    String jsonResponse = scanner.useDelimiter("\\A").next();

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
            } else {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle(TITLU_EROARE);
                alerta.setHeaderText("Eroare la încărcarea datelor");
                alerta.setContentText("Cod răspuns: " + codRaspuns);
                alerta.showAndWait();
            }
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle(TITLU_EROARE);
            alerta.setHeaderText("Eroare la încărcarea datelor");
            alerta.setContentText("A apărut o eroare la încărcarea datelor. Te rog să încerci din nou.");
            alerta.showAndWait();
            e.printStackTrace();
        }
    }
}
