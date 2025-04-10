package com.proiect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;

class User {
    String nume;
    String prenume;
    String dataNasterii;
    String email;
    String username;
    String parola;

    public User(String nume, String prenume, String dataNasterii, String email, String username, String parola) {
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.email = email;
        this.username = username;
        this.parola = parola;
    }
}

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
        return new User(nume, prenume, dataNasterii, null, null, null);
    }

    @FXML
    private void creareContFinal() throws IOException {
        try {
            String username = ((javafx.scene.control.TextField) App.scene.lookup("#username")).getText();
            String parola = ((javafx.scene.control.TextField) App.scene.lookup("#parola")).getText();
            String confirmareParola = ((javafx.scene.control.TextField) App.scene.lookup("#confirmareParola")).getText();
            String email = ((javafx.scene.control.TextField) App.scene.lookup("#email")).getText();

            if (username.isEmpty() || parola.isEmpty() || confirmareParola.isEmpty() || email.isEmpty()) {
                logger.warning("All fields are mandatory.");
                return;
            }

            if (parola.length() < 8) {
                logger.warning("Password must be at least 8 characters long.");
                return;
            }

            if (!parola.equals(confirmareParola)) {
                logger.warning("Passwords do not match.");
                return;
            }

            if (!email.contains("@")) {
                logger.warning("Invalid email format.");
                return;
            }

            User utilizatorNou = new User(
                (String) App.getUserData().get("nume"),
                (String) App.getUserData().get("prenume"),
                (String) App.getUserData().get("dataNasterii"),
                email,
                username,
                parola
            );

            String url = "http://localhost:3000/users/creareCont";
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            httpClient.setRequestMethod("POST");
            httpClient.setRequestProperty("Content-Type", "application/json");
            httpClient.setDoOutput(true);

            String jsonInputString = String.format(
                "{\"nume\": \"%s\", \"prenume\": \"%s\", \"dataNasterii\": \"%s\", \"email\": \"%s\", \"username\": \"%s\", \"parola\": \"%s\"}",
                utilizatorNou.nume, utilizatorNou.prenume, utilizatorNou.dataNasterii, utilizatorNou.email, utilizatorNou.username, utilizatorNou.parola
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
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning(String.format("Error creating account. Response code: %d", responseCode));
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server connection error.", e);
            App.setRoot("eroareConexiune");
        }
    }
}
