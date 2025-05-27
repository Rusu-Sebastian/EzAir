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
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    private static final Logger jurnal = Logger.getLogger(App.class.getName());
    private static final Map<String, String> dateUtilizator = new HashMap<>();
    private static final String SCENA_EROARE_CONEXIUNE = "eroareConexiune";
    
    public static Map<String, String> getDateUtilizator() {
        return dateUtilizator;
    }

    static Scene scena;

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) {
        String urlServer = System.getProperty("server.url", "http://localhost:3000");
        HttpURLConnection conexiune = null;
        
        try {
            URI uri = new URI(urlServer);
            conexiune = (HttpURLConnection) uri.toURL().openConnection();
            conexiune.setRequestMethod("GET");
            conexiune.setConnectTimeout(30000); // 30 secunde
            conexiune.setReadTimeout(30000);
            conexiune.setRequestProperty("Accept", "application/json");
            conexiune.setInstanceFollowRedirects(true);
            conexiune.setUseCaches(true);
            
            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                jurnal.info("Serverul răspunde");
                setScene(stage, "login");
            } else {
                jurnal.log(Level.WARNING, "Serverul a returnat un cod de răspuns neașteptat: {0}", codRaspuns);
                setScene(stage, SCENA_EROARE_CONEXIUNE);
            }
            
        } catch (URISyntaxException e) {
            jurnal.log(Level.SEVERE, "URL-ul serverului este invalid: {0}", urlServer);
            setScene(stage, SCENA_EROARE_CONEXIUNE);
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Conexiunea la server a eșuat: {0}", e.getMessage());
            setScene(stage, SCENA_EROARE_CONEXIUNE);
        } finally {
            if (conexiune != null) {
                conexiune.disconnect();
            }
        }
    }

    private static void setScene(Stage stage, String fxml) {
        try {
            scena = new Scene(loadFXML(fxml), 1680, 720);
            stage.setTitle("EZAir");
             Image icon = new Image(App.class.getResourceAsStream("logo.png"));
            stage.getIcons().add(icon);
            stage.setScene(scena);
            stage.show();
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Nu s-a putut încărca fișierul FXML {0}: {1}", new Object[]{fxml, e.getMessage()});
        }
    }

    static void setRoot(String fxml) throws IOException {
        scena.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}