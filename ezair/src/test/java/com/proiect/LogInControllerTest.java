package com.proiect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
class LogInControllerTest {
    
    @Mock
    private HttpConnectionFactory mockConnectionFactory;
    
    @Mock
    private HttpURLConnection mockConnection;
    
    @Mock
    private OutputStream mockOutputStream;

    private LogInController controller;
    private TextField numeUtilizator;
    private PasswordField parola;

    @Start
    public void start(Stage stage) {
        // Initialize UI components in the JavaFX thread
        numeUtilizator = new TextField();
        parola = new PasswordField();
        
        controller = new LogInController(mockConnectionFactory);
        controller.numeUtilizator = numeUtilizator;
        controller.parola = parola;
        
        // Create a minimal UI scene
        VBox root = new VBox(numeUtilizator, parola);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
    @BeforeEach
    void setUp() throws Exception {
        // Setup common mocks
        when(mockConnectionFactory.createConnection(any(URI.class))).thenReturn(mockConnection);
        when(mockConnection.getOutputStream()).thenReturn(mockOutputStream);
    }

    @Test
    void testValideazaIntrariCampuriGoale() {
        // Setup
        Platform.runLater(() -> {
            numeUtilizator.setText("");
            parola.setText("");
        });
        
        // Wait for UI updates
        WaitForAsyncUtils.waitForFxEvents();

        // Execute & Verify
        assertFalse(controller.valideazaIntrari(), "Validarea ar trebui să eșueze pentru câmpuri goale");
    }

    @Test
    void testValideazaIntrariCampuriCompletate() {
        // Setup
        Platform.runLater(() -> {
            numeUtilizator.setText("utilizator");
            parola.setText("parola123");
        });
        
        // Wait for UI updates
        WaitForAsyncUtils.waitForFxEvents();

        // Execute & Verify
        assertTrue(controller.valideazaIntrari(), "Validarea ar trebui să reușească pentru câmpuri completate");
    }

    @Test
    void testAutentificareSuccesAdmin() throws IOException, URISyntaxException {
        // Setup
        String jsonRaspuns = "{"
            + "\"" + LogInController.CHEIE_NUME_UTILIZATOR + "\":\"admin\","
            + "\"" + LogInController.CHEIE_NUME + "\":\"Admin\","
            + "\"" + LogInController.CHEIE_PRENUME + "\":\"User\","
            + "\"" + LogInController.CHEIE_ID_UTILIZATOR + "\":\"123\","
            + "\"" + LogInController.CHEIE_ADMIN + "\":true"
            + "}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
            jsonRaspuns.getBytes(StandardCharsets.UTF_8));
        
        // Mock behavior
        Platform.runLater(() -> {
            numeUtilizator.setText("admin");
            parola.setText("parola123");
        });
        
        // Wait for UI updates
        WaitForAsyncUtils.waitForFxEvents();

        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        
        // Execute
        controller.autentificare();
        
        // Verify HTTP request setup
        verify(mockConnection).setRequestMethod("POST");
        verify(mockConnection).setRequestProperty("Content-Type", "application/json");
        verify(mockConnection).setDoOutput(true);
        verify(mockOutputStream).write(any(byte[].class));
        verify(mockOutputStream).flush();
    }

    @Test
    void testAutentificareSuccesUtilizatorNormal() throws IOException, URISyntaxException {
        // Setup
        String jsonRaspuns = "{"
            + "\"" + LogInController.CHEIE_NUME_UTILIZATOR + "\":\"user\","
            + "\"" + LogInController.CHEIE_NUME + "\":\"Normal\","
            + "\"" + LogInController.CHEIE_PRENUME + "\":\"User\","
            + "\"" + LogInController.CHEIE_ID_UTILIZATOR + "\":\"456\","
            + "\"" + LogInController.CHEIE_ADMIN + "\":false"
            + "}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
            jsonRaspuns.getBytes(StandardCharsets.UTF_8));
        
        // Mock behavior
        Platform.runLater(() -> {
            numeUtilizator.setText("user");
            parola.setText("parola123");
        });
        
        // Wait for UI updates
        WaitForAsyncUtils.waitForFxEvents();

        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        
        // Execute
        controller.autentificare();
        
        // Verify
        verify(mockConnection).setRequestMethod("POST");
        verify(mockConnection).setRequestProperty("Content-Type", "application/json");
        verify(mockConnection).setDoOutput(true);
        verify(mockOutputStream).write(any(byte[].class));
        verify(mockOutputStream).flush();
    }

    @Test
    void testAutentificareCredentialeInvalide() throws IOException, URISyntaxException {
        // Setup
        Platform.runLater(() -> {
            numeUtilizator.setText("invalid");
            parola.setText("wrong");
        });
        
        // Wait for UI updates
        WaitForAsyncUtils.waitForFxEvents();

        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_UNAUTHORIZED);
        
        // Execute
        controller.autentificare();
        
        // Verify
        verify(mockConnection).setRequestMethod("POST");
        verify(mockConnection).setRequestProperty("Content-Type", "application/json");
        verify(mockConnection).setDoOutput(true);
        verify(mockOutputStream).write(any(byte[].class));
        verify(mockOutputStream).flush();
    }

    @Test
    void testAutentificareEroareServer() throws IOException, URISyntaxException {
        // Setup
        Platform.runLater(() -> {
            numeUtilizator.setText("user");
            parola.setText("parola123");
        });
        
        // Wait for UI updates
        WaitForAsyncUtils.waitForFxEvents();

        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_INTERNAL_ERROR);
        
        // Execute
        controller.autentificare();
        
        // Verify
        verify(mockConnection).setRequestMethod("POST");
        verify(mockConnection).setRequestProperty("Content-Type", "application/json");
        verify(mockConnection).setDoOutput(true);
        verify(mockOutputStream).write(any(byte[].class));
        verify(mockOutputStream).flush();
    }

    @Test
    void testAutentificareTimeoutConexiune() throws IOException, URISyntaxException {
        // Setup
        Platform.runLater(() -> {
            numeUtilizator.setText("user");
            parola.setText("parola123");
        });
        
        // Wait for UI updates
        WaitForAsyncUtils.waitForFxEvents();

        when(mockConnection.getOutputStream()).thenThrow(new IOException("Connection timed out"));
        
        // Execute
        controller.autentificare();
        
        // Verify
        verify(mockConnection).setRequestMethod("POST");
        verify(mockConnection).setRequestProperty("Content-Type", "application/json");
        verify(mockConnection).setDoOutput(true);
    }
}
