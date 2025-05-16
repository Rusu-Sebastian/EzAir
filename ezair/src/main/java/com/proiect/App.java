package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final Map<String, String> userData = new HashMap<>();
    private static final String CONNECTION_ERROR_SCENE = "eroareConexiune";

    public static Map<String, String> getUserData() {
        return userData;
    }

    static Scene scene;

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) {
        try {
            // Create a connection to the server
            String serverUrl = System.getProperty("server.url", "http://localhost:3000");
            URI uri = new URI(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // Set timeout to 5 seconds
            connection.setReadTimeout(5000);

            // Connect and check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Server is responding.");
                setScene(stage, "paginaPrincipalaAdmin"); //SCENA DE INCEPUT DE AICI O POTI SCHIMBA PENTRU TESTARE AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA CA SA VEZI BINE
            } else {
                setScene(stage, CONNECTION_ERROR_SCENE);
                setScene(stage, CONNECTION_ERROR_SCENE);
            }
        } catch (java.net.URISyntaxException | java.net.MalformedURLException e) {
            logger.log(Level.SEVERE, "Invalid server URL", e);
            setScene(stage, CONNECTION_ERROR_SCENE);
        } catch (java.io.IOException e) {
            logger.log(Level.SEVERE, "I/O error while connecting to the server", e);
            setScene(stage, CONNECTION_ERROR_SCENE);
        }
    }

    private static void setScene(Stage stage, String fxml) {
        try {
            scene = new Scene(loadFXML(fxml), 1144, 640);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load FXML: " + fxml, e);
        }
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}