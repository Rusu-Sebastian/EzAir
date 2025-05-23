package com.proiect;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class EditareContClientController {
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    private static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";
    
    @FXML private TextField fieldNume;
    @FXML private TextField fieldPrenume;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldTelefon;
    
    @FXML
    private void initialize() {
        // Populează câmpurile cu datele utilizatorului curent
        fieldNume.setText((String) App.getDateUtilizator().get("nume"));
        fieldPrenume.setText((String) App.getDateUtilizator().get("prenume"));
        fieldEmail.setText((String) App.getDateUtilizator().get("email"));
        fieldTelefon.setText((String) App.getDateUtilizator().getOrDefault("telefon", ""));
    }
    
    @FXML
    private void salveazaModificari() {
        if (!valideazaDate()) {
            return;
        }
        
        actualizeazaDateUtilizator();
        afiseazaSucces("Informațiile au fost actualizate cu succes!");
        
        try {
            revino();
        } catch (Exception e) {
            afiseazaEroare("Nu s-a putut reveni la pagina anterioară.");
        }
    }
    
    private boolean valideazaDate() {
        if (fieldNume.getText().isEmpty() || 
            fieldPrenume.getText().isEmpty() || 
            fieldEmail.getText().isEmpty()) {
            afiseazaEroare("Numele, prenumele și email-ul sunt obligatorii.");
            return false;
        }
        
        if (!esteEmailValid(fieldEmail.getText())) {
            afiseazaEroare("Adresa de email nu este validă.");
            return false;
        }
        
        return true;
    }
    
    private void actualizeazaDateUtilizator() {
        App.getDateUtilizator().put("nume", fieldNume.getText());
        App.getDateUtilizator().put("prenume", fieldPrenume.getText());
        App.getDateUtilizator().put("email", fieldEmail.getText());
        App.getDateUtilizator().put("telefon", fieldTelefon.getText());
    }
    
    @FXML
    private void revino() throws Exception {
        App.setRoot("paginaContClient");
    }
    
    @FXML
    private void anuleaza() throws Exception {
        App.setRoot("paginaContClient");
    }
    
    private boolean esteEmailValid(String email) {
        return email.matches(REGEX_EMAIL);
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
