package com.proiect;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class EditareContClientController {
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_SUCCES = "Succes";
    private static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";
    
    @FXML private TextField campNume;
    @FXML private TextField campPrenume;
    @FXML private TextField campEmail;
    @FXML private TextField campTelefon;
    
    @FXML
    private void initializeaza() {
        // Populează câmpurile cu datele utilizatorului curent
        campNume.setText((String) App.getDateUtilizator().get("nume"));
        campPrenume.setText((String) App.getDateUtilizator().get("prenume"));
        campEmail.setText((String) App.getDateUtilizator().get("email"));
        campTelefon.setText((String) App.getDateUtilizator().getOrDefault("telefon", ""));
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
        if (campNume.getText().isEmpty() || 
            campPrenume.getText().isEmpty() || 
            campEmail.getText().isEmpty()) {
            afiseazaEroare("Numele, prenumele și email-ul sunt obligatorii.");
            return false;
        }
        
        if (!esteEmailValid(campEmail.getText())) {
            afiseazaEroare("Adresa de email nu este validă.");
            return false;
        }
        
        return true;
    }
    
    private void actualizeazaDateUtilizator() {
        App.getDateUtilizator().put("nume", campNume.getText());
        App.getDateUtilizator().put("prenume", campPrenume.getText());
        App.getDateUtilizator().put("email", campEmail.getText());
        App.getDateUtilizator().put("telefon", campTelefon.getText());
    }
    
    @FXML
    private void revino() throws Exception {
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
