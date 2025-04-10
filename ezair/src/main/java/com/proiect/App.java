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
            URI uri = new URI("http://localhost:3000");
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // Set timeout to 5 seconds
            connection.setReadTimeout(5000);

            // Connect and check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Server is responding.");
                setScene(stage, "login");
            } else {
                logger.log(Level.WARNING, "Server is not responding. Response code: {0}", responseCode);
                setScene(stage, "eroareConexiune");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while connecting to the server", e);
            setScene(stage, "eroareConexiune");
        }
    }

    private void setScene(Stage stage, String fxml) {
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