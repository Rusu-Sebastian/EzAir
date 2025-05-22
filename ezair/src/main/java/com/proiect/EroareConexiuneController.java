package com.proiect;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class EroareConexiuneController {

    private static final Logger logger = Logger.getLogger(EroareConexiuneController.class.getName());
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    private static final String SERVER_URL = "http://localhost:3000";
    
    @FXML
    private Label statusText;
    
    @FXML
    private Button butonReincercare;

    @FXML
    private void reincarcare(ActionEvent event) {
        // Disable the retry button while checking connection
        butonReincercare.setDisable(true);
        statusText.setText("Se încearcă reconectarea la server...");
        
        // Run connection check in background thread
        new Thread(() -> {
            try {
                URI uri = new URI(SERVER_URL);
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(CONNECTION_TIMEOUT);
                connection.setInstanceFollowRedirects(true);

                try {
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        logger.info("Server connection restored");
                        Platform.runLater(() -> {
                            try {
                                App.setRoot("login");
                            } catch (Exception e) {
                                updateStatus("Eroare la încărcarea paginii de autentificare");
                                logger.log(Level.SEVERE, "Failed to load login page", e);
                            }
                        });
                    } else {
                        updateStatus("Serverul nu răspunde corect (cod " + responseCode + ")");
                        logger.log(Level.WARNING, "Server returned unexpected response code: {0}", responseCode);
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                if (errorMessage.contains("Connection refused")) {
                    updateStatus("Serverul nu este pornit sau nu este accesibil");
                } else if (errorMessage.contains("timed out")) {
                    updateStatus("Conexiunea la server durează prea mult");
                } else {
                    updateStatus("Eroare de conexiune: " + errorMessage);
                }
                logger.log(Level.SEVERE, "Connection error", e);
            }
        }).start();
    }
    
    private void updateStatus(String message) {
        Platform.runLater(() -> {
            statusText.setText(message);
            butonReincercare.setDisable(false);
        });
    }
    
    @FXML
    private void initialize() {
        // Clear any previous status message
        statusText.setText("");
    }
}