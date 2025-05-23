package com.proiect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class EditareZborControllerTest {

    private EditareZborController controller;
    private Map<String, String> dateUtilizator;

    @Start
    public void start(Stage stage) {
        controller = new EditareZborController();
        
        // Initialize all TextField members
        controller.origine = new TextField();
        controller.destinatie = new TextField();
        controller.dataPlecarii1 = new TextField();
        controller.dataPlecarii2 = new TextField();
        controller.dataPlecarii3 = new TextField();
        controller.oraPlecarii1 = new TextField();
        controller.oraPlecarii2 = new TextField();
        controller.dataSosirii1 = new TextField();
        controller.dataSosirii2 = new TextField();
        controller.dataSosirii3 = new TextField();
        controller.oraSosirii1 = new TextField();
        controller.oraSosirii2 = new TextField();
        controller.modelAvion = new TextField();
        controller.pret = new TextField();
        controller.locuriLibere = new TextField();
        controller.idZbor = new TextField();

        // Add fields to scene
        VBox root = new VBox(
            controller.origine, controller.destinatie,
            controller.dataPlecarii1, controller.dataPlecarii2, controller.dataPlecarii3,
            controller.oraPlecarii1, controller.oraPlecarii2,
            controller.dataSosirii1, controller.dataSosirii2, controller.dataSosirii3,
            controller.oraSosirii1, controller.oraSosirii2,
            controller.modelAvion, controller.pret, controller.locuriLibere, controller.idZbor
        );
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void setUp() {
        // Set up test data
        dateUtilizator = new HashMap<>();
        dateUtilizator.put("id", "123");
        dateUtilizator.put("origine", "Bucuresti");
        dateUtilizator.put("destinatie", "Londra");
        dateUtilizator.put("dataPlecare", "23.05.2025");
        dateUtilizator.put("oraPlecare", "10:30");
        dateUtilizator.put("dataSosire", "23.05.2025");
        dateUtilizator.put("oraSosire", "12:45");
        dateUtilizator.put("modelAvion", "Boeing 737");
        dateUtilizator.put("locuriLibere", "150");
        dateUtilizator.put("pret", "299.99");
    }

    @Test
    void testIncarcaCampuri() {
        // Set the test data
        App.setDateUtilizator(dateUtilizator);
        
        // Call initialize method
        controller.initializeaza();

        // Verify all fields are correctly populated
        assertEquals("123", controller.idZbor.getText());
        assertEquals("Bucuresti", controller.origine.getText());
        assertEquals("Londra", controller.destinatie.getText());
        assertEquals("23", controller.dataPlecarii1.getText());
        assertEquals("05", controller.dataPlecarii2.getText());
        assertEquals("2025", controller.dataPlecarii3.getText());
        assertEquals("10", controller.oraPlecarii1.getText());
        assertEquals("30", controller.oraPlecarii2.getText());
        assertEquals("23", controller.dataSosirii1.getText());
        assertEquals("05", controller.dataSosirii2.getText());
        assertEquals("2025", controller.dataSosirii3.getText());
        assertEquals("12", controller.oraSosirii1.getText());
        assertEquals("45", controller.oraSosirii2.getText());
        assertEquals("Boeing 737", controller.modelAvion.getText());
        assertEquals("150", controller.locuriLibere.getText());
        assertEquals("299.99", controller.pret.getText());
    }

    @Test
    void testValideazaDate_CuDateValide() {
        // Set valid data
        controller.dataPlecarii1.setText("23");
        controller.dataPlecarii2.setText("05");
        controller.dataPlecarii3.setText("2025");
        controller.oraPlecarii1.setText("10");
        controller.oraPlecarii2.setText("30");
        controller.dataSosirii1.setText("23");
        controller.dataSosirii2.setText("05");
        controller.dataSosirii3.setText("2025");
        controller.oraSosirii1.setText("12");
        controller.oraSosirii2.setText("45");
        controller.locuriLibere.setText("150");
        controller.pret.setText("299.99");

        // Test validation
        assertTrue(controller.valideazaDate());
    }

    @Test
    void testValideazaDate_CuDateInvalide() {
        // Test with empty fields
        controller.dataPlecarii1.setText("");
        controller.oraPlecarii1.setText("");
        controller.locuriLibere.setText("-1");
        controller.pret.setText("invalid");

        // Test validation
        assertFalse(controller.valideazaDate());
    }

    @Test
    void testValideazaDate_CuPretNegativ() {
        // Set up valid data except price
        controller.dataPlecarii1.setText("23");
        controller.dataPlecarii2.setText("05");
        controller.dataPlecarii3.setText("2025");
        controller.oraPlecarii1.setText("10");
        controller.oraPlecarii2.setText("30");
        controller.dataSosirii1.setText("23");
        controller.dataSosirii2.setText("05");
        controller.dataSosirii3.setText("2025");
        controller.oraSosirii1.setText("12");
        controller.oraSosirii2.setText("45");
        controller.locuriLibere.setText("150");
        controller.pret.setText("-299.99");

        // Test validation
        assertFalse(controller.valideazaDate());
    }
}
