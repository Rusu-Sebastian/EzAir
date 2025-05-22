package com.proiect;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class EditareContClientController {
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    
    @FXML private TextField fieldNume;
    @FXML private TextField fieldPrenume;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldTelefon;
    
    @FXML
    private void initialize() {
        // Populează câmpurile cu datele utilizatorului curent
        fieldNume.setText((String) App.getUserData().get("nume"));
        fieldPrenume.setText((String) App.getUserData().get("prenume"));
        fieldEmail.setText((String) App.getUserData().get("email"));
        fieldTelefon.setText((String) App.getUserData().getOrDefault("telefon", ""));
    }
    
    @FXML
    private void salveazaModificari() {
        if (fieldNume.getText().isEmpty() || 
            fieldPrenume.getText().isEmpty() || 
            fieldEmail.getText().isEmpty()) {
            afiseazaEroare("Numele, prenumele și email-ul sunt obligatorii.");
            return;
        }
        
        // Validare email
        if (!isValidEmail(fieldEmail.getText())) {
            afiseazaEroare("Adresa de email nu este validă.");
            return;
        }
        
        // Aici ar trebui să fie logica de salvare a datelor în server
        // Pentru testare, vom afișa un mesaj de succes și vom actualiza userData
        
        App.getUserData().put("nume", fieldNume.getText());
        App.getUserData().put("prenume", fieldPrenume.getText());
        App.getUserData().put("email", fieldEmail.getText());
        App.getUserData().put("telefon", fieldTelefon.getText());
        
        afiseazaSucces("Informațiile au fost actualizate cu succes!");
        
        try {
            anuleaza();
        } catch (Exception e) {
            afiseazaEroare("Nu s-a putut reveni la pagina anterioară.");
        }
    }
    
    @FXML
    private void anuleaza() throws Exception {
        App.setRoot("paginaContClient");
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
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
