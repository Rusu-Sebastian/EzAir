package com.proiect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class CreareContController {

    private static final Logger logger = Logger.getLogger(CreareContController.class.getName());

    @FXML
    private void backCreareContInceput() throws IOException {
        logger.info("Navigating back to login page.");
        App.setRoot("login");
    }

    @FXML
    private void backCreareContFinal() throws IOException {
        logger.info("Navigating back to creareContInceput page.");
        App.setRoot("creareContInceput");
    }

    @FXML
    private void cancelCreareCont() throws IOException {
        logger.info("Canceling account creation and navigating back to login page.");
        App.setRoot("login");
    }

    @FXML
    private void creareContInceput() throws IOException {
        try {
            String nume = ((javafx.scene.control.TextField) App.scene.lookup("#nume")).getText();
            String prenume = ((javafx.scene.control.TextField) App.scene.lookup("#prenume")).getText();

            if (nume.isEmpty() || prenume.isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#ziuaNasterii")).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#lunaNasterii")).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scene.lookup("#anNasterii")).getText().isEmpty()) {
                logger.warning("All fields are mandatory.");
                return;
            }

            if (((javafx.scene.control.TextField) App.scene.lookup("#ziuaNasterii")).getText().length() > 2 ||
                ((javafx.scene.control.TextField) App.scene.lookup("#lunaNasterii")).getText().length() > 2 ||
                ((javafx.scene.control.TextField) App.scene.lookup("#anNasterii")).getText().length() > 4) {
                logger.warning("Invalid birth date format.");
                return;
            }

            int ziNastere = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#ziuaNasterii")).getText());
            int lunaNastere = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#lunaNasterii")).getText());
            int anNastere = Integer.parseInt(((javafx.scene.control.TextField) App.scene.lookup("#anNasterii")).getText());
            int currentYear = java.time.Year.now().getValue();

            if (ziNastere > 31 || lunaNastere > 12 || anNastere < 1900 || anNastere > currentYear) {
                logger.warning("Invalid birth date.");
                return;
            }

            String dataNasterii = ziNastere + "/" + lunaNastere + "/" + anNastere;
            creareUserPartial(nume, prenume, dataNasterii);
            App.setRoot("creareContFinal");
            App.getUserData().put("nume", nume);
            App.getUserData().put("prenume", prenume);
            App.getUserData().put("dataNasterii", dataNasterii);
            logger.info("User partial data saved successfully.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during account creation step 1.", e);
        }
    }

    private User creareUserPartial(String nume, String prenume, String dataNasterii) {
        return new User(nume, prenume, dataNasterii, null, null, null, null);
    }

    @FXML
    private void creareContFinal() throws IOException {
        try {
            String username = ((javafx.scene.control.TextField) App.scene.lookup("#username")).getText();
            String parola = ((javafx.scene.control.TextField) App.scene.lookup("#parola")).getText();
            String confirmareParola = ((javafx.scene.control.TextField) App.scene.lookup("#confirmareParola")).getText();
            String email = ((javafx.scene.control.TextField) App.scene.lookup("#email")).getText();

           if (username.isEmpty() || parola.isEmpty() || confirmareParola.isEmpty() || email.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenție");
            alert.setHeaderText("Câmpuri incomplete");
            alert.setContentText("Toate câmpurile sunt obligatorii.");
            alert.showAndWait();
            return;
        }

            if (parola.length() < 8) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Atenție");
                alert.setHeaderText("Parolă prea scurtă");
                alert.setContentText("Parola trebuie să aibă cel puțin 8 caractere.");
                alert.showAndWait();
                return;
            }

            if (!parola.equals(confirmareParola)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Atenție");
                alert.setHeaderText("Parolele nu coincid");
                alert.setContentText("Te rog să introduci aceeași parolă în ambele câmpuri.");
                alert.showAndWait();
                return;
            }

            if (!email.contains("@")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Atenție");
                alert.setHeaderText("Email invalid");
                alert.setContentText("Te rog să introduci un email valid.");
                alert.showAndWait();
                return;
            }

            User utilizatorNou = new User(
                (String) App.getUserData().get("nume"),
                (String) App.getUserData().get("prenume"),
                (String) App.getUserData().get("dataNasterii"),
                email,
                username,
                parola,
                false, // Implicit, utilizatorul nu este admin
                null // ID-ul va fi generat de server
                
            );

            String url = "http://localhost:3000/users/creareCont";
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            httpClient.setRequestMethod("POST");
            httpClient.setRequestProperty("Content-Type", "application/json");
            httpClient.setDoOutput(true);

            String jsonInputString = String.format(
                "{\"nume\": \"%s\", \"prenume\": \"%s\", \"dataNasterii\": \"%s\", \"email\": \"%s\", \"username\": \"%s\", \"parola\": \"%s\", \"admin\": %b}",
                utilizatorNou.getNume(), utilizatorNou.getPrenume(), utilizatorNou.getDataNasterii(),
                utilizatorNou.getEmail(), utilizatorNou.getUsername(), utilizatorNou.getParola(), utilizatorNou.isAdmin()
            );

            try (OutputStream os = httpClient.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = httpClient.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Account created successfully.");
                App.setRoot("login");
            } else {
                logger.warning(String.format("Error creating account. Response code: %d", responseCode));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server connection error.", e);
            App.setRoot("eroareConexiune");
        }
    }
}
