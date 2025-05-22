package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
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
        String serverUrl = System.getProperty("server.url", "http://localhost:3000");
        HttpURLConnection connection = null;
        
        try {
            URI uri = new URI(serverUrl);
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 secunde
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(true);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Server is responding");
                setScene(stage, "paginaPrincipalaUser");
            } else {
                logger.log(Level.WARNING, "Server returned unexpected response code: {0}", responseCode);
                setScene(stage, CONNECTION_ERROR_SCENE);
            }
            
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Invalid server URL: {0}", serverUrl);
            setScene(stage, CONNECTION_ERROR_SCENE);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to connect to server: {0}", e.getMessage());
            setScene(stage, CONNECTION_ERROR_SCENE);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void setScene(Stage stage, String fxml) {
        try {
            scene = new Scene(loadFXML(fxml), 1680, 720);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load FXML file {0}: {1}", new Object[]{fxml, e.getMessage()});
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