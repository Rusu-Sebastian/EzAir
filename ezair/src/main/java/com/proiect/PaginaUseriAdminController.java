package com.proiect;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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
    @FXML
    private TableView<User> tabelUseri;
    @FXML
    private TableColumn<User, String> coloanaUsername;
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
    private void editareUser() throws Exception {
        
    }

    @FXML
    private void deleteUser() throws Exception {
        User userSelectat = tabelUseri.getSelectionModel().getSelectedItem();
        if (userSelectat == null) {
            // Afișează un pop-up pentru utilizatorul neales
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenție");
            alert.setHeaderText("Niciun utilizator selectat");
            alert.setContentText("Te rog să selectezi un utilizator din tabel pentru a-l șterge.");
            alert.showAndWait();
            return;
        }

        String idUser = userSelectat.getId();
        String url = "http://localhost:3000/users/" + idUser;
        HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
        httpClient.setRequestMethod("DELETE");
        int responseCode = httpClient.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Afișează un pop-up pentru ștergere reușită
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succes");
            alert.setHeaderText("Utilizator șters");
            alert.setContentText("Utilizatorul a fost șters cu succes.");
            alert.showAndWait();

            tabelUseri.getItems().remove(userSelectat); // Elimină utilizatorul din tabel
            incarcaDate(); // Reîncarcă datele
        } else {
            // Afișează un pop-up pentru eroare
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Eroare");
            alert.setHeaderText("Eroare la ștergerea utilizatorului");
            alert.setContentText("Cod răspuns: " + responseCode);
            alert.showAndWait();
        }
    }

    @FXML
    private void reload() throws Exception {
        App.setRoot("paginaPrincipalaAdmin");
    }

    @FXML
    private void initialize() {
        // Configurează coloanele
        coloanaUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        coloanaParola.setCellValueFactory(new PropertyValueFactory<>("parola"));
        coloanaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        coloanaNume.setCellValueFactory(new PropertyValueFactory<>("nume"));
        coloanaPrenume.setCellValueFactory(new PropertyValueFactory<>("prenume"));
        coloanaDataNasterii.setCellValueFactory(new PropertyValueFactory<>("dataNasterii")); // Corect
        coloanaAdmin.setCellValueFactory(new PropertyValueFactory<>("admin"));
        coloanaId.setCellValueFactory(new PropertyValueFactory<>("id")); // Corect

        // Încarcă datele în tabel
        incarcaDate();
    }

    private void incarcaDate() {
        try {
            String url = "http://localhost:3000/users/populareLista";
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            httpClient.setRequestMethod("GET");

            int responseCode = httpClient.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream is = httpClient.getInputStream();
                     Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                    String jsonResponse = scanner.useDelimiter("\\A").next();

                    JSONArray usersArray = new JSONArray(jsonResponse);
                    ObservableList<User> users = FXCollections.observableArrayList();

                    for (int i = 0; i < usersArray.length(); i++) {
                        JSONObject userJson = usersArray.getJSONObject(i);
                        User user = new User(
                            userJson.getString("nume"),
                            userJson.getString("prenume"),
                            userJson.getString("dataNasterii"),
                            userJson.getString("email"),
                            userJson.getString("username"),
                            userJson.getString("parola"),
                            userJson.getBoolean("admin"),
                            userJson.getString("id")
                        );
                        users.add(user);
                    }

                    tabelUseri.setItems(users);
                }
            } else {
                System.err.println("Eroare la încărcarea datelor. Cod răspuns: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
