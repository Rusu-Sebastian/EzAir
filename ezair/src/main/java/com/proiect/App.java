package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.proiect.config.ConfigurationManager;
import com.proiect.util.HttpUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    private static final Logger jurnal = Logger.getLogger(App.class.getName());
    private static final String SCENA_EROARE_CONEXIUNE = "eroareConexiune";
    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    private static final Map<String, Object> dateUtilizator = new HashMap<>();
    private static App instance;
    
    private Scene scene;
    private Stage stage;

    public static Map<String, Object> getDateUtilizator() {
        return dateUtilizator;
    }

    public static App getInstance() {
        return instance;
    }
    
    public static Scene getScena() {
        return getInstance().scene;
    }
    
    public static Stage getStage() {
        return getInstance().stage;
    }

    @Override
    public void start(Stage primaryStage) {
        setInstance(this);
        this.stage = primaryStage;
        verificaConexiuneServer();
    }
    
    private static void setInstance(App app) {
        instance = app;
    }

    private void verificaConexiuneServer() {
        try {
            HttpUtil.retryConnection(() -> {
                HttpURLConnection conexiune = HttpUtil.createConnection("", "GET");
                try {
                    HttpUtil.validateResponse(conexiune);
                    jurnal.info("Serverul răspunde");
                    Platform.runLater(() -> initScene("login"));
                } finally {
                    HttpUtil.disconnect(conexiune);
                }
            });
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Nu s-a putut stabili conexiunea cu serverul: {0}", e.getMessage());
            Platform.runLater(() -> initScene(SCENA_EROARE_CONEXIUNE));
        } catch (Exception e) {
            jurnal.log(Level.SEVERE, "Eroare neașteptată: {0}", e.getMessage());
            Platform.runLater(() -> initScene(SCENA_EROARE_CONEXIUNE));
        }
    }

    private void initScene(String fxml) {
        try {
            scene = new Scene(loadFXML(fxml));
            stage.setScene(scene);
            stage.getIcons().add(new Image(App.class.getResourceAsStream("/com/proiect/logo.png")));
            stage.setTitle(config.getAppName());
            stage.show();
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Nu s-a putut încărca scena {0}: {1}", new Object[]{fxml, e.getMessage()});
        }
    }

    public static void setRoot(String fxml) throws IOException {
        getInstance().scene.setRoot(getInstance().loadFXML(fxml));
        getInstance().stage.setTitle(config.getAppName());
    }
    
    public void setRootInstance(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        stage.setTitle(config.getAppName());
    }

    private Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}