package com.proiect;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class EroareConexiuneController {

    private static final Logger logger = Logger.getLogger(App.class.getName());

    @FXML
    private void reincarcare(ActionEvent event) {
        try {
            // Create a connection to the server
            URI uri = new URI("http://localhost:3000");
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // Set timeout to 5 seconds
            connection.setReadTimeout(5000);

            // Connect and check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Server is responding.");
                App.setRoot("login");
            } else {
                logger.log(Level.WARNING, "Server is not responding. Response code: {0}", responseCode);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while connecting to the server", e);
        }
    }
}