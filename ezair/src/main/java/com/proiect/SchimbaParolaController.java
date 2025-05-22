package com.proiect;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;

public class SchimbaParolaController {
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    
    @FXML private PasswordField fieldParolaActuala;
    @FXML private PasswordField fieldParolaNoua;
    @FXML private PasswordField fieldConfirmareParola;
    
    @FXML
    private void schimbaParola() {
        if (fieldParolaActuala.getText().isEmpty() || 
            fieldParolaNoua.getText().isEmpty() || 
            fieldConfirmareParola.getText().isEmpty()) {
            afiseazaEroare("Toate câmpurile sunt obligatorii.");
            return;
        }
        
        if (!fieldParolaNoua.getText().equals(fieldConfirmareParola.getText())) {
            afiseazaEroare("Parolele noi nu coincid.");
            return;
        }
        
        // Aici ar trebui să fie logica de schimbare a parolei în server
        // Pentru testare, vom afișa un mesaj de succes
        
        afiseazaSucces("Parola a fost schimbată cu succes!");
        
        try {
            inapoi();
        } catch (Exception e) {
            afiseazaEroare("Nu s-a putut reveni la pagina anterioară.");
        }
    }
    
    @FXML
    private void inapoi() throws Exception {
        App.setRoot("paginaContClient");
    }
    
    private void afiseazaEroare(String mesaj) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(TITLU_EROARE);
        alerta.setHeaderText(TITLU_EROARE);
        alerta.setContentText(mesaj);
        alerta.showAndWait();
    }
    
    private void afiseazaSucces(String mesaj) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(TITLU_SUCCES);
        alerta.setHeaderText(TITLU_SUCCES);
        alerta.setContentText(mesaj);
        alerta.showAndWait();
    }
}
