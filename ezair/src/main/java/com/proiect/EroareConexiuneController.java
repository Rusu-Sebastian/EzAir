package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

public class EroareConexiuneController {
    private static final Logger jurnal = Logger.getLogger(EroareConexiuneController.class.getName());
    private static final int TIMP_EXPIRARE_CONEXIUNE = 10000; // 10 secunde
    private static final String URL_SERVER = "http://localhost:3000";
    private static final String MESAJ_RECONECTARE = "Se încearcă reconectarea la server...";
    private static final String EROARE_INCARCARE = "Nu s-a putut încărca pagina de autentificare";
    private static final String EROARE_SERVER_OPRIT = "Serverul nu este pornit sau nu este accesibil";
    private static final String EROARE_TIMEOUT = "Conexiunea la server durează prea mult";
    private static final String EROARE_RASPUNS = "Serverul nu răspunde corect (cod %d)";
    
    private final ExecutorService executorVerificareConexiune = Executors.newSingleThreadExecutor();
    
    @FXML private Label textStatus;
    @FXML private Button butonReincercare;
    @FXML private ProgressIndicator indicatorProgres;
    
    @FXML
    public void initialize() {
        textStatus.setText("");
        indicatorProgres.setVisible(false);
    }
    
    @FXML
    public void reincearcaConexiunea() {
        pregatestePentruReconectare();
        verificaConexiuneaInBackground();
    }
    
    private void pregatestePentruReconectare() {
        butonReincercare.setDisable(true);
        indicatorProgres.setVisible(true);
        actualizeazaStatus(MESAJ_RECONECTARE);
    }
    
    private void verificaConexiuneaInBackground() {
        executorVerificareConexiune.submit(() -> {
            try {
                if (testeazaConexiuneaServer()) {
                    jurnal.info("Conexiunea la server a fost restabilită");
                    Platform.runLater(this::navigheazaLaAutentificare);
                }
            } catch (IOException | URISyntaxException e) {
                gestioneazaEroareConexiune(e);
            } finally {
                Platform.runLater(() -> {
                    butonReincercare.setDisable(false);
                    indicatorProgres.setVisible(false);
                });
            }
        });
    }
    
    private boolean testeazaConexiuneaServer() throws IOException, URISyntaxException {
        HttpURLConnection conexiune = null;
        try {
            conexiune = (HttpURLConnection) new URI(URL_SERVER).toURL().openConnection();
            conexiune.setRequestMethod("GET");
            conexiune.setConnectTimeout(TIMP_EXPIRARE_CONEXIUNE);
            conexiune.setReadTimeout(TIMP_EXPIRARE_CONEXIUNE);
            conexiune.setInstanceFollowRedirects(true);
            
            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                jurnal.log(Level.WARNING, "Serverul a returnat un cod de răspuns neașteptat: {0}", codRaspuns);
                actualizeazaStatus(String.format(EROARE_RASPUNS, codRaspuns));
                return false;
            }
        } finally {
            if (conexiune != null) {
                conexiune.disconnect();
            }
        }
    }
    
    private void gestioneazaEroareConexiune(Exception e) {
        String mesajEroare = e.getMessage();
        if (mesajEroare.contains("Connection refused")) {
            actualizeazaStatus(EROARE_SERVER_OPRIT);
        } else if (mesajEroare.contains("timed out")) {
            actualizeazaStatus(EROARE_TIMEOUT);
        } else {
            actualizeazaStatus("Eroare de conexiune: " + mesajEroare);
        }
        jurnal.log(Level.SEVERE, "Eroare de conexiune", e);
    }
    
    private void navigheazaLaAutentificare() {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, EROARE_INCARCARE, e);
            actualizeazaStatus(EROARE_INCARCARE);
        }
    }
    
    private void actualizeazaStatus(String mesaj) {
        Platform.runLater(() -> textStatus.setText(mesaj));
    }
    
    public void opresteProceseleBackground() {
        executorVerificareConexiune.shutdown();
        try {
            if (!executorVerificareConexiune.awaitTermination(2, TimeUnit.SECONDS)) {
                executorVerificareConexiune.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorVerificareConexiune.shutdownNow();
        }
    }
}