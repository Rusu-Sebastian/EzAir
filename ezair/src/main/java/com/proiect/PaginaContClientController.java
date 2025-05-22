package com.proiect;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

public class PaginaContClientController {
    private static final Logger logger = Logger.getLogger(PaginaContClientController.class.getName());
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    private static final String USER_ID_KEY = "userId";
    private static final String SERVER_BASE_URL = "http://localhost:3000";
    private static final String EROARE_CONEXIUNE = "Eroare la configurarea conexiunii.";

    @FXML private Label labelNume;
    @FXML private Label labelEmail;
    @FXML private Label labelUsername;
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
            String userId = (String) App.getUserData().get(USER_ID_KEY);
            java.net.URI uri = new java.net.URI(SERVER_BASE_URL + "/users/" + userId);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JSONObject user = new JSONObject(new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                labelNume.setText(user.getString("nume") + " " + user.getString("prenume"));
                labelEmail.setText(user.getString("email"));
                labelUsername.setText(user.getString("username"));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Eroare la încărcarea informațiilor utilizatorului", e);
            afiseazaEroare("Nu s-au putut încărca informațiile contului.");
        }
    }

    private void incarcaBilete() {
        try {
            String userId = (String) App.getUserData().get(USER_ID_KEY);
            java.net.URI uri = new java.net.URI(SERVER_BASE_URL + "/users/" + userId + "/bilete");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JSONArray bilete = new JSONArray(response);
                ObservableList<Bilet> listaBilete = FXCollections.observableArrayList();
                
                for (int i = 0; i < bilete.length(); i++) {
                    JSONObject bilet = bilete.getJSONObject(i);
                    Bilet b = new Bilet(
                        bilet.getString("id"),
                        bilet.getString(USER_ID_KEY),
                        bilet.getString("zborId"),
                        bilet.getString("detaliiZbor"),
                        LocalDateTime.parse(bilet.getString("dataZbor")),
                        bilet.getString("stare"),
                        bilet.getDouble("pret")
                    );
                    listaBilete.add(b);
                }
                
                tabelBilete.setItems(listaBilete);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Eroare la încărcarea biletelor", e);
            afiseazaEroare("Nu s-au putut încărca biletele.");
        }
    }

    @FXML
    private void anulareBilet() {
        Bilet biletSelectat = tabelBilete.getSelectionModel().getSelectedItem();
        if (biletSelectat == null) {
            afiseazaEroare("Te rog să selectezi un bilet pentru anulare.");
            return;
        }

        try {
            java.net.URI uri = new java.net.URI(SERVER_BASE_URL + "/bilete/" + biletSelectat.getId() + "/anulare");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle(TITLU_SUCCES);
                alerta.setHeaderText("Bilet anulat");
                alerta.setContentText("Biletul a fost anulat cu succes.");
                alerta.showAndWait();
                incarcaBilete();
            } else {
                afiseazaEroare("Nu s-a putut anula biletul. Te rugăm să încerci din nou.");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Eroare la anularea biletului", e);
            afiseazaEroare("Nu s-a putut anula biletul.");
        }
    }

    @FXML
    private void schimbaData() {
        Bilet biletSelectat = tabelBilete.getSelectionModel().getSelectedItem();
        if (biletSelectat == null) {
            afiseazaEroare("Te rog să selectezi un bilet pentru schimbarea datei.");
            return;
        }

        Dialog<LocalDateTime> dialog = new Dialog<>();
        dialog.setTitle("Schimbă data zborului");
        dialog.setHeaderText("Alege noua dată pentru zbor");

        ButtonType buttonTypeOk = new ButtonType("Confirmă", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker();
        ComboBox<String> timeComboBox = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            timeComboBox.getItems().add(String.format("%02d:00", i));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Data:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Ora:"), 0, 1);
        grid.add(timeComboBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOk && datePicker.getValue() != null && timeComboBox.getValue() != null) {
                return LocalDateTime.of(
                    datePicker.getValue(),
                    LocalTime.parse(timeComboBox.getValue())
                );
            }
            return null;
        });

        Optional<LocalDateTime> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                java.net.URI uri = new java.net.URI(SERVER_BASE_URL + "/bilete/" + biletSelectat.getId() + "/modificare-data");
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonInputString = String.format("{\"dataNoua\": \"%s\"}", result.get().toString());
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
                }

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                    alerta.setTitle(TITLU_SUCCES);
                    alerta.setHeaderText("Data modificată");
                    alerta.setContentText("Data zborului a fost modificată cu succes.");
                    alerta.showAndWait();
                    incarcaBilete();
                } else {
                    afiseazaEroare("Nu s-a putut modifica data zborului. Te rugăm să încerci din nou.");
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Eroare la modificarea datei zborului", e);
                afiseazaEroare("Nu s-a putut modifica data zborului.");
            }
        }
    }

    @FXML
    private void editareInformatii() throws Exception {
        App.setRoot("editareContClient");
    }

    @FXML 
    private void notificari() throws Exception {
        App.setRoot("setariNotificari");
    }

    @FXML
    private void schimbaParola() throws Exception {
        App.setRoot("schimbaParola");
    }

    @FXML
    private void inapoi() throws Exception {
        App.setRoot("paginaPrincipalaUser");
    }

    private void afiseazaEroare(String mesaj) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(TITLU_EROARE);
        alerta.setHeaderText(TITLU_EROARE);
        alerta.setContentText(mesaj);
        alerta.showAndWait();
    }
}
