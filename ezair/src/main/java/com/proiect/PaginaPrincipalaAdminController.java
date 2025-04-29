package com.proiect;

import javafx.fxml.FXML;

public class PaginaPrincipalaAdminController {
    
    @FXML
    private void zboruriAdmin() throws Exception {
        App.setRoot("paginaZboruriAdmin");
    }

    @FXML
    private void useriAdmin() throws Exception {
        App.setRoot("paginaUseriAdmin");
    }
    @FXML
    private void adaugareZbor() throws Exception {
        
    }
    @FXML
    private void adaugareUser() throws Exception {
        App.setRoot("creareContInceputAdmin");
    }
    @FXML
    private void editareZbor() throws Exception {
        
    }
    @FXML
    private void editareUser() throws Exception {
        
    }
    @FXML
    private void deleteZbor() throws Exception {
        
    }
    @FXML
    private void deleteUser() throws Exception {
        
    }
    @FXML
    private void reload() throws Exception {
        App.setRoot("paginaPrincipalaAdmin");
    }
}
