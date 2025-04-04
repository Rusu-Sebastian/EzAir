package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        try {
            // Create a connection to the server
            URL url = new URL("http://localhost:3000");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // Set timeout to 5 seconds
            connection.setReadTimeout(5000);

            // Connect and check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Server is responding.");
            } else {
                System.out.println("Server is not responding. Response code: " + responseCode);
            }
        } catch (IOException e) {
            System.err.println("Error while connecting to the server: " + e.getMessage());
        }
    }

    private static final Map<String, String> userData = new HashMap<>();

    public static Map<String, String> getUserData() {
        return userData;
    }

    static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"), 1144, 640); stage.setScene(scene);
        stage.show();
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