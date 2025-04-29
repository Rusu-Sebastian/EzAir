package com.proiect;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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
    private void adaugareUser() throws Exception {
        App.setRoot("creareContInceputAdmin");
    }

    @FXML
    private void editareUser() throws Exception {
        
    }

    @FXML
    private void deleteUser() throws Exception {
        
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
        coloanaDataNasterii.setCellValueFactory(new PropertyValueFactory<>("dataNasterii"));
        coloanaAdmin.setCellValueFactory(new PropertyValueFactory<>("admin"));

        // Încarcă datele în tabel
        incarcaDate();
    }

    private void incarcaDate() {
        try {
            // URL-ul serverului
            String url = "http://localhost:3000/users/populareLista";
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            httpClient.setRequestMethod("GET");

            // Verifică răspunsul serverului
            int responseCode = httpClient.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Citește răspunsul JSON
                try (InputStream is = httpClient.getInputStream();
                     Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                    String jsonResponse = scanner.useDelimiter("\\A").next();

                    // Parsează răspunsul JSON
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
                            userJson.getBoolean("admin")
                        );
                        users.add(user);
                    }

                    // Adaugă utilizatorii în tabel
                    tabelUseri.setItems(users);
                }
            } else {
                System.err.println("Eroare la încărcarea datelor. Cod răspuns: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
